package com.ticketbooking.model;

import com.ticketbooking.util.CsvUtil;

// User class that inherits from Entity class
public class User extends Entity {

    //Variables to store user information
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String password;

    //Default User constructor
    public User() {}

    //Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }

    // Converts User object into CSV format for file storage
    @Override
    public String serialize() {
        return CsvUtil.join(id, username, email, fullName, phone, createdAt, password);
    }

    // Converts CSV line back into a User object
    public static User deserialize(String line) {

        // Split CSV line into separate values
        String[] p = CsvUtil.split(line);

        // Create empty User object
        User u = new User();

        // Restore object fields from CSV values
        u.id = CsvUtil.unescape(p[0]);
        u.username = CsvUtil.unescape(p[1]);
        u.email = CsvUtil.unescape(p[2]);
        u.fullName = CsvUtil.unescape(p[3]);
        u.phone = CsvUtil.unescape(p[4]);

        // Safety check to avoid ArrayIndexOutOfBoundsException
        u.createdAt = p.length > 5 ? CsvUtil.unescape(p[5]) : "";
        u.password = p.length > 6 ? CsvUtil.unescape(p[6]) : "";

        return u;
    }
}

