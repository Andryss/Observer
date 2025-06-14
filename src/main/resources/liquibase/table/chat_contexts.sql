--liquibase formatted sql

--changeset andryss:create-chat_contexts-table
create table chat_contexts (
    chat_id text primary key,
    messages jsonb not null,
    updated_at timestamp not null,
    created_at timestamp not null
);

comment on table chat_contexts is 'Контекст сообщений для чата';

comment on column chat_contexts.chat_id is 'Идентификатор чата';
comment on column chat_contexts.messages is 'Контекст последних сообщений';
comment on column chat_contexts.updated_at is 'Время последнего обновления записи';
comment on column chat_contexts.created_at is 'Время создания записи';
