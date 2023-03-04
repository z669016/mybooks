create table author
(
    id   varchar(48)  not null,
    name varchar(255) not null,
    primary key (id)
);

create table site
(
    author_id varchar(48)  not null,
    name      varchar(255) not null,
    url       varchar(255) not null,
    primary key (author_id, name),
    foreign key (author_id) references author (id) on delete cascade
);

create table book
(
    id_type     varchar(16)   not null,
    id          varchar(48)   not null,
    title       varchar(256)  not null,
    description varchar(1024) not null,
    primary key (id_type, id)
);

create table book_format
(
    book_id_type varchar(16) not null,
    book_id      varchar(48) not null,
    format       varchar(10) not null,
    primary key (book_id_type, book_id, format),
    foreign key (book_id_type, book_id) references book(id_type, id) on delete cascade
);

create table book_key_word
(
    book_id_type varchar(16) not null,
    book_id      varchar(48) not null,
    keyword      varchar(64) not null,
    primary key (book_id_type, book_id),
    foreign key (book_id_type, book_id) references book(id_type, id) on delete cascade
);

create table book_author
(
    book_id_type varchar(16) not null,
    book_id      varchar(48) not null,
    author_id    varchar(48) not null,
    primary key (book_id_type, book_id, author_id),
    foreign key (author_id) references author (id),
    foreign key (book_id_type, book_id) references book(id_type, id) on delete cascade
);


