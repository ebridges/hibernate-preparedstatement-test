USE scratch
;

IF EXISTS (SELECT * FROM sysobjects WHERE name='hibernate_test_table' and type='U')
    DROP TABLE hibernate_test_table
;

