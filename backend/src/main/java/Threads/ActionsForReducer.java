package Threads;

import Data.Room;
import Servers.Reducer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;


/**
 * Thread class handling the requests for the reducer
 */
public class ActionsForReducer extends Thread{
    ObjectInputStream inputStream;

    public ActionsForReducer(ObjectInputStream in){
        inputStream = in;
    }

    @Override
    public void run() {
        ObjectOutputStream masterOutputStream = null;
        ObjectInputStream masterInputStream = null;
        Socket socketMaster = null;
        try {
            int mapID = inputStream.readInt();
            ArrayList<Room> filteredRooms = (ArrayList<Room>) inputStream.readObject();

            ArrayList<Room> rooms;
            synchronized (Reducer.filters) {
                rooms = Reducer.filters.get(mapID);
            }

            // check if there's rooms for this map id already
            if (rooms == null) {
                Reducer.filters.put(mapID, filteredRooms);
            } else {
                synchronized (rooms) {
                    rooms.addAll(filteredRooms);
                }
            }


            // update the number of results aggregated for this map id
            Reducer.updateResultsNumber(mapID);

            // check if all filtered rooms for this map id have been aggregated
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            int workers = Integer.parseInt(appProps.getProperty("WORKERS"));

            int currentResultsNumber = Reducer.resultsPerMapID.get(mapID);
            if (currentResultsNumber == workers) {
                // send the results to master
                String masterIP = appProps.getProperty("MASTER_IP");
                int masterPort = Integer.parseInt(appProps.getProperty("MASTER_PORT_SEARCH"));
                socketMaster = new Socket(masterIP, masterPort);
                masterOutputStream = new ObjectOutputStream(socketMaster.getOutputStream());
                masterInputStream = new ObjectInputStream(socketMaster.getInputStream());

                // send map id to master
                masterOutputStream.writeInt(mapID);
                masterOutputStream.flush();

                // send filtered rooms to master
                ArrayList<Room> roomsFiltered = Reducer.filters.get(mapID);
                masterOutputStream.writeObject(roomsFiltered);
                masterOutputStream.flush();

                System.out.println("Sent filtered rooms to Master for map id: " + mapID);

                // remove items in hashmaps for the processed filter request
                Reducer.resultsPerMapID.remove(mapID);
                Reducer.filters.remove(mapID);

                // Wait for acknowledgment from worker
                String acknowledgment = masterInputStream.readUTF();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if(socketMaster!=null){
                    socketMaster.close();
                    masterOutputStream.close();
                }
                inputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}