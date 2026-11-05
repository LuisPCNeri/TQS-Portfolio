Feature: UC1 - Find a locker
  As a recipient
  I want to find the nearest locker stations for an address
  So that I can choose where to receive my parcel

  Scenario: Show the nearest station and its free compartments per size
    Given the geocoding service locates "Rua de Santa Catarina, Porto" at 41.1496, -8.6109
    When the recipient searches for lockers near "Rua de Santa Catarina, Porto"
    Then the nearest station is "Porto Central"
    And the station offers 2 free S compartments
    And the station offers 2 free M compartments
    And the station offers 1 free L compartment

  Scenario: Order stations by distance from the searched address
    Given the geocoding service locates "Aveiro" at 40.6405, -8.6537
    When the recipient searches for lockers near "Aveiro"
    Then the nearest station is "Aveiro Baixa"
