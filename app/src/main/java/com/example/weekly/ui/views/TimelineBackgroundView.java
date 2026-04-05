package com.example.weekly.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weekly.R;
import com.example.weekly.ui.utils.TimelineMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimelineBackgroundView extends View {
    private Paint linePaint;
    private Paint dashPaint;
    private Paint textPaint;
    
    // Indicador de tiempo actual
    private Paint indicatorBgPaint;
    private Paint indicatorTextPaint;
    private final RectF indicatorRect = new RectF();
    
    private int hourHeight;
    private float labelPadding;
    private float cornerRadius;
    private float rectPadding;
    
    private int startHour = 0;
    private int endHour = 24;
    private LocalDate date;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 60000); // Actualizar cada minuto
        }
    };

    public TimelineBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        hourHeight = TimelineMapper.dpToPx(TimelineMapper.HOUR_HEIGHT_DP, getContext());
        labelPadding = TimelineMapper.dpToPx(12, getContext());
        cornerRadius = TimelineMapper.dpToPx(4, getContext());
        rectPadding = TimelineMapper.dpToPx(4, getContext());

        int primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        int textColor = ContextCompat.getColor(getContext(), R.color.text_primary_light);

        linePaint = new Paint();
        linePaint.setColor(primaryColor);
        linePaint.setAlpha(30); 
        linePaint.setStrokeWidth(TimelineMapper.dpToPx(1f, getContext()));

        dashPaint = new Paint();
        dashPaint.setColor(primaryColor);
        dashPaint.setAlpha(15);
        dashPaint.setStrokeWidth(TimelineMapper.dpToPx(1f, getContext()));
        dashPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setAlpha(180);
        textPaint.setTextSize(TimelineMapper.dpToPx(12, getContext()));

        // Nuevo Indicador dinámico
        indicatorBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorBgPaint.setColor(primaryColor); 
        indicatorBgPaint.setStyle(Paint.Style.FILL);

        indicatorTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorTextPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        indicatorTextPaint.setTextSize(TimelineMapper.dpToPx(10, getContext()));
        indicatorTextPaint.setTextAlign(Paint.Align.CENTER);
        indicatorTextPaint.setFakeBoldText(true);
    }

    public void setTimeRange(int start, int end) {
        this.startHour = start;
        this.endHour = end;
        requestLayout();
        invalidate();
    }

    public void setDate(LocalDate date) {
        this.date = date;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int range = endHour - startHour;
        int height = hourHeight * range;
        height += TimelineMapper.dpToPx(20, getContext());
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(updateTimeRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(updateTimeRunnable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float labelWidth = TimelineMapper.dpToPx(50, getContext());

        // Dibujar horas y rejilla
        for (int h = startHour; h <= endHour; h++) {
            float y = (h - startHour) * hourHeight;
            canvas.drawLine(labelWidth, y, getWidth(), y, linePaint);
            
            String hourLabel = String.format("%02d:00", h);
            canvas.drawText(hourLabel, labelPadding, y + (textPaint.getTextSize() / 3), textPaint);
        }

        // Indicador de Hora Actual Dinámico (Solo si es hoy)
        LocalDate today = LocalDate.now();
        if (date != null && date.equals(today)) {
            LocalTime now = LocalTime.now();
            if (now.getHour() >= startHour && now.getHour() < endHour) {
                float currentY = TimelineMapper.getTopMarginPx(now, startHour, getContext());
                
                String timeStr = now.format(timeFormatter);
                float textWidth = indicatorTextPaint.measureText(timeStr);
                
                float rectWidth = textWidth + (rectPadding * 2);
                float rectHeight = indicatorTextPaint.getTextSize() + (rectPadding * 2);
                
                float rectLeft = (labelWidth - rectWidth) / 2f;
                float rectTop = currentY - (rectHeight / 2f);
                
                indicatorRect.set(rectLeft, rectTop, rectLeft + rectWidth, rectTop + rectHeight);
                
                // 1. Dibujar el recuadro redondeado (#BFA8E6)
                canvas.drawRoundRect(indicatorRect, cornerRadius, cornerRadius, indicatorBgPaint);
                
                // 2. Dibujar el texto de la hora actual (Blanco)
                canvas.drawText(timeStr, indicatorRect.centerX(), indicatorRect.centerY() + (indicatorTextPaint.getTextSize() / 3f), indicatorTextPaint);
            }
        }
    }
}
