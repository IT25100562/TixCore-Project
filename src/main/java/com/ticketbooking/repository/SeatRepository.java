package com.ticketbooking.repository;

import com.ticketbooking.model.Seat;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

@Repository
public class SeatRepository extends FileRepository<Seat> {
    @Override protected String fileName() { return "seats.txt"; }
    @Override protected Function<String, Seat> deserializer() { return Seat::deserialize; }
}
