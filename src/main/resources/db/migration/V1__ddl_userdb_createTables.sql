drop table if exists user;


create table users (
    id varbinary(16) not null,
    first_name varchar(255),
    last_name varchar(255),
    password varchar(255),
    email varchar(255),
    mobile varchar(255),
    created timestamp,
    updated timestamp,
    primary key (id)
) engine=InnoDB;
