package Data;

import java.util.ArrayList;

/**
 * a class for storing the rooms that can be reviewed
 */
public class RoomsToReviewStorage {
    private static ArrayList<Room> rooms = new ArrayList<>();

    public synchronized static void add(Room room) {
        rooms.add(room);
    }

    public synchronized static ArrayList<Room> getRooms() {
        return new ArrayList<>(rooms); // Return a copy to avoid external modifications
    }

    public synchronized static void delete(Room room){
        rooms.remove(room);
    }
    public static boolean contains(Room room){
        return rooms.contains(room);
    }
}
