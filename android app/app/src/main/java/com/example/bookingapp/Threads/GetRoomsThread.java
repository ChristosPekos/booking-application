package com.example.bookingapp.Threads;

import android.os.Bundle;
import android.os.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import android.os.Handler;
import com.example.bookingapp.Activities.SearchActivity;
import com.example.bookingapp.Utils.Helper;

import Data.Filter;
import Data.Room;

public class GetRoomsThread extends Thread{
    Handler handler;
    Filter filter;
    SearchActivity searchActivity;

    public GetRoomsThread(SearchActivity searchActivity, Handler handler, Filter filter){
        this.handler = handler;
        this.filter = filter;
        this.searchActivity = searchActivity;
    }
    @Override
    public void run() {
        ArrayList<Room> rooms = new ArrayList<>();
        Socket socket = null;
        ObjectInputStream inputStream =null;
        ObjectOutputStream outputStream = null;
        try {
            // read master ip and port
            int masterPort = Helper.getConfigValueInt(searchActivity,"MASTER_PORT");
            String masterIP = Helper.getConfigValueString(searchActivity,"MASTER_IP");

            socket = new Socket(masterIP, masterPort);
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            outputStream.writeUTF("ROOM_SEARCH");
            outputStream.flush();

            outputStream.writeObject(filter);
            outputStream.flush();

            rooms = (ArrayList<Room>) inputStream.readObject();

            // Send acknowledgment back to the sender
            outputStream.writeUTF("ACK");
            outputStream.flush();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putBoolean("connected",false);
            message.setData(bundle);
            handler.sendMessage(message);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                    inputStream.close();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putBoolean("connected",true);
        bundle.putSerializable("rooms",rooms);
        message.setData(bundle);
        handler.sendMessage(message);


    }
}
