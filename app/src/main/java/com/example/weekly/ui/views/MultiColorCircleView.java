package com.example.weekly.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiColorCircleView extends View {
    private final List<Integer> colors = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();

    public MultiColorCircleView(Context context) {
        super(context);
    }

    public MultiColorCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiColorCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setColors(List<Integer> colors) {
        this.colors.clear();
        this.colors.addAll(colors);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (colors.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f;
        rectF.set(width / 2f - radius, height / 2f - radius, width / 2f + radius, height / 2f + radius);

        if (colors.size() == 1) {
            paint.setColor(colors.get(0));
            canvas.drawOval(rectF, paint);
        } else {
            float angleStep = 360f / colors.size();
            float currentAngle = -90f; // Empezar desde arriba

            for (int color : colors) {
                paint.setColor(color);
                canvas.drawArc(rectF, currentAngle, angleStep, true, paint);
                currentAngle += angleStep;
            }
        }
    }
}
