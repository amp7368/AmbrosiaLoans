SELECT table_name,
       PG_SIZE_PRETTY(table_size)   AS table_size,
       PG_SIZE_PRETTY(indexes_size) AS indexes_size,
       PG_SIZE_PRETTY(total_size)   AS total_size
FROM (
     SELECT table_name,
            PG_TABLE_SIZE(table_name)          AS table_size,
            PG_INDEXES_SIZE(table_name)        AS indexes_size,
            PG_TOTAL_RELATION_SIZE(table_name) AS total_size
     FROM (
          SELECT ('"' || table_schema || '"."' || table_name || '"') AS table_name
          FROM information_schema.tables) AS all_tables
     ORDER BY total_size DESC) AS pretty_sizes;
SELECT PG_SIZE_PRETTY(SUM(table_size))   AS table_size,
       PG_SIZE_PRETTY(SUM(indexes_size)) AS indexes_size,
       PG_SIZE_PRETTY(SUM(total_size))   AS total_size
FROM (
     SELECT table_name,
            PG_TABLE_SIZE(table_name)          AS table_size,
            PG_INDEXES_SIZE(table_name)        AS indexes_size,
            PG_TOTAL_RELATION_SIZE(table_name) AS total_size
     FROM (
          SELECT ('"' || table_schema || '"."' || table_name || '"') AS table_name
          FROM information_schema.tables) AS all_tables
     ORDER BY total_size DESC) AS pretty_sizes;



SELECT schemaname, relname, n_live_tup
FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;

SELECT SUM(n_live_tup)
FROM pg_stat_user_tables;



SELECT ('"' || table_schema || '"."' || table_name || '"') AS table_name
FROM information_schema.tables
