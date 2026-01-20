-- user
create  table  if not exists user
(
    id bigint auto_increment  primary key ,
    userAccount varchar(256) not null ,
    userPassword varchar(512) not null,
    userName varchar(256)  null ,
    userAvatar varchar(1024) null ,
    userProfile varchar(512) null,
    userRole varchar(256) default 'user' not null,
    editTime datetime default CURRENT_TIMESTAMP not null,
    createTime datetime default CURRENT_TIMESTAMP not null,
    updateTime datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP not null,
    isDelete tinyint default 0 not null,
    UNIQUE KEY uk_userAccount(userAccount),
    INDEX idx_userName(userName)
)collate = utf8mb4_unicode_ci;

drop table if exists user