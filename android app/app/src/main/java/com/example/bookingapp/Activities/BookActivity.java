package com.example.bookingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bookingapp.R;
import com.example.bookingapp.Threads.BookRoomThread;
import java.time.LocalDate;
import java.util.Calendar;
import Data.Booking;
import Data.Room;
import Data.RoomsToReviewStorage;

public class BookActivity extends AppCompatActivity {


    TextView bookTextView;
    TextView arrivalTextView;
    TextView arrivalDateText;
    TextView departureTextView;
    TextView departureDateText;
    Button selectArrivalBtn;
    Button selectDepartureBtn;
    Button bookNowBtn;

    public Handler myHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            Boolean connected = message.getData().getBoolean("connected");
            if (!connected){
                Toast.makeText(BookActivity.this,"Couldnt connect to Master. Please check the config file",Toast.LENGTH_SHORT).show();
                return false;
            }

            String bookingStatus = message.getData().getString("confirmation");

            if (!bookingStatus.equals("The booking was successful.")){
                Toast.makeText(BookActivity.this, "The dates that u selected where not available", Toast.LENGTH_SHORT).show();
                return false;
            }else {
                Toast.makeText(BookActivity.this, "Booking was successful", Toast.LENGTH_SHORT).show();
            }

            Room room = (Room) getIntent().getSerializableExtra("room");
            RoomsToReviewStorage.add(room);

            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        bookTextView = findViewById(R.id.book_the_room_text_view);
        arrivalTextView = findViewById(R.id.arrival_date_text);
        arrivalDateText = findViewById(R.id.arrival_date_finalized);
        departureTextView = findViewById(R.id.departure_date_text);
        departureDateText = findViewById(R.id.departure_date_finalized);
        selectArrivalBtn = findViewById(R.id.select_arrival_date);
        selectDepartureBtn = findViewById(R.id.select_departure_date);
        bookNowBtn = findViewById(R.id.book_now_btn);

        selectArrivalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePickerDialog("arrival");
            }
        });

        selectDepartureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePickerDialog("departure");
            }
        });


        bookNowBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                //getting arrival date
                String startDate = arrivalDateText.getText().toString();
                if (startDate.isEmpty()){
                    Toast.makeText(BookActivity.this, "Please enter the date of arrival", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] date_parts = startDate.split("/");
                int year = Integer.parseInt(date_parts[0]);
                int month = Integer.parseInt(date_parts[1]);
                int day = Integer.parseInt(date_parts[2]);
                LocalDate arrivalDate = LocalDate.of(year,month,day);

                //getting departure date
                String endDate = departureDateText.getText().toString();
                if(endDate.isEmpty()){
                    Toast.makeText(BookActivity.this,"Please enter the Availability End Date ",Toast.LENGTH_SHORT).show();
                    return;
                }

                date_parts = endDate.split("/");
                year = Integer.parseInt(date_parts[0]);
                month = Integer.parseInt(date_parts[1]);
                day = Integer.parseInt(date_parts[2]);
                LocalDate departureDate = LocalDate.of(year, month, day);

                //checking that the departure date is after arrival date
                if(arrivalDate.isAfter(departureDate)){
                    Toast.makeText(BookActivity.this, "Arrival Date should be after Departure date",Toast.LENGTH_SHORT).show();
                    return;
                }

                Room room = (Room) getIntent().getSerializableExtra("room");
                Booking booking = new Booking(arrivalDate,departureDate,room);

                Thread thread = new BookRoomThread(BookActivity.this, myHandler,booking);
                thread.start();
            }
        });

    }

    private void openDatePickerDialog(String button){

        LocalDate dateNow = LocalDate.now();
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String formattedDate = year + "/" + (month + 1) + "/" + day;
                if(button.equals("arrival")){
                    arrivalDateText.setText(formattedDate);
                }else {
                    departureDateText.setText(formattedDate);
                }

            }
        }, dateNow.getYear(), dateNow.getMonthValue()-1,dateNow.getDayOfMonth() );

        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        dialog.show();
    }


}