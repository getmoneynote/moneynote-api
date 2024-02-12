-- 版本升级需要执行的sql

UPDATE `t_user_category` SET type = 100 where type = 0;
UPDATE `t_user_category` SET type = 200 where type = 1;

UPDATE `t_user_account` SET type = 100 where type = 0;
UPDATE `t_user_account` SET type = 200 where type = 1;
UPDATE `t_user_account` SET type = 300 where type = 2;
UPDATE `t_user_account` SET type = 400 where type = 3;

UPDATE `t_user_balance_flow` SET type = 100 where type = 0;
UPDATE `t_user_balance_flow` SET type = 200 where type = 1;
UPDATE `t_user_balance_flow` SET type = 300 where type = 2;
UPDATE `t_user_balance_flow` SET type = 400 where type = 3;

ALTER TABLE `t_user_account` CHANGE `balance` `balance` DECIMAL(20,2) NULL DEFAULT NULL;
ALTER TABLE `t_user_account` CHANGE `credit_limit` `credit_limit` DECIMAL(20,2) NULL DEFAULT NULL;
ALTER TABLE `t_user_account` CHANGE `initial_balance` `initial_balance` DECIMAL(20,2) NULL DEFAULT NULL;

ALTER TABLE `t_user_balance_flow` CHANGE `amount` `amount` DECIMAL(15,2) NULL DEFAULT NULL;
ALTER TABLE `t_user_balance_flow` CHANGE `converted_amount` `converted_amount` DECIMAL(15,2) NULL DEFAULT NULL;

ALTER TABLE `t_user_category_relation` CHANGE `amount` `amount` DECIMAL(15,2) NULL DEFAULT NULL;
ALTER TABLE `t_user_category_relation` CHANGE `converted_amount` `converted_amount` DECIMAL(15,2) NULL DEFAULT NULL;

ALTER TABLE `t_user_tag_relation` CHANGE `amount` `amount` DECIMAL(15,2) NULL DEFAULT NULL;
ALTER TABLE `t_user_tag_relation` CHANGE `converted_amount` `converted_amount` DECIMAL(15,2) NULL DEFAULT NULL;

