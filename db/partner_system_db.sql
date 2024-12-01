/**
 * 数据库表的初始化
 * @author: 袁德光
 * @date: 2024-10-30
 */
-- 创建数据库
create database if not exists partner_system_db
    character set utf8mb4
    collate utf8mb4_unicode_ci;

-- 使用库
use partner_system_db;

-- 用户表 user
create table if not exists user
(
    id            bigint unsigned primary key comment 'id',
    user_account  varchar(16)                          not null comment '账号',
    user_password varchar(256)                         not null comment '密码',
    user_name     varchar(256)                         null comment '昵称',
    user_gender   tinyint    default 2                 null comment '0-女，1-男，2-未知',
    user_age      tinyint    default 22                not null comment '年龄',
    user_email    varchar(255)                         null comment '邮箱',
    user_phone    char(11)                             null comment '手机号',
    user_avatar   varchar(1024)                        null comment '头像',
    user_profile  varchar(512)                         null comment '简介',
    user_role     varchar(5) default 'user'            not null comment '用户角色：user/admin/ban',
    friend_list   varchar(1024)                        null comment '好友列表',
    tags          varchar(1024)                        null comment '标签',
    create_by     bigint unsigned                      not null comment '创建者id',
    update_by     bigint unsigned                      not null comment '更新者id',
    create_time   datetime   default current_timestamp not null comment '创建时间',
    update_time   datetime   default current_timestamp not null on update current_timestamp comment '更新时间',
    is_delete     tinyint    default 0                 not null comment '逻辑删除，0:默认，1:删除',
    unique index uidx_user_account (user_account) comment '用户账号唯一索引',
    unique index uidex_user_email (user_email) comment '用户邮箱唯一索引'
) comment '用户表' engine = innodb
                   character set utf8mb4
                   collate utf8mb4_unicode_ci;

-- 队伍表 team
create table if not exists team
(
    id          bigint unsigned primary key comment 'id',
    name        varchar(256)                       not null comment '名称',
    description varchar(1024)                      null comment '描述',
    cover_image varchar(255)                       null comment '封面图片',
    max_num     int      default 1                 not null comment '最大人数',
    current_num int      default 1                 not null comment '当前人数',
    expire_time datetime                           null comment '过期时间',
    create_by   bigint unsigned                    not null comment '创建者id',
    update_by   bigint unsigned                    not null comment '更新者id',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '逻辑删除，0:默认，1:删除'
) comment '队伍表' engine = innodb
                   character set utf8mb4
                   collate utf8mb4_unicode_ci;

-- 队伍成员表 user_team
create table if not exists user_team
(
    id          bigint unsigned primary key comment 'id',
    create_by   bigint unsigned                    not null comment '创建者id',
    update_by   bigint unsigned                    not null comment '更新者id',
    team_id     bigint unsigned                    not null comment '队伍id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    is_delete   tinyint  default 0                 not null comment '逻辑删除，0:默认，1:删除'
) comment '用户队伍表' engine = innodb
                       character set utf8mb4
                       collate utf8mb4_unicode_ci;

-- 文章帖子表
create table if not exists post
(
    id          bigint unsigned primary key comment 'id',
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容',
    cover_image varchar(255)                       null comment '封面图片',
    status      tinyint  default 0                 not null comment '帖子状态（0：审核中，1：审核通过，2：审核未通过）',
    praise_num  int      default 0                 not null comment '点赞数',
    collect_num int      default 0                 not null comment '收藏数',
    comment_num int      default 0                 not null comment '评论数',
    create_by   bigint unsigned                    not null comment '创建者id',
    update_by   bigint unsigned                    not null comment '更新者id',
    create_time datetime default current_timestamp not null comment '创建时间',
    update_time datetime default current_timestamp not null on update current_timestamp comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '逻辑删除，0:默认，1:删除',
    index idx_create_by (create_by)
) comment '文章帖子表' engine = innodb
                       character set utf8mb4
                       collate utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_praise
(
    id          bigint unsigned primary key comment 'id',
    post_id     bigint unsigned                    not null comment '帖子 id',
    create_by   bigint unsigned                    not null comment '创建者id',
    update_by   bigint unsigned                    not null comment '更新者id',
    create_time datetime default current_timestamp not null comment '创建时间',
    update_time datetime default current_timestamp not null on update current_timestamp comment '更新时间',
    index idx_post_id (post_id),
    index idx_create_by (create_by)
) comment '帖子点赞表' engine = innodb
                       character set utf8mb4
                       collate utf8mb4_unicode_ci;

-- 帖子收藏表（硬删除）
create table if not exists post_collect
(
    id          bigint unsigned primary key comment 'id',
    post_id     bigint unsigned                    not null comment '帖子的id',
    create_by   bigint unsigned                    not null comment '创建者id',
    update_by   bigint unsigned                    not null comment '更新者id',
    create_time datetime default current_timestamp not null comment '创建时间',
    update_time datetime default current_timestamp not null on update current_timestamp comment '更新时间',
    index idx_post_id (post_id),
    index idx_create_by (create_by)
) comment '帖子收藏表' engine = innodb
                       character set utf8mb4
                       collate utf8mb4_unicode_ci;

-- 帖子评论表
create table if not exists post_comment
(
    id          bigint unsigned primary key comment 'id',
    create_by   bigint unsigned                               not null comment '创建者id',
    update_by   bigint unsigned                               not null comment '更新者id',
    post_id     bigint unsigned                               not null comment '帖子id',
    parent_id   bigint unsigned                               null comment '关联的1级评论id，如果是一级评论，则值为null',
    answer_id   bigint unsigned                               null comment '回复的评论id',
    to_user_id  bigint unsigned                               null comment '被回复的用户id',
    content     varchar(1024)                                 not null comment '回复的内容',
    praise_num  int(8) unsigned     default 0                 null comment '点赞数',
    status      tinyint(1) unsigned default 0                 not null comment '状态，0：正常，1：被举报，2：禁止查看',
    create_time timestamp           default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp           default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint             default 0                 null comment '逻辑删除，0:默认，1:删除'
) comment '帖子评论表' engine = innodb
                       character set utf8mb4
                       collate utf8mb4_unicode_ci;

-- 帖子评论点赞表（硬删除）
create table if not exists post_comment_praise
(
    id          bigint unsigned primary key comment 'id',
    post_id     bigint unsigned                    not null comment '帖子id',
    create_by   bigint unsigned                    not null comment '创建者id',
    update_by   bigint unsigned                    not null comment '更新者id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_post_id (post_id),
    index idx_create_by (create_by)
) comment '帖子评论点赞表' engine = innodb
                           character set utf8mb4
                           collate utf8mb4_unicode_ci;

-- 聊天表
create table if not exists chat
(
    id          bigint unsigned primary key comment 'id',
    create_by   bigint unsigned                         not null comment '发送消息id',
    update_by   bigint unsigned                         not null comment '更新者id',
    to_id       bigint                                  null comment '接收消息id',
    text        varchar(512) collate utf8mb4_unicode_ci not null comment '消息内容',
    chat_type   tinyint                                 not null comment '聊天类型 1-私聊 2-队伍群聊 3-大厅聊天',
    is_read     tinyint  default 0                      null comment '是否已读 1-已读 2-未读',
    team_id     bigint                                  null comment '群聊id',
    create_time datetime default CURRENT_TIMESTAMP      null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP      null comment '更新时间',
    is_delete   tinyint  default 0                      null comment '逻辑删除，0:默认，1:删除'
) comment '聊天表' engine = innodb
                   character set utf8mb4
                   collate utf8mb4_unicode_ci;

-- 关注表
create table if not exists follow
(
    id             bigint unsigned primary key comment 'id',
    create_by      bigint unsigned                     not null comment '创建者id',
    update_by      bigint unsigned                     not null comment '更新者id',
    follow_user_id bigint                              not null comment '关注的用户id',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete      tinyint   default 0                 not null comment '逻辑删除，0:默认，1:删除'
) comment '关注表' engine = innodb
                   character set utf8mb4
                   collate utf8mb4_unicode_ci;

-- 好友表
create table if not exists friend
(
    id          bigint unsigned primary key comment '好友申请id',
    create_by   bigint unsigned                    not null comment '发送申请的用户id',
    update_by   bigint unsigned                    not null comment '更新者id',
    receive_id  bigint unsigned                    null comment '接收申请的用户id ',
    is_read     tinyint  default 0                 not null comment '是否已读(0-未读 1-已读)',
    status      tinyint  default 0                 not null comment '申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    remark      varchar(225)                       null comment '好友申请备注信息',
    is_delete   tinyint  default 0                 not null comment '逻辑删除，0:默认，1:删除'
) comment '好友表' engine = innodb
                   character set utf8mb4
                   collate utf8mb4_unicode_ci;

-- 消息表
create table if not exists message
(
    id          bigint unsigned primary key comment '主键',
    type        tinyint                            not null comment '1：帖子点赞，2：帖子评论，3：私聊，4：队伍群聊，5：官方公共群聊',
    create_by   bigint unsigned                    not null comment '消息发送的用户id',
    update_by   bigint unsigned                    not null comment '更新者',
    to_id       bigint unsigned                    null comment '消息接收的用户id',
    content     varchar(255)                       not null comment '消息内容',
    is_read     tinyint  default 0                 not null comment '已读-0 未读 ,1 已读',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '逻辑删除，0:默认，1:删除'
) comment '消息表' engine = innodb
                   character set utf8mb4
                   collate utf8mb4_unicode_ci;

-- 首页内容展示表
create table if not exists home_page_content_display
(
    id          bigint unsigned primary key comment 'id',
    content     varchar(255)                        not null comment '内容',
    type        tinyint                             not null comment '0-通知栏 1-轮播图',
    create_by   bigint unsigned                     not null comment '创建者',
    update_by   bigint unsigned                     not null comment '更新者',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint   default 0                 not null comment '逻辑删除，0:默认，1:删除'
) comment '首页内容展示表' engine = innodb
                           character set utf8mb4
                           collate utf8mb4_unicode_ci;

-- 标签表
create table if not exists tag
(
    id          bigint unsigned                                        not null primary key comment 'id',
    tag_name    varchar(512) character set utf8mb4 collate utf8mb4_bin not null comment '标签名称',
    category    varchar(512) CHARACTER SET utf8mb4 collate utf8mb4_bin null null default '默认' comment '分类',
    create_by   bigint unsigned                                        not null comment '创建者id',
    update_by   bigint unsigned                                        not null comment '更新者id',
    create_time datetime                                               not null  default current_timestamp comment '创建时间',
    update_time datetime                                               not null  default current_timestamp on update current_timestamp comment '更新时间',
    is_delete   tinyint                                                not null  default 0 comment '逻辑删除，0:默认，1:删除'
) comment '标签表' engine = innodb
                   character set utf8mb4
                   collate utf8mb4_unicode_ci;

-- 举报反馈表
CREATE TABLE if not exists report
(
    id          bigint unsigned                                not null primary key comment 'id',
    content     text character set utf8mb4 collate utf8mb4_bin not null comment '内容',
    report_id   bigint unsigned                                not null comment '被举报对象id',
    type        tinyint                                        not null comment '0：用户，1：帖子文章，2：帖子评论，3：聊天，4：队伍',
    status      int                                            not null default 0 comment '状态（0-未处理, 1-已处理）',
    create_by   bigint unsigned                                not null comment '创建者id',
    update_by   bigint unsigned                                not null comment '更新者id',
    create_time datetime                                       not null default current_timestamp comment '创建时间',
    update_time datetime                                       not null default current_timestamp ON UPDATE current_timestamp comment '更新时间',
    is_delete   tinyint                                        not null default 0 comment '逻辑删除，0:默认，1:删除'
) comment '举报反馈表' engine = innodb
                       character set utf8mb4
                       collate utf8mb4_unicode_ci;

-- 图片表
CREATE TABLE if not exists img
(
    id          bigint unsigned                                        not null COMMENT 'id',
    path        varchar(225) character set utf8mb4 collate utf8mb4_bin not null comment '图片路径',
    type        tinyint                                                not null default 0 comment '图片类型（0-轮播图，1-用户头像，2-帖子文章图片，3-队伍图标）',
    create_by   bigint unsigned                                        not null comment '创建者id',
    update_by   bigint unsigned                                        not null comment '更新者id',
    create_time datetime                                               not null default current_timestamp comment '创建时间',
    update_time datetime                                               not null default current_timestamp ON UPDATE current_timestamp comment '更新时间',
    is_delete   tinyint                                                not null default 0 comment '逻辑删除，0:默认，1:删除'
) comment '图片表' engine = innodb
                   character set utf8mb4
                   collate utf8mb4_unicode_ci;