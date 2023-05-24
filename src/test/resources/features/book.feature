Feature: book retrieval and registration

  Scenario: Create a new book
    Given a successful user login
    When a temp book with title "new book" was created
    Then the client receives status code of 201
    And book has title "new book"

  Scenario: retrieve details on all known books
    Given a successful user login
    Given a temp book with title "getBooks-1" was created
    Given a temp book with title "getBooks-2" was created
    When  send a get request for books
    Then the client receives status code of 200
    And response contains details on more than 1 book
    And books contain book with title "getBooks-1"
    And books contain book with title "getBooks-2"

  Scenario: retrieve details on all books for an author
    Given a successful user login
    Given a temp book with title "getBookByAuthor-1" was created
    Given a temp book with title "getBookByAuthor-2" was created
    Given a temp book with title "getBookByAutho-3 should not be found" was created
    When  send a get request for books from author with name "getBookByAuthor"
    Then the client receives status code of 200
    And response contains details on more than 1 book
    And books contain book with title "getBookByAuthor-1"
    And books contain book with title "getBookByAuthor-2"

  Scenario: retrieve details on all books with a certain title
    Given a successful user login
    Given a temp book with title "getBookByTitle-1" was created
    Given a temp book with title "getBookByTitle-2" was created
    Given a temp book with title "getBookByTitl-3 should not be found" was created
    When  send a get request for books with title "getBookByTitle"
    Then the client receives status code of 200
    And response contains details on more than 1 book
    And books contain book with title "getBookByTitle-1"
    And books contain book with title "getBookByTitle-2"

  Scenario: retrieve details on one specific book
    Given a successful user login
    Given a temp book with title "getBookById" was created
    When  send a get request for temp book with id
    Then the client receives status code of 200
    And book has title "getBookById"

  Scenario: retrieve details on one specific book
    Given a successful user login
    When  send a get request for book with schema uuid and id bla
    Then the client receives status code of 400
    And errors contains parameters

