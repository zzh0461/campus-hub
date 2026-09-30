-- ============================================================
-- 06-update-avatars.sql  存量库头像改为在线地址
-- 作用:把 campus_user 的头像统一改成 DiceBear avataaars 在线人像插画,
--       不再依赖任何本地文件(此前的 http://localhost:8084/uploads/*.png
--       因 uploads/ 目录下并无那 24 张 PNG,线上必然 404 裂图)。
--
-- 编号规则与种子数据保持一致:第 id 个用户 -> seed=avatar-((id-1)%24+1)
-- 24 个头像循环使用,与 campus_003..campus_007 等既有账号的对应关系不变。
--
-- 需要切回自托管时的做法:把 24 张 PNG 放进 market-service 工作目录的 uploads/
-- (由 UploadResourceConfig 映射为 /uploads/**),再把下面的 SET 换成
--   CONCAT('http://localhost:8084/uploads/avatar-', LPAD(((id-1)%24)+1, 2, '0'), '.png')
--
-- 执行一次即可,幂等可重复执行。
-- ============================================================
USE `campushub`;

UPDATE `campus_user`
SET `avatar` = CONCAT(
  'https://api.dicebear.com/8.x/avataaars/svg?seed=avatar-',
  LPAD(((`id` - 1) % 24) + 1, 2, '0')
);
