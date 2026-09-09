-- V11: Corrigir o hash BCrypt do PIN de gerente padrão ('1234') para os tenants existentes
ALTER TABLE tenants
    ALTER COLUMN pin_gerente SET DEFAULT '$2a$12$SNRJGpipF03YqxFVydHSjeXUn0bMAb09v2yYQ1HQIuuJp8ruyWLy6';

UPDATE tenants
SET pin_gerente = '$2a$12$SNRJGpipF03YqxFVydHSjeXUn0bMAb09v2yYQ1HQIuuJp8ruyWLy6'
WHERE pin_gerente = '$2a$12$e8YwWb4HqU3jB07l90yFGeiQ85aG9Gv1u.h.b4L9hP9WkF1q2c3uG'
   OR pin_gerente NOT LIKE '$2a$%';
