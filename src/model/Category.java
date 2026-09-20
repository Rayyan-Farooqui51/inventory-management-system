package model;

import java.util.Optional;

public class Category {
    private final String categoryId;
    private String name;
    private String description;

    public Category(String categoryId, String name){
        this(categoryId, name, null);
    }

    public Category(String categoryId, String name, String description){

        if (categoryId == null || categoryId.isBlank()){
            throw new IllegalArgumentException("Category Id cannot be null or blank");
        }

        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public String toString() {

        if (description == null || description.isBlank()){
            return "Category{" +
                    "categoryId='" + categoryId + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }

        return "Category{" +
                "categoryId='" + categoryId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}' ;
    }
}
