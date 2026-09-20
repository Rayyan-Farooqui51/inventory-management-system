package model;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class Supplier {
    private final String supplierId;
    private String name;
    private String email;
    private String phone;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Supplier(String supplierId, String name, String email){
        this(supplierId, name, email, null);
    }

    public Supplier(String supplierId, String name, String email, String phone){

        if (supplierId == null || supplierId.isBlank()){
            throw new IllegalArgumentException("Supplier Id cannot be null or blank");
        }

        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("Email Id cannot be null or blank");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()){
            throw new IllegalArgumentException("Email format is invalid");
        }

        this.supplierId = supplierId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Optional<String> getPhone() {
        return Optional.ofNullable(phone);
    }

    @Override
    public String toString() {
        if (phone == null || phone.isBlank()){
            return "Supplier{" +
                    "supplierId='" + supplierId + '\'' +
                    ", name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }

        return "Supplier{" +
                "supplierId='" + supplierId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Supplier supplier = (Supplier) obj;

        return Objects.equals(this.supplierId, supplier.supplierId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.supplierId);
    }
}
