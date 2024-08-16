package Threads;

import Data.Booking;
import Data.Room;
import Servers.Master;
import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Properties;


/**
 * Thread class handling the requests from the managers
 */
public class ActionsForManagers extends Thread{
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    private final String requestType;
    public ActionsForManagers(ObjectInputStream in,ObjectOutputStream out, String requestType){
        this.requestType = requestType;
        outputStream = out;
        inputStream = in;

    }

    /**
     * has function determining in which worker should a room be saved
     * @param roomName the name of the room to be added
     * @return the id of the worker in which the room should be saved
     */
    public int hash(String roomName){
        return Math.abs(roomName.hashCode());
    }

    /**
     * method for handling the requests for adding rooms
     */
    public void addRoom(){
        JSONObject roomJson;
        Socket socket = null;
        ObjectOutputStream outputStreamWorker = null;
        ObjectInputStream inputStreamWorker = null;
        try {
            String roomJsonString = (String) inputStream.readObject();
            roomJson = new JSONObject(roomJsonString);
            String name = roomJson.getString("roomName");
            int noOfPeople = roomJson.getInt("noOfPersons");
            String area = roomJson.getString("area");
            float stars = roomJson.getFloat("stars");
            int noOfReviews = roomJson.getInt("noOfReviews");
            String roomImagePath = roomJson.getString("roomImage");
            int price = roomJson.getInt("price");
            LocalDate availabilityStart = LocalDate.parse((String) roomJson.get("startDate"));
            LocalDate availabilityEnd = LocalDate.parse((String) roomJson.get("endDate"));
            Room room = new Room(name,noOfPeople,area,stars,noOfReviews,roomImagePath,availabilityStart,availabilityEnd,price);

            int imageFileNameLength = inputStream.readInt();

            if(imageFileNameLength>0) {
                // image length sent
                byte[] imageFileNameBytes = new byte[imageFileNameLength];
                inputStream.readFully(imageFileNameBytes, 0, imageFileNameLength);

                String imageFileName = new String(imageFileNameBytes);

                int imageFileContentLength = inputStream.readInt();

                byte[] imageFileContentBytes = new byte[imageFileContentLength];
                if (imageFileContentLength > 0) {
                    // image content length sent
                    imageFileContentBytes = new byte[imageFileContentLength];
                    inputStream.readFully(imageFileContentBytes, 0, imageFileContentLength);

                }



                Properties appProps = new Properties();
                appProps.load(new FileInputStream("././././config.properties"));
                int workersNumber = Integer.parseInt(appProps.getProperty("WORKERS"));

                // select worker
                int hashResult = hash(roomJson.getString("roomName"));
                int workerId = (hashResult % workersNumber) + 1;
                System.out.println("Worker id: " + workerId + " hash result: " + hashResult + " workersNumber: " + workersNumber);

                //connect to worker
                String ip = appProps.getProperty("WORKER" + workerId + "_IP");
                int port = Integer.parseInt(appProps.getProperty("WORKER" + workerId + "_PORT"));

                boolean isUp = Master.getHealthCheck(ip,port);
                System.out.println("Health check: " + isUp);

                if(isUp){
                    socket = new Socket(ip, port);

                    outputStreamWorker = new ObjectOutputStream(socket.getOutputStream());
                    inputStreamWorker = new ObjectInputStream(socket.getInputStream());

                    outputStreamWorker.writeUTF("SAVE_ROOM");
                    outputStreamWorker.flush();

                    outputStreamWorker.writeInt(workerId);
                    outputStreamWorker.flush();

                    outputStreamWorker.writeObject(room);
                    outputStreamWorker.flush();

                    outputStreamWorker.writeInt(imageFileNameLength);
                    outputStreamWorker.flush();

                    outputStreamWorker.write(imageFileNameBytes);
                    outputStreamWorker.flush();

                    outputStreamWorker.writeInt(imageFileContentLength);
                    outputStreamWorker.flush();

                    outputStreamWorker.write(imageFileContentBytes);
                    outputStreamWorker.flush();

                    System.out.println("SAVE_ROOM request has been sent to the Worker " + workerId);

                    // Wait for acknowledgment from worker
                    String acknowledgment = inputStreamWorker.readUTF();
                }

                // send request in replicas
                for (int i =1;i<=workersNumber;i++){

                    //connect to worker
                    String workerIP = appProps.getProperty("WORKER" + i + "_IP");
                    int workerPort = Integer.parseInt(appProps.getProperty("WORKER" + i + "_PORT"));

                    if(i==workerId){
                        continue;
                    }


                    isUp = Master.getHealthCheck(workerIP,workerPort);

                    if(!isUp){
                        continue;
                    }

                    socket = new Socket(workerIP, workerPort);

                    outputStreamWorker = new ObjectOutputStream(socket.getOutputStream());
                    inputStreamWorker = new ObjectInputStream(socket.getInputStream());

                    outputStreamWorker.writeUTF("SAVE_ROOM");
                    outputStreamWorker.flush();

                    outputStreamWorker.writeInt(workerId);
                    outputStreamWorker.flush();

                    outputStreamWorker.writeObject(room);
                    outputStreamWorker.flush();

                    outputStreamWorker.writeInt(imageFileNameLength);
                    outputStreamWorker.flush();

                    outputStreamWorker.write(imageFileNameBytes);
                    outputStreamWorker.flush();

                    outputStreamWorker.writeInt(imageFileContentLength);
                    outputStreamWorker.flush();

                    outputStreamWorker.write(imageFileContentBytes);
                    outputStreamWorker.flush();

                    System.out.println("SAVE_ROOM request has been sent to replica Worker " + i);

                    // Wait for acknowledgment from worker
                    String acknowledgment = inputStreamWorker.readUTF();

                }
            }

        } catch (IOException | ClassNotFoundException e ) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStreamWorker != null) {
                    outputStreamWorker.close();
                }
                if (inputStreamWorker != null) {
                    inputStreamWorker.close();
                }
                if (socket != null) {
                    socket.close();
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
    }


    /**
     * method for handling the requests for returning the bookings of manager's rooms
     */
    public void showBookings(){
        Socket workerSocket = null;
        ObjectOutputStream outputStream2 = null;
        ObjectInputStream inputStream2 = null;
        try {
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            int workersNumber = Integer.parseInt(appProps.getProperty("WORKERS"));

            ArrayList<Booking> bookings = new ArrayList<>();

            LocalDate firstDate = (LocalDate) inputStream.readObject();

            LocalDate lastDate = (LocalDate) inputStream.readObject();

            String area = inputStream.readUTF();

            for (int i = 1;i<=workersNumber;i++){
                String workerIP = appProps.getProperty("WORKER" + i + "_IP");
                int workerPort =  Integer.parseInt(appProps.getProperty("WORKER" + i + "_PORT"));

                boolean isUP = Master.getHealthCheck(workerIP,workerPort);

                if(isUP){
                    // connect to worker
                    workerSocket = new Socket(workerIP,workerPort);
                    outputStream2 = new ObjectOutputStream(workerSocket.getOutputStream());
                    inputStream2 = new ObjectInputStream(workerSocket.getInputStream());

                    outputStream2.writeUTF("GET_BOOKINGS");
                    outputStream2.flush();

                    // send worker ID
                    outputStream2.writeInt(i);
                    outputStream2.flush();

                    outputStream2.writeObject(firstDate);
                    outputStream2.flush();

                    outputStream2.writeObject(lastDate);
                    outputStream2.flush();

                    outputStream2.writeUTF(area);
                    outputStream2.flush();

                    System.out.println("GET_BOOKINGS request has been sent to Worker " + i);
                    bookings.addAll((ArrayList<Booking>)inputStream2.readObject());
                }else {
                    for (int j = 1;j<=workersNumber;j++) {
                        workerIP = appProps.getProperty("WORKER" + j + "_IP");
                        workerPort = Integer.parseInt(appProps.getProperty("WORKER" + j + "_PORT"));

                        if(i==j){
                            continue;
                        }

                        isUP = Master.getHealthCheck(workerIP, workerPort);

                        if (isUP) {
                            // connect to worker
                            workerSocket = new Socket(workerIP, workerPort);
                            outputStream2 = new ObjectOutputStream(workerSocket.getOutputStream());
                            inputStream2 = new ObjectInputStream(workerSocket.getInputStream());

                            outputStream2.writeUTF("GET_BOOKINGS");
                            outputStream2.flush();

                            // send worker ID
                            outputStream2.writeInt(i);
                            outputStream2.flush();

                            outputStream2.writeObject(firstDate);
                            outputStream2.flush();

                            outputStream2.writeObject(lastDate);
                            outputStream2.flush();

                            outputStream2.writeUTF(area);
                            outputStream2.flush();

                            System.out.println("GET_BOOKINGS request has been sent to replica Worker " + j);
                            bookings.addAll((ArrayList<Booking>) inputStream2.readObject());
                            break;
                        }
                    }
                }
            }


            outputStream.writeObject(bookings);
            outputStream.flush();


            System.out.println("Received bookings have been sent back to the Manager");

            // Wait for acknowledgment from worker
            String acknowledgment = inputStream.readUTF();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(workerSocket!=null){
                    workerSocket.close();
                    outputStream2.close();
                    inputStream2.close();
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
    }


    /**
     * method for handling the requests for returning the rooms of the manager
     */
    public void showRooms(){
        Socket workerSocket = null;
        ObjectOutputStream outputStream2 = null;
        ObjectInputStream inputStream2 = null;
        try {
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            int workersNumber = Integer.parseInt(appProps.getProperty("WORKERS"));

            ArrayList<Room> rooms = new ArrayList<>();
            for (int i = 1;i<=workersNumber;i++){
                String workerIP = appProps.getProperty("WORKER" + i + "_IP");
                int workerPort =  Integer.parseInt(appProps.getProperty("WORKER" + i + "_PORT"));

                boolean isUP = Master.getHealthCheck(workerIP,workerPort);

                if(isUP){
                    // connect to worker
                    workerSocket = new Socket(workerIP,workerPort);
                    outputStream2 = new ObjectOutputStream(workerSocket.getOutputStream());
                    inputStream2 = new ObjectInputStream(workerSocket.getInputStream());

                    outputStream2.writeUTF("GET_ROOMS");
                    outputStream2.flush();

                    outputStream2.writeInt(i);
                    outputStream2.flush();

                    System.out.println("GET_ROOMS request has been sent to Worker " + i);
                    rooms.addAll((ArrayList<Room>)inputStream2.readObject());
                }else {
                    for (int j=1;j<=workersNumber;j++){
                        workerIP = appProps.getProperty("WORKER" + j + "_IP");
                        workerPort =  Integer.parseInt(appProps.getProperty("WORKER" + j + "_PORT"));

                        if(i==j){
                            continue;
                        }

                        isUP = Master.getHealthCheck(workerIP,workerPort);

                        if(isUP){
                            // connect to replica worker
                            workerSocket = new Socket(workerIP,workerPort);
                            outputStream2 = new ObjectOutputStream(workerSocket.getOutputStream());
                            inputStream2 = new ObjectInputStream(workerSocket.getInputStream());

                            outputStream2.writeUTF("GET_ROOMS");
                            outputStream2.flush();

                            outputStream2.writeInt(i);
                            outputStream2.flush();

                            System.out.println("GET_ROOMS request has been sent to replica Worker " + j);
                            ArrayList<Room> receivedRooms = (ArrayList<Room>)inputStream2.readObject();
                            if(receivedRooms!=null){
                                rooms.addAll(receivedRooms);
                            }
                            break;
                        }
                    }
                }
                outputStream2.writeUTF("ACK");
                outputStream2.flush();
            }

            outputStream.writeObject(rooms);
            outputStream.flush();
            System.out.println("Received rooms have been sent back to the Manager");

            // Wait for acknowledgment from worker
            String acknowledgment = inputStream.readUTF();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(workerSocket!=null){
                    workerSocket.close();
                    outputStream2.close();
                    inputStream2.close();
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        switch (requestType) {
            case "MANAGER/ADD_ROOM" -> addRoom();
            case "MANAGER/SHOW_BOOKINGS" -> showBookings();
            case "MANAGER/SHOW_ROOMS" -> showRooms();
        }
    }
}

