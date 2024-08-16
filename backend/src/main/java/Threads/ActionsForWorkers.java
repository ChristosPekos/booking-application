package Threads;

import Data.Booking;
import Data.Filter;
import Data.Room;
import Servers.Worker;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Properties;


/**
 * Thread class handling the requests for the workers
 */
public class ActionsForWorkers extends Thread{
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    private final Worker worker;
    private final String requestType;
    public ActionsForWorkers(ObjectOutputStream out,ObjectInputStream in, Worker worker,String requestType){
            this.worker = worker;
            this.requestType = requestType;
            outputStream = out;
            inputStream = in;
    }

    /**
     * method for saving a room image in a specific path
     * @param fileName the name of the image file
     * @param imageData the data of the image in a byte array form
     */
    private void saveImageToFile(String fileName, byte[] imageData) {
        try {
            // Create a FileOutputStream to write to the file
            FileOutputStream fileOutputStream = new FileOutputStream("././././images/"+ fileName);

            // Write the image data to the file
            fileOutputStream.write(imageData);

            // Close the output stream
            fileOutputStream.close();

            System.out.println("New room image " + fileName + " saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method for handling the requests for saving a room to the worker
     */
    public void saveRoom(){
        try {
            int workerID = inputStream.readInt();
            Room room = (Room) inputStream.readObject();
            room.setWorkerID(workerID);

            if(workerID == worker.getWorkerID()){
                // add to main list
                synchronized (worker.roomsToRent){
                    worker.roomsToRent.add(room);
                }
            }else {
                // add to replica
                synchronized (worker.roomReplicas){
                    if(worker.roomReplicas.get(workerID)==null){
                        worker.roomReplicas.put(workerID,new ArrayList<Room>());
                    }
                    worker.roomReplicas.get(workerID).add(room);
                }
            }

            System.out.println("New room has been added");

            int imageFileNameLength = inputStream.readInt();

            if(imageFileNameLength>0) {
                // image length sent
                byte[] imageFileNameBytes = new byte[imageFileNameLength];
                inputStream.readFully(imageFileNameBytes, 0, imageFileNameLength);

                String imageFileName = new String(imageFileNameBytes);

                int imageFileContentLength = inputStream.readInt();

                if (imageFileContentLength > 0) {
                    // image content length sent
                    byte[] imageFileContentBytes = new byte[imageFileContentLength];
                    inputStream.readFully(imageFileContentBytes, 0, imageFileContentLength);

                    // Save the image content to a file
                    saveImageToFile(imageFileName, imageFileContentBytes);
                }

            }

            // Send acknowledgment back to the sender
            outputStream.writeUTF("ACK");
            outputStream.flush();


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

    /**
     * method for handling the requests for filtering the rooms that agree on some filtering criteria and then sending these rooms to the reducer
     */
    public void filterRooms() {
        Socket reducerSocket = null;
        ObjectOutputStream outputToReducer = null;
        ArrayList<Room> roomsFiltered = new ArrayList<>();
        ArrayList<Room> roomsToRent = new ArrayList<>();
        try {
            // read map id and filter from master
            int mapID = inputStream.readInt();
            Filter filter = (Filter) inputStream.readObject();

            LocalDate filterFirstDate = filter.getFirstDate();
            LocalDate filterLastDate = filter.getLastDate();

            boolean replicationNeeded = inputStream.readBoolean();

            int idOfFailedWorker = inputStream.readInt();

            // Send acknowledgment back to the sender
            outputStream.writeUTF("ACK");
            outputStream.flush();

            if(replicationNeeded){
                roomsToRent = worker.roomReplicas.get(idOfFailedWorker);
            } else{
                roomsToRent = worker.roomsToRent;
            }


            if(roomsToRent!=null){
                for(Room roomToRent: roomsToRent){

                    LocalDate availabilityStart = roomToRent.getAvailabilityStart();
                    LocalDate availabilityEnd= roomToRent.getAvailabilityEnd();

                    // check availability
                    boolean dateAvailability  = false;
                    if(!filterFirstDate.isBefore(availabilityStart) && !filterLastDate.isAfter(availabilityEnd)){
                        dateAvailability = true;
                    }

                    if(dateAvailability){
                        if (roomToRent.getPrice() <= filter.getMaxPrice() && roomToRent.getPrice() >= filter.getMinPrice() && roomToRent.getArea().equalsIgnoreCase(filter.getArea()) && roomToRent.getStars() >= filter.getStars() ) {
                            if(filter.isPeopleNoFilter()){
                                if(roomToRent.getNoOfPeople() == filter.getNoOfPeople()){
                                    FileInputStream fileInputStream = new FileInputStream( "././././" + roomToRent.getRoomImagePath());
                                    byte[] image = fileInputStream.readAllBytes();
                                    fileInputStream.close();
                                    roomToRent.setRoomImage(image);
                                    roomsFiltered.add(roomToRent);
                                }
                            }else {
                                FileInputStream fileInputStream = new FileInputStream( "././././" + roomToRent.getRoomImagePath());
                                byte[] image = fileInputStream.readAllBytes();
                                fileInputStream.close();
                                roomToRent.setRoomImage(image);
                                roomsFiltered.add(roomToRent);
                            }
                        }
                    }
                }
            }



            // send filters to reducer
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            String reducerIP = appProps.getProperty("REDUCER_IP");
            int reducerPort = Integer.parseInt(appProps.getProperty("REDUCER_PORT"));

            reducerSocket = new Socket(reducerIP, reducerPort);
            outputToReducer = new ObjectOutputStream(reducerSocket.getOutputStream());

            // send map id to reducer
            outputToReducer.writeInt(mapID);
            outputToReducer.flush();

            // send filtered rooms to reducer
            outputToReducer.writeObject(roomsFiltered);
            outputToReducer.flush();



        } catch (IOException | NumberFormatException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if(reducerSocket!=null){
                    reducerSocket.close();
                    outputToReducer.close();
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * method for handling the requests for booking a room
     */
    public synchronized void bookRoom(){

        try {
            Booking receivedBooking = (Booking)inputStream.readObject();
            Room roomToBook = receivedBooking.getRoom();
            int workerID = inputStream.readInt();


            ArrayList<Booking> bookingsOfRoom = new ArrayList<>();
            if(workerID==worker.getWorkerID()){
                int roomIndex = worker.getRoomsToRent().indexOf(roomToBook);
                bookingsOfRoom = worker.getRoomsToRent().get(roomIndex).getBookings();
            }else {
                int roomIndex = worker.roomReplicas.get(workerID).indexOf(roomToBook);
                bookingsOfRoom = worker.roomReplicas.get(workerID).get(roomIndex).getBookings();
            }

            boolean canBeBooked = true;
            boolean isWithinAvailability = !receivedBooking.getFirstDate().isBefore(roomToBook.getAvailabilityStart()) && !receivedBooking.getLastDate().isAfter(roomToBook.getAvailabilityEnd());
            if (isWithinAvailability) {
                for (Booking booking : bookingsOfRoom){
                    boolean datesOverlap = !receivedBooking.getFirstDate().isAfter(booking.getLastDate()) && !receivedBooking.getLastDate().isBefore(booking.getFirstDate());
                    if (datesOverlap) {
                        canBeBooked = false;
                        break;
                    }
                }
            }else {
                canBeBooked = false;
            }

            if (canBeBooked) {
                bookingsOfRoom.add(receivedBooking);
                outputStream.writeUTF("The booking was successful.");
                outputStream.flush();
            }else{
                outputStream.writeUTF("The dates that you chose where not available.");
                outputStream.flush();
            }

            // Wait for acknowledgment from worker
            String acknowledgment = inputStream.readUTF();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }

    /**
     * method for handling the requests for returning the bookings of the manager's rooms, based on some filters or not
     */
    public void getBookings(){
        LocalDate firstDate = null;
        LocalDate lastDate = null;
        String area = null;
        int workerID = 0;
        try {
            workerID = inputStream.readInt();

            //read dates and area in case of aggregation query
            firstDate = (LocalDate) inputStream.readObject();
            lastDate = (LocalDate) inputStream.readObject();
            area = inputStream.readUTF();



        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<Room> bookedRooms = new ArrayList<>();
        if (workerID == worker.getWorkerID()) {
            bookedRooms = worker.getBookedRooms();
        } else {
            bookedRooms = worker.roomReplicas.get(workerID);
        }

        ArrayList<Booking> bookings = new ArrayList<>();


        boolean checkDates = firstDate != null;
        boolean checkArea = !area.isEmpty();

        if(bookedRooms!=null){
            if(checkDates || checkArea ){
                for (int i = 0; i <= bookedRooms.size() - 1; i++) {
                    for (Booking booking : bookedRooms.get(i).getBookings()) {
                        if (checkDates && checkArea) {
                            if (booking.getRoom().getArea().equalsIgnoreCase(area) && (!booking.getFirstDate().isBefore(firstDate) && !booking.getLastDate().isAfter(lastDate)))
                                bookings.add(booking);

                        } else if (checkDates && !checkArea) {

                            if (!booking.getFirstDate().isBefore(firstDate) && !booking.getLastDate().isAfter(lastDate))
                                bookings.add(booking);

                        } else if (checkArea && !checkDates) {
                            if ((booking.getRoom().getArea().equalsIgnoreCase(area)))
                                bookings.add(booking);
                        }
                    }
                }
            }
            else{
                for (int i = 0; i <= bookedRooms.size() - 1; i++) {
                    bookings.addAll(bookedRooms.get(i).getBookings());
                }
            }
        }



        try {
            outputStream.writeObject(bookings);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Booked rooms have been sent");
    }

    /**
     * method for handling the requests for saving a review to the worker
     */
    public void saveReview(){
        try {
            Room roomToReview = (Room) inputStream.readObject();
            int review = inputStream.readInt();
            int idOfMainWorker = roomToReview.getWorkerID();

            // Send acknowledgment back to the sender
            outputStream.writeUTF("ACK");
            outputStream.flush();


            //save review for main worker
            if (idOfMainWorker == worker.getWorkerID()) {
                ArrayList<Room> roomsToRent = worker.getRoomsToRent();

                for (Room room : roomsToRent) {
                    if (room.equals(roomToReview)) {
                        room.addReview(review);
                    }
                }
                worker.setRoomsToRent(roomsToRent);
            } else {
                //save review for replicas
                ArrayList<Room> roomsToRentReplicas = worker.roomReplicas.get(idOfMainWorker);
                for (Room room : roomsToRentReplicas) {
                    if (room.equals(roomToReview))
                        room.addReview(review);
                }
                worker.roomReplicas.replace(idOfMainWorker, roomsToRentReplicas);
            }


            System.out.println("New review has been added. Room stars have been updated");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * method for handling the requests for getting manager's rooms and sending it back to the master, which in turn gets sent back to the manager
     */
    public void getRooms(){
        int workerID = 0;
        try {
            workerID = inputStream.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Room> rooms;
        if (workerID == worker.getWorkerID()) {
            rooms = worker.getRoomsToRent();
        } else {
            rooms = worker.roomReplicas.get(workerID);
        }

        try {
            outputStream.writeObject(rooms);
            outputStream.flush();

            String ack = inputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Rooms have been sent");
    }

    /**
     * method for handling the requests for checking if the current worker is up and running
     */
    public void isUp(){
        try {
            outputStream.writeUTF("ACK");
            outputStream.flush();

            String acknowledgment = inputStream.readUTF();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void run() {
        switch (this.requestType) {
            case "SAVE_ROOM" -> saveRoom();
            case "GET_ROOMS" -> getRooms();
            case "GET_BOOKINGS" -> getBookings();
            case "FILTER_ROOMS" -> filterRooms();
            case "SAVE_REVIEW" -> saveReview();
            case "CHECK_BOOK_ROOM" -> bookRoom();
            case "IS_UP" -> isUp();
        }
    }
}
