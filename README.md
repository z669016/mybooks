# mybooks - Hexagonal/clean architecture experiment

Entities:
```mermaid
classDiagram
    Author "1" -- "1" AuthorId: has
    Author "1" *-- "0..*" Site: has
    Site "1" -- "1" SiteId: has
    Site "1" -- "1" SiteType: has
    Book "1" *-- "1..*" Author: has
    Book "1" -- "1" BookId: has
    Book "1" *-- "1..*" FormatType: has
```

Services:
```mermaid
classDiagram
    BookInquiryService --|> AuthorById: implements
    BookInquiryService --|> AuthorsByName: implements
    BookInquiryService --|> Authors: implements
    BookService --|> RegisterAuthor: implements
    BookService --|> BookInquiryService: extends
    
    RegisterAuthor -- RegisterAuthorCommand: uses

    BookInquiryService "1" -- "1" BookReadOnlyRepository: has
    BookService "1" -- "1" BookRepository: has
```
