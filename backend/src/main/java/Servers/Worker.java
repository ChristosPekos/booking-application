package Servers;

import Data.Room;
import Threads.ActionsForWorkers;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * class for the Workers that save the rooms
 */
public class Worker{
    ServerSocket serverSocket;
    Socket connection = null;
    private int workerID;
    public ArrayList<Room> roomsToRent = new ArrayList<>();
    public HashMap<Integer,ArrayList<Room>> roomReplicas = new HashMap<>();

    /**
     * method for returning the local ip of the worker
     * @return the  local ip of the worker
     * @throws IOException
     */
    public static String getLocalIP() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
        }
    }


    /**
     * methods for opening the worker server, including its main functionality
     * @param workerNumber the number of the worker that will be running
     */
    public void openServer(int workerNumber){
        try {
            if(workerNumber==-1){
                Properties appProps = new Properties();
                appProps.load(new FileInputStream("././././config.properties"));

                //set worker id
                String localIP = getLocalIP();
                int workersNumber = Integer.parseInt(appProps.getProperty("WORKERS"));

                for (int i=1;i<=workersNumber;i++){
                    String workerIP = appProps.getProperty("WORKER" + i + "_IP");
                    if (localIP.equals(workerIP)){
                        setWorkerID(i);
                        break;
                    }
                }

                int workerPort = Integer.parseInt(appProps.getProperty("WORKER" + getWorkerID() + "_PORT"));
                serverSocket = new ServerSocket(workerPort);
                System.out.println("Worker is running..");

            }else {
                setWorkerID(workerNumber);

                Properties appProps = new Properties();
                appProps.load(new FileInputStream("././././config.properties"));

                int workerPort = Integer.parseInt(appProps.getProperty("WORKER" + workerNumber + "_PORT"));
                serverSocket = new ServerSocket(workerPort);
                System.out.println("Worker is running..");
            }



            while (true){
                System.out.println("Waiting for new connection..");
                connection = serverSocket.accept();
                System.out.println("New connection established");
                ObjectInputStream inputStream = new ObjectInputStream(connection.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(connection.getOutputStream());
                String requestType = inputStream.readUTF();
                System.out.println("New request of type " + requestType + " is being processed on a new thread");

                switch (requestType){
                    case "SAVE_ROOM":
                        Thread workerThread = new ActionsForWorkers(outputStream,inputStream,this,"SAVE_ROOM");
                        workerThread.start();
                        break;
                    case "GET_ROOMS":
                        Thread workerThread2 = new ActionsForWorkers(outputStream,inputStream,this,"GET_ROOMS");
                        workerThread2.start();
                        break;
                    case "GET_BOOKINGS":
                        Thread workerThread3 = new ActionsForWorkers(outputStream,inputStream,this,"GET_BOOKINGS");
                        workerThread3.start();
                        break;
                    case "FILTER_ROOMS":
                        Thread workerThread4 = new ActionsForWorkers(outputStream,inputStream, this, "FILTER_ROOMS");
                        workerThread4.start();
                        break;
                    case "CHECK_BOOK_ROOM":
                        Thread workerThread5 = new ActionsForWorkers(outputStream,inputStream,this,"CHECK_BOOK_ROOM");
                        workerThread5.start();
                        break;
                    case "SAVE_REVIEW":
                        Thread workerThread6 = new ActionsForWorkers(outputStream,inputStream,this,"SAVE_REVIEW");
                        workerThread6.start();
                        break;

                    case "IS_UP":
                        Thread workerThread7 = new ActionsForWorkers(outputStream,inputStream,this,"IS_UP");
                        workerThread7.start();
                        break;
                }

            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

    /**
     * getter method for the roomsToRent arraylist
     * @return the roomsToRent arraylist
     */
    public ArrayList<Room> getRoomsToRent() {
        return roomsToRent;
    }

    /**
     * setter method for the roomsToRent arraylist
     * @param roomsToRent the new roomsToRent arraylist
     */
    public synchronized void setRoomsToRent(ArrayList<Room> roomsToRent) {
        this.roomsToRent = roomsToRent;
    }

    /**
     * getter method for the bookings of the roomsToRent arraylist
     * @return all the bookings from the roomsToRent arraylist in an arraylist of bookings form
     */
    public ArrayList<Room> getBookedRooms(){
        ArrayList<Room> rooms = new ArrayList<>();
        for(Room room: roomsToRent){
            if(!room.getBookings().isEmpty()){
                rooms.add(room);
            }
        }
        return rooms;
    }

    /**
     * getter method for the id of the worker running
     * @return the id of the worker running
     */
    public int getWorkerID() {
        return workerID;
    }

    /**
     * getter method for the id of the worker running
     * @param workerID the id of the worker running
     */
    public void setWorkerID(int workerID) {
        this.workerID = workerID;
    }

    public static void main(String[] args){
        if (args.length == 1) {
            // worker id argument given
            int workerNumber = Integer.parseInt(args[0]);
            new Worker().openServer(workerNumber);
        }else if(args.length == 0) {
            // no argument given
            new Worker().openServer(-1);
        }else {
            System.err.println("Usage: java Worker <workerNumber>");
            System.exit(1);
        }

    }
}
