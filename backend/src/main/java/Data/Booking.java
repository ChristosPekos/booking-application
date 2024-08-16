package Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * a class representing a booking for a room on a specific time frame
 */
public class Booking implements Serializable {

    private LocalDate firstDate;
    private LocalDate lastDate;
    private Room room;

    public Booking(LocalDate firstDate, LocalDate lastDate, Room room){
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.room = room;
    }

    public LocalDate getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(LocalDate firstDate) {
        this.firstDate = firstDate;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public void setLastDate(LocalDate lastDate) {
        this.lastDate = lastDate;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "Associated room: " + getRoom().getName() + '\n' +"First Date: " + getFirstDate() + '\n' + "Last Date: " + getLastDate();

    }
}
