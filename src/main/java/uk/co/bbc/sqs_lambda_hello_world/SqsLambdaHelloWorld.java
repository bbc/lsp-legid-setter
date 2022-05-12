package uk.co.bbc.sqs_lambda_hello_world;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import uk.co.bbc.config_fetcher.ConfigFile;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;

public class SqsLambdaHelloWorld implements Handler<SQSEvent.SQSMessage> {
    private final ConfigFile<ExampleJustConfig> configFile;

    public SqsLambdaHelloWorld(ConfigFile<ExampleJustConfig> configFile) {
        this.configFile = configFile;
    }

    @Override
    public LambdaEvent<SQSEvent.SQSMessage> apply(LambdaEvent<SQSEvent.SQSMessage> event) throws MatchNotFoundException {
        ExampleJustConfig config = configFile.get();

        ExampleSqsRequest request = event.getBody(ExampleSqsRequest.class);

        if (request.getMatchId().equals("forced_failure")){
            throw new IllegalArgumentException("Forced failure for component test to demonstrate logic");
        }

        var match = config.getMatches().get(request.getMatchId());

        if (match == null) {
            throw new MatchNotFoundException(String.format("Can not find match for %s", request.getMatchId()));
        }

        return event.withBody(match.toString());
    }
}
