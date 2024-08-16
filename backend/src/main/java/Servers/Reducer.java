package Servers;

import Data.Room;
import Threads.ActionsForReducer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


/**
 * class for the reducer sending the result of the search functionality
 */
public class Reducer extends Thread{
    ServerSocket serverSocket;
    Socket connection;
    public static HashMap<Integer, ArrayList<Room>> filters = new HashMap<>();
    public static HashMap<Integer,Integer> resultsPerMapID = new HashMap<>();

    /**
     * method for opening the reducer server, including its main functionality
     */
    public void openServer(){
        try {
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            int reducerPort = Integer.parseInt(appProps.getProperty("REDUCER_PORT"));
            serverSocket = new ServerSocket(reducerPort);
            System.out.println("Reducer is running..");

            while (true){
                System.out.println("Waiting for new connection..");
                connection = serverSocket.accept();
                System.out.println("New connection established");
                ObjectInputStream inputStream = new ObjectInputStream(connection.getInputStream());

                Thread thread = new ActionsForReducer(inputStream);
                thread.start();
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
     * method for updating the number of results received per map ID
     * @param mapID the map ID of the search function
     */
    public synchronized static void updateResultsNumber(int mapID){
        if(Reducer.resultsPerMapID.isEmpty()){
            Reducer.resultsPerMapID.put(mapID,1);
        }else {
            int currentResultsNumber = Reducer.resultsPerMapID.get(mapID);
            Reducer.resultsPerMapID.put(mapID, currentResultsNumber + 1);
        }
    }

    public static void main(String[] args){
        new Reducer().openServer();
    }
}
