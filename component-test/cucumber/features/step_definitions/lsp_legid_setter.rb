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
  content = if status == 200
              '{"leg":"legId"}'
            else
              "#{status} body"
            end
  @ribbon_get_leg_double = RestAssured::Double.create(
    fullpath: '/packager/pips-pid-1234/leg',
    verb: 'GET',
    status: status,
    content: content
  )
end

Given('Ribbon Put leg endpoint will respond with {int}') do |status|
  @ribbon_put_leg_double = RestAssured::Double.create(
    fullpath: '/packager/pips-pid-1234/leg',
    verb: 'PUT',
    status: status,
    content: "#{status} body"
  )
end

Given('Medialive State Api Get live stream endpoint will respond with {int} and workflow type {string}') do
                                                                                      |status, workflow_type|
  content = if status == 200
              {
                'cvid' => 'pips-pid-1234',
                'workflow_type' => workflow_type,
                'profile_name' => 'profile_name',
                'tenant' => 'tenant',
                'start_time' => '1578572375000',
                'end_time' => '1578572375000',
                'slate_input_name' => 'slateInputName',
                'created_timestamp' => '1578572375000',
                'last_update_timestamp' => '1578572375257'
              }
            else
              {}
            end
  @state_get_live_stream_double = RestAssured::Double.create(
    fullpath: '/livestreams?cvid=pips-pid-1234',
    verb: 'GET',
    status: status,
    content: JSON.generate([content])
  )
end

Given('Medialive State Api Get channels endpoint will respond with {int} and leg id {string}') do |status, legid|
  content = if status == 200
              {
                'channel_arn' => 'Arn',
                'workflow_type' => 'Event',
                'state' => 'CREATING',
                'cvid' => 'pips-pid-1234',
                'active_input' => 'activeInput',
                'target_input' => 'targetInput',
                'slate_input_name' => 'slateInputName',
                'leg_id' => legid,
                'created_timestamp' => '1578572375000',
                'last_update_timestamp' => '1578572375257'
              }
            else
              {}
            end
  @state_get_channel_double = RestAssured::Double.create(
    fullpath: '/channels?cvid=pips-pid-1234',
    verb: 'GET',
    status: status,
    content: JSON.generate([content])
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

Then('the iSpy event {string} is emitted') do |message|
  expected_payload = {
    event_name: message,
    aws_request_id: @aws_request_id,
    message_id_received: 'dummy_message_id',
    activity_id: 'correlation_id',
    id: 'pips-pid-1234'
  }
  event = ISPY.wait_find(expected_payload, true)
  puts "event: #{event}"
end

Given('Ribbon Get leg endpoint is called') do
  @ribbon_get_leg_double.wait_for_requests(1)
end

Given('Ribbon Put leg endpoint is called') do
  @ribbon_put_leg_double.wait_for_requests(1)
end

Given('Medialive State Api Get channels endpoint is called') do
  @state_get_channel_double.wait_for_requests(1)
end

Then('a message appears in the Fail Message Queue') do
  sqs_message = SQS.receive_message('FMQ', 30)
  puts "FMQ: #{sqs_message}"

  expect(sqs_message.body).to include('<failmsg>')
end
