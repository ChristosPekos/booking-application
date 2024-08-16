package com.example.bookingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bookingapp.R;
import com.example.bookingapp.Threads.GetRoomsThread;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import Data.Filter;
import Data.Room;

public class SearchActivity extends AppCompatActivity {

    TextView filtersTextView;
    Button searchBtn;
    EditText regionEditText;
    TextView regionTextView;
    TextView availabilityStartTextView;
    TextView finalAvailabilityStartTextView;
    Button selectAvailabilityStartBtn;
    TextView availabilityEndTextView;
    TextView finalAvailabilityEndTextView;
    Button selectAvailabilityEndBtn;
    TextView noOfPeopleTextView;
    EditText noOfPeopleEditText;
    TextView minPriceTextView;
    EditText minPriceEditText;
    TextView maxPriceTextView;
    EditText maxPriceEditText;
    TextView priceTextView;
    RangeSlider priceRangeSlider;
    TextView minStarsTextView;
    TextView currentMinStarsText;
    SeekBar currentMinStarsSeekBar;

    public Handler myHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            Boolean connected = message.getData().getBoolean("connected");
            if (!connected){
                Toast.makeText(SearchActivity.this,"Couldnt connect to Master. Please check the config file",Toast.LENGTH_SHORT).show();
                return false;
            }
            ArrayList<Room> resultRooms = (ArrayList<Room>) message.getData().getSerializable("rooms");
            Intent intent = new Intent(getApplicationContext(), RoomsListActivity.class);
            intent.putExtra("rooms", resultRooms);
            startActivity(intent);
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        filtersTextView = findViewById(R.id.Filters_TextView);
        regionEditText = findViewById(R.id.region_edit_text);
        searchBtn = findViewById(R.id.search_button);
        regionTextView = findViewById(R.id.region_textView);
        availabilityStartTextView = findViewById(R.id.availability_start_date_text);
        finalAvailabilityStartTextView = findViewById(R.id.availability_start_date_finalized);
        selectAvailabilityStartBtn = findViewById(R.id.select_availability_start_date);
        availabilityEndTextView = findViewById(R.id.availability_end_date_text);
        finalAvailabilityEndTextView = findViewById(R.id.availability_end_date_finalized);
        selectAvailabilityEndBtn = findViewById(R.id.select_availability_end_date);
        noOfPeopleTextView = findViewById(R.id.noofpeople_textView);
        noOfPeopleEditText = findViewById(R.id.noofpeople_editText);
        priceTextView = findViewById(R.id.price_textView);
        minPriceTextView = findViewById(R.id.min_price_textView);
        minPriceEditText = findViewById(R.id.min_price_Edit_Text);
        maxPriceTextView = findViewById(R.id.max_price_textView);
        maxPriceEditText = findViewById(R.id.max_price_Edit_Text);
        minStarsTextView = findViewById(R.id.min_stars_textView);
        currentMinStarsText = findViewById(R.id.current_min_stars);
        currentMinStarsSeekBar = findViewById(R.id.stars_seekBar);
        priceRangeSlider = findViewById(R.id.price_slider);



        selectAvailabilityStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePickerDialog("availability_start");
            }
        });

        selectAvailabilityEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePickerDialog("availability_end");
            }
        });

        currentMinStarsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                float ratingValue = progress / 10.0f;
                currentMinStarsText.setText(String.valueOf(ratingValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        priceRangeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                currencyFormat.setCurrency(Currency.getInstance("EUR"));
                return currencyFormat.format(value);
            }
        });

        priceRangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> values = slider.getValues();
                minPriceEditText.setText(String.valueOf(values.get(0).intValue()));
                maxPriceEditText.setText(String.valueOf(values.get(1).intValue()));
            }
        });


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String area = regionEditText.getText().toString().trim();
                if(area.isEmpty()){
                    Toast.makeText(SearchActivity.this,"Region field is required. Please enter the region ",Toast.LENGTH_SHORT).show();
                    return;
                }
                String startDate = finalAvailabilityStartTextView.getText().toString();
                if(startDate.isEmpty()){
                    Toast.makeText(SearchActivity.this,"Please enter the Availability Start Date ",Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] date_parts = startDate.split("/");
                int year = Integer.parseInt(date_parts[0]);
                int month = Integer.parseInt(date_parts[1]);
                int day = Integer.parseInt(date_parts[2]);
                LocalDate availabilityStart = LocalDate.of(year, month, day);
                String endDate = finalAvailabilityEndTextView.getText().toString();
                if(endDate.isEmpty()){
                    Toast.makeText(SearchActivity.this,"Please enter the Availability End Date ",Toast.LENGTH_SHORT).show();
                    return;
                }
                date_parts = endDate.split("/");
                year = Integer.parseInt(date_parts[0]);
                month = Integer.parseInt(date_parts[1]);
                day = Integer.parseInt(date_parts[2]);
                LocalDate availabilityEnd = LocalDate.of(year, month, day);

                String noOfPeopleText = noOfPeopleEditText.getText().toString();
                int noOfPeople;
                if(noOfPeopleText.isEmpty()){
                    noOfPeople = 0;
                }else {
                    noOfPeople = Integer.parseInt(noOfPeopleText);
                }

                String minPriceText = minPriceEditText.getText().toString();
                int minPrice;
                if(minPriceText.isEmpty()){
                    minPrice = 0;
                }else {
                    minPrice = Integer.parseInt(minPriceText);
                }

                String maxPriceText = maxPriceEditText.getText().toString();
                int maxPrice;
                if(maxPriceText.isEmpty()){
                    maxPrice = Integer.MAX_VALUE;
                }else {
                    maxPrice = Integer.parseInt(maxPriceText);
                }

                if(minPrice>maxPrice){
                    Toast.makeText(SearchActivity.this,"Min Price should be less than Max Price",Toast.LENGTH_SHORT).show();
                    return;
                }

                String starsText = currentMinStarsText.getText().toString();
                float stars;
                if(starsText.isEmpty()){
                    stars = 0;
                }else {
                    stars = Float.parseFloat(starsText);
                }

                if(availabilityStart.isAfter(availabilityEnd)){
                    Toast.makeText(SearchActivity.this,"Availability Start Date should be before Availability End Date",Toast.LENGTH_SHORT).show();
                    return;
                }

                Filter filter = new Filter(area, availabilityStart, availabilityEnd, noOfPeople, minPrice,maxPrice,stars);

                Thread thread = new GetRoomsThread(SearchActivity.this,myHandler,filter);
                thread.start();

            }
        });
    }

    private void openDatePickerDialog(String button) {
        LocalDate dateNow = LocalDate.now();
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String formattedDate = year + "/" + (month + 1) + "/" + day;
                if (button.equals("availability_start")) {
                    finalAvailabilityStartTextView.setText(formattedDate);
                } else {
                    finalAvailabilityEndTextView.setText(formattedDate);
                }
            }
        }, dateNow.getYear(), dateNow.getMonthValue() - 1, dateNow.getDayOfMonth());

        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        dialog.show();
    }
}