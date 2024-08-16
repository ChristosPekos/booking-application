package com.example.bookingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bookingapp.R;
import com.example.bookingapp.Threads.ReviewThread;
import Data.Room;
import Data.RoomsToReviewStorage;

public class ReviewActivity extends AppCompatActivity {

    TextView textView;
    TextView reviewSuccess;
    RatingBar ratingBar;
    Button reviewButton;

    public Handler myHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            Toast.makeText(ReviewActivity.this, "The review was succesful",Toast.LENGTH_SHORT).show();

            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        textView = findViewById(R.id.review_textview);
        ratingBar = findViewById(R.id.review_stars);
        reviewSuccess = findViewById(R.id.review_success);
        reviewButton = findViewById(R.id.review_button);

        Room room = (Room)getIntent().getSerializableExtra("room");

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rating = (int)ratingBar.getRating();

                if(rating == 0){
                    Toast.makeText(ReviewActivity.this, "Please choose your rating", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!RoomsToReviewStorage.contains(room)){
                    Toast.makeText(ReviewActivity.this, "Cannot review room that hasn't been booked or has already been reviewed", Toast.LENGTH_SHORT).show();
                    return;
                }

                Thread thread = new ReviewThread(ReviewActivity.this, myHandler, rating, room);
                thread.start();

            }
        });



    }

}