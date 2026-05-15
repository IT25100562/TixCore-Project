# Event Ticket Booking System (Spring Boot)

A web-based Event Ticket Booking System built with Spring Boot and Thymeleaf. Uses plain text files (`.txt`) as the database — no SQL server or external database required.

## Modules

1. User Management
2. Admin Management
3. Venue Management
4. Event Management
5. Ticket Booking
6. Reviews / Feedback

Each module supports full CRUD (Create, Read, Update, Delete).

## Requirements

- Java 17 or later
- Maven 3.8+

## How to Run

```bash
mvn spring-boot:run
```

Then open: http://localhost:8080

The first run creates a `data/` folder beside the project with `.txt` files for each entity. Sample data is already provided.

## Build a runnable JAR

```bash
mvn clean package
java -jar target/ticket-booking-1.0.0.jar
```

## Data Storage

All records are stored in pipe-separated text files inside the `data/` directory:

- `users.txt`
- `admins.txt`
- `venues.txt`
- `events.txt`
- `bookings.txt`
- `reviews.txt`

You can open these files in Notepad to inspect them directly.
