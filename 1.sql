-- 删除所有账单记录
DELETE FROM `t_user_category_relation`;
DELETE FROM `t_user_tag_relation`;
DELETE FROM `t_user_balance_flow`;


SELECT * FROM `t_user_balance_flow` WHERE `include` IS NULL;







select g1_0.id,g1_0.creator_id,
       g1_0.default_book_id,g1_0.default_currency_code,
       g1_0.enable,g1_0.name,g1_0.notes
from t_user_group g1_0 where exists(select 1 from t_user_user_group_relation r1_0 where r1_0.user_id=? and g1_0.id=r1_0.group_id) limit ?,?