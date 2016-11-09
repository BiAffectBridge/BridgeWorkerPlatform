package org.sagebionetworks.bridge.workerPlatform.multiplexer;

import com.fasterxml.jackson.databind.JsonNode;
import org.sagebionetworks.bridge.json.DefaultObjectMapper;
import org.sagebionetworks.bridge.workerPlatform.request.ServiceType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.sagebionetworks.bridge.json.DefaultObjectMapper.*;
import static org.testng.Assert.assertEquals;

public class BridgeWorkerPlatformRequestTest {
    private static final String TEST_JSON_NODE_STRING = "{\n" +
            "   \"scheduler\":\"test-scheduler\",\n" +
            "   \"scheduleType\":\"DAILY\",\n" +
            "   \"startDateTime\":\"2016-10-19T00:00:00.000Z\",\n" +
            "   \"endDateTime\":\"2016-10-20T23:59:59.000Z\"\n" +
            "}";

    private static final ServiceType TEST_SERVICE_TYPE = ServiceType.REPORTER;
    private static JsonNode TEST_BODY;

    @BeforeClass
    public void setup() throws Exception{
        TEST_BODY = DefaultObjectMapper.INSTANCE.readTree(TEST_JSON_NODE_STRING);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*service.*")
    public void nullServiceType() {
        new BridgeWorkerPlatformRequest.Builder().withBody(TEST_BODY).build();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*body.*")
    public void nullBody() {
        new BridgeWorkerPlatformRequest.Builder().withService(TEST_SERVICE_TYPE).build();
    }

    @Test
    public void jsonSerialization() throws Exception {
        // start with JSON
        String jsonText = "{\n" +
                "   \"service\":\"REPORTER\",\n" +
                "   \"body\":" + "{\n" +
                "       \"scheduler\":\"test-scheduler\",\n" +
                "       \"scheduleType\":\"DAILY\",\n" +
                "       \"startDateTime\":\"2016-10-19T00:00:00.000Z\",\n" +
                "       \"endDateTime\":\"2016-10-20T23:59:59.000Z\"\n" +
                "   }\n" +
                "}";

        // convert to POJO
        BridgeWorkerPlatformRequest request = INSTANCE.readValue(jsonText, BridgeWorkerPlatformRequest.class);
        assertEquals(request.getBody(), TEST_BODY);
        assertEquals(request.getService(), TEST_SERVICE_TYPE);
    }
}
