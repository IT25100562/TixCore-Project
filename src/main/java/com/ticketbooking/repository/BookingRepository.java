package com.ticketbooking.repository;

import com.ticketbooking.model.Booking;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

@Repository // Marks this interface as a Spring Data Repository for component scanning.
// OOP & RELATIONSHIP: Inherits from a generic FileRepository specifically typed for <Booking>.
public class BookingRepository extends FileRepository<Booking> {
    
    @Override // OOP: Polymorphism  Provides the specific file name for bookings.
    protected String fileName() { return "bookings.txt"; } // Data will be stored in this text file.
    
    @Override // OOP: Polymorphism  Provides the specific deserialization logic for Booking objects.
    protected Function<String, Booking> deserializer() { 
        // Passes a method reference to the Booking class's static deserialize method.
        return Booking::deserialize; 
    }
}