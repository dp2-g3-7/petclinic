Feature: Delete banner
  As an admin, I want to delete banners.

  Scenario: Delete banner succesfuly (positive)
    Given I am not logged in the system
    When Im logged in the system as "admin1"
    And I list all the banners
    Then The banner is deleted

  Scenario: Fail deleting banner (negative)
  	Given I am not logged in the system
    When Im logged in the system as "admin1"
    And I list the banners
    Then A message with the error appears