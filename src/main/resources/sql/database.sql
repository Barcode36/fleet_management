drop database if exists fleet_db;
create database fleet_db;
use fleet_db;

drop table if exists users;
create table users(
  user_id int not null primary key auto_increment,
  user_name varchar(64) not null,
  first_name varchar(64) not null,
  last_name varchar(64),
  category varchar(64) not null
);
insert into users values (1, 'admin', 'Admin', '', 'ADMIN');
alter table users auto_increment = 2;

drop table if exists fleet_owners;
create table fleet_owners(
  national_id varchar(64) not null primary key,
  first_name varchar(64) not null,
  middle_name varchar(64) null,
  last_name varchar(64) not null,
  occupation varchar(64),
  residence varchar(64),
  phone_number varchar(64)
);

drop table if exists vehicles;
create table vehicles(
  reg_number varchar(64) not null primary key,
  model varchar(64),
  inspection_date date,
  speed_gov_renewal_date date,
  license_renewal_date date,
  reg_date date not null,
  category varchar(64) not null,
  type varchar(16) not null,
  make varchar(16) not null,
  num_seats int not null,
  cost double not null,
  bought_on_loan boolean not null,
  servicing_date date,
  owner_id varchar(64) not null,
  foreign key (owner_id) references fleet_owners(national_id)
  on update cascade
  on delete cascade
);

drop table if exists insurance_companies;
create table insurance_companies(
  name varchar(64) not null primary key
);

drop table if exists vehicle_brands;
create table vehicle_brands(
  name varchar(64) not null primary key
);



drop table if exists user_permissions;
create table user_permissions(
  user_id int not null,
  permission varchar(64) not null,
  allowed boolean not null,
  primary key (user_id, permission)
);

drop table if exists activity_log;
create table activity_log(
  user_id int not null,
  message varchar(1028),
  date date not null,
  time time not null
);

drop table if exists vehicle_insurance;
create table vehicle_insurance(
  reg_number varchar(64) not null primary key ,
  company varchar(64) not null,
  policy_type varchar(64) not null,
  policy_number varchar(64) not null,
  premium_amount double,
  start_date date not null,
  expiry_date date not null,
  foreign key (reg_number) references vehicles(reg_number),
  foreign key (company) references insurance_companies(name)
  on update cascade
  on delete cascade
);

drop table if exists documents;
create table documents(
  id int not null auto_increment primary key,
  owner_id varchar(64) not null,
  #   owner id could be a vehicle reg number,employee's id number or fleet owner id number
  file mediumblob,
  width int not null,
  height int not null,
  name varchar(128) not null
);

drop table if exists loans;
create table loans(
  reg_number varchar(64) not null primary key ,
  interest_type varchar(64) not null,
  rate double not null,
  num_years int not null,
  principal double not null,
  start_date date not null,
  foreign key (reg_number) references vehicles(reg_number)
  on update cascade
  on delete cascade
);

drop table if exists employees;
create table employees(
  category varchar(16) not null,
  national_id varchar(64) not null,
  first_name varchar(64) not null,
  last_name varchar(64) not null,
  license_number varchar(64) not null,
  license_issue_date date not null,
  license_expiry_date date not null,
  residence varchar(128) null,
  phone_number varchar(64) null,
  primary key (national_id)
);

drop table if exists ledger_entry;
create table ledger_entry(
  entry_id int not null primary key auto_increment,
  reg_number varchar(64) not null,
  income double not null,
  expense double not null,
  category varchar(64) not null,
  driver_id varchar(64) not null,
  conductor_id varchar(64) not null,
  date date not null,
  details text null,
  foreign key (conductor_id) references employees(national_id),
  foreign key (reg_number) references vehicles(reg_number)
  on update cascade
  on delete no action
);

drop table if exists vehicle_assignments;
create table vehicle_assignments(
  reg_number varchar(64) not null,
  driver_id varchar(64) not null,
  conductor_id varchar(64) not null,
  date date not null,
  primary key (reg_number, date),
  foreign key (reg_number)references vehicles(reg_number),
  foreign key (driver_id) references employees(national_id),
  foreign key (conductor_id) references employees(national_id)
  on update cascade
  on delete  cascade
);

drop table if exists accidents;
create table accidents(
  accident_id int not null primary key auto_increment,
  driver_id varchar(64) not null,
  reg_number varchar(64) not null,
  description text,
  date_reported_insurance date,
  date date not null,
  time time not null,
  place varchar(128) not null,
  station_reported varchar(64) not null,
  foreign key (driver_id) references employees(national_id),
  foreign key (reg_number) references vehicles(reg_number)
  on update cascade
  on delete no action
);

# hire
drop table if exists vehicle_hire;
create table vehicle_hire(
  reg_number varchar(64) not null,
  hiree varchar(64) not null,
  driver_id varchar(64) not null,
  start_date date not null,
  end_date date not null,
  foreign key (driver_id) references employees(national_id),
  foreign key (reg_number) references vehicles(reg_number)
  on update cascade
  on delete cascade
);

# scheduled services
drop table if exists scheduled_services;
create table scheduled_services(
  reg_number varchar(64) not null,
  speed_gov_renewal date,
  inspection date,
  ntsa_license_renewal date,
  servicing date,
  insurance date,
  primary key (reg_number),
  foreign key (reg_number) references vehicles(reg_number)
  on update cascade
  on delete cascade
);


# settings
drop table if exists settings;
create table settings(
  setting varchar(32) not null primary key,
  duration int not null,
  time_unit varchar(16) not null
);
# triggers
drop trigger if exists update_vehicle;
delimiter //
create trigger update_vehicle
  after update on vehicles
  for each row
  if (New.bought_on_loan = 0) then
  delete from loans where loans.reg_number = NEW.reg_number;
  end if;

//
delimiter ;



