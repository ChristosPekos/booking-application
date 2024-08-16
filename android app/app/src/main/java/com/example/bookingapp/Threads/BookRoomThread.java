package com.example.bookingapp.Threads;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.example.bookingapp.Activities.BookActivity;
import com.example.bookingapp.Utils.Helper;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import Data.Booking;
import Data.Room;

public class BookRoomThread extends Thread{
    Handler handler;
    Booking booking;

    BookActivity bookActivity;


    public BookRoomThread(BookActivity bookActivity, Handler handler, Booking booking){
        this.handler = handler;
        this.booking = booking;
        this.bookActivity = bookActivity;
    }


    public void run() {
        String confirmation = null;
        Socket socket = null;
        ObjectInputStream inputStream =null;
        ObjectOutputStream outputStream = null;
        try {
            // read master ip and port
            int masterPort = Helper.getConfigValueInt(bookActivity,"MASTER_PORT");
            String masterIP = Helper.getConfigValueString(bookActivity,"MASTER_IP");

            socket = new Socket(masterIP, masterPort);
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            outputStream.writeUTF("BOOK_ROOM");
            outputStream.flush();

            outputStream.writeObject(booking);
            outputStream.flush();

            confirmation = inputStream.readUTF();

            // Send acknowledgment back to the sender
            outputStream.writeUTF("ACK");
            outputStream.flush();

        } catch (IOException e) {
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
        bundle.putString("confirmation",confirmation);
        message.setData(bundle);
        handler.sendMessage(message);


    }
}
