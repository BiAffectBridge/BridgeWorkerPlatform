package org.sagebionetworks.bridge.workerPlatform.multiplexer;

import com.fasterxml.jackson.databind.JsonNode;
import org.sagebionetworks.bridge.exporter.request.BridgeExporterProcessor;
import org.sagebionetworks.bridge.json.DefaultObjectMapper;
import org.sagebionetworks.bridge.reporter.worker.BridgeReporterProcessor;
import org.sagebionetworks.bridge.sqs.PollSqsCallback;
import org.sagebionetworks.bridge.sqs.PollSqsWorkerBadRequestException;
import org.sagebionetworks.bridge.udd.worker.BridgeUddProcessor;
import org.sagebionetworks.bridge.workerPlatform.request.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * SQS callback. Called by the PollSqsWorker. This handles a reporting request.
 */
@Component
public class BridgeWorkerPlatformSqsCallback implements PollSqsCallback {
    private static final Logger LOG = LoggerFactory.getLogger(BridgeWorkerPlatformSqsCallback.class);

    private BridgeReporterProcessor bridgeReporterProcessor;
    private BridgeExporterProcessor bridgeExporterProcessor;
    private BridgeUddProcessor bridgeUddProcessor;

    @Autowired
    public void setBridgeReporterProcessor(BridgeReporterProcessor bridgeReporterProcessor) {
        this.bridgeReporterProcessor = bridgeReporterProcessor;
    }

    @Autowired
    public void setBridgeUddProcessor(BridgeUddProcessor bridgeUddProcessor) {
        this.bridgeUddProcessor = bridgeUddProcessor;
    }

    @Autowired
    public void setBridgeExporterProcessor(BridgeExporterProcessor bridgeExporterProcessor) {
        this.bridgeExporterProcessor = bridgeExporterProcessor;
    }

    /** Parses the SQS message. */
    @Override
    public void callback(String messageBody) throws IOException, PollSqsWorkerBadRequestException {
        BridgeWorkerPlatformRequest request;
        try {
            request = DefaultObjectMapper.INSTANCE.readValue(messageBody, BridgeWorkerPlatformRequest.class);
        } catch (IOException ex) {
            throw new PollSqsWorkerBadRequestException("Error parsing request: " + ex.getMessage(), ex);
        }

        ServiceType service = request.getService();
        JsonNode body = request.getBody();

        LOG.info("Received request for hash[service]=" + service.getType() + ", body=" + body.toString());

        // main block to assign thread to service processor
        if (service == ServiceType.REPORTER) {
            new Thread("Reporter Thread") {
                public void run() {
                    LOG.info("Thread: " + this.getName() + " running");
                    try {
                        bridgeReporterProcessor.process(body);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else if (service == ServiceType.EXPORTER) {
            new Thread("Reporter Thread") {
                public void run() {
                    LOG.info("Thread: " + this.getName() + " running");
                    try {
                        bridgeExporterProcessor.process(body);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else if (service == ServiceType.UDD) {
            new Thread("UDD Thread") {
                public void run() {
                    LOG.info("Thread: " + this.getName() + " running");
                    try {
                        bridgeUddProcessor.process(body);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            throw new PollSqsWorkerBadRequestException("Invalid service type: " + service.getType());
        }
    }
}
