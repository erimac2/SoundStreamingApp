package com.erimac2.soundstreamingapp.Lab1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.erimac2.soundstreamingapp.R;

import java.io.Serializable;
import java.util.List;


public class NewItem extends AppCompatActivity {

    private Button addItem;
    private EditText editName;
    private EditText editDescription;
    private ImageView editImage;

    private ListItem item;

    private List<ListItem> items;

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newitemdesign);

        addItem = findViewById(R.id.Add_New);
        editName = findViewById(R.id.Edit_Name);
        editDescription = findViewById(R.id.Edit_Description);
        editImage = findViewById(R.id.Edit_Image);


        Intent intent = getIntent();

        items = (List<ListItem>)intent.getSerializableExtra("list");

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = new ListItem(editName.getText().toString(), android.R.mipmap.sym_def_app_icon, editDescription.getText().toString());
                items.add(item);
                Intent intent = new Intent(NewItem.this, ThirdActivity.class);
                intent.putExtra("newlist", (Serializable) items);
                startActivity(intent);
            }
        });
    }
}
