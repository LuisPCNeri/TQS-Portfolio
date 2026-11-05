Feature: UC6 - Supervise a station
  As an operator
  I want to supervise a station
  So that I can manage parcels and compartments

  Scenario: List parcels at a station by state
    Given a registered "M" parcel for "recipient@example.com" at "Porto Central"
    And a parcel ready for pickup at "Porto Central"
    When the operator lists "READY_FOR_PICKUP" parcels at "Porto Central"
    Then the list contains 1 parcel
    And the listed parcel is the one ready for pickup

  Scenario: Put an available compartment out of service
    Given an available compartment at "Porto Central"
    When the operator marks the compartment as out of service
    Then the compartment is "FAULTY"
    And the compartment is no longer available for new parcels

  Scenario: Only available compartments can be put out of service
    Given an occupied compartment at "Porto Central"
    When the operator tries to mark the compartment as out of service
    Then the operation is rejected
