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
    private final String filePath;
    private final Class<T> classType;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private boolean isDataChanged = false;

    public GenericRepository(String filePath, Class<T> classType) {
        this.filePath = filePath;
        this.classType = classType;
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                objectMapper.writeValue(file, new ArrayList<T>());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create repository file: " + e.getMessage(), e);
            }
        }
    }

    public boolean isDataChanged() {
        return isDataChanged;
    }

    @Override
    public void add(T item) {
        items.add(item);
        System.out.println("Added: " + item);
        isDataChanged = true;
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    @Override
    public void delete(T item) {
        if (items.remove(item)) {
            System.out.println("Deleted: " + item);
            isDataChanged = true;
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
            File directory = new File(filePath).getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
            objectMapper.writeValue(new File(filePath), items);
            System.out.println("Items saved to JSON file: " + filePath);
            isDataChanged = false;
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

    @Override
    public String toString() {
        String fileName = new File(filePath).getName();
        if (fileName.endsWith(".json")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".json"));
        }
        return fileName;
    }

    public Object getType() {
        return classType;
    }
}
