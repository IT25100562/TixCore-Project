package com.ticketbooking.model;

// Utility class used for CSV file handling (join & split data)
import com.ticketbooking.util.CsvUtil;

// Event class represents an event object in the system
// It extends Entity class (so it inherits id and createdAt)
public class Event extends Entity {

    // Event title (e.g., Concert, Conference)
    private String title;

    // Description of the event
    private String description;

    // Category of event (e.g., Music, Sports)
    private String category;

    // Event date (stored as String)
    private String date;

    // Event time
    private String time;

    // ID of the venue where event is held
    private String venueId;

    // Base ticket price
    private double basePrice;

    // Event status (e.g., active, cancelled)
    private String status;

    // Default constructor (required for object creation)
    public Event() {}

    // Getter method for title
    public String getTitle() { return title; }

    // Setter method for title
    public void setTitle(String v) { this.title = v; }

    // Getter for description
    public String getDescription() { return description; }

    // Setter for description
    public void setDescription(String v) { this.description = v; }

    // Getter for category
    public String getCategory() { return category; }

    // Setter for category
    public void setCategory(String v) { this.category = v; }

    // Getter for date
    public String getDate() { return date; }

    // Setter for date
    public void setDate(String v) { this.date = v; }

    // Getter for time
    public String getTime() { return time; }

    // Setter for time
    public void setTime(String v) { this.time = v; }

    // Getter for venue ID
    public String getVenueId() { return venueId; }

    // Setter for venue ID
    public void setVenueId(String v) { this.venueId = v; }

    // Getter for base price
    public double getBasePrice() { return basePrice; }

    // Setter for base price
    public void setBasePrice(double v) { this.basePrice = v; }

    // Getter for status
    public String getStatus() { return status; }

    // Setter for status
    public void setStatus(String v) { this.status = v; }

    // Convert object into CSV format string (used when saving to file)
    @Override
    public String serialize() {

        // Join all fields into single CSV line
        return CsvUtil.join(
                id, title, description, category,
                date, time, venueId, basePrice, status, createdAt
        );
    }

    // Convert CSV line back into Event object (used when reading from file)
    public static Event deserialize(String line) {

        // Split CSV line into parts
        String[] p = CsvUtil.split(line);

        // Create new Event object
        Event e = new Event();

        // Assign values from CSV to object fields
        e.id = CsvUtil.unescape(p[0]);
        e.title = CsvUtil.unescape(p[1]);
        e.description = CsvUtil.unescape(p[2]);
        e.category = CsvUtil.unescape(p[3]);
        e.date = CsvUtil.unescape(p[4]);
        e.time = CsvUtil.unescape(p[5]);
        e.venueId = CsvUtil.unescape(p[6]);

        // Convert price from String to double
        try {
            e.basePrice = Double.parseDouble(CsvUtil.unescape(p[7]));
        } catch (Exception ex) {
            e.basePrice = 0; // Default value if conversion fails
        }

        // Set event status
        e.status = CsvUtil.unescape(p[8]);

        // Set created date (if available)
        e.createdAt = p.length > 9 ? CsvUtil.unescape(p[9]) : "";

        // Return fully built Event object
        return e;
    }
}