USE scratch
;

IF EXISTS (SELECT * FROM sysobjects WHERE name='hibernate_test_table' and type='U')
    DROP TABLE hibernate_test_table
;

create table scratch..hibernate_test_table (
    id int primary key,
    name varchar(32) unique,
    last_modified datetime not null,
    simple_value int not null,
    big_text varchar(1024) not null,
    amount numeric(10,10) not null
)
;




