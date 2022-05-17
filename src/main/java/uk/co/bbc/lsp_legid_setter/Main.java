package uk.co.bbc.lsp_legid_setter;

import uk.co.bbc.freeman.aws.FailMessageHandler;
import uk.co.bbc.freeman.ispy.IspyingExceptionHandler;
import uk.co.bbc.freeman.aws.BadMessageHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonParseException;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.bbc.freeman.aws.MessageIdReceived;
import uk.co.bbc.freeman.aws.SendToSns;
import uk.co.bbc.freeman.aws.SnsJsonExtractor;
import uk.co.bbc.freeman.aws.SnsJsonUnwrapper;
import uk.co.bbc.freeman.core.Handler;
import uk.co.bbc.freeman.core.LambdaEvent;

import static uk.co.bbc.freeman.ispy.LambdaEventIspyContext.addIspyContextToEvent;
import static uk.co.bbc.freeman.core.ExceptionHandler.addExceptionHandler;
import static uk.co.bbc.freeman.core.ExceptionalFunction.makeSafe;
import static uk.co.bbc.freeman.ispy.IspyContentFiller.fillIspyContent;

import uk.co.bbc.freeman.ispy.SimpleIspy;
import uk.co.bbc.ispy.Ispy;
import uk.co.bbc.ispy.Ispyer;
import uk.co.bbc.ispy.IspyerInstantiationException;
import uk.co.bbc.ispy.core.IspyContext;
import uk.co.bbc.ispy.core.IspyPreparer;

public class Main implements RequestHandler<SQSEvent, Void> {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String ISPY_EVENT_PREFIX = "sqs-lambda-hello-world";
    private static final String COMPONENT_NAME = "sqslambdahelloworld";

    private final Environment env = new Environment();
    private final AwsClientProvider clientProvider = new AwsClientProvider(env);

    private Handler<SQSMessage> xmlParser = new XmlParser();
    private Handler<SQSMessage> helloWorldHandler;
    private BadMessageHandler badMessageHandler;
    private FailMessageHandler failMessageHandler;
    private Ispyer ispyer;
    private SendToSns<SQSMessage> sendToSns;

    /**
     * Public, Zero-argument constructor is required by λ.
     */
    public Main() {
    }

    Main(
            Handler<SQSMessage> helloWorldHandler,
            Handler<SQSMessage> xmlParser,
            SendToSns<SQSMessage> sendToSns,
            BadMessageHandler badMessageHandler,
            FailMessageHandler failMessageHandler,
            Ispyer ispyer
    ) {
        this.helloWorldHandler = helloWorldHandler;
        this.xmlParser = xmlParser;
        this.sendToSns = sendToSns;
        this.badMessageHandler = badMessageHandler;
        this.failMessageHandler = failMessageHandler;
        this.ispyer = ispyer;
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        logStartupInfo(event, context);
        initialiseOnFirstCall(context);

        event.getRecords().stream()
                .map(LambdaEvent::new)
                .map(addExceptionHandler(badMessageHandler, JsonParseException.class))
                .map(addExceptionHandler(failMessageHandler, MatchNotFoundException.class))
                .map(addExceptionHandler(new IspyingExceptionHandler<>() /*, no exception filters, catch all. */))
                .map(addIspyContextToEvent(ispyer, ISPY_EVENT_PREFIX))
                .map(makeSafe(new MessageIdReceived(new SnsJsonExtractor())))
                .map(makeSafe(new SnsJsonUnwrapper(new SnsJsonExtractor())))
                .map(makeSafe(xmlParser))
                .filter(LambdaEvent::isNotException) // Don't try to do any more processing of jobs that have already failed.
                .map(fillIspyContent())
                .map(new SimpleIspy<>("info.message.received"))
                .map(makeSafe(helloWorldHandler))
                .map(makeSafe(sendToSns))
                .filter(LambdaEvent::isNotException)
                .forEach(new SimpleIspy<SQSMessage>("info.message.sent").toConsumer());

        return null;
    }

    /**
     * There are several classes that are fairly expensive to create.
     * examples include AWS service clients or loading a config file from S3.
     * Given that instances of this class can, and will be reused with multiple
     * invocations of handleRequest, we can create some of these expensive
     * classes once, and then re-use them for subsequent invocations.
     *
     * @param context
     */
    private void initialiseOnFirstCall(Context context) {
        if (ispyer == null || helloWorldHandler == null || badMessageHandler == null || sendToSns == null) {
            IspyPreparer preparer = new IspyPreparer(new StaticParams(context).getIspyMapSupplier());
            try {
                ispyer = Ispy.newIspyer(env.getIspyTopicArn(), clientProvider.provideSnsClient(), preparer);
            } catch (IspyerInstantiationException ex) {
                throw new RuntimeException("Failed to create ispyer", ex);
            }
            helloWorldHandler = new SqsLambdaHelloWorld();

            badMessageHandler = new BadMessageHandler(clientProvider.provideSqsClient(), env.getBadMessageQueueUrl(), COMPONENT_NAME);
            failMessageHandler = new FailMessageHandler(clientProvider.provideSqsClient(), env.getFailMessageQueueUrl(), COMPONENT_NAME);
            sendToSns = new SendToSns<>(clientProvider.provideSnsClient(), env.getOutputTopicArn());
        }
    }

    private void logStartupInfo(SQSEvent event, Context context) {
        LOG.info("Event: {}", event);
        LOG.info("Context: {}", context);
        LOG.info("Environment {}", new SecretLessMap(System.getenv()));
        LOG.info("Properties {}", System.getProperties());
    }

    /**
     * Implemented as a class so that IF/WHEN info level logging is disabled
     * then the toString() method is never called, and therefore the
     * filtering never happens.
     * If this was in-line in the LOG.info(..) call, the the filtering
     * would happen every time, regardless of logging level.
     */
    private static class SecretLessMap {
        private final Map<String, String> wrapped;

        public SecretLessMap(Map<String, String> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public String toString() {
            return wrapped.entrySet().stream()
                    .filter(e -> !e.getKey().startsWith("AWS_")) // For other secrets, add additional filters.
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                    .toString();
        }
    }
}
