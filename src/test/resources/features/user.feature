Feature: User login and creation

  Scenario: Successful login
    When  send a login request for user "z669016@gmail.com" with password "1password!"
    Then the client receives status code of 200
    And response contains a token
    And response authorization header contains bearer token
    And response cookie jwt is set with token

  Scenario: Login without userid or password
    When  send a login request for user "null" with password "null"
    Then the client receives status code of 400
    And errors contains id
    And errors contains password

  Scenario: Login with unknown userid
    When  send a login request for user "bla" with password "bla"
    Then the client receives status code of 400
    And errors contains id
    And errors contains password

  Scenario: Create successful new user
    Given a successful admin login
    When  send a new user request for user with id "z669016@outlook.com", name "Me", password "3password@" and role "user"
    Then the client receives status code of 200
    And  user has id "z669016@outlook.com"
    And  user has name "Me"
    And  user has role "USER"

  Scenario: Create invalid new user
    Given a successful admin login
    When  send a new user request for user with id "null", name "null", password "null" and role "null"
    Then the client receives status code of 400
    And  errors contains id
    And  errors contains name
    And  errors contains password
    And  errors contains accessRole
