package application.event;


public class TransactionEvent extends Event  {
    private Transaction transaction;

    public TransactionEvent(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
