package uk.co.bbc.sqs_lambda_hello_world;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.bbc.config_fetcher.ConfigFile;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.sqs_lambda_hello_world.ExampleJustConfig.Match;

@ExtendWith(MockitoExtension.class)
class SqsLambdaHelloWorldTest {

    @Mock private ConfigFile<ExampleJustConfig> configFile;

    @BeforeEach
    void setup() {
        Map<String, Match> data = new HashMap<>();
        data.put("semi1", new Match("bulgaria", "samoa"));
        when(configFile.get()).thenReturn(new ExampleJustConfig(data));
    }

    @Test
    void itReplacesTheBodyWithTheMatchFromTheConfig() throws MatchNotFoundException {

        SQSEvent.SQSMessage original = new SQSEvent.SQSMessage();
        LambdaEvent<SQSMessage> event = new LambdaEvent<>(original)
                .withBody(new ExampleSqsRequest("act", "semi1"));

        SqsLambdaHelloWorld undertest = new SqsLambdaHelloWorld(configFile);

        LambdaEvent<SQSMessage> result = undertest.apply(event);

        assertThat(result.getBody(String.class), is("Match [home=bulgaria, away=samoa]"));
    }

    @Test
    void itThrowsExceptionWHenMatchNotFoundInConfig() {

        SQSEvent.SQSMessage original = new SQSEvent.SQSMessage();
        LambdaEvent<SQSMessage> event = new LambdaEvent<>(original)
                .withBody(new ExampleSqsRequest("act", "failure"));

        SqsLambdaHelloWorld undertest = new SqsLambdaHelloWorld(configFile);

        assertThrows(MatchNotFoundException.class, () -> undertest.apply(event));

    }
}
