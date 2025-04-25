UPDATE collateral
SET name = ''
WHERE name = 'collateral description';
UPDATE collateral
SET name = link
WHERE link IS NOT NULL;
UPDATE collateral
SET link = NULL
WHERE link IS NOT NULL;