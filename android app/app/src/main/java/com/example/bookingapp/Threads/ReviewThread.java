package com.example.bookingapp.Threads;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.example.bookingapp.Activities.ReviewActivity;
import com.example.bookingapp.Utils.Helper;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import Data.Room;
import Data.RoomsToReviewStorage;

public class ReviewThread extends Thread{
    Handler handler;
    int rating;
    ReviewActivity reviewActivity;

    Room room;

    public ReviewThread(ReviewActivity reviewActivity, Handler handler, int rating, Room room){
        this.handler = handler;
        this.rating = rating;
        this.reviewActivity = reviewActivity;
        this.room = room;
    }

    @Override
    public void run() {
        Socket socket = null;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
        try{
            // read master ip and port
            int masterPort = Helper.getConfigValueInt(reviewActivity,"MASTER_PORT");
            String masterIP = Helper.getConfigValueString(reviewActivity,"MASTER_IP");

            socket = new Socket(masterIP, masterPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeUTF("ADD_REVIEW");
            outputStream.flush();

            outputStream.writeObject(room);
            outputStream.flush();

            outputStream.writeInt(rating);
            outputStream.flush();

            RoomsToReviewStorage.delete(room);

            // Wait for acknowledgment from worker
            String acknowledgment = inputStream.readUTF();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(socket!=null){
                    socket.close();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Message message = new Message();
        Bundle bundle = new Bundle();
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
