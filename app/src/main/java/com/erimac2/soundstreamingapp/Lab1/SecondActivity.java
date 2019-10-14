package com.erimac2.soundstreamingapp.Lab1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.erimac2.soundstreamingapp.ArtistActivity;
import com.erimac2.soundstreamingapp.ArtistItem;
import com.erimac2.soundstreamingapp.PlayerActivity;
import com.erimac2.soundstreamingapp.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {
    private Button addItem;
    private ListView myList;
    private ListAdapter adapter;

    private final List<ListItem> items = new ArrayList<>();

    @Override
    protected void  onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondactivitydesign);
        myList = findViewById(R.id.listview);
        addItem = findViewById(R.id.Add_Button);

        myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                ListItem item = (ListItem) parent.getItemAtPosition(position);

                adapter.remove(item);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, NewItem.class);
                intent.putExtra("list", (Serializable) items);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();



        adapter = new ListAdapter(this, items);


        if(intent.getBooleanExtra("flag", true))
        {
            items.add(new ListItem("something", R.drawable.ic_launcher_background ,"something something" ));
        }
        myList.setAdapter(adapter);
    }
}
