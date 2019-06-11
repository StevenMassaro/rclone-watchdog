alter table rclonewatchdog.command add column filter int null;
alter table rclonewatchdog.command drop column filters cascade;