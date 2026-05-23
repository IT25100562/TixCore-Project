package com.ticketbooking.model;

import com.ticketbooking.util.CsvUtil;

public class Venue extends Entity {
    private String name;
    private String address;
    private String city;
    private int capacity;

    public Venue() {}

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getCity() { return city; }
    public void setCity(String v) { this.city = v; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int v) { this.capacity = v; }

    @Override
    public String serialize() {
        return CsvUtil.join(id, name, address, city, capacity, createdAt);
    }

    public static Venue deserialize(String line) {
        String[] p = CsvUtil.split(line);
        Venue v = new Venue();
        v.id = CsvUtil.unescape(p[0]);
        v.name = CsvUtil.unescape(p[1]);
        v.address = CsvUtil.unescape(p[2]);
        v.city = CsvUtil.unescape(p[3]);
        try { v.capacity = Integer.parseInt(CsvUtil.unescape(p[4])); } catch (Exception e) { v.capacity = 0; }
        v.createdAt = p.length > 5 ? CsvUtil.unescape(p[5]) : "";
        return v;
    }
}
