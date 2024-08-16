package Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * a class representing a Room belonging to a manager
 */
public class Room implements Serializable {
    private String name;
    private int noOfPeople;
    private String area;
    private float stars;
    private int noOfReviews;
    private String roomImagePath;
    private float price;
    private LocalDate availabilityStart;
    private LocalDate availabilityEnd;
    private ArrayList<Booking> bookings;
    private int workerID;
    private byte[] roomImage;

    public Room(String name, int noOfPeople, String area, float stars, int noOfReviews, String roomImagePath, LocalDate availabilityStart, LocalDate availabilityEnd, int price){
        this.name = name;
        this.noOfPeople = noOfPeople;
        this.area = area;
        this.stars = stars;
        this.noOfReviews = noOfReviews;
        this.roomImagePath = roomImagePath;
        this.bookings = new ArrayList<>();
        this.availabilityStart = availabilityStart;
        this.availabilityEnd = availabilityEnd;
        this.price = price;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoOfPeople() {
        return noOfPeople;
    }

    public void setNoOfPeople(int noOfPeople) {
        this.noOfPeople = noOfPeople;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public float getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getNoOfReviews() {
        return noOfReviews;
    }

    public void setNoOfReviews(int noOfReviews) {
        this.noOfReviews = noOfReviews;
    }

    public String getRoomImagePath() {
        return roomImagePath;
    }

    public void setRoomImagePath(String roomImagePath) {
        this.roomImagePath = roomImagePath;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public LocalDate getAvailabilityStart() {
        return availabilityStart;
    }

    public void setAvailabilityStart(LocalDate availabilityStart) {
        this.availabilityStart = availabilityStart;
    }

    public LocalDate getAvailabilityEnd() {
        return availabilityEnd;
    }

    public void setAvailabilityEnd(LocalDate availabilityEnd) {
        this.availabilityEnd = availabilityEnd;
    }

    public ArrayList<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(ArrayList<Booking> bookings) {
        this.bookings = bookings;
    }

    public int getWorkerID() {
        return workerID;
    }

    public void setWorkerID(int workerID) {
        this.workerID = workerID;
    }

    public byte[] getRoomImage() {
        return roomImage;
    }

    public void setRoomImage(byte[] roomImage) {
        this.roomImage = roomImage;
    }

    /**
     * method that adds a review for the specific room and updates the review average and number of reviews
     * @param newReview the new review to be added
     */
    public synchronized void addReview(int newReview) {
        // Calculate the new average reviews
        float newStars = ((stars * noOfReviews) + newReview) / (noOfReviews + 1);
        // Update the number of reviews
        noOfReviews++;
        // Update the average reviews
        stars = newStars;

        // round stars to 2 decimals
        BigDecimal rounded = new BigDecimal(stars).setScale(2, RoundingMode.HALF_UP);
        stars = rounded.floatValue();
    }


    @Override
    public String toString() {
        return "Name: " + this.getName() + '\n' +
                "Area: " + this.getArea() + '\n' +
                "Number of people: " + this.getNoOfPeople() +'\n' +
                "Stars: " + this.getStars() + '\n' +
                "Number of Reviews: " + this.getNoOfReviews() + '\n' +
                "Price: " + this.getPrice() + '\n' +
                "Room Image Path: " + this.getRoomImagePath() + '\n' +
                "Available from " + this.getAvailabilityStart() + " to " + this.getAvailabilityEnd();
    }

    @Override
    public boolean equals(Object obj) {
        Room room = (Room) obj;
        return this.name.equals(room.name) &&
                noOfPeople == room.noOfPeople &&
                this.area.equals(room.area) &&
                stars == room.stars &&
                noOfReviews == room.noOfReviews &&
                this.roomImagePath.equals(room.roomImagePath) &&
                this.price == room.price &&
                this.availabilityStart.equals(room.availabilityStart) &&
                this.availabilityEnd.equals(room.availabilityEnd);
    }
}
