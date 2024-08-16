package com.example.bookingapp.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.bookingapp.R;
import java.util.ArrayList;
import Data.Room;

public class MyAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<Room> roomItems;

    public MyAdapter(LayoutInflater layoutInflater, ArrayList<Room> roomItems) {
        this.inflater = layoutInflater;
        this.roomItems = roomItems;
    }

    @Override
    public int getCount() {
        return roomItems.size();
    }

    @Override
    public Room getItem(int i) {
        return roomItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.room_list_item,container,false);
        }

        Room room = roomItems.get(position);

        TextView nameTextView = convertView.findViewById(R.id.list_item_room_name);
        ImageView roomImageView = convertView.findViewById(R.id.room_item_image);

        nameTextView.setText(room.getName());

        // Decode byte array to Bitmap and set to ImageView
        byte[] imageBytes = room.getRoomImage();
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            roomImageView.setImageBitmap(bitmap);
        } else {
            roomImageView.setImageResource(R.drawable.ic_launcher_background);  // Default image if no image provided
        }


        return convertView;
    }
}
