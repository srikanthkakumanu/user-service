-- Extended profile owned by user-service, keyed by the identity provider's user ID.
-- Identity, account state and credentials live in the identity provider, not here.
create table user_profile (
    user_id      uuid          primary key,
    phone_number varchar(30),
    job_title    varchar(100),
    department   varchar(100),
    locale       varchar(35),
    time_zone    varchar(100),
    bio          varchar(1000),
    created_at   timestamptz   not null,
    updated_at   timestamptz   not null,
    version      bigint        not null default 0
);
