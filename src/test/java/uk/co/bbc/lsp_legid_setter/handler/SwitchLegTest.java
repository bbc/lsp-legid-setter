package uk.co.bbc.lsp_legid_setter.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.co.bbc.freeman.ispy.LambdaEventIspyContext.ISPY_CONTEXT_PROPERTY;

import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.lsp_legid_setter.ribbon.RibbonClient;
import uk.co.bbc.lsp_medialive.domain.LivestreamEvent;
import uk.co.bbc.lsp_medialive.restclient.stateapi.LspMedialiveStateClient;
import uk.co.bbc.lsp_medialive.restclient.stateapi.domain.channels.ChannelRecord;
import uk.co.bbc.lsp_medialive.restclient.stateapi.exception.StateApiException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SwitchLegTest {

    private static final String LEG_ID = "legId";
    private static final String CVID = "cvid";
    @Mock RibbonClient ribbonClient;
    @Mock LspMedialiveStateClient lspMedialiveStateClient;
    @Mock
    private IspyContext ispyContext;
    
    SwitchLeg underTest;
    
    @BeforeEach
    void setup() {
        underTest = new SwitchLeg(ribbonClient, lspMedialiveStateClient);
    }
    
    @Test
    void itSetsLeg() throws Exception {
        ChannelRecord channelRecord = new ChannelRecord.Builder().with(b -> {b.legId = LEG_ID;}).build();
        Optional<ChannelRecord> channelRecordOptional = Optional.of(channelRecord);
        when(lspMedialiveStateClient.getChannelRecord(CVID)).thenReturn(channelRecordOptional);
        
        LivestreamEvent livestreamEvent = new LivestreamEvent.Builder().id(CVID).build();
        LambdaEvent<SQSMessage> event =  new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty(ISPY_CONTEXT_PROPERTY, ispyContext)
                .withBody(livestreamEvent);
        
        underTest.apply(event);
        
        verify(lspMedialiveStateClient).getChannelRecord(CVID);
        verify(ribbonClient).setLegId(CVID, LEG_ID);
        verify(ispyContext).ispy("ribbon.set");
    }
    
    @Test
    void itThrowsExceptionWhenNoChannelRecord() throws Exception {
        Optional<ChannelRecord> channelRecordOptional = Optional.ofNullable(null);
        when(lspMedialiveStateClient.getChannelRecord(CVID)).thenReturn(channelRecordOptional);
        
        LivestreamEvent livestreamEvent = new LivestreamEvent.Builder().id(CVID).build();
        LambdaEvent<SQSMessage> event =  new LambdaEvent<>(new SQSEvent.SQSMessage()).withBody(livestreamEvent);
        
        assertThrows(StateApiException.class, () -> underTest.apply(event));
        verify(lspMedialiveStateClient).getChannelRecord(CVID);
    }

    @Test
    void itShouldNotSetLegForUSP() throws Exception {
        ChannelRecord channelRecord = new ChannelRecord.Builder().with(b -> {b.legId = "USP";}).build();
        Optional<ChannelRecord> channelRecordOptional = Optional.of(channelRecord);
        when(lspMedialiveStateClient.getChannelRecord(CVID)).thenReturn(channelRecordOptional);

        LivestreamEvent livestreamEvent = new LivestreamEvent.Builder().id(CVID).build();
        LambdaEvent<SQSMessage> event =  new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty(ISPY_CONTEXT_PROPERTY, ispyContext)
                .withBody(livestreamEvent);

        underTest.apply(event);

        verify(lspMedialiveStateClient).getChannelRecord(CVID);
        verifyNoInteractions(ribbonClient);
        verify(ispyContext).ispy("ribbon.ignore");
    }
}
