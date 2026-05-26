package com.ticketbooking.model;

import com.ticketbooking.util.CsvUtil;

// RELATIONSHIP &  Booking inherits from Entity class (Inheritance concept).
public class Booking extends Entity { 
    //  Encapsulation  Making fields private.
    private String userId; // Stores the id of the user who made the booking.
    private String eventId; // Stores the id of the booked event.
    private String seats; // Stores seat numbers as a comma separated string.
    private double totalPrice; // Stores the total cost of the booking.
    private String status; //  confirmed, cancelled.

    public Booking() {} // Default empty constructor required for object instantiation.

    //  Encapsulation  Public getters and setters to access private fields.
    public String getUserId() { return userId; }
    public void setUserId(String v) { this.userId = v; }
    public String getEventId() { return eventId; }
    public void setEventId(String v) { this.eventId = v; }
    public String getSeats() { return seats; }
    public void setSeats(String v) { this.seats = v; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double v) { this.totalPrice = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    @Override // Polymorphism  Overriding the serialize method from the parent Entity class.
    public String serialize() {
        // Converts the object's properties into a CSV formatted string for file storage.
        return CsvUtil.join(id, userId, eventId, seats, totalPrice, status, createdAt);
    }

    // Static method used to create a Booking object from a CSV line .
    public static Booking deserialize(String line) {
        String[] p = CsvUtil.split(line);
        Booking b = new Booking();
        b.id = CsvUtil.unescape(p[0]);
        b.userId = CsvUtil.unescape(p[1]);
        b.eventId = CsvUtil.unescape(p[2]);
        b.seats = CsvUtil.unescape(p[3]);
        
        // ERROR HANDLING Try catch block to handle potential NumberFormatException during parsing.
        try { b.totalPrice = Double.parseDouble(CsvUtil.unescape(p[4])); } catch (Exception ex) { b.totalPrice = 0; }
        
        b.status = CsvUtil.unescape(p[5]); // Sets Status.
        // Checks if createdAt exists in the array to prevent IndexOutOfBoundsException.
        b.createdAt = p.length > 6 ? CsvUtil.unescape(p[6]) : ""; 
        return b;
    }
}