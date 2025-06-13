--liquibase formatted sql

--changeset andryss:create-key_storage-table
create table key_storage (
    key text primary key,
    value text not null
);

comment on table key_storage is 'Хранилище пар ключ-значение';

comment on column key_storage.key is 'Ключ';
comment on column key_storage.value is 'Значение';
