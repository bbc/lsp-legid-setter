# frozen_string_literal: true

Given('lsp-legid-setter lambda exists') do
  puts '---------------'
  puts '- lsp-legid-setter -'
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

When('a Livestream Created message is sent to the queue') do
  @deel_message = {
    header: {
      deelVersion: '2-0-0',
      correlationID: 'correlation_id',
      sourceMessageID: 'source_message_id',
      eventTimestamp: '2019-12-03T10:45:01.555Z',
      messageType: 'STATUS',
      primaryEntityID: 'pips-pid-1234',
      origin: 'lsp-livestream-finished-adapter'
    },
    messageContext: {
      domain: 'bbc.content.distribution',
      function: 'liveEncode',
      statement: 'livestreamFinished',
      contentTypes: [
        'media'
      ]
    },
    data: {
      entityDescription: {},
      payload: {
        id: 'pips-pid-1234',
        event: 'LIVESTREAM_FINISHED',
        inputName: 'inputName',
        encoderPipeline: 'encoderPipeline',
        encoderTimestamp: 'encoderTimestamp'
      }
    }
  }

  event_bus_message = {
    version: '0',
    id: '4348494e-a677-5dc4-81a3-4b28e9611ef2',
    'detail-type': 'detail-type',
    source: 'source',
    account: '123456789',
    time: '2022-02-14T15:24:57Z',
    region: 'eu-west-1',
    resources: [],
    detail: @deel_message
  }
  sqs_message = {
    records: [
      {
        messageId: 'dummy_message_id',
        awsRegion: 'eu-west-1',
        body: JSON.generate(event_bus_message)
      }
    ]
  }
  begin
    RestClient.post(BASE_URL, sqs_message.to_json)
  rescue RestClient::BadRequest
    @lambda_failed = true
  end
end

Given('Ribbon Get leg endpoint will respond with {int}') do |status|
  if status == "200"
    content = '{"leg":"indigo"}'
  else
    content = "#{status} body"
  end
  @double = RestAssured::Double.create(
    fullpath: '/packager/pips-pid-1234/leg',
    verb: 'GET',
    status: status,
    content: content
  )
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

Then('the AWS lambda reports the exception') do
  expected_payload = { event_name: 'sqs-lambda-hello-world.error.exception' }
  ISPY.wait_find(expected_payload, true)
end

Then('the AWS lambda is terminated') do
  expect(@lambda_response.code).to eq(400)
end
