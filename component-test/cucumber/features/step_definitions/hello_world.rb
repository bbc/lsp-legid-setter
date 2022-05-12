# frozen_string_literal: true

Given('an sqs-lambda-hello-world') do
  puts '---------------'
  puts '- Hello World -'
  puts '---------------'
end

When('an SQS message is sent to the queue') do
  message = {
    records: [
      {
        messageId: 'dummy_message_id',
        awsRegion: 'eu-west-1',
        body: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><matchRequest providerActivityId="random_activity_id" matchId="match1"/>'
      }
    ]
  }
  RestClient.post(BASE_URL, message.to_json)
end

When('a SQS message is sent to the queue with a match not in the config') do
  message = {
    records: [
      {
        messageId: 'dummy_message_id',
        awsRegion: 'eu-west-1',
        body: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><matchRequest providerActivityId="random_activity_id" matchId="failure"/>'
      }
    ]
  }
  RestClient.post(BASE_URL, message.to_json)
end

When('a bad SQS message is sent to the queue') do
  message = {
    records: [
      {
        messageId: 'dummy_message_id',
        awsRegion: 'eu-west-1',
        body: 'not xml'
      }
    ]
  }

  RestClient.post(BASE_URL, message.to_json)
end

Then('the AWS lambda is invoked') do
  # check for invoked message with our message id
  expected_payload = {
    event_name: 'sqs-lambda-hello-world.info.message.received',
    message_id_received: 'dummy_message_id'
  }
  event = ISPY.wait_find(expected_payload, true)
  # save this for later checks
  @aws_request_id = event[:aws_request_id]
end

# this is checking the sns via the subscription to the output topic
Then('a message is sent to the output topic') do
  sqs_message = SQS.receive_message('OUT', 30)
  puts "OUT: #{sqs_message}"
  puts "OUT: #{sqs_message.body}"
  expect(sqs_message).not_to be(nil)
  expect(sqs_message.message_id).not_to be(nil)
  # NB the sqs_message message_id is NOT message_id returned from the sns.publish
end

Then('the processing is finished') do
  # just check ispy message
  expected_payload = {
    event_name: 'sqs-lambda-hello-world.info.message.sent',
    aws_request_id: @aws_request_id,
    message_id_received: 'dummy_message_id'
  }
  event = ISPY.wait_find(expected_payload, true)
  puts "event: #{event}"
  expect(event[:message_id_sent]).not_to be(nil)
end

Then('a message appears in the Bad Message Queue') do
  sqs_message = SQS.receive_message('BMQ', 30)
  puts "BMQ: #{sqs_message}"

  expect(sqs_message.body).to include('<badmsg>')
end

Then('a message appears in the Fail Message Queue') do
  sqs_message = SQS.receive_message('FMQ', 30)
  puts "FMQ: #{sqs_message}"

  expect(sqs_message.body).to include('<failmsg>')
end

Given('a message with a bad matchId is sent to the queue') do
  message = {
    records: [
      {
        messageId: 'dummy_message_id',
        awsRegion: 'eu-west-1',
        body: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><matchRequest providerActivityId="random_activity_id" matchId="forced_failure"/>'
      }
    ]
  }
  begin
    RestClient.post(BASE_URL, message.to_json)
    expect('fail').to eq('test should have thrown an exception')
  rescue RestClient::ExceptionWithResponse => e
    @lambda_response = e.response
  end
end

Then('the AWS lambda reports the exception') do
  expected_payload = { event_name: 'sqs-lambda-hello-world.error.exception' }
  ISPY.wait_find(expected_payload, true)
end

Then('the AWS lambda is terminated') do
  expect(@lambda_response.code).to eq(400)
end
