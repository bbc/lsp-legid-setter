package uk.co.bbc.lsp_legid_setter.handler;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.freeman.ispy.LambdaEventIspyContext;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.lsp_legid_setter.exception.FailQueueException;
import uk.co.bbc.lsp_legid_setter.ribbon.RibbonClient;
import uk.co.bbc.lsp_medialive.domain.LivestreamEvent;
import uk.co.bbc.lsp_medialive.restclient.stateapi.LspMedialiveStateClient;
import uk.co.bbc.lsp_medialive.restclient.stateapi.domain.channels.ChannelRecord;
import uk.co.bbc.lsp_medialive.restclient.stateapi.domain.livestreams.LivestreamRecord;
import uk.co.bbc.lsp_medialive.restclient.stateapi.exception.StateApiException;

public class GetLegIdFromRibbon implements Handler<SQSEvent.SQSMessage>{
    public static final String LEG_ID = "LEG_ID";

    private static final String SIMULCAST = "simulcast";
    private final RibbonClient ribbonClient;

    private final LspMedialiveStateClient lspMedialiveStateClient;

    public GetLegIdFromRibbon(RibbonClient ribbonClient, LspMedialiveStateClient lspMedialiveStateClient) {
        this.ribbonClient = ribbonClient;
        this.lspMedialiveStateClient = lspMedialiveStateClient;
    }

    @Override
    public LambdaEvent<SQSMessage> apply(LambdaEvent<SQSMessage> event) throws Exception {
        LivestreamEvent livestreamEvent = event.getBody(LivestreamEvent.class);

        String cvid = livestreamEvent.getId();
        String legId = ribbonClient.getLegId(cvid);

        IspyContext ispyContext = LambdaEventIspyContext.getIspyContextFromEvent(event);

        Optional<LivestreamRecord> livestreamRecordOptional = lspMedialiveStateClient.getLivestreamRecord(cvid);
        final LivestreamRecord livestreamRecord = livestreamRecordOptional.
                orElseThrow(() -> new StateApiException(String.format("Livestream record not found for cvid: %s", cvid), 200));

        Optional<ChannelRecord> channelRecordOptional = lspMedialiveStateClient.getChannelRecord(cvid);
        final ChannelRecord channelRecord = channelRecordOptional
                .orElseThrow(() -> new StateApiException(String.format("Channel record not found for cvid: %s", cvid), 200));

            if (!SIMULCAST.equalsIgnoreCase(livestreamRecord.getWorkflowType())) {

                if (legId.equals(channelRecord.getLegId())) {
                    ispyContext.ispy("info.identical-leg-id-exists");
                } else {
                    ispyContext.ispy("error.different-leg-id-exists.notification");
                    throw new FailQueueException(
                            "Workflow type is not simulcast, throwing failQueue exception - House keep to clear");
                }
            }

        return event.withProperty(LEG_ID, legId);
    }
}
