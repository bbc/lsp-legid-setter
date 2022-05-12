# frozen_string_literal: true

require 'aws-helper-v2/s3'

BASE_DIR = File.expand_path('../../../..', File.dirname(__FILE__))
CUCUMBER_DIR = File.expand_path('../..', File.dirname(__FILE__))
FIXTURES_DIR = "#{CUCUMBER_DIR}/fixtures"

S3_BUCKET = AwsHelperV2::S3.cucumber_bucket


UNDERTEST_HANDLER = 'uk.co.bbc.sqs_lambda_lsp_legid_setter.Main'
ISPY_MSG_DIR = '/tmp/sqs-lambda-hello-world/ispy'

ENV['ISPY_TOPIC_ARN'] = ISPY_MSG_DIR
ENV['ENVIRONMENT'] = 'Int'
CLOUD_ID = ENV['CLOUD_ID']

# Normally provided by AWS Lambda or not necessary
ENV['AWS_REGION'] = 'eu-west-1'

OUTPUT_TOPIC_NAME = "#{CLOUD_ID}-SqsLambdaHelloWorldOutputTopic"
