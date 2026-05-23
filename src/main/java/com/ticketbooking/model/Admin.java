package com.ticketbooking.model;
// Import CSV utility helper class
import com.ticketbooking.util.CsvUtil;
// Admin class inherits from Entity class
public class Admin extends Entity {
    private String username; // Admin username
    private String email; // Admin email address
    private String fullName; // Admin full name
    private String role; // Admin role
    private String password; // Admin password

    public Admin() {}  // Default constructor

    public String getUsername() { return username; } // Get username
    public void setUsername(String v) { this.username = v; } // Set username
    public String getEmail() { return email; }  // Get email
    public void setEmail(String v) { this.email = v; }  // Set email
    public String getFullName() { return fullName; }  // get full name
    public void setFullName(String v) { this.fullName = v; } // Set full name
    public String getRole() { return role; }  // Get role
    public void setRole(String v) { this.role = v; }  // Set role
    public String getPassword() { return password; }  // Get password
    public void setPassword(String v) { this.password = v; } // Set password

    // Convert object into CSV format
    @Override
    public String serialize() {
        // Join all values into one CSV line
        return CsvUtil.join(id, username, email, fullName, role, createdAt, password);
    }
    // Convert CSV line into Admin object  
    public static Admin deserialize(String line) {
        // Split CSV line into array
        String[] p = CsvUtil.split(line);
        // Create empty Admin object
        Admin a = new Admin();
        // Assign values from CSV
        a.id = CsvUtil.unescape(p[0]);
        a.username = CsvUtil.unescape(p[1]);
        a.email = CsvUtil.unescape(p[2]);
        a.fullName = CsvUtil.unescape(p[3]);
        a.role = CsvUtil.unescape(p[4]);
        // Set created date if exists
        a.createdAt = p.length > 5 ? CsvUtil.unescape(p[5]) : "";
        // Set password if exists
        a.password = p.length > 6 ? CsvUtil.unescape(p[6]) : "";
        // Return completed Admin object
        return a;
    }
}
