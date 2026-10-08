INSERT IGNORE INTO users (id, name, email, password, role) VALUES
(1, 'Riya Sharma', 'host@parkshare.com', '1234', 'HOST'),
(2, 'Arjun Mehta', 'driver@parkshare.com', '1234', 'DRIVER'),
(3, 'Neha Kapoor', 'neha.host@parkshare.com', '1234', 'HOST');

INSERT IGNORE INTO vehicle (id, user_id, vehicle_number, vehicle_type, model) VALUES
(1, 2, 'MH12AB1234', 'CAR', 'Hyundai i20');

INSERT IGNORE INTO parking_space (id, host_id, title, address, latitude, longitude, price_per_hour, vehicle_type, active) VALUES
(1, 1, 'Covered Space Near City Mall', 'City Mall Road, Pune', 18.5204, 73.8567, 60.00, 'CAR', true),
(2, 1, 'Secure Basement Parking', 'FC Road, Pune', 18.5314, 73.8446, 75.00, 'SUV', true),
(3, 3, 'Budget Two-Wheeler Parking', 'Kothrud Depot, Pune', 18.5074, 73.8077, 20.00, 'TWO_WHEELER', true);

INSERT IGNORE INTO availability (id, parking_space_id, date, start_time, end_time) VALUES
(1, 1, '2026-10-10', '08:00:00', '22:00:00'),
(2, 2, '2026-10-10', '09:00:00', '21:00:00'),
(3, 3, '2026-10-10', '07:00:00', '23:00:00');
