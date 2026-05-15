package com.ticketbooking.repository;

import com.ticketbooking.model.Entity;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class FileRepository<T extends Entity> {

    @Value("${app.data.dir}")
    private String dataDir;

    private Path file;

    protected abstract String fileName();
    protected abstract Function<String, T> deserializer();

    @PostConstruct
    public void init() throws IOException {
        Path dir = Paths.get(dataDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        file = dir.resolve(fileName());
        if (!Files.exists(file)) Files.createFile(file);
    }

    public synchronized List<T> findAll() {
        try {
            List<String> lines = Files.readAllLines(file);
            List<T> result = new ArrayList<>();
            for (String line : lines) {
                if (line.isBlank()) continue;
                try { result.add(deserializer().apply(line)); } catch (Exception ignored) {}
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<T> findById(String id) {
        return findAll().stream().filter(e -> id.equals(e.getId())).findFirst();
    }

    public synchronized T save(T entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        if (entity.getCreatedAt() == null || entity.getCreatedAt().isBlank()) {
            entity.setCreatedAt(Instant.now().toString());
        }
        List<T> all = findAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(entity.getId())) {
                all.set(i, entity);
                found = true;
                break;
            }
        }
        if (!found) all.add(entity);
        writeAll(all);
        return entity;
    }

    public synchronized boolean deleteById(String id) {
        List<T> all = findAll();
        List<T> kept = all.stream().filter(e -> !id.equals(e.getId())).collect(Collectors.toList());
        if (kept.size() == all.size()) return false;
        writeAll(kept);
        return true;
    }

    private void writeAll(List<T> entities) {
        try {
            List<String> lines = entities.stream().map(Entity::serialize).collect(Collectors.toList());
            Files.write(file, lines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
