Feature: UC2 - Register a parcel
  As a partner
  I want to register a parcel for a station
  So that a compartment is reserved and a tracking code is returned

  Scenario: Register a medium parcel at Porto Central
    Given the station "Porto Central" has 2 available M compartments
    When the partner registers an "M" parcel for "recipient@example.com" at "Porto Central"
    Then the parcel is in state "CREATED"
    And a tracking code is returned
    And the parcel has a pickup deadline
    And the station "Porto Central" has 1 available M compartment

  Scenario: Registration fails when no compartment of the requested size is available
    Given all "L" compartments at "Aveiro Baixa" are occupied
    When the partner registers an "L" parcel for "recipient@example.com" at "Aveiro Baixa"
    Then registration fails because no compartment is available
