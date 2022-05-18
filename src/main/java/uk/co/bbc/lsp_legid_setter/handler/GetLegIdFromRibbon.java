package uk.co.bbc.lsp_legid_setter.handler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.lsp_legid_setter.ribbon.RibbonClient;
import uk.co.bbc.lsp_medialive.domain.LivestreamEvent;

public class GetLegIdFromRibbon implements Handler<SQSEvent.SQSMessage>{
    private final RibbonClient ribbonClient;
    
    public GetLegIdFromRibbon(RibbonClient ribbonClient) {
        this.ribbonClient = ribbonClient;
    }

    @Override
    public LambdaEvent<SQSMessage> apply(LambdaEvent<SQSMessage> event) throws Exception {
        LivestreamEvent livestreamEvent = event.getBody(LivestreamEvent.class);
        String cvid = livestreamEvent.getId();
        String legId = ribbonClient.getLegId(cvid);
        return event.withBody(legId);
    }
}
