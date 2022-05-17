package uk.co.bbc.lsp_legid_setter;

import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.lsp_legid_setter.ExampleSqsRequest;
import uk.co.bbc.lsp_legid_setter.XmlParser;


class XmlParserTest {

    @Test
    void testReturnCorrectObject() throws Exception {
        
        XmlParser undertest = new XmlParser();

        ExampleSqsRequest expected = new ExampleSqsRequest("pAI", "mI");
        LambdaEvent<SQSMessage> sqsMessageLambdaEvent = new LambdaEvent<>(new SQSMessage())
            .withBody("<matchRequest><providerActivityId>pAI</providerActivityId><matchId>mI</matchId></matchRequest>");

        LambdaEvent<SQSMessage> result = undertest.apply(sqsMessageLambdaEvent);

        ExampleSqsRequest actual = result.getBody(ExampleSqsRequest.class);

        assertTrue(expected.equals(actual));

    }

    @Test
    void testInvalidXml() throws Exception {

        XmlParser undertest = new XmlParser();
        LambdaEvent<SQSMessage> sqsMessageLambdaEvent = new LambdaEvent<>(new SQSMessage())
            .withBody("<matchRequest><providerActivityId>pAI</providerActivityId><matchId>mI</matchId></matchRequest");

        assertThrows(IOException.class, () -> undertest.apply(sqsMessageLambdaEvent));

    }

}
