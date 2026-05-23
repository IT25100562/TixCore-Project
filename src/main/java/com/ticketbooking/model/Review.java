package com.ticketbooking.model;

import com.ticketbooking.util.CsvUtil;

public class Review extends Entity {
    private String userId;
    private String eventId;
    private int rating;
    private String comment;

    public Review() {}

    public String getUserId() { return userId; }
    public void setUserId(String v) { this.userId = v; }
    public String getEventId() { return eventId; }
    public void setEventId(String v) { this.eventId = v; }
    public int getRating() { return rating; }
    public void setRating(int v) { this.rating = v; }
    public String getComment() { return comment; }
    public void setComment(String v) { this.comment = v; }

    @Override
    public String serialize() {
        return CsvUtil.join(id, userId, eventId, rating, comment, createdAt);
    }

    public static Review deserialize(String line) {
        String[] p = CsvUtil.split(line);
        Review r = new Review();
        r.id = CsvUtil.unescape(p[0]);
        r.userId = CsvUtil.unescape(p[1]);
        r.eventId = CsvUtil.unescape(p[2]);
        try { r.rating = Integer.parseInt(CsvUtil.unescape(p[3])); } catch (Exception ex) { r.rating = 0; }
        r.comment = CsvUtil.unescape(p[4]);
        r.createdAt = p.length > 5 ? CsvUtil.unescape(p[5]) : "";
        return r;
    }
}
