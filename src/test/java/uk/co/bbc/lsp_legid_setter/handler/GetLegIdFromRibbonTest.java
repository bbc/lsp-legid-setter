package uk.co.bbc.lsp_legid_setter.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.bbc.freeman.ispy.LambdaEventIspyContext.ISPY_CONTEXT_PROPERTY;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.lsp_legid_setter.exception.FailQueueException;
import uk.co.bbc.lsp_legid_setter.ribbon.RibbonClient;
import uk.co.bbc.lsp_medialive.domain.LivestreamEvent;
import uk.co.bbc.lsp_medialive.restclient.stateapi.LspMedialiveStateClient;
import uk.co.bbc.lsp_medialive.restclient.stateapi.domain.channels.ChannelRecord;
import uk.co.bbc.lsp_medialive.restclient.stateapi.domain.livestreams.LivestreamRecord;
import uk.co.bbc.lsp_medialive.restclient.stateapi.exception.StateApiException;

@ExtendWith(MockitoExtension.class)
class GetLegIdFromRibbonTest {
    @Mock
    RibbonClient ribbonClient;
    
    GetLegIdFromRibbon underTest;

    @Mock
    LspMedialiveStateClient lspMedialiveStateClient;

    @Mock
    LivestreamRecord livestreamRecord;

    @Mock
    ChannelRecord channelRecord;

    @Mock
    private IspyContext ispyContext;

    @BeforeEach
    void setup() {
        underTest = new GetLegIdFromRibbon(ribbonClient, lspMedialiveStateClient);
    }

    @Test
    void itGetLegId_is_simulcast() throws Exception {
        when(ribbonClient.getLegId("cvid")).thenReturn("legid");
        when(lspMedialiveStateClient.getLivestreamRecord("cvid")).thenReturn(Optional.of(livestreamRecord));
        when(livestreamRecord.getWorkflowType()).thenReturn("simulcast");
        when(lspMedialiveStateClient.getChannelRecord("cvid")).thenReturn(Optional.of(channelRecord));

        LivestreamEvent livestreamEvent = new LivestreamEvent.Builder().id("cvid").build();
        LambdaEvent<SQSMessage> event = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withBody(livestreamEvent);
        LambdaEvent<SQSMessage> apply = underTest.apply(event);
        assertEquals("legid", apply.getPropertyAs(String.class, "LEG_ID"));
        assertEquals(livestreamEvent, apply.getBody(LivestreamEvent.class));
    }

    @Test
    void itGetLegId_is_simulcast_with_no_channel_record() throws Exception {
        when(ribbonClient.getLegId("cvid")).thenReturn("legid");
        when(lspMedialiveStateClient.getLivestreamRecord("cvid")).thenReturn(Optional.of(livestreamRecord));
        LivestreamEvent livestreamEvent = new LivestreamEvent.Builder().id("cvid").build();
        LambdaEvent<SQSMessage> event = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withBody(livestreamEvent);

        assertThrows(StateApiException.class, () -> underTest.apply(event));
    }

    @Test
    void itGetLegId_not_simulcast_but_different_leg_id_exist() throws Exception {
        when(ribbonClient.getLegId("cvid")).thenReturn("legid");
        when(lspMedialiveStateClient.getLivestreamRecord("cvid")).thenReturn(Optional.of(livestreamRecord));
        when(lspMedialiveStateClient.getChannelRecord("cvid")).thenReturn(Optional.of(channelRecord));

        when(livestreamRecord.getWorkflowType()).thenReturn("not simulcast");
        when(channelRecord.getLegId()).thenReturn("differentLegId");

        LivestreamEvent livestreamEvent = new LivestreamEvent.Builder().id("cvid").build();
        LambdaEvent<SQSMessage> event = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty(ISPY_CONTEXT_PROPERTY, ispyContext)
                .withBody(livestreamEvent);

        assertThrows(FailQueueException.class, () -> underTest.apply(event));
        verify(ispyContext).ispy("error.different-leg-id-exists.notification");
    }

    @Test
    void itGetLegId_not_simulcast_but_identical_leg_id_exist() throws Exception {
        when(ribbonClient.getLegId("cvid")).thenReturn("legid");
        when(lspMedialiveStateClient.getLivestreamRecord("cvid")).thenReturn(Optional.of(livestreamRecord));
        when(lspMedialiveStateClient.getChannelRecord("cvid")).thenReturn(Optional.of(channelRecord));

        when(livestreamRecord.getWorkflowType()).thenReturn("not simulcast");
        when(channelRecord.getLegId()).thenReturn("legid");

        LivestreamEvent livestreamEvent = new LivestreamEvent.Builder().id("cvid").build();
        LambdaEvent<SQSMessage> event = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty(ISPY_CONTEXT_PROPERTY, ispyContext)
                .withBody(livestreamEvent);

        LambdaEvent<SQSMessage> apply = underTest.apply(event);

        assertEquals("legid", apply.getPropertyAs(String.class, "LEG_ID"));
        assertEquals(livestreamEvent, apply.getBody(LivestreamEvent.class));

        verify(ispyContext).ispy("info.identical-leg-id-exists");
    }

}
