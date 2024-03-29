create database rclonewatchdog;

create schema rclonewatchdog;

drop table if exists rclonewatchdog.profile;
create table rclonewatchdog.profile(
  id int not null,
  name varchar(200) not null,
  commandId int not null,
  primary key(id, name, commandId)
);

drop table if exists rclonewatchdog.source;
create table rclonewatchdog.source(
    id serial not null unique,
    name varchar(200) not null,
    directory varchar not null,
    primary key (name, directory)
);

drop table if exists rclonewatchdog.destination;
create table rclonewatchdog.destination(
    id serial not null unique,
    name varchar(200) not null,
    remote varchar not null,
    directory varchar not null,
    primary key(name, remote, directory)
);

drop table if exists rclonewatchdog.filter;
create table rclonewatchdog.filter(
    id serial not null unique,
    filter varchar primary key
);

drop table if exists rclonewatchdog.command;
create table rclonewatchdog.command(
  id serial not null,
  name varchar(200) not null,
  command varchar not null,
  source int not null references rclonewatchdog.source(id),
  destination int not null references rclonewatchdog.destination(id),
  filter int null references rclonewatchdog.filter(id),
  hidden boolean default false,
  schedule varchar(200),
  primary key(name, command, source, destination, filter)
);

drop table if exists rclonewatchdog.statustype;
create table rclonewatchdog.statustype(
    statustype int not null unique,
    name varchar(200) not null,
    description varchar,
    primary key (statustype, name)
);

drop table if exists rclonewatchdog.status;
create table rclonewatchdog.status(
  id serial primary key,
  commandid int not null,
  statustype int not null references rclonewatchdog.statustype(statustype),
  description varchar,
  modifieddate timestamp default now()
);

insert into rclonewatchdog.statustype (statustype, name) values (0, 'EXECUTION_START');
insert into rclonewatchdog.statustype (statustype, name) values (1, 'EXECUTION_FAIL');
insert into rclonewatchdog.statustype (statustype, name) values (2, 'EXECUTION_SUCCESS');
insert into rclonewatchdog.statustype (statustype, name) values (3, 'DRY_RUN_EXECUTION_START');
insert into rclonewatchdog.statustype (statustype, name) values (4, 'DRY_RUN_EXECUTION_FAIL');
insert into rclonewatchdog.statustype (statustype, name) values (5, 'DRY_RUN_EXECUTION_SUCCESS');

CREATE VIEW rclonewatchdog.commands AS
select
c.id commandid,
c.command command,
s.directory source,
d.remote destinationremote,
d.directory destination,
c.filter filters
from rclonewatchdog.command c
inner join rclonewatchdog.source s on c.source = s.id
inner join rclonewatchdog.destination d on c.destination = d.id
order by c.id;

ALTER TABLE rclonewatchdog.command ADD healthchecksurl varchar(56) NULL;