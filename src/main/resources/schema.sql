create table author
(
    id varchar(48) not null,
    name varchar(255) not null,
    primary key(id)
);

create table site
(
    id varchar(48) not null,
    author_id varchar(48) not null,
    name varchar(255) not null,
    url varchar(255) not null,
    primary key(id),
    foreign key(author_id) references author(id)
);