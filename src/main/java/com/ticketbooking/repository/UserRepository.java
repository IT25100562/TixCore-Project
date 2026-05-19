package com.ticketbooking.repository;

import com.ticketbooking.model.User;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

@Repository
public class UserRepository extends FileRepository<User> {
    @Override protected String fileName() { return "users.txt"; }
    @Override protected Function<String, User> deserializer() { return User::deserialize; }
}
