package com.ticketbooking.repository;

import com.ticketbooking.model.Event;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

// Handles event data storage, extends FileRepository for shared file logic
@Repository
public class EventRepository extends FileRepository<Event> {

    // File where event data is stored
    @Override
    protected String fileName() {
        return "events.txt";
    }

    // Converts a file line into an Event object using Event's deserialize method
    @Override
    protected Function<String, Event> deserializer() {
        return Event::deserialize;
    }
}