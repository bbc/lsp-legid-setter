# frozen_string_literal: true

require 'aws-helper-v2'
require 'aws-helper-v2/s3'
require 'erubis'
require 'ispy'
require 'json'
require 'rest-assured'
require 'rest-client'
require 'mod_av_cucumber_env'

require 'pry' # TODO: don't commit this line

BeforeAll do
  QUEUES = {
    BMQ: "#{CLOUD_ID}-lsp-legid-setter-BMQ",
    FMQ: "#{CLOUD_ID}-lsp-legid-setter-FMQ"
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
      'RIBBON_URL' => 'http://127.0.0.1:5432',
      'ENVIRONMENT' => 'cucumber',
      'STATE_API_CHANNELS_ENDPOINT' => 'http://127.0.0.1:5432/channels',
      'STATE_API_LIVE_STREAM_ENDPOINT' => 'http://127.0.0.1:5432/livestreams'
    }
  )
  BASE_URL = under_test.base_url
  RestAssured::Server.start(database: ':memory:', port: 5432)
end

Before do
  RestClient.delete "#{RestAssured::Server.address}/doubles/all"
end
