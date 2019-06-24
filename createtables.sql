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

drop table if exists rclonewatchdog.status;
create table rclonewatchdog.status(
  commandid int not null,
  statustype int not null,
  description varchar,
  modifieddate timestamp default now()
);

drop table if exists rclonewatchdog.statustype;
create table rclonewatchdog.statustype(
  statustype int not null,
  name varchar(200) not null,
  description varchar
);

insert into rclonewatchdog.statustype (statustype, name) values (0, 'Execution started');
insert into rclonewatchdog.statustype (statustype, name) values (1, 'Execution failed');
insert into rclonewatchdog.statustype (statustype, name) values (2, 'Execution success');
insert into rclonewatchdog.statustype (statustype, name) values (3, 'Dry run execution started');
insert into rclonewatchdog.statustype (statustype, name) values (4, 'Dry run execution failed');
insert into rclonewatchdog.statustype (statustype, name) values (5, 'Dry run execution success');

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