package uk.co.bbc.sqs_lambda_hello_world;

import com.amazonaws.services.lambda.runtime.Context;

import java.util.Map;
import java.util.function.Supplier;

/*
** This class is designed to hold all the invariant details for this server for use when logging.
** These include per-instance and per-release details like the following:
**
** event_host -> constant
** deployment_region -> per release
** deployment_account -> per release
** deployment_id -> per release
** aws_log_group_name -> per instance
** aws_log_stream_name -> per instance
** 
** but not per-event things like requestId or messageId (see Main.provideIspyContext())
**
** (there's nothing to guarantee that lambda instances will be reused but in practice they are
** and as reused instances share a log stream i've included those as static)
*/

public class StaticParams {

    private final Context context;

    public StaticParams(Context context) {
        this.context = context;
    }

    public Supplier<Map<String, Object>> getIspyMapSupplier() {
        return () -> {
            String[] bits = context.getInvokedFunctionArn().split(":");
            return Map.of("event_host", "AWS/Lambda",
                    "deployment_region", bits[3],
                    "deployment_account", bits[4],
                    "deployment_id", bits[6],
                    "aws_log_group_name", context.getLogGroupName(),
                    "aws_log_stream_name", context.getLogStreamName());
        };
    }
}
