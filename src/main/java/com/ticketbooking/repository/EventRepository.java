package com.ticketbooking.repository;

import com.ticketbooking.model.Event;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

@Repository
public class EventRepository extends FileRepository<Event> {
    @Override protected String fileName() { return "events.txt"; }
    @Override protected Function<String, Event> deserializer() { return Event::deserialize; }
}
