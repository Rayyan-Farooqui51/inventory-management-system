package service;

import exception.CategoryNotFoundException;
import exception.DuplicateCategoryException;
import model.Category;
import repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        if (categoryRepository == null){
            throw new IllegalArgumentException("Category Repository cannot be null");
        }

        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(String categoryId, String name, String description){
        if (categoryRepository.existsById(categoryId)){
            throw new DuplicateCategoryException("Category with ID " + categoryId + " already exists");
        }

        if (categoryRepository.existsByName(name)){
            throw new DuplicateCategoryException("Category with name " + name + " already exists");
        }

        Category category = new Category(categoryId, name, description);

        categoryRepository.save(category);

        return category;
    }

    public Category createCategory(String categoryId, String name){
        return createCategory(categoryId, name, null);
    }

    public Category findById(String categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + categoryId + " does not exist"));
    }

    public Category findByName(String name){
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category with name " + name + " does not exist"));
    }

    public List<Category> findAll(){
        return categoryRepository.findAll();
    }
}
