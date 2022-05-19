package uk.co.bbc.lsp_legid_setter.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.bbc.freeman.ispy.LambdaEventIspyContext.ISPY_CONTEXT_PROPERTY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import uk.co.bbc.deel.StatusMessage;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.lsp_legid_setter.exception.DeserialisationException;
import uk.co.bbc.lsp_legid_setter.util.TestUtils;
import uk.co.bbc.lsp_medialive.domain.LivestreamEvent;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LivestreamEventParserTest {

    @Mock
    private LambdaEvent<SQSEvent.SQSMessage> lambdaEvent;
    @Mock
    private IspyContext ispyContext;
    
    LivestreamEventParser underTest;
    
    @BeforeEach
    void setup() {
        when(this.ispyContext.withActivityId(anyString())).thenReturn(ispyContext);
        underTest = new LivestreamEventParser();
    }
    
    @Test
    void itShouldUnwrapPayload() throws Exception {
        String string = TestUtils.loadResource("livestream-created.json");
        StatusMessage statusMessage = StatusMessage.fromJson(string);
        LivestreamEvent livestreamEvent = statusMessage.getData().getPayload(LivestreamEvent.class);

        lambdaEvent = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty(ISPY_CONTEXT_PROPERTY, ispyContext)
                .withBody(string);

        LambdaEvent<SQSMessage> lambdaEventResult = underTest.apply(lambdaEvent);

        assertEquals(livestreamEvent, lambdaEventResult.getBody(LivestreamEvent.class));

        verify(ispyContext).with(livestreamEvent);
    }
    
    @Test
    void itShouldThrowExceptionWhenEventTypeIsNotCreated() throws Exception {
        String string = TestUtils.loadResource("livestream-deleted.json");
        
        lambdaEvent = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withProperty(ISPY_CONTEXT_PROPERTY, ispyContext)
                .withBody(string);
        
        assertThrows(DeserialisationException.class, () -> underTest.apply(lambdaEvent));
    }
}
