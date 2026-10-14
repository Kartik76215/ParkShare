INSERT IGNORE INTO users (id, name, email, password, role) VALUES
(1, 'Riya Sharma', 'host@parkshare.com', 'pbkdf2:120000:8L+3ZvL/cyAV3zQ5W0/J2w==:IT1FKkTKt3c6NRnlEO0+0ps+PstQMBaQpZMiV/gJAJ4=', 'HOST'),
(2, 'Arjun Mehta', 'driver@parkshare.com', 'pbkdf2:120000:D9tr5JpCx6kZEheLAoIVhA==:+9ucUuuCT1AZhvwJDCza6hflq4Rpg+8YpQb/690yk6Y=', 'DRIVER'),
(3, 'Neha Kapoor', 'neha.host@parkshare.com', 'pbkdf2:120000:0Y0KIr8znbZlHsgPYpfvEA==:hMBoPaK7LfhwgJ0VPKD+36fihcl0y3gtTdwVxrlifLY=', 'HOST');

INSERT IGNORE INTO vehicle (id, user_id, vehicle_number, vehicle_type, model) VALUES
(1, 2, 'MH12AB1234', 'CAR', 'Hyundai i20');

INSERT IGNORE INTO parking_space (id, host_id, title, address, city, locality, description, image_url, latitude, longitude, price_per_hour, vehicle_type, active) VALUES
(1, 1, 'Covered Space Near City Mall', 'City Mall Road, Pune', 'Pune', 'City Mall Road', 'Covered parking space with security guard access and easy entry from the main road.', '', 18.5204, 73.8567, 60.00, 'CAR', true),
(2, 1, 'Secure Basement Parking', 'FC Road, Pune', 'Pune', 'FC Road', 'Basement slot suitable for SUV vehicles near cafes and office buildings.', '', 18.5314, 73.8446, 75.00, 'SUV', true),
(3, 3, 'Budget Two-Wheeler Parking', 'Kothrud Depot, Pune', 'Pune', 'Kothrud', 'Affordable two-wheeler parking close to Kothrud depot.', '', 18.5074, 73.8077, 20.00, 'TWO_WHEELER', true);

INSERT IGNORE INTO availability (id, parking_space_id, date, start_time, end_time) VALUES
(4, 1, '2026-10-20', '08:00:00', '22:00:00'),
(5, 2, '2026-10-20', '09:00:00', '21:00:00'),
(6, 3, '2026-10-20', '07:00:00', '23:00:00');
