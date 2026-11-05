Feature: UC5 - Track a parcel
  As a recipient or partner
  I want to see the current state and the timestamped history of a parcel
  So that I know where it is

  Scenario: Track a parcel after drop-off
    Given a parcel ready for pickup at "Porto Central"
    When the parcel is tracked by its tracking code
    Then the parcel state is "READY_FOR_PICKUP"
    And the history contains the following transitions:
      | state            | actor   |
      | CREATED          | Partner |
      | IN_TRANSIT       | Courier |
      | READY_FOR_PICKUP | Courier |
    And every history entry has a timestamp

  Scenario: Track a parcel after pickup
    Given a parcel ready for pickup at "Porto Central"
    When the recipient picks up the parcel with the correct pickup code
    And the parcel is tracked by its tracking code
    Then the parcel state is "PICKED_UP"
    And the history contains the following transitions:
      | state            | actor     |
      | CREATED          | Partner   |
      | IN_TRANSIT       | Courier   |
      | READY_FOR_PICKUP | Courier   |
      | PICKED_UP        | Recipient |
