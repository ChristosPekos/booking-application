package Data;

import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * a class storing tasks for sending rooms back to the user
 */
public class UserSendTask {
    private int mapID;
    private ObjectOutputStream outputStream;
    private ArrayList<Room> rooms;

    public UserSendTask(int mapID, ObjectOutputStream outputStream, ArrayList<Room> rooms) {
        this.mapID = mapID;
        this.outputStream = outputStream;
        this.rooms = rooms;
    }

    public int getMapID() {
        return mapID;
    }

    public void setMapID(int mapID) {
        this.mapID = mapID;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }

    @Override
    public boolean equals(Object obj) {
        UserSendTask userSendTask = (UserSendTask) obj;
        return this.mapID == userSendTask.mapID;
    }
}
