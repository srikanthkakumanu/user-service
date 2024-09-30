drop table if exists tbl_user;
drop table if exists tbl_user_profile;
drop table if exists tbl_user_address;
drop table if exists tbl_role;

create table tbl_user_address (
    id varbinary(16) not null primary key,
    type varchar(20),
    address1 varchar(255),
    address2 varchar(255),
    landmark varchar(255),
    area varchar(255),
    city varchar(255),
    zip_code varchar(255),
    state varchar(255),
    country varchar(255),
    created timestamp,
    updated timestamp
) engine=InnoDB;

create table tbl_user_profile (
    id varbinary(16) not null primary key,
    first_name varchar(255),
    last_name varchar(255),
    email varchar(255),
    mobile varchar(255),
    perm_address_id varbinary(16),
    current_address_id varbinary(16),
    created timestamp,
    updated timestamp,
    constraint perm_address_fk foreign key (perm_address_id) references tbl_user_address(id),
    constraint current_address_fk foreign key (current_address_id) references tbl_user_address(id)
) engine=InnoDB;

create table tbl_role (
    id varbinary(16) not null primary key,
    role varchar(20) not null unique,
    description varchar(100),
    created timestamp,
    updated timestamp
) engine=InnoDB;

create table tbl_user (
    id varbinary(16) not null primary key,
    login_id varchar(255) unique,
    password varchar(255),
    profile_id varbinary(16) not null,
    user_status varchar(10),
    user_agent_type varchar(10),
    created timestamp,
    updated timestamp,
    constraint user_profile_fk foreign key (profile_id) references tbl_user_profile(id)
) engine=InnoDB;



