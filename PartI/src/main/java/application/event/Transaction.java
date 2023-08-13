package application.event;

public class Transaction {
    private String id;
    private double total;

    public Transaction(String id, double total) {
        this.id = id;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public double getTotal() {
        return total;
    }
}
