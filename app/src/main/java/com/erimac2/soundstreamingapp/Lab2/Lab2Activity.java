package com.erimac2.soundstreamingapp.Lab2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.erimac2.soundstreamingapp.R;

public class Lab2Activity extends AppCompatActivity implements RequestOperator.RequestOperatorListener {
    Button sendRequestButton;
    TextView title;
    TextView bodyText;
    public static int count;

    private ModelPost publication;

    private IndicatingView indicator;
    public static IndicatingViewBar indicatorBar;

    public static ProgressBar bar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab2design);

        sendRequestButton = findViewById(R.id.send_request);
        sendRequestButton.setOnClickListener(requestButtonClicked);
        indicator = findViewById(R.id.generated_graphic);
        indicatorBar = findViewById(R.id.generated_graphic_bar);

        title = findViewById(R.id.title);
        bodyText = findViewById(R.id.body_text);

        bar = findViewById(R.id.simpleProgressBar);
        bar.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        RequestOperator.globalCount = 0;
    }

    View.OnClickListener requestButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            bar.setVisibility(View.VISIBLE);
            setIndicatorStatus(IndicatingView.RUNNING);
            sendRequest();
        }
    };

    private void sendRequest()
    {
        RequestOperator ro = new RequestOperator();
        ro.setListener(this);
        ro.start();
    }

    public void updatePublication()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(publication != null)
                {
                    bar.setVisibility(View.GONE);
                    title.setText(publication.getTitle());
                    bodyText.setText(publication.getBodyText());
                }
                else
                {
                    title.setText("");
                    bodyText.setText("");
                }
            }
        });
    }
    public void setIndicatorStatus(final int status)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                indicator.setState(status);
                indicator.invalidate();
            }
        });
    }
    @Override
    public void success(ModelPost publication)
    {
        bar.setVisibility(View.INVISIBLE);
        this.publication = publication;
        count = publication.getCount();
        setIndicatorStatus(IndicatingView.SUCCESS);
        updatePublication();
    }
    @Override
    public void failed(int responseCode)
    {
        this.publication = null;
        setIndicatorStatus(IndicatingView.FAILED);
        updatePublication();
    }
}
