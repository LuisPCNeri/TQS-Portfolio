Feature: UC4 - Pick up a parcel
  As a recipient
  I want to pick up my parcel with the pickup code
  So that I receive it and the compartment is freed

  Scenario: Recipient picks up a parcel with the correct pickup code
    Given a parcel ready for pickup at "Porto Central"
    When the recipient picks up the parcel with the correct pickup code
    Then the parcel is in state "PICKED_UP"
    And the compartment is available again

  Scenario: Recipient cannot pick up with an invalid pickup code
    Given a parcel ready for pickup at "Porto Central"
    When the recipient picks up the parcel with an invalid pickup code
    Then the pickup is rejected as invalid
    And the parcel remains in state "READY_FOR_PICKUP"
