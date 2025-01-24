package main.java.com.expenseTracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenericRepository<T> implements Repository<T> {
    private final List<T> items = new ArrayList<>();
    private String filePath;
    private Class<T> classType;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public GenericRepository(String filePath, Class<T> classType) {
        this.filePath = filePath;
        this.classType = classType;
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public void add(T item) {
        items.add(item);
        System.out.println("Added: " + item);
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    @Override
    public void delete(T item) {
        if (items.remove(item)) {
            System.out.println("Deleted: " + item);
        } else {
            System.out.println("Item not found: " + item);
        }
    }

    @Override
    public void clear() {
        items.clear();
        System.out.println("All items have been cleared.");
    }

    @Override
    public void save() {
        try {
            objectMapper.writeValue(new File(filePath), items);
            System.out.println("Items saved to JSON file: " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving items: " + e.getMessage());
        }
    }

    @Override
    public void load() {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                List<T> loadedItems = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, classType));
                items.addAll(loadedItems);
                System.out.println("Items loaded from JSON file: " + filePath);
            } else {
                System.out.println("File not found: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error loading items: " + e.getMessage());
        }
    }
}
