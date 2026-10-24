select * from "produto";

-- ADM Geral, cadastro inicial (senha admin123)
--INSERT INTO "adm_geral_sistema" ("adm_geral_sistema_id", "adm_geral_sistema_status", "adm_geral_sistema_nome", "adm_geral_sistema_usuario", "adm_geral_sistema_senha") 
--VALUES (gen_random_uuid(), 'ATIVO', 'Administrador Master', 'admin', '$2a$10$K9o/M1TfIL1KPXnhBmEOM.POP6HTxFKocCKATr1V6J7angUgdiG0e');

select * from "adm_geral_sistema";
select * from "adm_unidade";
select * from "unidade";
select * from "produto";
select * from "imagem_produto";