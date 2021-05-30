package com.dkanada.gramophone.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ColorCircleDrawable extends View {
    Paint circle = new Paint();
    Paint border = new Paint();

    public ColorCircleDrawable(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = getContext().obtainStyledAttributes(new int[]{android.R.attr.divider});
        int colorCircle = getContext().getResources().getColor(android.R.color.black);
        int colorBorder = getContext().getResources().getColor(android.R.color.darker_gray);

        circle.setAntiAlias(true);
        circle.setColor(colorCircle);

        border.setAntiAlias(true);
        border.setColor(attributes.getColor(0, colorBorder));

        attributes.recycle();
    }

    public void setColor(int color) {
        circle.setColor(color);
    }

    @Override
    @SuppressLint("CanvasSize")
    protected void onDraw(Canvas canvas) {
        int size = canvas.getHeight() / 2;

        canvas.drawCircle(size, size, size, border);
        canvas.drawCircle(size, size, size - 4f, circle);
    }
}
