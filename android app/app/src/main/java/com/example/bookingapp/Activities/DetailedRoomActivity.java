package com.example.bookingapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.bookingapp.R;
import Data.Room;

public class DetailedRoomActivity extends AppCompatActivity {

    Button bookBtn;
    Button reviewBtn;
    TextView roomName;
    ImageView roomImage;
    TextView noOfPeople;
    TextView area;
    TextView price;
    TextView availability;
    RatingBar ratingBar;
    TextView ratingNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatiled_room);

        bookBtn = findViewById(R.id.book_button);
        reviewBtn = findViewById(R.id.review_button);
        roomName = findViewById(R.id.room_name);
        roomImage = findViewById(R.id.room_image);
        noOfPeople = findViewById(R.id.noofpeople_answer);
        area = findViewById(R.id.area_answer);
        price = findViewById(R.id.price_number);
        availability = findViewById(R.id.dates);
        ratingBar = findViewById(R.id.rating_bar);
        ratingNumber = findViewById(R.id.rating_number);

        Room room = (Room) getIntent().getSerializableExtra("room");

        roomName.setText(room.getName());

        byte[] image = room.getRoomImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);
        roomImage.setImageBitmap(bitmap);

        noOfPeople.setText(String.valueOf(room.getNoOfPeople()));

        area.setText(room.getArea());

        price.setText(String.valueOf(room.getPrice()));

        String text = room.getAvailabilityStart().toString().replace("-","/") + " - " + room.getAvailabilityEnd().toString().replace("-","/");
        availability.setText(text);

        ratingBar.setRating(room.getStars());
        ratingNumber.setText(String.valueOf(room.getStars()));


        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BookActivity.class);
                intent.putExtra("room", room);
                startActivity(intent);
            }
        });

        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ReviewActivity.class);
                intent.putExtra("room", room);
                startActivity(intent);
            }
        });

    }
}