Feature: 
  AS MS
  WE WANT a lambda to set the legid via the Ribbon API
  SO that push packager can consume the streamSQS Lambda receives SQS messages and outputs results

  Scenario: GET stream details
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 200
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And the iSpy event "lsp-legid-setter.ribbon.ignored" is emitted

  Scenario: set leg id by Ribbon  
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 404
    And Ribbon Put leg endpoint will respond with 200
    And medialive state api Get channels endpoint will respond with 200
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And medialive state api Get channels endpoint is called
    And Ribbon Put leg endpoint is called
    Then the iSpy event "lsp-legid-setter.ribbon.set" is emitted

  Scenario: Bad message ends up in the bad message queue
    Given lsp-legid-setter lambda exists
    When a bad SQS message is sent to the queue
    Then a message appears in the Bad Message Queue

  @sad
  Scenario: GET stream details from Ribbon but Ribbon returns error
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 500
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And the iSpy event "lsp-legid-setter.error.exception" is emitted

  @sad
  Scenario: get channel endpoint from state api but it returns error
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 404
    And medialive state api Get channels endpoint will respond with 500
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And medialive state api Get channels endpoint is called
    Then the iSpy event "lsp-legid-setter.error.exception" is emitted


