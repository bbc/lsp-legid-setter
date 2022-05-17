Feature: SQS Lambda receives SQS messages and outputs results

  Scenario: GET stream details
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 200
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called

  Scenario: Bad message ends up in the bad message queue
    Given lsp-legid-setter lambda exists
    When a bad SQS message is sent to the queue
    Then a message appears in the Bad Message Queue


