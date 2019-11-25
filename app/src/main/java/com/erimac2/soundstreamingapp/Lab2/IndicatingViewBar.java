package com.erimac2.soundstreamingapp.Lab2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class IndicatingViewBar extends View {


    public IndicatingViewBar (Context context)
    {
        super(context);
    }
    public IndicatingViewBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public IndicatingViewBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        Paint paint;
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(-15061504);
        colors.add(-12884224);
        colors.add(-11696128);
        colors.add(-10772224);
        colors.add(-9980416);
        paint = new Paint();
        paint.setStrokeWidth(50f);

        double count = RequestOperator.globalCount;

        int j = 0;
        for (double x = 0; x < 1 && x < count; x+= 0.2)
        {
            paint.setColor(colors.get(j++));
            canvas.drawLine((float) x * width, height/2, (float)(x + 0.2) * width, height / 2, paint);
            this.postInvalidate();
        }

    }
}
