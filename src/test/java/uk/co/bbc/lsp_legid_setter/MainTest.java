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
import uk.co.bbc.freeman.aws.FailMessageHandler;
import uk.co.bbc.freeman.aws.SendToSns;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.Handler.EmptyHandler;
import uk.co.bbc.freeman.core.LambdaEvent;
import uk.co.bbc.freeman.ispy.LambdaEventIspyContext;
import uk.co.bbc.ispy.Ispyer;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.lsp_legid_setter.ExampleSqsRequest;
import uk.co.bbc.lsp_legid_setter.Main;
import uk.co.bbc.lsp_legid_setter.MatchNotFoundException;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith(MockitoExtension.class)
class MainTest {

    @Mock(lenient = true) private Handler<SQSMessage> helloWorldHandler;
    @Mock(lenient = true) private SQSEvent sqsEvent;
    @Mock(lenient = true) private Context context;
    @Mock(lenient = true) private SQSMessage sqsMessage0;
    @Mock(lenient = true) private SQSMessage sqsMessage1;
    @Mock(lenient = true) private SQSMessage sqsMessage2;
    @Mock(lenient = true) private BadMessageHandler badMessageHandler;
    @Mock(lenient = true) private FailMessageHandler failMessageHandler;
    @Mock(lenient = true) private EmptyHandler<SQSMessage> xmlParser;
    @Mock(lenient = true) private SendToSns<SQSMessage> sendToSns;
    @Mock(lenient = true) private Ispyer ispyer;
    @Captor private ArgumentCaptor<LambdaEvent<SQSMessage>> messageCaptor;

    @BeforeEach
    void setup() throws Exception{
        when(xmlParser.apply(any())).thenCallRealMethod();
        when(badMessageHandler.handleException(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(failMessageHandler.handleException(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(sendToSns.apply(any())).thenAnswer(i -> i.getArgument(0));
        when(helloWorldHandler.apply(any())).thenAnswer(i -> i.getArgument(0));

        when(sqsMessage0.getBody()).thenReturn("");
        when(sqsMessage0.getMessageId()).thenReturn("");
        when(sqsMessage1.getBody()).thenReturn("");
        when(sqsMessage1.getMessageId()).thenReturn("");
        when(sqsMessage2.getBody()).thenReturn("");
        when(sqsMessage2.getMessageId()).thenReturn("");
    }

    @Test
    void testHandleZeroMessages() {
        when(sqsEvent.getRecords()).thenReturn(List.of());

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verifyNoInteractions(helloWorldHandler);
    }

    @Test
    void testHandleOneGoodMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(helloWorldHandler).apply(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getOriginal(), is(sqsMessage0));
    }

    @Test
    void testHandleMultipleMessages() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0, sqsMessage1));

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(helloWorldHandler, times(2)).apply(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(0).getOriginal(), is(sqsMessage0));
        assertThat(messageCaptor.getAllValues().get(1).getOriginal(), is(sqsMessage1));

    }

    @Test
    void testHandleOneBadMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        doThrow(new JsonParseException(null, "foo")).when(xmlParser).apply(any());

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(badMessageHandler).handleException(any(), any());
        verifyNoInteractions(helloWorldHandler);

    }

    @Test
    void testHandleOneGoodMessageOneBadMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0, sqsMessage1));
        doCallRealMethod().doThrow(new JsonParseException(null, "foo")).when(xmlParser).apply(any());

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(helloWorldHandler, times(1)).apply(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(0).getOriginal(), is(sqsMessage0));

    }

    @Test
    void testHandleOneBadMessageOneGoodMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0, sqsMessage1));
        doThrow(new JsonParseException(null, "foo")).doCallRealMethod().when(xmlParser).apply(any());

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(helloWorldHandler, times(1)).apply(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(0).getOriginal(), is(sqsMessage1));
    }

    @Test
    void testHandleOneFailMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        doThrow(new MatchNotFoundException("foo")).when(helloWorldHandler).apply(any());

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(failMessageHandler).handleException(messageCaptor.capture(), any());
        assertThat(messageCaptor.getAllValues().get(0).getOriginal(), is(sqsMessage0));

    }

    @Test
    void testHandleOneGoodMessageOneFailMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0, sqsMessage1));
        doAnswer(i -> i.getArgument(0)).doThrow(new MatchNotFoundException("foo")).when(helloWorldHandler).apply(any());

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(failMessageHandler, times(1)).handleException(messageCaptor.capture(), any());
        assertThat(messageCaptor.getAllValues().get(0).getOriginal(), is(sqsMessage1));
    }

    @Test
    void testHandleOneFailMessageOneGoodMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0, sqsMessage1));
        doThrow(new MatchNotFoundException("foo")).doAnswer(i -> i.getArgument(0)).when(helloWorldHandler).apply(any());

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(failMessageHandler, times(1)).handleException(messageCaptor.capture(), any());
        assertThat(messageCaptor.getAllValues().get(0).getOriginal(), is(sqsMessage0));
    }

    @Test
    void testHandlesTheGoodTheBadAndTheUglyMessages() throws Exception {
        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0, sqsMessage1, sqsMessage2));
        doCallRealMethod().doThrow(new JsonParseException(null, "foo")).doCallRealMethod().when(xmlParser).apply(any());
        doAnswer(i -> i.getArgument(0)).doThrow(new MatchNotFoundException("foo")).when(helloWorldHandler).apply(any());

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);

        verify(badMessageHandler, times(1)).handleException(messageCaptor.capture(), any());
        assertThat(messageCaptor.getAllValues().get(0).getOriginal(), is(sqsMessage1));
        verify(failMessageHandler, times(1)).handleException(messageCaptor.capture(), any());
        assertThat(messageCaptor.getAllValues().get(1).getOriginal(), is(sqsMessage2));
    }

    @Test
    void testAddIspyContextToEvent() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(helloWorldHandler, times(1)).apply(messageCaptor.capture());
        final IspyContext ispyContext = messageCaptor.getValue().getPropertyAs(
                IspyContext.class,
                LambdaEventIspyContext.ISPY_CONTEXT_PROPERTY
        );
        assertThat(ispyContext.getEventNamePrefix(), is("sqs-lambda-hello-world"));
    }

    @Test
    void testAddMessageIdToContext() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        when(sqsMessage0.getMessageId()).thenReturn("uuid");

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(helloWorldHandler, times(1)).apply(messageCaptor.capture());
        final IspyContext ispyContext = messageCaptor.getValue().getPropertyAs(
                IspyContext.class,
                LambdaEventIspyContext.ISPY_CONTEXT_PROPERTY
        );
        assertThat(ispyContext.toMap(), hasEntry("message_id_received", "uuid"));

    }

    @Test
    void testMessageReceivedIspy() {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(ispyer).ispy(eq("sqs-lambda-hello-world.info.message.received"), eq(""), any());
     }

    @Test
    void testMessageReceivedIspyWithFilledContent() throws Exception {

        ExampleSqsRequest exampleSqsRequest = new ExampleSqsRequest("uuid", "6");
        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        when(xmlParser.apply(any())).thenAnswer( iom -> {
            LambdaEvent<SQSMessage> event = iom.getArgument(0);
            return event.withBody(exampleSqsRequest);
        });

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(ispyer).ispy(
                eq("sqs-lambda-hello-world.info.message.received"),
                eq("uuid"),
                (Map<String, Object>)argThat(hasEntry("match_id", (Object)"6")));
     }

    @Test
    void testHandleLambdaEventOfTypeExampleSqsRequest() throws Exception {

        ExampleSqsRequest exampleSqsRequest = new ExampleSqsRequest("uuid", "4");
        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        when(xmlParser.apply(any())).thenAnswer( iom -> {
            LambdaEvent<SQSMessage> event = iom.getArgument(0);
            return event.withBody(exampleSqsRequest);
        });

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(helloWorldHandler, times(1)).apply(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(0).getBody(ExampleSqsRequest.class), is(exampleSqsRequest));

    }

    @Test
    void testHandleSnsSourcedMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        when(sqsMessage0.getBody()).thenReturn("{\"MessageId\": \"uuid\", \"Message\": \"sdfghjkl\"}");

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(xmlParser).apply(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(0).getBody(String.class), is("sdfghjkl"));

    }

    @Test
    void testHandleSqsSourcedMessage() throws Exception {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        when(sqsMessage0.getBody()).thenReturn("sdfghjkl");

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(xmlParser).apply(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(0).getBody(String.class), is("sdfghjkl"));

    }

    @Test
    void testSendsResultToSns() {

        when(sqsEvent.getRecords()).thenReturn(List.of(sqsMessage0));
        when(sqsMessage0.getBody()).thenReturn("sdfghjkl");

        Main undertest = new Main(helloWorldHandler, xmlParser, sendToSns, badMessageHandler, failMessageHandler, ispyer);
        undertest.handleRequest(sqsEvent, context);
        verify(sendToSns).apply(any());

    }

}
