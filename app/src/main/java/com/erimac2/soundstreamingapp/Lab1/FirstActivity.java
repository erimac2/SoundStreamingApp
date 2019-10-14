package com.erimac2.soundstreamingapp.Lab1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.erimac2.soundstreamingapp.R;

public class FirstActivity extends AppCompatActivity {

    private Button myButton;
    private TextView myTextField;
    private Button secondActivityButton;
    private Button removeButton;
    private Context context = this;
    private Boolean visible = true;

    private ViewGroup vg;

    String text;

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstactivitydesign);

        myButton = findViewById(R.id.Button);
        secondActivityButton = findViewById(R.id.Second_Activity_Button);
        myTextField = findViewById(R.id.Text);
        removeButton = findViewById(R.id.Remove_Button);

        secondActivityButton.setOnClickListener(startSecondActivity);
        vg = ((ViewGroup)myTextField.getParent());
        myButton.setOnClickListener(myButtonClick);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(visible) {
                    text = myTextField.getText().toString();
                    vg.removeView(myTextField);
                    visible = false;
                }
                else
                {
                    myTextField = new TextView(FirstActivity.this);
                    vg.addView(myTextField);
                    myTextField.setText(text);
                    visible = true;
                }
            }
        });
    }

    View.OnClickListener myButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            myTextField.setText(myTextField.getText()+"\n"+"Next line");
        }
    };
    public void runSecondActivity(boolean b){
        Intent intent = new Intent(context, SecondActivity.class);
        intent.putExtra("flag", b);
        context.startActivity(intent);
    }
    View.OnClickListener startSecondActivity = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            runSecondActivity(true);
        }
    };
}
