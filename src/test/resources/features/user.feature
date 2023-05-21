Feature: user login and creation

  Scenario: retrieve details on all known authors
    When  send a login request for user "z669016@gmail.com" with password "1password!"
    Then the client receives status code of 200
    And response contains a token
    And response authorization header contains bearer token
    And response cookie jwt is set with token

