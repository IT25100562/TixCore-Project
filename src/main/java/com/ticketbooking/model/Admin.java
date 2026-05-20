package com.ticketbooking.model;

import com.ticketbooking.util.CsvUtil;

public class Admin extends Entity {
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String password;

    public Admin() {}

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }

    @Override
    public String serialize() {
        return CsvUtil.join(id, username, email, fullName, role, createdAt, password);
    }

    public static Admin deserialize(String line) {
        String[] p = CsvUtil.split(line);
        Admin a = new Admin();
        a.id = CsvUtil.unescape(p[0]);
        a.username = CsvUtil.unescape(p[1]);
        a.email = CsvUtil.unescape(p[2]);
        a.fullName = CsvUtil.unescape(p[3]);
        a.role = CsvUtil.unescape(p[4]);
        a.createdAt = p.length > 5 ? CsvUtil.unescape(p[5]) : "";
        a.password = p.length > 6 ? CsvUtil.unescape(p[6]) : "";
        return a;
    }
}
