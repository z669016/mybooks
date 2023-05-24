Feature: load epub and extract meta data
  Scenario: Load data on authors and books from all epub files in a folder and sub folders
    Given an existing root folder "/Users/renevanputten/OneDrive/Documents/Books"
#    Given an existing root folder "/Users/renevanputten/OneDrive/Documents/Books/leanpub"
    When all books are loaded from the the root folder
    Then all authors list is not empty
    And all book list is not empty
    And loading stats