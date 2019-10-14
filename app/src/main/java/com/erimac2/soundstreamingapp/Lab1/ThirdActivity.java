package com.erimac2.soundstreamingapp.Lab1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.erimac2.soundstreamingapp.R;

import java.util.List;

public class ThirdActivity extends AppCompatActivity {

    private ListView myList;
    private ListAdapter adapter;

    private List<ListItem> items;

    @Override
    protected void  onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thirdactivitydesign);
        myList = findViewById(R.id.listview);


        Intent intent = getIntent();

        items = (List<ListItem>)intent.getSerializableExtra("newlist");

        adapter = new ListAdapter(this, items);

        myList.setAdapter(adapter);
    }
}
