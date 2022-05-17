# frozen_string_literal: true

Given('lsp-legid-setter lambda exists') do
  puts '---------------'
  puts '- lsp-legid-setter -'
  puts '---------------'
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
      statement: 'livestreamCreated',
      contentTypes: [
        'media'
      ]
    },
    data: {
      entityDescription: {},
      payload: {
        id: 'pips-pid-1234',
        event: 'LIVESTREAM_CREATED',
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
  @ribbon_get_leg_double = RestAssured::Double.create(
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

Then('a message appears in the Bad Message Queue') do
  sqs_message = SQS.receive_message('BMQ', 30)
  puts "BMQ: #{sqs_message}"

  expect(sqs_message.body).to include('<badmsg>')
end

Then('the AWS lambda is terminated') do
  expect(@lambda_response.code).to eq(400)
end

Then('the iSpy event {word} is emitted') do |message|
  expected_payload = {
    event_name: "lsp-legid-setter.#{message}",
    aws_request_id: @aws_request_id,
    message_id_received: 'dummy_message_id',
    activity_id: 'correlation_id',
    id: 'pips-pid-1234'
  }
  event = ISPY.wait_find(expected_payload, true)
  puts "event: #{event}"
end

Given('Ribbon Get leg endpoint is called') do |status|
  @ribbon_get_leg_double.wait_for_requests(1)
end
