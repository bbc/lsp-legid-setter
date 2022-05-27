package uk.co.bbc.lsp_legid_setter.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.lsp_legid_setter.exception.DeserialisationException;

class EventbridgeDetailUnwrapperTest {

    private EventbridgeDetailUnwrapper underTest;

    @BeforeEach
    void setUp() {
        underTest = new EventbridgeDetailUnwrapper();
    }

    @Test
    void itUnwrapsDetail() throws Exception {
        String event = "{\"version\": \"zero\", \"detail\": {\"key\": \"value\"}}";
        LambdaEvent<SQSEvent.SQSMessage> lambdaEvent = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withBody(event);
        LambdaEvent<SQSEvent.SQSMessage> result = underTest.apply(lambdaEvent);
        assertThat(result.getBody(String.class), is("{\"key\":\"value\"}"));
    }

    @Test
    void itThrowsIfNoDetail() {
        String event = "{\"version\": \"zero\"}";
        LambdaEvent<SQSEvent.SQSMessage> lambdaEvent = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withBody(event);
        assertThrows(DeserialisationException.class, () -> underTest.apply(lambdaEvent));
    }
}
