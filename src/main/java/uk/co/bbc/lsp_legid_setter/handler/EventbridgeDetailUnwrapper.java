package uk.co.bbc.lsp_legid_setter.handler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.lsp_legid_setter.exception.DeserialisationException;

import java.util.Map;

public class EventbridgeDetailUnwrapper implements Handler<SQSEvent.SQSMessage> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public LambdaEvent<SQSEvent.SQSMessage> apply(LambdaEvent<SQSEvent.SQSMessage> lambdaEvent) throws Exception {
        Map<String, Object> message = OBJECT_MAPPER.readValue(lambdaEvent.getBody(String.class), Map.class);
        if (null == message.get("detail")) {
            throw new DeserialisationException("Expected detail in message");
        }
        String detail = OBJECT_MAPPER.writeValueAsString(message.get("detail")); // Back to string for StatusMessage.fromJson(messageBody)
        return lambdaEvent.withBody(detail);
    }
}
