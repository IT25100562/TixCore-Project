package com.ticketbooking.model;

import com.ticketbooking.util.CsvUtil;

public class Booking extends Entity {
    private String userId;
    private String eventId;
    private String seats;
    private double totalPrice;
    private String status;

    public Booking() {}

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

    @Override
    public String serialize() {
        return CsvUtil.join(id, userId, eventId, seats, totalPrice, status, createdAt);
    }

    public static Booking deserialize(String line) {
        String[] p = CsvUtil.split(line);
        Booking b = new Booking();
        b.id = CsvUtil.unescape(p[0]);
        b.userId = CsvUtil.unescape(p[1]);
        b.eventId = CsvUtil.unescape(p[2]);
        b.seats = CsvUtil.unescape(p[3]);
        try { b.totalPrice = Double.parseDouble(CsvUtil.unescape(p[4])); } catch (Exception ex) { b.totalPrice = 0; }
        b.status = CsvUtil.unescape(p[5]);
        b.createdAt = p.length > 6 ? CsvUtil.unescape(p[6]) : "";
        return b;
    }
}
