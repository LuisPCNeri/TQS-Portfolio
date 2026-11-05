INSERT INTO locker_station (name, addr, latitude, longitude)
SELECT v.name, v.addr, v.latitude, v.longitude
FROM (VALUES
  ('Porto Central',  'Rua de Santa Catarina 100, Porto',   41.1496, -8.6109),
  ('Aveiro Baixa',   'Rua Viana do Castelo 12, Aveiro',    40.6405, -8.6537),
  ('Lisboa Oriente', 'Av. Infante Dom Henrique 1, Lisboa', 38.7675, -9.0988)
) AS v(name, addr, latitude, longitude)
WHERE NOT EXISTS (SELECT 1 FROM locker_station s WHERE s.addr = v.addr);

INSERT INTO compartment (size, status, station_id)
SELECT v.size, 'AVAILABLE', s.id
FROM (VALUES ('S'), ('S'), ('M'), ('M'), ('L')) AS v(size)
JOIN locker_station s ON s.addr = 'Rua de Santa Catarina 100, Porto'
WHERE NOT EXISTS (SELECT 1 FROM compartment c WHERE c.station_id = s.id);

INSERT INTO compartment (size, status, station_id)
SELECT v.size, 'AVAILABLE', s.id
FROM (VALUES ('S'), ('M'), ('L')) AS v(size)
JOIN locker_station s ON s.addr = 'Rua Viana do Castelo 12, Aveiro'
WHERE NOT EXISTS (SELECT 1 FROM compartment c WHERE c.station_id = s.id);

INSERT INTO compartment (size, status, station_id)
SELECT v.size, 'AVAILABLE', s.id
FROM (VALUES ('S'), ('M'), ('L')) AS v(size)
JOIN locker_station s ON s.addr = 'Av. Infante Dom Henrique 1, Lisboa'
WHERE NOT EXISTS (SELECT 1 FROM compartment c WHERE c.station_id = s.id);
