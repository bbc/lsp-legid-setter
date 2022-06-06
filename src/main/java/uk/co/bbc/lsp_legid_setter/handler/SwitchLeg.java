package uk.co.bbc.lsp_legid_setter.handler;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.freeman.ispy.LambdaEventIspyContext;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.lsp_legid_setter.ribbon.RibbonClient;
import uk.co.bbc.lsp_medialive.domain.LivestreamEvent;
import uk.co.bbc.lsp_medialive.restclient.stateapi.LspMedialiveStateClient;
import uk.co.bbc.lsp_medialive.restclient.stateapi.domain.channels.ChannelRecord;
import uk.co.bbc.lsp_medialive.restclient.stateapi.exception.StateApiException;

public class SwitchLeg implements Handler<SQSEvent.SQSMessage>{
    public static final String USP = "USP";
    private final RibbonClient ribbonClient;
    private final LspMedialiveStateClient lspMedialiveStateClient;
    
    public SwitchLeg(RibbonClient ribbonClient, LspMedialiveStateClient lspMedialiveStateClient) {
        this.ribbonClient = ribbonClient;
        this.lspMedialiveStateClient = lspMedialiveStateClient;
    }

    @Override
    public LambdaEvent<SQSMessage> apply(LambdaEvent<SQSMessage> event) throws Exception {
        LivestreamEvent livestreamEvent = event.getBody(LivestreamEvent.class);
        IspyContext ispyContext = LambdaEventIspyContext.getIspyContextFromEvent(event);
        String cvid = livestreamEvent.getId();
        Optional<ChannelRecord> channelRecordOptional = lspMedialiveStateClient.getChannelRecord(cvid);
        final ChannelRecord channelRecord = channelRecordOptional
                .orElseThrow(() -> new StateApiException(String.format("Channel record not found for cvid: %s", cvid), 200));
        String legId = channelRecord.getLegId();
        if (legId.equalsIgnoreCase(USP)) {
            ispyContext.ispy("ribbon.ignore");
        }
        else {
            ribbonClient.setLegId(cvid, legId);
            ispyContext.ispy("ribbon.set");
        }
        return event;
    }
}
