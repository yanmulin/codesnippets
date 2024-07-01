CREATE DATABASE IF NOT EXISTS test;
DROP TABLE IF EXISTS test.student;
CREATE TABLE test.student (
    `id` BIGINT(20) AUTO_INCREMENT,
    `name` VARCHAR(64),
    `age` TINYINT(2),
    PRIMARY KEY(`id`)
);
INSERT test.student (`id`, `name`, `age`) VALUES (1, 'John', 20), (2, 'Mary', 23), (3, 'Tom', 21);

DROP TABLE IF EXISTS test.sequence;
CREATE TABLE test.sequence (`value` BIGINT NOT NULL) ENGINE=MyISAM;
INSERT test.sequence VALUES (0);

DROP TABLE IF EXISTS test.blob_table;
CREATE TABLE test.blob_table (
    `id` BIGINT(20) AUTO_INCREMENT,
    `blob` BLOB,
    PRIMARY KEY(`id`)
);

DROP TABLE IF EXISTS test.tx_data;
CREATE TABLE test.tx_data (
    `id` INT,
    `data` INT
);
INSERT test.tx_data VALUES (1, 1), (2, 1), (3, 1);
