#!/bin/sh

# publishes an example message to the 'wrapped' input topic

INT_TOPIC_ARN=arn:aws:sns:eu-west-1:038610054328:IntModavSqsLambdaHelloWorld-TestInputTopic

TOPIC_ARN=${INT_TOPIC_ARN}
ACTIVITY_ID="test_activity_${RANDOM}"
echo ACTIVITY_ID ${ACTIVITY_ID}

MESSAGE="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><matchRequest><providerActivityId>${ACTIVITY_ID}</providerActivityId><matchId>match1</matchId></matchRequest>"

# copy config file to just-config
with-aws modav-development aws s3 cp src/main/resources/sqs-lambda-hello-world.json s3://intmodavsharedresources-configurationbucket-1cimkikp09qru/just-config/sqs-lambda-hello-world.json

sleep 5

# publish message
with-aws modav-development aws sns publish --topic-arn ${TOPIC_ARN} --message "${MESSAGE}" --message-attributes '{"message-attribute":{"DataType":"String","StringValue":"value"}}'

