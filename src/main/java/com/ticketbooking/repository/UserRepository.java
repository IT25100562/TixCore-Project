package com.ticketbooking.repository;

import com.ticketbooking.model.User;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

// Marks this class as a Spring repository component
@Repository

// Repository class used to manage User data storage
public class UserRepository extends FileRepository<User> {

    // Specifies the file used to store user data
    @Override protected String fileName() { return "users.txt"; }

    // Specifies how CSV lines are converted back into User objects
    @Override protected Function<String, User> deserializer() { return User::deserialize; }
}
