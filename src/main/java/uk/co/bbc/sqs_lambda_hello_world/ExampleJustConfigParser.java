package uk.co.bbc.sqs_lambda_hello_world;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
** Parses the configuration file in just-config
*/

public class ExampleJustConfigParser implements Function<InputStream, ExampleJustConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleJustConfigParser.class);
    private static final ObjectReader OBJECT_READER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readerFor(ExampleJustConfig.class);

    @Override
    public ExampleJustConfig apply(InputStream inputStream) {
        try {
            LOG.info("Reading config from stream...");
            ExampleJustConfig config = OBJECT_READER.readValue(inputStream);
            LOG.info("Config [{}]", config);
            return config;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialise configuration file", e);
        }
    }
}
