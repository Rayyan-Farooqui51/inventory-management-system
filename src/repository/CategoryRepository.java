package repository;

import model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    void save(Category category);

    Optional<Category> findById(String categoryId);

    Optional<Category> findByName(String name);

    List<Category> findAll();

    boolean existsById(String categoryId);

    boolean existsByName(String name);
}
