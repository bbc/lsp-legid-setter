package uk.co.bbc.lsp_legid_setter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.co.bbc.freeman.aws.BadMessageHandler;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.Handler.EmptyHandler;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.freeman.ispy.LambdaEventIspyContext;
import uk.co.bbc.ispy.Ispyer;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.lsp_legid_setter.predicator.LegIdIsNotSet;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MainTest {

    @Mock private Handler<SQSMessage> getLegIdFromRibbon;
    @Mock private SQSEvent sqsEvent;
    @Mock private Context context;
    @Mock private SQSMessage sqsMessage0;
    @Mock private BadMessageHandler badMessageHandler;
    @Mock private EmptyHandler<SQSMessage> liveStreamEventParser;
    @Mock private Ispyer ispyer;
    @Mock private Handler<SQSMessage> switchLeg;
    @Mock private LegIdIsNotSet legIdIsNotSet;
    @Mock private LegIdIsNotSet legIdIsNotSetNegate;
    @Captor private ArgumentCaptor<LambdaEvent<SQSMessage>> messageCaptor;

    @BeforeEach
    void setup() throws Exception{
        when(liveStreamEventParser.apply(any())).thenAnswer(i -> i.getArgument(0));;
        when(badMessageHandler.handleException(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(getLegIdFromRibbon.apply(any())).thenAnswer(i -> i.getArgument(0));
        when(switchLeg.apply(any())).thenAnswer(i -> i.getArgument(0));
        when(legIdIsNotSet.test(any())).thenReturn(true);
        when(legIdIsNotSet.negate()).thenReturn(legIdIsNotSetNegate);
        when(legIdIsNotSetNegate.test(any())).thenReturn(false);

        when(sqsMessage0.getBody()).thenReturn("{\"detail\": \"value\"}");
        when(sqsMessage0.getMessageId()).thenReturn("");
        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
    }

    @Test
    void testHandleZeroMessages() {
        when(sqsEvent.getRecords()).thenReturn(List.of());

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, legIdIsNotSet, switchLeg, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verifyNoInteractions(getLegIdFromRibbon);
    }

    @Test
    void happyPath() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, legIdIsNotSet, switchLeg, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(ispyer).ispy(eq("lsp-legid-setter.livestream-created.received"), any(), any());
        verify(getLegIdFromRibbon).apply(any());
        verify(switchLeg).apply(any());
    }
    
    @Test
    void itIgnoresWhenLegIdIsSet() throws Exception {
        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        when(legIdIsNotSet.test(any())).thenReturn(false);
        when(legIdIsNotSetNegate.test(any())).thenReturn(true);

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, legIdIsNotSet, switchLeg, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        
        verify(getLegIdFromRibbon).apply(any());
        verifyNoInteractions(switchLeg);
        verify(ispyer).ispy(eq("lsp-legid-setter.livestream-created.received"), any(), any());
        verify(ispyer).ispy(eq("lsp-legid-setter.ribbon.ignored"), any(), any());
    }

    @Test
    void testHandleOneBadMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        doThrow(new JsonParseException(null, "foo")).when(liveStreamEventParser).apply(any());

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, legIdIsNotSet, switchLeg, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(badMessageHandler).handleException(any(), any());
        verifyNoInteractions(getLegIdFromRibbon);

    }

    @Test
    void testAddIspyContextToEvent() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, legIdIsNotSet, switchLeg, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(getLegIdFromRibbon, times(1)).apply(messageCaptor.capture());
        final IspyContext ispyContext = messageCaptor.getValue().getPropertyAs(
                IspyContext.class,
                LambdaEventIspyContext.ISPY_CONTEXT_PROPERTY
        );
        assertThat(ispyContext.getEventNamePrefix(), is("lsp-legid-setter"));
    }

    @Test
    void testAddMessageIdToContext() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        when(sqsMessage0.getMessageId()).thenReturn("uuid");

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, legIdIsNotSet, switchLeg, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(getLegIdFromRibbon, times(1)).apply(messageCaptor.capture());
        final IspyContext ispyContext = messageCaptor.getValue().getPropertyAs(
                IspyContext.class,
                LambdaEventIspyContext.ISPY_CONTEXT_PROPERTY
        );
        assertThat(ispyContext.toMap(), hasEntry("message_id_received", "uuid"));

    }

    @Test
    void testMessageReceivedIspy() {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, legIdIsNotSet, switchLeg, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(ispyer).ispy(eq("lsp-legid-setter.livestream-created.received"), eq(""), any());
     }

}
