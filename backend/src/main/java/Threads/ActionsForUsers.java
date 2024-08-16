package Threads;

import Data.Booking;
import Data.Filter;
import Data.Room;
import Data.UserSendTask;
import Servers.Master;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Thread class handling the requests from the users
 */
public class ActionsForUsers extends Thread {
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    private final String requestType;
    private Socket connection;
    private ServerSocket reducerServerSocket;
    private int requestMapID;

    public ActionsForUsers(ObjectInputStream in,ObjectOutputStream out, String requestType){
        this.requestType = requestType;
        outputStream = out;
        inputStream = in;
    }

    public ActionsForUsers(ObjectInputStream in,ObjectOutputStream out, Socket connection ,ServerSocket reducerServerSocket,int mapID, String requestType){
        this.requestType = requestType;
        outputStream = out;
        inputStream = in;
        this.connection = connection;
        this.reducerServerSocket = reducerServerSocket;
        this.requestMapID = mapID;
    }


    /**
     * method for handling the requests for searching the rooms based on some filters
     */
    public void roomSearch() {
        ObjectOutputStream outputToWorkers = null;
        ObjectInputStream inputToWorkers = null;
        Socket reducerConnection = null;
        Socket socket = null;
        try {
            Filter filters = (Filter) inputStream.readObject();

            //send the map to the workers
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            int workersNumber = Integer.parseInt(appProps.getProperty("WORKERS"));

            for (int i = 1;i<=workersNumber;i++) {

                String workerIP = appProps.getProperty("WORKER" + i + "_IP");
                int workerPort = Integer.parseInt(appProps.getProperty("WORKER" + i + "_PORT"));

                boolean isUP = Master.getHealthCheck(workerIP,workerPort);

                if(isUP) {
                    //initializing socket and output stream
                    socket = new Socket(workerIP, workerPort);
                    outputToWorkers = new ObjectOutputStream(socket.getOutputStream());
                    inputToWorkers = new ObjectInputStream(socket.getInputStream());

                    //commanding the worker to filter his saved rooms
                    outputToWorkers.writeUTF("FILTER_ROOMS");
                    outputToWorkers.flush();

                    // send mapID to worker
                    outputToWorkers.writeInt(Master.getMapID());
                    outputToWorkers.flush();

                    // send filters to worker
                    outputToWorkers.writeObject(filters);
                    outputToWorkers.flush();

                    //send boolean to confirm that active replication is needed
                    outputToWorkers.writeBoolean(false);
                    outputToWorkers.flush();

                    //send the id of worker to get rooms
                    outputToWorkers.writeInt(i);
                    outputToWorkers.flush();

                    // Wait for acknowledgment from worker
                    String acknowledgment = inputToWorkers.readUTF();
                }
                else{

                    for (int j=1;j<=workersNumber;j++){
                        workerIP = appProps.getProperty("WORKER" + j + "_IP");
                        workerPort = Integer.parseInt(appProps.getProperty("WORKER" + j + "_PORT"));

                        if(i==j){
                            continue;
                        }

                        isUP = Master.getHealthCheck(workerIP,workerPort);

                        if(isUP){
                            //initializing socket and output stream
                            socket = new Socket(workerIP, workerPort);
                            outputToWorkers = new ObjectOutputStream(socket.getOutputStream());
                            inputToWorkers = new ObjectInputStream(socket.getInputStream());

                            //commanding the worker to filter his saved rooms
                            outputToWorkers.writeUTF("FILTER_ROOMS");
                            outputToWorkers.flush();

                            // send mapID to worker
                            outputToWorkers.writeInt(Master.getMapID());
                            outputToWorkers.flush();

                            // send filters to worker
                            outputToWorkers.writeObject(filters);
                            outputToWorkers.flush();


                            //send boolean to confirm that active replication is needed
                            outputToWorkers.writeBoolean(true);
                            outputToWorkers.flush();

                            //send the id of worker to get rooms
                            outputToWorkers.writeInt(i);
                            outputToWorkers.flush();

                            // Wait for acknowledgment from worker
                            String acknowledgment = inputToWorkers.readUTF();

                            break;
                        }

                    }


                }
            }


            // listen to port for reducer connection
            reducerConnection = reducerServerSocket.accept();
            ObjectInputStream reducerInputStream = new ObjectInputStream(reducerConnection.getInputStream());
            ObjectOutputStream reducerOutputStream = new ObjectOutputStream(reducerConnection.getOutputStream());

            int mapID = reducerInputStream.readInt();
            ArrayList<Room> rooms = (ArrayList<Room>) reducerInputStream.readObject();

            // Send acknowledgment back to the sender
            reducerOutputStream.writeUTF("ACK");
            reducerOutputStream.flush();


            if(this.requestMapID == mapID){
                outputStream.writeObject(rooms);
                outputStream.flush();
            }else {
                // unmatched mapID
                UserSendTask myUserSendTask = new UserSendTask(this.requestMapID,outputStream,null);
                UserSendTask userSendTask = new UserSendTask(mapID,null,rooms);
                Master.getUserSendTasks().add(userSendTask);

                while (true){
                    if(Master.getUserSendTasks().contains(myUserSendTask)){
                        int index = Master.getUserSendTasks().indexOf(myUserSendTask);
                        outputStream.writeObject(Master.getUserSendTasks().get(index).getRooms());
                        Master.getUserSendTasks().remove(index);
                        break;
                    }
                }
            }

            // Wait for acknowledgment from user
            String acknowledgment = inputStream.readUTF();

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(socket!=null){
                    socket.close();
                    outputToWorkers.close();
                }
                if(reducerConnection!=null){
                    reducerConnection.close();
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * method for handling the requests for booking a room
     */
    public void bookRoom() {
        ObjectOutputStream outputStream2 = null;
        ObjectInputStream inputStream2 = null;
        Socket workerSocket = null;
        try {
            Booking booking = (Booking) inputStream.readObject();
            Room receivedRoom = booking.getRoom();

            int workerID = receivedRoom.getWorkerID();

            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            int workerPort = Integer.parseInt(appProps.getProperty("WORKER" + workerID + "_PORT"));
            String workerIP = appProps.getProperty("WORKER" + workerID + "_IP");

            boolean isUP = Master.getHealthCheck(workerIP,workerPort);
            boolean mainWorkerIsUP = isUP;

            if(isUP){
                // connect to worker
                workerSocket = new Socket(workerIP,workerPort);
                outputStream2 = new ObjectOutputStream(workerSocket.getOutputStream());
                inputStream2 = new ObjectInputStream(workerSocket.getInputStream());

                outputStream2.writeUTF("CHECK_BOOK_ROOM");
                outputStream2.flush();

                outputStream2.writeObject(booking);
                outputStream2.flush();

                outputStream2.writeInt(workerID);
                outputStream2.flush();

                String confirmation = inputStream2.readUTF();

                // Send acknowledgment back to the sender
                outputStream2.writeUTF("ACK");
                outputStream2.flush();

                outputStream.writeUTF(confirmation);
                outputStream.flush();

                // Wait for acknowledgment from user
                String acknowledgment = inputStream.readUTF();
            }

            // book the room in the replica workers
            int workersNumber = Integer.parseInt(appProps.getProperty("WORKERS"));
            int timesSentConfirmation = 0;
            for (int i=1;i<=workersNumber;i++){
                workerPort = Integer.parseInt(appProps.getProperty("WORKER" + i + "_PORT"));
                workerIP = appProps.getProperty("WORKER" + i + "_IP");

                if(i==workerID){
                    continue;
                }

                isUP = Master.getHealthCheck(workerIP,workerPort);

                if(isUP){
                    // connect to replica worker
                    workerSocket = new Socket(workerIP,workerPort);
                    outputStream2 = new ObjectOutputStream(workerSocket.getOutputStream());
                    inputStream2 = new ObjectInputStream(workerSocket.getInputStream());

                    outputStream2.writeUTF("CHECK_BOOK_ROOM");
                    outputStream2.flush();

                    outputStream2.writeObject(booking);
                    outputStream2.flush();

                    outputStream2.writeInt(workerID);
                    outputStream2.flush();

                    String confirmation = inputStream2.readUTF();

                    // Send acknowledgment back to the sender
                    outputStream2.writeUTF("ACK");
                    outputStream2.flush();

                    if(!mainWorkerIsUP && timesSentConfirmation == 0){
                        timesSentConfirmation++;
                        outputStream.writeUTF(confirmation);
                        outputStream.flush();

                        // Wait for acknowledgment from user
                        String acknowledgment = inputStream.readUTF();
                    }
                }
            }
            
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * method for handling the requests for adding a review
     */
    public void addReview() {
        ObjectOutputStream outputStreamWorker = null;
        ObjectInputStream inputToWorker = null;
        Socket socket = null;
        try {
            Room room = (Room) inputStream.readObject();
            int review = inputStream.readInt();

            // Send acknowledgment back to the sender
            outputStream.writeUTF("ACK");
            outputStream.flush();

            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            int port = Integer.parseInt(appProps.getProperty("WORKER" + room.getWorkerID() + "_PORT"));
            String ip = appProps.getProperty("WORKER" + room.getWorkerID() + "_IP");
            int workersNumber = Integer.parseInt(appProps.getProperty("WORKERS"));

            boolean isUp = Master.getHealthCheck(ip,port);

            if(isUp) {

                socket = new Socket(ip, port);
                outputStreamWorker = new ObjectOutputStream(socket.getOutputStream());
                inputToWorker = new ObjectInputStream(socket.getInputStream());


                outputStreamWorker.writeUTF("SAVE_REVIEW");
                outputStreamWorker.flush();
                System.out.println("SAVE_REVIEW request has been sent to worker " + room.getWorkerID());

                outputStreamWorker.writeObject(room);
                outputStreamWorker.flush();

                outputStreamWorker.writeInt(review);
                outputStreamWorker.flush();

                // Wait for acknowledgment from worker
                String acknowledgment = inputToWorker.readUTF();
            }

            // send request in replicas
            for (int i =1;i<=workersNumber;i++) {
                //connect to worker
                String workerIP = appProps.getProperty("WORKER" + i + "_IP");
                int workerPort = Integer.parseInt(appProps.getProperty("WORKER" + i + "_PORT"));

                if(i==room.getWorkerID()){
                    continue;
                }

                isUp = Master.getHealthCheck(workerIP, workerPort);

                if (!isUp) {
                    continue;
                }


                socket = new Socket(workerIP, workerPort);
                outputStreamWorker = new ObjectOutputStream(socket.getOutputStream());
                inputToWorker = new ObjectInputStream(socket.getInputStream());


                outputStreamWorker.writeUTF("SAVE_REVIEW");
                outputStreamWorker.flush();
                System.out.println("SAVE_REVIEW request has been sent to worker " + i);

                outputStreamWorker.writeObject(room);
                outputStreamWorker.flush();

                outputStreamWorker.writeInt(review);
                outputStreamWorker.flush();

                // Wait for acknowledgment from worker
                String acknowledgment = inputToWorker.readUTF();


            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(socket!=null){
                    socket.close();
                    outputStreamWorker.close();
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        switch (requestType) {
            case "ROOM_SEARCH" -> roomSearch();
            case "BOOK_ROOM" -> bookRoom();
            case "ADD_REVIEW" -> addReview();
        }
    }
}
