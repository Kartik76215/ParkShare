# ParkShare

ParkShare is a Java Spring Boot PBL project for peer-to-peer parking space booking. It keeps the backend simple and explainable while providing a polished frontend demo.

## Technology Stack

- Frontend: HTML, CSS, JavaScript
- Backend: Java, Spring Boot, REST APIs, Spring Data JPA
- Database: MySQL
- Build tool: Maven

## How To Run

1. Start MySQL.
2. Set your local MySQL password as an environment variable named `DB_PASSWORD`, or leave it empty if your local root user has no password.
3. Run:

```bash
mvn spring-boot:run
```

4. Open:

```text
http://localhost:8080
```

The database `parkshare` is created automatically if it does not already exist.

## Demo Login

- Host: `host@parkshare.com` / `1234`
- Driver: `driver@parkshare.com` / `1234`

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
- Driver role is correct.
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

## Second Demo Checklist

1. Show package structure.
2. Explain entities and database tables.
3. Show controllers and endpoints.
4. Show service layer business logic.
5. Explain repositories and JPA.
6. Show MySQL connection in `application.properties`.
7. Demonstrate live API request and database update.
