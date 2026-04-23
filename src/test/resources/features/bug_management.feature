Feature: Bug Management
  As a QA engineer
  I want to create, view, and manage bugs
  So that I can track defects in the application

  Scenario: Create a new bug
    Given I am on the BugTracker home page
    When I click the "New Bug" button
    And I fill in the bug form with title "Dropdown not working" and priority "HIGH"
    And I submit the bug form
    Then I should see "Dropdown not working" in the bug list

  Scenario: View bugs in the list
    Given a bug with title "Broken navbar" exists
    And I am on the BugTracker home page
    Then I should see "Broken navbar" in the bug list

  Scenario: Edit an existing bug
    Given a bug with title "Typo in footer" exists
    And I am on the BugTracker home page
    When I click the edit button for "Typo in footer"
    And I change the title to "Typo in footer - fixed"
    And I submit the bug form
    Then I should see "Typo in footer - fixed" in the bug list

  Scenario: Delete a bug
    Given a bug with title "Temp test bug" exists
    And I am on the BugTracker home page
    When I click the delete button for "Temp test bug"
    And I confirm the deletion
    Then I should not see "Temp test bug" in the bug list
