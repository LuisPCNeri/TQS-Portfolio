INSERT INTO locker_station (name, addr, latitude, longitude)
VALUES
  ('Porto Central',  'Rua de Santa Catarina 100, Porto',   41.1496, -8.6109),
  ('Aveiro Baixa',   'Rua Viana do Castelo 12, Aveiro',    40.6405, -8.6537),
  ('Lisboa Oriente', 'Av. Infante Dom Henrique 1, Lisboa', 38.7675, -9.0988)
ON CONFLICT DO NOTHING;

-- a few compartments per station (adjust IDs to match your generated ones)
INSERT INTO compartment (size, status, station_id) VALUES
  ('S', 'AVAILABLE', 1), ('S', 'AVAILABLE', 1),
  ('M', 'AVAILABLE', 1), ('M', 'AVAILABLE', 1),
  ('L', 'AVAILABLE', 1),
  ('S', 'AVAILABLE', 2), ('M', 'AVAILABLE', 2), ('L', 'AVAILABLE', 2),
  ('S', 'AVAILABLE', 3), ('M', 'AVAILABLE', 3), ('L', 'AVAILABLE', 3);
