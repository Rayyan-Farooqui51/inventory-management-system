package service;

import exception.CategoryNotFoundException;
import exception.DuplicateCategoryException;
import model.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp(){
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    void shouldRejectNullRepository(){
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new CategoryService(null)
        );
    }

    @Test
    void shouldRejectDuplicateId(){

        when(categoryRepository.existsById("CAT001")).thenReturn(true);

        Assertions.assertThrows(
                DuplicateCategoryException.class,
                ()->categoryService.createCategory("CAT001","Electronics")
        );

        verify(categoryRepository).existsById("CAT001");
        verify(categoryRepository, never()).existsByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldRejectDuplicateName(){

        when(categoryRepository.existsById("CAT001")).thenReturn(false);
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        Assertions.assertThrows(
                DuplicateCategoryException.class,
                ()->categoryService.createCategory("CAT001","Electronics")
        );

        verify(categoryRepository).existsById("CAT001");
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldCreateCategorySuccessfully(){
        when(categoryRepository.existsById("CAT001")).thenReturn(false);
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);

        Category category = categoryService.createCategory("CAT001","Electronics", "Electronic Product");

        Assertions.assertEquals("CAT001",category.getCategoryId());
        Assertions.assertEquals("Electronics",category.getName());
        Assertions.assertEquals(Optional.of("Electronic Product"), category.getDescription());

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);

        verify(categoryRepository).existsById("CAT001");
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).save(captor.capture());

        Category category1 = captor.getValue();
        Assertions.assertEquals("CAT001",category1.getCategoryId());
        Assertions.assertEquals("Electronics",category1.getName());
        Assertions.assertEquals(Optional.of("Electronic Product"), category1.getDescription());

    }

    @Test
    void shouldCreateCategoryWithoutDescription(){
        when(categoryRepository.existsById("CAT001")).thenReturn(false);
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);

        Category category = categoryService.createCategory("CAT001","Electronics");

        Assertions.assertEquals("CAT001",category.getCategoryId());
        Assertions.assertEquals("Electronics",category.getName());
        Assertions.assertEquals(Optional.empty(),category.getDescription());

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);

        verify(categoryRepository).existsById("CAT001");
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).save(captor.capture());

        Category category1 = captor.getValue();
        Assertions.assertEquals("CAT001",category1.getCategoryId());
        Assertions.assertEquals("Electronics",category1.getName());
        Assertions.assertEquals(Optional.empty(),category1.getDescription());
    }

    @Test
    void shouldFindCategoryById(){
        Category category = new Category("CAT001", "Electronics");

        when(categoryRepository.findById("CAT001")).thenReturn(Optional.of(category));

        Category result = categoryService.findById("CAT001");

        Assertions.assertEquals(category,result);

        verify(categoryRepository).findById("CAT001");

    }

    @Test
    void shouldThrowExceptionWhenCategoryIdNotFound(){
        when(categoryRepository.findById("CAT001")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                ()->categoryService.findById("CAT001")
        );

        verify(categoryRepository).findById("CAT001");

    }

    @Test
    void shouldFindCategoryByName(){
        Category category = new Category("CAT001", "Electronics");

        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(category));

        Category result = categoryService.findByName("Electronics");

        Assertions.assertEquals(category, result);

        verify(categoryRepository).findByName("Electronics");

    }

    @Test
    void shouldThrowExceptionWhenCategoryNameNotFound(){
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                ()->categoryService.findByName("Electronics")
        );

        verify(categoryRepository).findByName("Electronics");

    }

    @Test
    void shouldFindAllCategories(){
        Category category1 = new Category("CAT001", "Electronics");
        Category category2 = new Category("CAT002", "Cloths");

        when(categoryRepository.findAll()).thenReturn(List.of(category1,category2));

        List<Category> result = categoryService.findAll();

        Assertions.assertEquals(List.of(category1,category2),result);

        verify(categoryRepository).findAll();
    }

    @Test
    void shouldRenameCategorySuccessfully(){
        Category category = new Category("CAT001", "Electronics");

        when(categoryRepository.findById("CAT001")).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Computers")).thenReturn(Optional.empty());

        categoryService.renameCategory("CAT001", "Computers");

        Assertions.assertEquals("Computers", category.getName());

        verify(categoryRepository).findById("CAT001");
        verify(categoryRepository).findByName("Computers");
        verify(categoryRepository).update(category);
    }

    @Test
    void shouldRejectRenameWhenNameAlreadyExists(){
        Category category1 = new Category("CAT001", "Electronics");

        Category category2 = new Category("CAT002", "Computers");

        when(categoryRepository.findById("CAT001")).thenReturn(Optional.of(category1));
        when(categoryRepository.findByName("Computers")).thenReturn(Optional.of(category2));

        Assertions.assertThrows(
                DuplicateCategoryException.class,
                ()->categoryService.renameCategory("CAT001","Computers")
        );

        Assertions.assertEquals("Electronics", category1.getName());

        verify(categoryRepository).findById("CAT001");
        verify(categoryRepository).findByName("Computers");
        verify(categoryRepository, never()).update(any());
    }

    @Test
    void shouldNotRenameWhenNewNameIsSameAsCurrentName(){
        Category category1 = new Category("CAT001", "Electronics");

        when(categoryRepository.findById("CAT001")).thenReturn(Optional.of(category1));
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(category1));

        categoryService.renameCategory("CAT001","Electronics");

        verify(categoryRepository).findById("CAT001");
        verify(categoryRepository).findByName("Electronics");
        verify(categoryRepository, never()).update(any());

    }

    @Test
    void shouldThrowExceptionWhenRenamingNonExistentCategory(){
        when(categoryRepository.findById("CAT001")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                ()->categoryService.renameCategory("CAT001","Computers")
        );

        verify(categoryRepository).findById("CAT001");
        verify(categoryRepository, never()).findByName("Computers");
        verify(categoryRepository, never()).update(any());
    }

    @Test
    void shouldChangeDescriptionSuccessfully(){
        Category category = new Category("CAT001", "Electronics");

        when(categoryRepository.findById("CAT001")).thenReturn(Optional.of(category));

        categoryService.changeDescription("CAT001", "Electronics Product");

        Assertions.assertEquals(Optional.of("Electronics Product"), category.getDescription());

        verify(categoryRepository).findById("CAT001");
        verify(categoryRepository).update(category);
    }

    @Test
    void shouldThrowExceptionWhenChangingDescriptionOfNonExistentCategory(){
        when(categoryRepository.findById("CAT001")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                ()->categoryService.changeDescription("CAT001", "Electronics Product")
        );

        verify(categoryRepository).findById("CAT001");
        verify(categoryRepository, never()).update(any());
    }

}
