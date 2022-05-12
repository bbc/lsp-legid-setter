package uk.co.bbc.sqs_lambda_lsp_legid_setter;

import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;


public class XmlParser implements Handler<SQSMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(XmlParser.class);
    private static final ObjectReader OBJECT_READER = new XmlMapper().readerFor(ExampleSqsRequest.class);

    @Override
    public LambdaEvent<SQSMessage> apply(LambdaEvent<SQSMessage> event) throws IOException {
        LOG.info("parser input event: {}", event);

        String eventText = event.getBody(String.class);
        ExampleSqsRequest exampleSqsRequest = OBJECT_READER.readValue(eventText);

        return event.withBody(exampleSqsRequest);
    }
}
