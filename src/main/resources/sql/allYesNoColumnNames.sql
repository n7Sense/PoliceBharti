
    select
        cd.application_no,
        cd.non_cremelayer,
        cd.maharashtra_domicile,
        cd.karnataka_domicile,
        cd.ex_soldier,
        cd.home_guard,
        cd.prakalpgrast,
        cd.bhukampgrast,
        cd.sportsperson,
        cd.female_reservation,
        cd.parent_in_police,
        cd.anath,
        cd.ex_service_dependent,
        cd.is_ncc,
        cd.naxalite_area,
        cd.small_vehicle,
        cd.work_on_contract,
        cd.mscit,
        cd.is_farmer_suicide,
        cd.ssc_result
    from candidate cd where application_category = 'Open';


SELECT distinct candidate.application_category from candidate;
SELECT distinct candidate.parallel_reservation from candidate;
SELECT distinct candidate.seventh_result from candidate;
SELECT candidate.application_category, candidate.parallel_reservation from candidate;
