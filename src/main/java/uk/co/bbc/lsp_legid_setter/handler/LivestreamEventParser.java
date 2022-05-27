package uk.co.bbc.lsp_legid_setter.handler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.bbc.deel.StatusMessage;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.freeman.ispy.LambdaEventIspyContext;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.lsp_legid_setter.exception.DeserialisationException;
import uk.co.bbc.lsp_medialive.domain.LivestreamEvent;
import uk.co.bbc.lsp_medialive.domain.LivestreamEventType;

import static uk.co.bbc.freeman.ispy.LambdaEventIspyContext.ISPY_CONTEXT_PROPERTY;

import java.io.IOException;

public class LivestreamEventParser implements Handler<SQSMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(LivestreamEventParser.class);

    @Override
    public LambdaEvent<SQSMessage> apply(LambdaEvent<SQSMessage> event) throws IOException {
        String messageBody = event.getBody(String.class);
        LOG.info("Received message: [{}]", messageBody);
        StatusMessage statusMessage = StatusMessage.fromJson(messageBody);

        final LivestreamEvent livestreamEvent = statusMessage.getData().getPayload(LivestreamEvent.class);
        
        if (livestreamEvent.getLivestreamEventType() != LivestreamEventType.LIVESTREAM_CREATED) {
            throw new DeserialisationException(String.format("Unexpected event type [%s]", livestreamEvent.getLivestreamEventType()));
        }
        
        final IspyContext ispyContext = LambdaEventIspyContext.getIspyContextFromEvent(event)
                .withActivityId(statusMessage.getHeader().getCorrelationID())
                .with(livestreamEvent);
        
        return event.withProperty(ISPY_CONTEXT_PROPERTY, ispyContext)
                .withBody(livestreamEvent);
    }
}

