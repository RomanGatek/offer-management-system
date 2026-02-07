-- Reset admin password to BCrypt("admin")
UPDATE users
SET password = '$2a$12$zfJBYvZwnLTfXwecMujo8ORzOFy6JZhpm7QaEcaBco0GzZ0dUSns2'
WHERE username = 'admin';