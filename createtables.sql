create database rclonewatchdog;

create schema rclonewatchdog;

drop table if exists rclonewatchdog.profile;
create table rclonewatchdog.profile(
  id int not null,
  name varchar(200) not null,
  commandId int not null
);

drop table if exists rclonewatchdog.command;
create table rclonewatchdog.command(
  id serial not null,
  name varchar(200) not null,
  command varchar not null,
  source int not null,
  destination int not null,
  filters varchar null
);

drop table if exists rclonewatchdog.source;
create table rclonewatchdog.source(
  id serial not null,
  name varchar(200) not null,
  directory varchar not null
);

drop table if exists rclonewatchdog.destination;
create table rclonewatchdog.destination(
  id serial not null,
  name varchar(200) not null,
  remote varchar not null,
  directory varchar not null
);