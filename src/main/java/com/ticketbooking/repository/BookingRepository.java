package com.ticketbooking.repository;

import com.ticketbooking.model.Booking;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

@Repository
public class BookingRepository extends FileRepository<Booking> {
    @Override protected String fileName() { return "bookings.txt"; }
    @Override protected Function<String, Booking> deserializer() { return Booking::deserialize; }
}
