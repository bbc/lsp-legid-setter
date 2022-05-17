Feature: SQS Lambda receives SQS messages and outputs results

  Scenario: SQS Lambda gets a request
    Given an sqs-lambda-hello-world
    And Ribbon Get leg endpoint will respond with 200
    When a Livestream Created message is sent to the queue
    Then the AWS lambda is invoked
    And the processing is finished

  Scenario: Bad message ends up in the bad message queue
    Given an sqs-lambda-hello-world
    When a bad SQS message is sent to the queue
    Then a message appears in the Bad Message Queue


