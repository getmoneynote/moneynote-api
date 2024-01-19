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
