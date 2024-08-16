package com.example.bookingapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.example.bookingapp.Adapters.MyAdapter;
import com.example.bookingapp.R;
import java.util.ArrayList;
import Data.Room;

public class RoomsListActivity extends AppCompatActivity {

    ListView listView;
    TextView noResultsText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms_list);

        listView = findViewById(R.id.room_list);
        noResultsText = findViewById(R.id.no_results_text);

        ArrayList<Room> roomItems = (ArrayList<Room>) getIntent().getExtras().getSerializable("rooms");
        if (!roomItems.isEmpty()){

            MyAdapter adapter = new MyAdapter(getLayoutInflater(),roomItems);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Room room = roomItems.get(i);
                    Intent intent = new Intent(getApplicationContext(), DetailedRoomActivity.class);
                    intent.putExtra("room",room);
                    startActivity(intent);
                }
            });

        }else {
            noResultsText.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

    }
}