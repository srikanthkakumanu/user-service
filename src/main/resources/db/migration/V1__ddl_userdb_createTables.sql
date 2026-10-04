create table tbl_user_address (
    id uuid not null primary key,
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
);

create table tbl_user_profile (
    id uuid not null primary key,
    first_name varchar(255),
    last_name varchar(255),
    email varchar(255),
    mobile varchar(255),
    employee_id varchar(100),
    department varchar(100),
    manager varchar(255),
    designation varchar(100),
    perm_address_id uuid,
    current_address_id uuid,
    created timestamp,
    updated timestamp,
    constraint perm_address_fk foreign key (perm_address_id) references tbl_user_address(id),
    constraint current_address_fk foreign key (current_address_id) references tbl_user_address(id)
);

create table tbl_role (
    id uuid not null primary key,
    role varchar(20) not null unique,
    description varchar(100),
    created timestamp,
    updated timestamp
);

create table tbl_user (
    id uuid not null primary key,
    login_id varchar(255) not null unique,
    keycloak_user_id varchar(255) not null unique,
    profile_id uuid not null,
    user_status varchar(20),
    user_agent_type varchar(20),
    created timestamp,
    updated timestamp,
    constraint user_profile_fk foreign key (profile_id) references tbl_user_profile(id)
);

create index idx_tbl_user_keycloak_user_id on tbl_user(keycloak_user_id);
create index idx_tbl_user_login_id on tbl_user(login_id);
