package uk.co.bbc.lsp_legid_setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
** Provides methods for reading values from the lambda-supplied environment.
** This is based on code the healthcheck lambda, with the names modified.
*/

public class Environment {
    private static final Logger LOG = LoggerFactory.getLogger(Environment.class);
    private static final String AWS_REGION = "AWS_REGION";
    private static final String BAD_MESSAGE_QUEUE_URL_NAME = "BAD_MESSAGE_QUEUE_URL";
    private static final String FAIL_MESSAGE_QUEUE_URL = "FAIL_MESSAGE_QUEUE_URL";
    private static final String ENVIRONMENT = "ENVIRONMENT";
    private static final String ISPY_TOPIC_ARN_NAME = "ISPY_TOPIC_ARN";
    private static final String RIBBON_URL = "RIBBON_URL";
    private static final String STATE_API_CHANNELS_ENDPOINT = "STATE_API_CHANNELS_ENDPOINT";
    private static final String STATE_API_LIVE_STREAM_ENDPOINT = "STATE_API_LIVE_STREAM_ENDPOINT";

    private String get(String key) {
        String s = System.getenv(key);
        LOG.info("getEnvironmentName [{}] = [{}]", key, s);
        return s;
    }

    public String getBadMessageQueueUrl() {
        return get(BAD_MESSAGE_QUEUE_URL_NAME);
    }

    public String getFailMessageQueueUrl() {
        return get(FAIL_MESSAGE_QUEUE_URL);
    }

    public String getEnvironmentName() {
        return get(ENVIRONMENT);
    }

    public String getIspyTopicArn() {
        return get(ISPY_TOPIC_ARN_NAME);
    }

    public String getRegion() {
        return get(AWS_REGION);
    }

    public String getRibbonUrl() {
        return get(RIBBON_URL);
    }
    
    public String getStateApiChannelsEndpoint() {
        return get(STATE_API_CHANNELS_ENDPOINT);
    }

    public String getStateApiLiveStreamEndpointEndpoint() {
        return get(STATE_API_LIVE_STREAM_ENDPOINT);
    }

}
