package com.ticketbooking.model;

import com.ticketbooking.util.CsvUtil;

public class Seat extends Entity {
    public static final String AVAILABLE = "AVAILABLE";
    public static final String LOCKED = "LOCKED";
    public static final String BOOKED = "BOOKED";

    private String eventId;
    private String rowLabel;
    private int seatNumber;
    private String status = AVAILABLE;
    private long lockedUntil; // epoch millis
    private String lockedBy;  // user id

    public Seat() {}

    public String getEventId() { return eventId; }
    public void setEventId(String v) { this.eventId = v; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String v) { this.rowLabel = v; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int v) { this.seatNumber = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public long getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(long v) { this.lockedUntil = v; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String v) { this.lockedBy = v; }

    public String getCode() { return rowLabel + seatNumber; }

    @Override
    public String serialize() {
        return CsvUtil.join(id, eventId, rowLabel, String.valueOf(seatNumber), status,
                String.valueOf(lockedUntil), lockedBy == null ? "" : lockedBy, createdAt);
    }

    public static Seat deserialize(String line) {
        String[] p = CsvUtil.split(line);
        Seat s = new Seat();
        s.id = CsvUtil.unescape(p[0]);
        s.eventId = CsvUtil.unescape(p[1]);
        s.rowLabel = CsvUtil.unescape(p[2]);
        s.seatNumber = Integer.parseInt(CsvUtil.unescape(p[3]));
        s.status = CsvUtil.unescape(p[4]);
        s.lockedUntil = p.length > 5 && !p[5].isEmpty() ? Long.parseLong(CsvUtil.unescape(p[5])) : 0L;
        s.lockedBy = p.length > 6 ? CsvUtil.unescape(p[6]) : "";
        s.createdAt = p.length > 7 ? CsvUtil.unescape(p[7]) : "";
        return s;
    }
}
