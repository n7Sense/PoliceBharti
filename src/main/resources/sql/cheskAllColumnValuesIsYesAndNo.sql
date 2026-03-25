SELECT application_no, 'non_cremelayer' AS column_name, non_cremelayer AS invalid_value
FROM candidate
WHERE non_cremelayer NOT IN ('Yes','No') OR non_cremelayer IS NULL

UNION ALL

SELECT application_no, 'maharashtra_domicile', maharashtra_domicile
FROM candidate
WHERE maharashtra_domicile NOT IN ('Yes','No') OR maharashtra_domicile IS NULL

UNION ALL

SELECT application_no, 'karnataka_domicile', karnataka_domicile
FROM candidate
WHERE karnataka_domicile NOT IN ('Yes','No') OR karnataka_domicile IS NULL

UNION ALL

SELECT application_no, 'ex_soldier', ex_soldier
FROM candidate
WHERE ex_soldier NOT IN ('Yes','No') OR ex_soldier IS NULL

UNION ALL

SELECT application_no, 'home_guard', home_guard
FROM candidate
WHERE home_guard NOT IN ('Yes','No') OR home_guard IS NULL

UNION ALL

SELECT application_no, 'prakalpgrast', prakalpgrast
FROM candidate
WHERE prakalpgrast NOT IN ('Yes','No') OR prakalpgrast IS NULL

UNION ALL

SELECT application_no, 'bhukampgrast', bhukampgrast
FROM candidate
WHERE bhukampgrast NOT IN ('Yes','No') OR bhukampgrast IS NULL

UNION ALL

SELECT application_no, 'sportsperson', sportsperson
FROM candidate
WHERE sportsperson NOT IN ('Yes','No') OR sportsperson IS NULL

UNION ALL

SELECT application_no, 'parttime', parttime
FROM candidate
WHERE parttime NOT IN ('Yes','No') OR parttime IS NULL

UNION ALL

SELECT application_no, 'female_reservation', female_reservation
FROM candidate
WHERE female_reservation NOT IN ('Yes','No') OR female_reservation IS NULL

UNION ALL

SELECT application_no, 'parent_in_police', parent_in_police
FROM candidate
WHERE parent_in_police NOT IN ('Yes','No') OR parent_in_police IS NULL

UNION ALL

SELECT application_no, 'anath', anath
FROM candidate
WHERE anath NOT IN ('Yes','No') OR anath IS NULL

UNION ALL

SELECT application_no, 'ex_service_dependent', ex_service_dependent
FROM candidate
WHERE ex_service_dependent NOT IN ('Yes','No') OR ex_service_dependent IS NULL

UNION ALL

SELECT application_no, 'is_ncc', is_ncc
FROM candidate
WHERE is_ncc NOT IN ('Yes','No') OR is_ncc IS NULL

UNION ALL

SELECT application_no, 'naxalite_area', naxalite_area
FROM candidate
WHERE naxalite_area NOT IN ('Yes','No') OR naxalite_area IS NULL

UNION ALL

SELECT application_no, 'small_vehicle', small_vehicle
FROM candidate
WHERE small_vehicle NOT IN ('Yes','No') OR small_vehicle IS NULL

UNION ALL

SELECT application_no, 'work_on_contract', work_on_contract
FROM candidate
WHERE work_on_contract NOT IN ('Yes','No') OR work_on_contract IS NULL

UNION ALL

SELECT application_no, 'mscit', mscit
FROM candidate
WHERE mscit NOT IN ('Yes','No') OR mscit IS NULL

UNION ALL

SELECT application_no, 'is_farmer_suicide', is_farmer_suicide
FROM candidate
WHERE is_farmer_suicide NOT IN ('Yes','No') OR is_farmer_suicide IS NULL;