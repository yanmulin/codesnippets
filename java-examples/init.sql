CREATE TABLE IF NOT EXISTS `jdbc_test`.`student` (
    `id` BIGINT(20) AUTO_INCREMENT,
    `name` VARCHAR(64),
    `age` TINYINT(2),
    PRIMARY KEY(`id`)
);
CREATE TABLE IF NOT EXISTS `jdbc_test`.`teacher` (
    `id` BIGINT(20) AUTO_INCREMENT,
    `name` VARCHAR(64),
    `school` VARCHAR(64),
    PRIMARY KEY(`id`)
);