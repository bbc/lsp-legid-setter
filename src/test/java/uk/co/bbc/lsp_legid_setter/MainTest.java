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
import uk.co.bbc.freeman.aws.BadMessageHandler;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.Handler.EmptyHandler;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.freeman.ispy.LambdaEventIspyContext;
import uk.co.bbc.ispy.Ispyer;
import uk.co.bbc.ispy.core.IspyContext;
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
class MainTest {

    @Mock(lenient = true) private Handler<SQSMessage> getLegIdFromRibbon;
    @Mock(lenient = true) private SQSEvent sqsEvent;
    @Mock(lenient = true) private Context context;
    @Mock(lenient = true) private SQSMessage sqsMessage0;
    @Mock(lenient = true) private BadMessageHandler badMessageHandler;
    @Mock(lenient = true) private EmptyHandler<SQSMessage> liveStreamEventParser;
    @Mock(lenient = true) private Ispyer ispyer;
    @Captor private ArgumentCaptor<LambdaEvent<SQSMessage>> messageCaptor;

    @BeforeEach
    void setup() throws Exception{
        when(liveStreamEventParser.apply(any())).thenAnswer(i -> i.getArgument(0));;
        when(badMessageHandler.handleException(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(getLegIdFromRibbon.apply(any())).thenAnswer(i -> i.getArgument(0));

        when(sqsMessage0.getBody()).thenReturn("{\"detail\": \"value\"}");
        when(sqsMessage0.getMessageId()).thenReturn("");
        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
    }

    @Test
    void testHandleZeroMessages() {
        when(sqsEvent.getRecords()).thenReturn(List.of());

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verifyNoInteractions(getLegIdFromRibbon);
    }

    @Test
    void testHandleOneGoodMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(getLegIdFromRibbon).apply(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getOriginal(), is(sqsMessage0));
    }

    @Test
    void testHandleOneBadMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        doThrow(new JsonParseException(null, "foo")).when(liveStreamEventParser).apply(any());

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(badMessageHandler).handleException(any(), any());
        verifyNoInteractions(getLegIdFromRibbon);

    }

    @Test
    void testAddIspyContextToEvent() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, badMessageHandler, ispyer);
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

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, badMessageHandler, ispyer);
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

        Main undertest = new Main(getLegIdFromRibbon, liveStreamEventParser, badMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(ispyer).ispy(eq("lsp-legid-setter.livestream-created.received"), eq(""), any());
     }

}
