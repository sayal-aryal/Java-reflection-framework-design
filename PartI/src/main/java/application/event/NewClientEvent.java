package application.event;

public class NewClientEvent extends Event {
    private Client client;

    public NewClientEvent(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }
}
