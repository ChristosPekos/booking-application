package Servers;

import Data.UserSendTask;
import Threads.ActionsForManagers;
import Threads.ActionsForUsers;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

/**
 * class for the Master server that will receive the requests and delegate them to the Workers
 */
public class Master {

    private ServerSocket serverSocket;
    private ServerSocket reducerServerSocket;
    private Socket connection;
    public static int currentMapID = 0;

    /**
     * method that calculates the next Map ID
     * @return the next map ID
     */
    public synchronized static int nextMapID(){
        currentMapID++;
        return currentMapID;
    }


    /**
     * getter method that for getting the current map ID
     * @return the current map ID
     */
    public synchronized static int getMapID(){
        return currentMapID;
    }
    public static ArrayList<UserSendTask> userSendTasks = new ArrayList<>();

    /**
     * getter method for the userSendTasks arraylist
     * @return the userSendTasks arraylist
     */
    public synchronized static ArrayList<UserSendTask> getUserSendTasks() {
        return userSendTasks;
    }


    /**
     * Checks whether a worker is up or down
     * @param ip the ip of the worker
     * @param port the port of the worker
     * @return true if the worker is up and running and false otherwise
     */
    public static boolean getHealthCheck(String ip, int port){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(ip,port);
            socket.setSoTimeout(1200);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeUTF("IS_UP");
            out.flush();

            String message = in.readUTF();
            out.writeUTF("ACK");
            out.flush();

            // Return true if the worker responds with "ACK"
            return message.equals("ACK");
        }catch (Exception e){
            return false;
        }finally {
            // Always close the socket if it was successfully created
            if (socket != null) {
                try {
                    socket.close();
                    out.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    /**
     * method for opening the master server, including its main functionality
     */
    public void openServer(){
        try {
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            int masterPort = Integer.parseInt(appProps.getProperty("MASTER_PORT"));
            int reducerPort = Integer.parseInt(appProps.getProperty("MASTER_PORT_SEARCH"));

            reducerServerSocket = new ServerSocket(reducerPort);
            serverSocket = new ServerSocket(masterPort);
            System.out.println("Master is running..");
            ObjectOutputStream outputStream = null;
            ObjectInputStream inputStream = null;

            while (true){
                System.out.println("Waiting for new connection..");
                connection = serverSocket.accept();
                System.out.println("New connection established");
                outputStream = new ObjectOutputStream(connection.getOutputStream());
                inputStream = new ObjectInputStream(connection.getInputStream());
                String requestType = inputStream.readUTF();
                System.out.println("New request of type " + requestType + " is being processed on a new thread");

                switch (requestType){
                    case "MANAGER/ADD_ROOM":
                        Thread managerThread = new ActionsForManagers(inputStream,outputStream,"MANAGER/ADD_ROOM");
                        managerThread.start();
                        break;
                    case "MANAGER/SHOW_ROOMS":
                        Thread managerThread2 = new ActionsForManagers(inputStream,outputStream,"MANAGER/SHOW_ROOMS");
                        managerThread2.start();
                        break;

                    case "MANAGER/SHOW_BOOKINGS":
                        Thread managerThread3 = new ActionsForManagers(inputStream,outputStream,"MANAGER/SHOW_BOOKINGS");
                        managerThread3.start();
                        break;
                    
                    case "ROOM_SEARCH":
                        Thread userSearchThread = new ActionsForUsers(inputStream,outputStream,connection,reducerServerSocket,nextMapID(),"ROOM_SEARCH");
                        userSearchThread.start();
                        break;

                    case "BOOK_ROOM":
                        Thread userFinalizeRoom = new ActionsForUsers(inputStream,outputStream, "BOOK_ROOM");
                        userFinalizeRoom.start();
                        break;

                    case "ADD_REVIEW":
                        Thread addReviewThread = new ActionsForUsers(inputStream,outputStream,"ADD_REVIEW");
                        addReviewThread.start();
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

    public static void main(String[] args){
        new Master().openServer();
    }
}
