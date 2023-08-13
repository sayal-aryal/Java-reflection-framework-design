package application.async;

import framework.Async;

public class Room implements Building{

    public Room() {

    }

    @Async
    public void manage() {
        System.out.println( "Managing the Room...");
    }

    public void clean() {
        System.out.println( "Room cleaning process going on...");
    }
}
