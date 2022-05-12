#!/bin/sh

# sends an example message directly to the input queue

INT_QUEUE_URL=https://sqs.eu-west-1.amazonaws.com/038610054328/IntModavSqsLambdaHelloWorldResources-Queue-852HEGR0Z31D

QUEUE_URL=${INT_QUEUE_URL}
ACTIVITY_ID="test_activity_${RANDOM}"
echo ACTIVITY_ID ${ACTIVITY_ID}

MESSAGE="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><matchRequest><providerActivityId>${ACTIVITY_ID}</providerActivityId><matchId>match1</matchId></matchRequest>"

# copy config file to just-config
with-aws modav-development aws s3 cp src/main/resources/sqs-lambda-hello-world.json s3://intmodavsharedresources-configurationbucket-1cimkikp09qru/just-config/sqs-lambda-hello-world.json

sleep 5

# send message
with-aws modav-development aws sqs send-message --queue-url ${QUEUE_URL} --message-body "${MESSAGE}" --message-attributes '{"message-attribute":{"DataType":"String","StringValue":"value"}}'

