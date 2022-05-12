Feature: SQS Lambda receives SQS messages and outputs results

  Scenario: SQS Lambda gets a request
    Given an sqs-lambda-hello-world
    When an SQS message is sent to the queue
    Then the AWS lambda is invoked
    And a message is sent to the output topic
    And the processing is finished

  Scenario: Bad message ends up in the bad message queue
    Given an sqs-lambda-hello-world
    When a bad SQS message is sent to the queue
    Then a message appears in the Bad Message Queue

  Scenario: Fail message ends up in the fail message queue
    Given an sqs-lambda-hello-world
    When a SQS message is sent to the queue with a match not in the config
    Then a message appears in the Fail Message Queue

  Scenario: Then AWS lambda reports exception on matchId before instance termination
    Given an sqs-lambda-hello-world
    When a message with a bad matchId is sent to the queue
    Then the AWS lambda reports the exception
    And the AWS lambda is terminated

# TODO more than one message in a request
