package uk.co.bbc.lsp_legid_setter.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.lsp_legid_setter.ribbon.RibbonClient;
import uk.co.bbc.lsp_medialive.domain.LivestreamEvent;

@ExtendWith(MockitoExtension.class)
class GetLegIdFromRibbonTest {
    @Mock
    RibbonClient ribbonClient;
    
    GetLegIdFromRibbon underTest;
    
    @BeforeEach
    void setup() {
        underTest = new GetLegIdFromRibbon(ribbonClient);
    }

    @Test
    void itGetLegId() throws Exception {
        when(ribbonClient.getLegId("cvid")).thenReturn("legid");
        LivestreamEvent livestreamEvent = new LivestreamEvent.Builder().id("cvid").build();
        LambdaEvent<SQSMessage> event = new LambdaEvent<>(new SQSEvent.SQSMessage())
                .withBody(livestreamEvent);
        LambdaEvent<SQSMessage> apply = underTest.apply(event);
        assertEquals("legid", apply.getBody(String.class));
    }
}
