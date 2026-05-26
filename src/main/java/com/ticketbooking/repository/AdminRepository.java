package com.ticketbooking.repository;

import com.ticketbooking.model.Admin;
import org.springframework.stereotype.Repository;
import java.util.function.Function;

@Repository
public class AdminRepository extends FileRepository<Admin> {
    @Override protected String fileName() { return "admins.txt"; }
    @Override protected Function<String, Admin> deserializer() { return Admin::deserialize; }
}
