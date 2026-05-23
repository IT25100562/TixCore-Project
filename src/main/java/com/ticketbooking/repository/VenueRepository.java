package com.ticketbooking.repository;

import com.ticketbooking.model.Venue;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

@Repository
public class VenueRepository extends FileRepository<Venue> {
    @Override protected String fileName() { return "venues.txt"; }
    @Override protected Function<String, Venue> deserializer() { return Venue::deserialize; }
}
