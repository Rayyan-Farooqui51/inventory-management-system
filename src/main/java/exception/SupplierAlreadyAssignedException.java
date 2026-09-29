package exception;

public class SupplierAlreadyAssignedException extends RuntimeException{
    public SupplierAlreadyAssignedException(String message){
        super(message);
    }
}
