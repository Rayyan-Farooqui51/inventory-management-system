package service;

import exception.DuplicateSupplierException;
import exception.SupplierNotFoundException;
import model.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.SupplierRepository;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    private SupplierService supplierService;

    @BeforeEach
    void setUp(){
        supplierService = new SupplierService(supplierRepository);
    }

    @Test
    void shouldRejectNullRepository(){
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()-> new SupplierService(null)
        );
    }

    @Test
    void shouldCreateSupplier(){

        when(supplierRepository.existsById("S1")).thenReturn(false);
        when(supplierRepository.existsByEmail("abc@example.com")).thenReturn(false);

        Supplier supplier = supplierService.createSupplier("S1","ABC Supplier","abc@example.com");

        Assertions.assertEquals("S1",supplier.getSupplierId());
        Assertions.assertEquals("ABC Supplier", supplier.getName());
        Assertions.assertEquals("abc@example.com", supplier.getEmail());

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);

        verify(supplierRepository).existsById("S1");
        verify(supplierRepository).existsByEmail("abc@example.com");
        verify(supplierRepository).save(captor.capture());

        Supplier savedSupplier = captor.getValue();

        Assertions.assertEquals("S1",savedSupplier.getSupplierId());
        Assertions.assertEquals("ABC Supplier", savedSupplier.getName());
        Assertions.assertEquals("abc@example.com", savedSupplier.getEmail());
        Assertions.assertSame(supplier,savedSupplier);

    }

    @Test
    void shouldRejectDuplicateSupplierId(){
        when(supplierRepository.existsById("S1")).thenReturn(true);

        Assertions.assertThrows(
                DuplicateSupplierException.class,
                ()-> supplierService.createSupplier("S1","ABC Supplier","abc@example.com")
        );

        verify(supplierRepository).existsById("S1");
        verify(supplierRepository, never()).existsByEmail("abc@example.com");
        verify(supplierRepository, never()).save(any());

    }

    @Test
    void shouldRejectDuplicateSupplierEmail(){
        when(supplierRepository.existsById("S1")).thenReturn(false);
        when(supplierRepository.existsByEmail("abc@example.com")).thenReturn(true);

        Assertions.assertThrows(
                DuplicateSupplierException.class,
                ()-> supplierService.createSupplier("S1","ABC Supplier","abc@example.com")
        );

        verify(supplierRepository).existsById("S1");
        verify(supplierRepository).existsByEmail("abc@example.com");
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void shouldCreateSupplierWithoutPhone() {
        when(supplierRepository.existsById("S1")).thenReturn(false);
        when(supplierRepository.existsByEmail("abc@example.com")).thenReturn(false);

        Supplier supplier = supplierService.createSupplier("S1", "ABC Supplier", "abc@example.com");

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);

        verify(supplierRepository).save(captor.capture());

        Supplier savedSupplier = captor.getValue();

        Assertions.assertSame(supplier, savedSupplier);
        Assertions.assertEquals("S1", savedSupplier.getSupplierId());
        Assertions.assertEquals(Optional.empty(), savedSupplier.getPhone());
    }

    @Test
    void shouldFindSupplierById(){
        Supplier supplier = new Supplier("S1", "ABC Supplier", "abc@example.com");

        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier));

        Supplier result = supplierService.findById("S1");

        Assertions.assertSame(result, supplier);

        verify(supplierRepository).findById("S1");
    }

    @Test
    void shouldRejectWhenSupplierNotFoundById(){
        when(supplierRepository.findById("S1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                SupplierNotFoundException.class,
                ()->supplierService.findById("S1")
        );

        verify(supplierRepository).findById("S1");

    }

    @Test
    void shouldFindSupplierByEmail(){
        Supplier supplier = new Supplier("S1", "ABC Supplier", "abc@example.com");

        when(supplierRepository.findByEmail("abc@example.com")).thenReturn(Optional.of(supplier));

        Supplier result = supplierService.findByEmail("abc@example.com");

        Assertions.assertSame(supplier, result);

        verify(supplierRepository).findByEmail("abc@example.com");

    }

    @Test
    void shouldRejectWhenSupplierNotFoundByEmail(){
        when(supplierRepository.findByEmail("abc@example.com")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                SupplierNotFoundException.class,
                ()->supplierService.findByEmail("abc@example.com")
        );

        verify(supplierRepository).findByEmail("abc@example.com");
    }

    @Test
    void shouldFindAllSuppliers(){
        Supplier supplier1 = new Supplier("S1", "ABC Supplier", "abc@example.com");
        Supplier supplier2 = new Supplier("S2", "ABC Supplier", "xyz@example.com");

        when(supplierRepository.findAll()).thenReturn(List.of(supplier1,supplier2));

        List<Supplier> result = supplierService.findAll();

        Assertions.assertEquals(List.of(supplier1,supplier2),result);

        verify(supplierRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoSuppliersExist(){
        when(supplierRepository.findAll()).thenReturn(Collections.emptyList());

        List<Supplier> result = supplierService.findAll();

        Assertions.assertTrue(result.isEmpty());

        verify(supplierRepository).findAll();
    }

    @Test
    void shouldRenameSupplier(){
        Supplier supplier = new Supplier("S1", "ABC Supplier", "abc@example.com");

        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier));

        supplierService.renameSupplier("S1", "XYZ Supplier");

        Assertions.assertEquals("XYZ Supplier", supplier.getName());

        verify(supplierRepository).findById("S1");
        verify(supplierRepository).update(supplier);
    }

    @Test
    void shouldRejectRenameWhenSupplierNotFound(){
        when(supplierRepository.findById("S1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                SupplierNotFoundException.class,
                ()->supplierService.renameSupplier("S1","XYZ Supplier")
        );

        verify(supplierRepository,never()).update(any());
    }

    @Test
    void shouldChangeEmail(){
        Supplier supplier = new Supplier("S1", "ABC Supplier", "abc@example.com");

        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier));
        when(supplierRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        supplierService.changeEmail("S1", "new@example.com");

        Assertions.assertEquals("new@example.com",supplier.getEmail());

        verify(supplierRepository).findById("S1");
        verify(supplierRepository).findByEmail("new@example.com");
        verify(supplierRepository).update(supplier);
    }

    @Test
    void shouldThrowExceptionWhenEmailBelongsToAnotherSupplier(){
        Supplier supplier1 = new Supplier("S1", "ABC Supplier", "abc@example.com");
        Supplier supplier2 = new Supplier("S2", "ABC Supplier", "xyz@example.com");

        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier1));
        when(supplierRepository.findByEmail("xyz@example.com")).thenReturn(Optional.of(supplier2));

        Assertions.assertThrows(
                DuplicateSupplierException.class,
                ()-> supplierService.changeEmail("S1", "xyz@example.com")
        );

        Assertions.assertEquals("abc@example.com",supplier1.getEmail());

        verify(supplierRepository, never()).update(any());

    }

    @Test
    void shouldNotChangeEmailThatBelongsToSameSupplier(){
        Supplier supplier = new Supplier("S1", "ABC Supplier", "abc@example.com");

        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier));
        when(supplierRepository.findByEmail("abc@example.com")).thenReturn(Optional.of(supplier));

        supplierService.changeEmail("S1","abc@example.com");

        Assertions.assertEquals("abc@example.com",supplier.getEmail());

        verify(supplierRepository).findById("S1");
        verify(supplierRepository).findByEmail("abc@example.com");
        verify(supplierRepository, never()).update(any());

    }

    @Test
    void shouldThrowExceptionWhenSupplierDoesNotExistsDuringEmailChange(){
        when(supplierRepository.findById("S1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                SupplierNotFoundException.class,
                ()->supplierService.changeEmail("S1","new@example.com")
        );

        verify(supplierRepository).findById("S1");
        verify(supplierRepository, never()).findByEmail("new@example.com");
        verify(supplierRepository, never()).update(any());
    }

    @Test
    void shouldChangePhone(){
        Supplier supplier = new Supplier("S1", "ABC Supplier", "abc@example.com");

        when(supplierRepository.findById("S1")).thenReturn(Optional.of(supplier));

        supplierService.changePhone("S1","9999999999");

        Assertions.assertEquals(Optional.of("9999999999"),supplier.getPhone());

        verify(supplierRepository).findById("S1");
        verify(supplierRepository).update(supplier);

    }

    @Test
    void shouldRejectPhoneChangeWhenSupplierNotFound(){
        when(supplierRepository.findById("S1")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                SupplierNotFoundException.class,
                ()-> supplierService.changePhone("S1","9876543210")
        );

        verify(supplierRepository).findById("S1");
        verify(supplierRepository, never()).update(any());
    }


}
