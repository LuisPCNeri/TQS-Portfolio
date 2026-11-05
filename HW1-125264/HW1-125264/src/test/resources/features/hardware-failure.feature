Feature: Hardware failure
  As a courier or recipient
  I want the system to report hardware failures safely
  So that parcel state is not corrupted

  Scenario: Drop-off fails when the compartment cannot be opened
    Given a registered "M" parcel for "recipient@example.com" at "Porto Central"
    And the compartment hardware has failed
    When the courier drops off the parcel
    Then a hardware failure is reported
    And the parcel remains in state "CREATED"

  Scenario: Pickup fails when the compartment cannot be opened
    Given a parcel ready for pickup at "Porto Central"
    And the compartment hardware has failed
    When the recipient picks up the parcel with the correct pickup code
    Then a hardware failure is reported
    And the parcel remains in state "READY_FOR_PICKUP"
    And the compartment is still occupied
