-- Migration: Convert 19 yes/no varchar columns to BOOLEAN (TINYINT(1)) in candidate table.
-- Run this once before deploying the Java entity change. Converts existing 'Yes'/'No' to 1/0, then alters column type.

-- Step 1: Normalize existing varchar values to 1/0/NULL (stored as string temporarily)
UPDATE candidate SET non_cremelayer     = CASE WHEN LOWER(TRIM(IFNULL(non_cremelayer,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(non_cremelayer,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET maharashtra_domicile = CASE WHEN LOWER(TRIM(IFNULL(maharashtra_domicile,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(maharashtra_domicile,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET karnataka_domicile  = CASE WHEN LOWER(TRIM(IFNULL(karnataka_domicile,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(karnataka_domicile,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET ex_soldier          = CASE WHEN LOWER(TRIM(IFNULL(ex_soldier,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(ex_soldier,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET home_guard          = CASE WHEN LOWER(TRIM(IFNULL(home_guard,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(home_guard,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET prakalpgrast        = CASE WHEN LOWER(TRIM(IFNULL(prakalpgrast,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(prakalpgrast,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET bhukampgrast        = CASE WHEN LOWER(TRIM(IFNULL(bhukampgrast,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(bhukampgrast,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET sportsperson        = CASE WHEN LOWER(TRIM(IFNULL(sportsperson,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(sportsperson,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET parttime            = CASE WHEN LOWER(TRIM(IFNULL(parttime,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(parttime,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET female_reservation  = CASE WHEN LOWER(TRIM(IFNULL(female_reservation,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(female_reservation,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET parent_in_police    = CASE WHEN LOWER(TRIM(IFNULL(parent_in_police,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(parent_in_police,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET anath               = CASE WHEN LOWER(TRIM(IFNULL(anath,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(anath,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET ex_service_dependent = CASE WHEN LOWER(TRIM(IFNULL(ex_service_dependent,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(ex_service_dependent,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET is_ncc              = CASE WHEN LOWER(TRIM(IFNULL(is_ncc,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(is_ncc,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET naxalite_area        = CASE WHEN LOWER(TRIM(IFNULL(naxalite_area,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(naxalite_area,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET small_vehicle        = CASE WHEN LOWER(TRIM(IFNULL(small_vehicle,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(small_vehicle,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET work_on_contract    = CASE WHEN LOWER(TRIM(IFNULL(work_on_contract,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(work_on_contract,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET mscit               = CASE WHEN LOWER(TRIM(IFNULL(mscit,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(mscit,''))) IN ('no','n','0') THEN '0' ELSE NULL END;
UPDATE candidate SET is_farmer_suicide    = CASE WHEN LOWER(TRIM(IFNULL(is_farmer_suicide,''))) IN ('yes','y','1') THEN '1' WHEN LOWER(TRIM(IFNULL(is_farmer_suicide,''))) IN ('no','n','0') THEN '0' ELSE NULL END;

-- Step 2: Alter column types to TINYINT(1) (MySQL BOOLEAN)
ALTER TABLE candidate
  MODIFY COLUMN non_cremelayer       TINYINT(1) NULL,
  MODIFY COLUMN maharashtra_domicile TINYINT(1) NULL,
  MODIFY COLUMN karnataka_domicile   TINYINT(1) NULL,
  MODIFY COLUMN ex_soldier           TINYINT(1) NULL,
  MODIFY COLUMN home_guard           TINYINT(1) NULL,
  MODIFY COLUMN prakalpgrast         TINYINT(1) NULL,
  MODIFY COLUMN bhukampgrast        TINYINT(1) NULL,
  MODIFY COLUMN sportsperson         TINYINT(1) NULL,
  MODIFY COLUMN parttime             TINYINT(1) NULL,
  MODIFY COLUMN female_reservation   TINYINT(1) NULL,
  MODIFY COLUMN parent_in_police     TINYINT(1) NULL,
  MODIFY COLUMN anath                TINYINT(1) NULL,
  MODIFY COLUMN ex_service_dependent TINYINT(1) NULL,
  MODIFY COLUMN is_ncc               TINYINT(1) NULL,
  MODIFY COLUMN naxalite_area        TINYINT(1) NULL,
  MODIFY COLUMN small_vehicle        TINYINT(1) NULL,
  MODIFY COLUMN work_on_contract     TINYINT(1) NULL,
  MODIFY COLUMN mscit                TINYINT(1) NULL,
  MODIFY COLUMN is_farmer_suicide    TINYINT(1) NULL;
