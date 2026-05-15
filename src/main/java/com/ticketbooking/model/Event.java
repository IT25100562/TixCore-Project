package com.ticketbooking.model;

import com.ticketbooking.util.CsvUtil;

public class Event extends Entity {
    private String title;
    private String description;
    private String category;
    private String date;
    private String time;
    private String venueId;
    private double basePrice;
    private String status;

    public Event() {}

    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public String getDate() { return date; }
    public void setDate(String v) { this.date = v; }
    public String getTime() { return time; }
    public void setTime(String v) { this.time = v; }
    public String getVenueId() { return venueId; }
    public void setVenueId(String v) { this.venueId = v; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double v) { this.basePrice = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    @Override
    public String serialize() {
        return CsvUtil.join(id, title, description, category, date, time, venueId, basePrice, status, createdAt);
    }

    public static Event deserialize(String line) {
        String[] p = CsvUtil.split(line);
        Event e = new Event();
        e.id = CsvUtil.unescape(p[0]);
        e.title = CsvUtil.unescape(p[1]);
        e.description = CsvUtil.unescape(p[2]);
        e.category = CsvUtil.unescape(p[3]);
        e.date = CsvUtil.unescape(p[4]);
        e.time = CsvUtil.unescape(p[5]);
        e.venueId = CsvUtil.unescape(p[6]);
        try { e.basePrice = Double.parseDouble(CsvUtil.unescape(p[7])); } catch (Exception ex) { e.basePrice = 0; }
        e.status = CsvUtil.unescape(p[8]);
        e.createdAt = p.length > 9 ? CsvUtil.unescape(p[9]) : "";
        return e;
    }
}
