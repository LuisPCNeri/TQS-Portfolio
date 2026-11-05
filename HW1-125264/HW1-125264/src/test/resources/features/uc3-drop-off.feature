Feature: UC3 - Drop off a parcel
  As a courier
  I want to drop off a parcel at a locker terminal
  So that the recipient can collect it

  Scenario: Courier drops off a registered parcel
    Given a registered "M" parcel for "recipient@example.com" at "Porto Central"
    When the courier drops off the parcel
    Then the parcel is in state "READY_FOR_PICKUP"
    And a pickup code is issued
    And the reserved compartment was opened
