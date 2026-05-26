package com.ticketbooking.repository;

// Import Event model class
import com.ticketbooking.model.Event;

// Marks this class as a Repository (used for data handling)
import org.springframework.stereotype.Repository;

// Used to define how to convert data (String → Event)
import java.util.function.Function;

// EventRepository class handles event data operations
// It EXTENDS FileRepository, so it REUSES common file logic (like read/write)
@Repository
public class EventRepository extends FileRepository<Event> {

    // This method returns the file name where event data is stored
    // Used by FileRepository internally
    @Override
    protected String fileName() {
        return "events.txt";
    }

    // This method provides logic to convert a file line into Event object
    // Event::deserialize means using Event class deserialize method
    @Override
    protected Function<String, Event> deserializer() {
        return Event::deserialize;
    }
}