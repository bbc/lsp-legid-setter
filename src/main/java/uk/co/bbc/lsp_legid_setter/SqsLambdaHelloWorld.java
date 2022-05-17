package uk.co.bbc.lsp_legid_setter;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;

public class SqsLambdaHelloWorld implements Handler<SQSEvent.SQSMessage> {

    public SqsLambdaHelloWorld() {
    }

    @Override
    public LambdaEvent<SQSEvent.SQSMessage> apply(LambdaEvent<SQSEvent.SQSMessage> event) throws MatchNotFoundException {

        ExampleSqsRequest request = event.getBody(ExampleSqsRequest.class);

        if (request.getMatchId().equals("forced_failure")){
            throw new IllegalArgumentException("Forced failure for component test to demonstrate logic");
        }


        return event.withBody("foo");
    }
}
