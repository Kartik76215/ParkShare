# ParkShare

A Peer-to-Peer Parking Space Sharing and Booking Platform.

ParkShare is a Java Spring Boot PBL project for peer-to-peer parking space booking. It keeps the backend simple and explainable while providing a polished frontend demo connected to REST APIs and MySQL.

## Technology Stack

- Frontend: HTML, CSS, JavaScript
- Backend: Java, Spring Boot, REST APIs, Spring Data JPA, Hibernate
- Database: MySQL
- Build tool: Maven
- Tests: JUnit 5 and Mockito

## How To Run

1. Start MySQL.
2. Create a database user/password for your local MySQL. Do not commit the password.
3. Either set an environment variable named `DB_PASSWORD`, or create a local ignored file named `application-local.properties` in the project root:

```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

4. Run:

```bash
mvn spring-boot:run
```

5. Open:

```text
http://localhost:8080
```

The database `parkshare` is created automatically if it does not already exist.

If Maven is not available in the terminal, open the project in IntelliJ IDEA and run `ParkShareApplication`, or use the local `run-parkshare.bat` helper on this laptop.

## Demo Login

- Host: `host@parkshare.com` / `1234`
- Driver: `driver@parkshare.com` / `1234`

The sample passwords are stored as PBKDF2 hashes in `data.sql`. Existing old demo rows with plain text passwords are upgraded after a successful login.

## Project Structure

```text
src/main/java/com/parkshare
├── controller
├── dto
├── entity
├── exception
├── repository
├── service
└── ParkShareApplication.java
```

## Database Tables

- `users`: stores driver and host accounts.
- `vehicle`: stores driver vehicle details.
- `parking_space`: stores host parking listings.
- `availability`: stores available date and time for a parking space.
- `booking`: stores driver bookings.
- `payment`: stores simulated payment records.
- `review`: stores ratings and comments.

## Main API Flow

Frontend sends data using `fetch()`.

```text
HTML/CSS/JS
-> REST Controller
-> Service
-> Repository
-> Spring Data JPA
-> MySQL
```

## Important APIs

```text
POST   /api/users/register
POST   /api/users/login

GET    /api/parking
GET    /api/parking?location=Pune&vehicleType=CAR&minPrice=20&maxPrice=100&date=2026-10-20&startTime=10:00&endTime=12:00
GET    /api/parking/{id}
GET    /api/parking/host/{hostId}
POST   /api/parking
PUT    /api/parking/{id}
DELETE /api/parking/{id}?hostId=1

POST   /api/bookings
GET    /api/bookings/driver/{driverId}
GET    /api/bookings/host/{hostId}
PUT    /api/bookings/{id}/cancel?driverId=2
PUT    /api/bookings/{id}/complete

POST   /api/payments
POST   /api/reviews
GET    /api/reviews/parking/{parkingSpaceId}
```

## Business Logic To Explain

### Price Calculation

`BookingService` calculates duration using `Duration.between(startTime, endTime)`.

```text
totalPrice = durationInHours * pricePerHour
```

### Availability Check

Before saving a booking, `BookingService` checks:

- Parking space exists.
- Parking space is active.
- Driver role is correct.
- Requested vehicle type matches the parking listing.
- Start and end time are valid 30-minute intervals.
- Requested time is inside the host's availability.
- Requested time does not overlap an existing confirmed booking.

Overlap condition:

```text
newStart < existingEnd && newEnd > existingStart
```

So:

- Existing `10:00 - 12:00`, new `11:00 - 13:00`: conflict.
- Existing `10:00 - 12:00`, new `12:00 - 14:00`: available.

### Cancellation

When a driver cancels, booking status changes to `CANCELLED`. Cancelled bookings are ignored during overlap checking, so the slot becomes available again.

### Payment

Payment is simulated. No real gateway is used. A successful payment record is saved in the `payment` table.

## First Demo Checklist

1. Open home page.
2. Login as host.
3. Add a parking space.
4. Show the parking space in MySQL.
5. Logout and login as driver.
6. Search parking.
7. Open parking details.
8. Book a slot.
9. Show booking in My Bookings.
10. Cancel booking.
11. Add review.

Use the sample date `2026-10-20` for the seeded parking spaces.

## Second Demo Checklist

1. Show package structure.
2. Explain entities and database tables.
3. Show controllers and endpoints.
4. Show service layer business logic.
5. Explain repositories and JPA.
6. Show MySQL connection in `application.properties`.
7. Demonstrate live API request and database update.

## Tests

Run:

```bash
mvn test
```

Current automated tests cover:

- Partial-period booking and fractional price calculation.
- Overlapping booking rejection.
- Adjacent booking acceptance using `[start, end)` interval rules.
- Invalid 30-minute interval rejection.
- Vehicle type mismatch rejection.

## Current Limitations

- Authentication is intentionally simple for a BCA PBL project. It uses login responses stored by the frontend instead of JWT/session security.
- Each parking listing currently supports one selected vehicle type. Multiple vehicle type support can be added later if required.
- Payment is a simulation only. No real card, UPI or banking details are collected.
- Map and distance-based search are not implemented; search uses address/city/locality text and filters.
