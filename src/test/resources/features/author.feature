Feature: author retrieval and update

  Scenario: retrieve details on all known authors
    Given a successful user login
    When  send a get request for authors
    Then the client receives status code of 200
    And response contains details on more than 0 authors

  Scenario: retrieve details on an Author by id
    Given a successful user login
    When  send a get request for author with id f6791adc-6a03-4d9b-ad30-ef7fd0545c58
    Then the client receives status code of 200
    And author has id f6791adc-6a03-4d9b-ad30-ef7fd0545c58
    And author has name "Brown, Simon"

  Scenario: retrieve details on an Author with invalid id
    Given a successful user login
    When  send a get request for author with id aaa
    Then the client receives status code of 400

  Scenario: Create a new author with 2 sites
    Given a successful user login
    When sent a post request for a new author with name "name" and sites
      | Home page | https://www.google.com |
      | News      | https://www.nos.com    |
    Then the client receives status code of 201
    And author has name "name"
    And author has sites
      | Home page | https://www.google.com |
      | News      | https://www.nos.com    |

  Scenario: Create a new author with invalid name and invalid site
    Given a successful user login
    When sent a post request for a new author with name " " and sites
      | " "  | bla  |
    Then the client receives status code of 400
    And errors contains name
    And errors contains sites

  Scenario: Change author name
    Given a successful user login
    Given a created temp author
    When sent a put request for temp author with new name "new name"
    Then the client receives status code of 200
    And author has name "new name"

  Scenario: Change author name
    Given a successful user login
    Given a created temp author
    When sent a put request for temp author with new name "  "
    Then the client receives status code of 400
    And errors contains name
