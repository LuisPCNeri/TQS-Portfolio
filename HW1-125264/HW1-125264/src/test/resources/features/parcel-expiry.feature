Feature: Parcel expiry
  As an operator
  I want expired parcels to be identified and removable
  So that I can return them to the partner

  Scenario: A parcel expires after its pickup deadline
    Given a parcel ready for pickup at "Porto Central"
    And the parcel's pickup deadline has passed
    When the expiry check runs
    Then the parcel is in state "EXPIRED"
    And the operator can see the parcel among expired parcels
    When the operator registers the parcel for removal
    Then the parcel is in state "RETURNED"
    And the compartment is available again
