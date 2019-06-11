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
  filter int null
);

drop table if exists rclonewatchdog.filter;
create table rclonewatchdog.filter(
  id serial not null,
  filter varchar not null
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


CREATE VIEW rclonewatchdog.commands AS
select
c.id commandid,
c.command command,
s.directory source,
d.remote destinationremote,
d.directory destination,
c.filters filters
from rclonewatchdog.command c
inner join rclonewatchdog.source s on c.source = s.id
inner join rclonewatchdog.destination d on c.destination = d.id
order by c.id;