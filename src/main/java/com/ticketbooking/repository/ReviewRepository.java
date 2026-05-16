package com.ticketbooking.repository;

import com.ticketbooking.model.Review;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

@Repository
public class ReviewRepository extends FileRepository<Review> {
    @Override protected String fileName() { return "reviews.txt"; }
    @Override protected Function<String, Review> deserializer() { return Review::deserialize; }
}
