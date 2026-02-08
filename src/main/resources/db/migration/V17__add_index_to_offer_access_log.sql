SELECT version, description, checksum
FROM flyway_schema_history
ORDER BY installed_rank DESC;
