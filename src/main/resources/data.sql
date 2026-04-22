-- 관리자 계정 초기화 (비밀번호: admin1234!)
INSERT INTO users (email, password, name, role, status, created_at, updated_at)
SELECT 'admin@edumanager.com',
       '$2a$10$L4HOmkm/2RtCRZUPoHxBvOe1L2b84jMBuHLv45/PU7wHiNju7cpA.',
       '시스템관리자',
       'ADMIN',
       'APPROVED',
       NOW(),
       NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@edumanager.com');

-- 기본 과목 데이터
INSERT INTO subjects (name, category) SELECT '국어', '주요과목' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = '국어');
INSERT INTO subjects (name, category) SELECT '수학', '주요과목' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = '수학');
INSERT INTO subjects (name, category) SELECT '영어', '주요과목' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = '영어');
