Feature: 
  AS MS
  WE WANT a lambda to set the legid via the Ribbon API
  SO that push packager can consume the streamSQS Lambda receives SQS messages and outputs results

  Scenario: Get stream details from Ribbon and workflow type is simulcast
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 200
    And Medialive State Api Get live stream endpoint will respond with 200 and workflow type "simulcast"
    And Medialive State Api Get channels endpoint will respond with 200 and leg id "legId"
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And the iSpy event "lsp-legid-setter.ribbon.ignored" is emitted

  Scenario: Get stream details from Ribbon and when identical leg-id exist and the workflow type is not simulcast
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 200
    And Medialive State Api Get live stream endpoint will respond with 200 and workflow type "not simulcast"
    And Medialive State Api Get channels endpoint will respond with 200 and leg id "legId"
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And the iSpy event "lsp-legid-setter.info.identical-leg-id-exists" is emitted

  Scenario: Get stream details from Ribbon and when different leg-id exist and the workflow type is not simulcast
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 200
    And Medialive State Api Get live stream endpoint will respond with 200 and workflow type "not simulcast"
    And Medialive State Api Get channels endpoint will respond with 200 and leg id "duplicateLegId"
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And the iSpy event "lsp-legid-setter.error.different-leg-id-exists.notification" is emitted
    Then a message appears in the Fail Message Queue

  Scenario: Set leg id by Ribbon  
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 404
    And Ribbon Put leg endpoint will respond with 200
    And Medialive State Api Get live stream endpoint will respond with 200 and workflow type "simulcast"
    And Medialive State Api Get channels endpoint will respond with 200 and leg id "legId"
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And Medialive State Api Get channels endpoint is called
    And Ribbon Put leg endpoint is called
    Then the iSpy event "lsp-legid-setter.ribbon.set" is emitted

  Scenario: Don't set leg id by Ribbon when leg id is USP  
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 404
    And Medialive State Api Get channels endpoint will respond with 200 and leg id "USP"
    And Medialive State Api Get live stream endpoint will respond with 200 and workflow type "simulcast"
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And Medialive State Api Get channels endpoint is called
    Then the iSpy event "lsp-legid-setter.ribbon.ignore" is emitted

  Scenario: Bad message ends up in the bad message queue
    Given lsp-legid-setter lambda exists
    When a bad SQS message is sent to the queue
    Then a message appears in the Bad Message Queue

  @sad
  Scenario: Get stream details from Ribbon but Ribbon returns error
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 500
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And the iSpy event "lsp-legid-setter.error.exception" is emitted

  @sad
  Scenario: Get channel endpoint from state api but it returns error
    Given lsp-legid-setter lambda exists
    And Ribbon Get leg endpoint will respond with 404
    And Medialive State Api Get channels endpoint will respond with 500 and leg id "legid"
    And Medialive State Api Get live stream endpoint will respond with 200 and workflow type "simulcast"
    When a Livestream Created message is sent to the queue
    Then the iSpy event "lsp-legid-setter.livestream-created.received" is emitted
    And Ribbon Get leg endpoint is called
    And Medialive State Api Get channels endpoint is called
    Then the iSpy event "lsp-legid-setter.error.exception" is emitted