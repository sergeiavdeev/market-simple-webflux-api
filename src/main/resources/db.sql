create table product
(
    id    uuid           default uuid_generate_v4() not null
        primary key,
    title varchar(150),
    price numeric(10, 2) default 0
);

comment on table product is 'Products';

create index product_title_index
    on product (title);

create table usr
(
    id         uuid default uuid_generate_v4() not null
        constraint usr_pk
            primary key,
    username   varchar(100),
    password   varchar(255),
    email      varchar(50),
    id_ext     varchar(150),
    first_name varchar(100),
    last_name  varchar(100),
    patronymic varchar(100),
    dob        date
);

comment on table usr is 'Users';

create table role
(
    id   uuid default uuid_generate_v4() not null
        constraint role_pk
            primary key,
    name varchar(20)                     not null
);

create table user_role
(
    id      uuid default uuid_generate_v4() not null
        constraint user_role_pk
            primary key,
    user_id uuid                            not null,
    role_id uuid                            not null
);

create index user_role_user_id_index
    on user_role (user_id);

create table file
(
    id        uuid default uuid_generate_v4() not null,
    owner_id  uuid,
    name      varchar(100),
    order_num integer,
    descr     varchar(200)
);

create index file_owner_id_index
    on file (owner_id);

