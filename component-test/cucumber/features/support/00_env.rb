# frozen_string_literal: true

require 'aws-helper-v2'
require 'aws-helper-v2/s3'
require 'erubis'
require 'ispy'
require 'json'
require 'rest-assured'
require 'rest-client'
require 'mod_av_cucumber_env'

AfterConfiguration do
  # create test topic and test queue
  # (can't use create_and_track_topic as we need topic arn)
  OUTPUT_TOPIC_ARN = AwsHelperV2SNS.create_topic(OUTPUT_TOPIC_NAME)
  OUTPUT_QUEUE_ARN, OUTPUT_QUEUE_URL = AwsHelperV2SNS.create_tracking_queue(OUTPUT_TOPIC_NAME, OUTPUT_TOPIC_ARN)
  AwsHelperV2SNS.create_raw_subscription(OUTPUT_TOPIC_ARN, OUTPUT_QUEUE_ARN)
  puts "OutputTopicArn: #{OUTPUT_TOPIC_ARN}"
  puts "OutputQueueUrl: #{OUTPUT_QUEUE_URL}"

  QUEUES = {
    OUT: OUTPUT_QUEUE_URL.split('/').last,
    BMQ: "#{CLOUD_ID}-sqslambdahelloworld-BMQ",
    FMQ: "#{CLOUD_ID}-sqslambdahelloworld-FMQ"
  }.freeze

  SQS = ModavCucumber::SqsHelper.create_helper_with_queues(Aws::SQS::Client.new, QUEUES, check_all_empty: true)
  puts "QUEUES: #{QUEUES}"

  ModavCucumber.setup_s3(bucket: S3_BUCKET)

  ISPY = ModavCucumber::FileIspyHelper.create_helper_with_hooks(ISPY_MSG_DIR)

  under_test = ModavCucumber.setup_and_start_under_test(
    [
      "#{BASE_DIR}/run",
      UNDERTEST_HANDLER,
      'com.amazonaws.services.lambda.runtime.events.SQSEvent'
    ],
    wait: /Started server/,
    wait_timeout: 15,
    #    :listener_pattern => /Listening on 0\.0\.0\.0:(\d+)/,
    listener_pattern: /Started server at port (\d+)/,
    print_lines: true,
    env: {
      'BAD_MESSAGE_QUEUE_URL' => SQS.urls[:BMQ],
      'FAIL_MESSAGE_QUEUE_URL' => SQS.urls[:FMQ],
      'OUTPUT_TOPIC_ARN' => OUTPUT_TOPIC_ARN,
      'RIBBON_URL' => 'http://127.0.0.1:5432'
    }
  )
  BASE_URL = under_test.base_url
  RestAssured::Server.start(database: ':memory:', port: 5432)
end

Before do
  fixtures_dir = File.expand_path('../../fixtures', File.dirname(__FILE__))
  RestClient.delete "#{RestAssured::Server.address}/doubles/all"
end
