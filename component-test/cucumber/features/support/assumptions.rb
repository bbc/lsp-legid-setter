# frozen_string_literal: true

require 'aws-helper-v2/s3'

BASE_DIR = File.expand_path('../../../..', File.dirname(__FILE__))
CUCUMBER_DIR = File.expand_path('../..', File.dirname(__FILE__))
FIXTURES_DIR = "#{CUCUMBER_DIR}/fixtures"

S3_BUCKET = AwsHelperV2::S3.cucumber_bucket

JUST_CONFIG_PREFIX = "#{CLOUD_ID}/sqs-lambda-hello-world/just-config"
JUST_CONFIG_FILENAME = "s3://#{S3_BUCKET}/#{JUST_CONFIG_PREFIX}/sqs-lambda-hello-world.json"

UNDERTEST_HANDLER = 'uk.co.bbc.sqs_lambda_hello_world.Main'
ISPY_MSG_DIR = '/tmp/sqs-lambda-hello-world/ispy'

ENV['ISPY_TOPIC_ARN'] = ISPY_MSG_DIR
ENV['JUST_CONFIG_FILENAME'] = JUST_CONFIG_FILENAME
ENV['ENVIRONMENT'] = 'Int'

# Normally provided by AWS Lambda or not necessary
ENV['AWS_REGION'] = 'eu-west-1'

OUTPUT_TOPIC_NAME = "#{CLOUD_ID}-SqsLambdaHelloWorldOutputTopic"
