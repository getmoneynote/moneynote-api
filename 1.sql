-- 删除所有账单记录
DELETE FROM `t_user_category_relation`;
DELETE FROM `t_user_tag_relation`;
DELETE FROM `t_user_balance_flow`;


SELECT * FROM `t_user_balance_flow` WHERE `include` IS NULL;

