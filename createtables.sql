create database rclonewatchdog;

create schema rclonewatchdog;

drop table if exists rclonewatchdog.profile;
create table rclonewatchdog.profile(
  id int not null,
  name varchar(200) not null,
  commandId int not null
)

drop table if exists rclonewatchdog.command;
create table rclonewatchdog.command(
  id serial not null,
  name varchar(200) not null,
  command varchar not null
)