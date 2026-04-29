package com.example.weekly.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weekly.R;
import com.example.weekly.ui.utils.TimelineMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimelineBackgroundView extends View {
    private Paint linePaint;
    private Paint dashPaint;
    private Paint textPaint;
    
    // Indicador de tiempo actual
    private Paint indicatorBgPaint;
    private Paint indicatorTextPaint;
    private final RectF indicatorRect = new RectF();

    // Vista previa fantasma para Drag & Drop
    private Paint ghostPaint;
    private Paint ghostBorderPaint;
    private Paint shadowPaint;
    private final RectF ghostRect = new RectF();
    private boolean showGhost = false;
    private boolean isGhostValid = true;
    
    private float dragX, dragY;
    private float touchOffsetY = 0;

    private long currentDraggingId = -1;
    private int currentDraggingDuration = 60;

    private List<TaskSlot> existingTasks = new java.util.ArrayList<>();
    
    private int hourHeight;
    private float labelPadding;
    private float cornerRadius;
    private float rectPadding;
    
    private int startHour = 0;
    private int endHour = 24;
    private LocalDate date;

    public static class TaskSlot {
        public final long id;
        public final LocalTime start;
        public final LocalTime end;

        public TaskSlot(long id, LocalTime start, LocalTime end) {
            this.id = id;
            this.start = start;
            this.end = end;
        }
    }

    public void setExistingTasks(List<TaskSlot> tasks) {
        this.existingTasks = tasks;
        invalidate();
    }

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

        ghostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ghostPaint.setColor(primaryColor);
        ghostPaint.setStyle(Paint.Style.FILL);
        ghostPaint.setAlpha(40); // Relleno muy tenue

        ghostBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ghostBorderPaint.setStyle(Paint.Style.STROKE);
        ghostBorderPaint.setStrokeWidth(TimelineMapper.dpToPx(3, getContext())); // Trazo grueso
        ghostBorderPaint.setColor(primaryColor);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

    @FunctionalInterface
    public interface OnTimeSlotDroppedListener {
        void onTimeSlotDropped(LocalTime time, android.view.DragEvent event);
        default void onDragLocation(View v, android.view.DragEvent event) {}
    }

    private OnTimeSlotDroppedListener onTimeSlotDroppedListener;

    public void setOnTimeSlotDroppedListener(OnTimeSlotDroppedListener listener) {
        this.onTimeSlotDroppedListener = listener;
        if (listener != null) {
            setupDragListener();
        } else {
            setOnDragListener(null);
        }
    }

    private void setupDragListener() {
        setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.DragEvent.ACTION_DRAG_STARTED:
                    // Usar localState para obtener ID y duración de forma inmediata y segura
                    Object localState = event.getLocalState();
                    if (localState instanceof long[]) {
                        long[] data = (long[]) localState;
                        currentDraggingId = data[0];
                        currentDraggingDuration = (int) data[1];
                        if (data.length > 2) {
                            touchOffsetY = data[2];
                        } else {
                            touchOffsetY = 0;
                        }
                        Log.d("DRAG_SPACE", "Drag iniciado: ID=" + currentDraggingId + ", Duración=" + currentDraggingDuration + ", Offset=" + touchOffsetY);
                    } else {
                        // Intento de fallback por ClipDescription si localState falla
                        currentDraggingId = -1;
                        currentDraggingDuration = 60;
                    }
                    return true;
                case android.view.DragEvent.ACTION_DRAG_LOCATION:
                    if (onTimeSlotDroppedListener != null) {
                        onTimeSlotDroppedListener.onDragLocation(v, event);
                    }
                    
                    dragX = event.getX();
                    dragY = event.getY();
                    
                    float yLoc = event.getY() - touchOffsetY;
                    float snapInterval = hourHeight / 12f; // 5 min
                    float snappedY = (float) Math.round(yLoc / snapInterval) * snapInterval;
                    LocalTime startTime = calculateTimeFromY(snappedY);

                    ValidationResult result = validatePosition(startTime, currentDraggingId, currentDraggingDuration);
                    isGhostValid = result.isValid;
                    
                    if (!isGhostValid && result.suggestedStart != null) {
                        startTime = result.suggestedStart;
                        snappedY = TimelineMapper.getTopMarginPx(startTime, startHour, getContext());
                        isGhostValid = true;
                    }

                    float labelWidth = TimelineMapper.dpToPx(50, getContext());
                    float ghostHeight = (currentDraggingDuration / 60f) * hourHeight;
                    ghostRect.set(labelWidth, snappedY, getWidth(), snappedY + ghostHeight);
                    showGhost = true;
                    
                    ghostBorderPaint.setColor(isGhostValid ? 
                        ContextCompat.getColor(getContext(), R.color.primary) : 
                        android.graphics.Color.RED);

                    invalidate();
                    return true;
                case android.view.DragEvent.ACTION_DRAG_ENTERED:
                    setBackgroundColor(0x10000000);
                    return true;
                case android.view.DragEvent.ACTION_DRAG_EXITED:
                    setBackgroundColor(0);
                    showGhost = false;
                    invalidate();
                    return true;
                case android.view.DragEvent.ACTION_DROP:
                    if (!isGhostValid) return false;

                    float yDrop = ghostRect.top; // Usar la posición del ghost validado
                    LocalTime droppedTime = calculateTimeFromY(yDrop);
                    if (onTimeSlotDroppedListener != null) {
                        onTimeSlotDroppedListener.onTimeSlotDropped(droppedTime, event);
                    }
                    setBackgroundColor(0);
                    showGhost = false;
                    invalidate();
                    return true;
                case android.view.DragEvent.ACTION_DRAG_ENDED:
                    setBackgroundColor(0);
                    showGhost = false;
                    invalidate();
                    return true;
            }
            return false;
        });
    }

    private ValidationResult validatePosition(LocalTime start, long draggingId, int durationMinutes) {
        LocalTime end = start.plusMinutes(durationMinutes);
        
        TaskSlot collision = null;
        for (TaskSlot slot : existingTasks) {
            if (slot.id == draggingId) continue; 
            if (start.isBefore(slot.end) && end.isAfter(slot.start)) {
                collision = slot;
                break;
            }
        }

        if (collision == null) {
            return new ValidationResult(true, null);
        }

        // Si hay colisión, intentar sugerir la posición inmediatamente anterior o posterior
        LocalTime candidateAfter = collision.end;
        LocalTime candidateBefore = collision.start.minusMinutes(durationMinutes);

        // Calcular cuál está más cerca de la posición del dedo
        long diffAfter = Math.abs(java.time.Duration.between(start, candidateAfter).toMinutes());
        long diffBefore = Math.abs(java.time.Duration.between(start, candidateBefore).toMinutes());

        LocalTime primarySuggestion = (diffAfter < diffBefore) ? candidateAfter : candidateBefore;
        LocalTime secondarySuggestion = (primarySuggestion == candidateAfter) ? candidateBefore : candidateAfter;

        if (canFit(primarySuggestion, durationMinutes, draggingId)) {
            return new ValidationResult(false, primarySuggestion);
        }
        if (canFit(secondarySuggestion, durationMinutes, draggingId)) {
            return new ValidationResult(false, secondarySuggestion);
        }

        return new ValidationResult(false, null);
    }

    private boolean canFit(LocalTime start, int durationMinutes, long draggingId) {
        LocalTime end = start.plusMinutes(durationMinutes);
        
        // Validar límites del Timeline (considerando medianoche)
        if (start.getHour() < startHour) return false;
        
        boolean endsAfterRange;
        if (end.equals(LocalTime.MIDNIGHT)) {
            endsAfterRange = endHour < 24;
        } else if (end.isBefore(start)) { // Cruzó medianoche
            endsAfterRange = true;
        } else {
            endsAfterRange = end.getHour() > endHour || (end.getHour() == endHour && end.getMinute() > 0);
        }
        
        if (endsAfterRange) return false;
        
        for (TaskSlot slot : existingTasks) {
            if (slot.id == draggingId) continue;
            if (start.isBefore(slot.end) && end.isAfter(slot.start)) return false;
        }
        return true;
    }

    private static class ValidationResult {
        final boolean isValid;
        final LocalTime suggestedStart;

        ValidationResult(boolean isValid, LocalTime suggestedStart) {
            this.isValid = isValid;
            this.suggestedStart = suggestedStart;
        }
    }

    public LocalTime calculateTimeFromY(float y) {
        // Asegurar que y no sea negativo para evitar cálculos de minutos inválidos
        if (y < 0) y = 0;

        // Cada hora tiene 'hourHeight' pixeles. La primera hora es 'startHour'.
        float hourFraction = y / (float) hourHeight;
        int hours = (int) hourFraction;
        float totalMinutes = (hourFraction - hours) * 60;
        
        // Redondear a intervalos de 5 minutos con mayor precisión
        int roundedMinutes = (int) (Math.round(totalMinutes / 5.0) * 5);
        
        int finalHour = startHour + hours;
        if (roundedMinutes == 60) {
            finalHour++;
            roundedMinutes = 0;
        }

        // Validar límites superiores (24:00)
        if (finalHour >= 24) {
            finalHour = 23;
            roundedMinutes = 55;
        }
        
        // Esta comprobación es extra de seguridad, aunque y ya se clampó a 0
        if (finalHour < 0) {
            finalHour = 0;
            roundedMinutes = 0;
        }
        
        return LocalTime.of(finalHour, roundedMinutes);
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

        // Dibujar vista previa fantasma AL FINAL
        if (showGhost) {
            int baseColor = isGhostValid ? 
                ContextCompat.getColor(getContext(), R.color.primary) : 
                android.graphics.Color.RED;
            
            // Efecto de sombra/resplandor radial desde la posición del dedo
            RadialGradient gradient = new RadialGradient(
                dragX, dragY, 
                Math.max(ghostRect.width(), ghostRect.height()) * 1.5f,
                new int[]{Color.argb(100, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)), 
                          Color.argb(0, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))},
                null, Shader.TileMode.CLAMP
            );
            
            shadowPaint.setShader(gradient);
            
            canvas.drawRoundRect(ghostRect, cornerRadius, cornerRadius, shadowPaint);
            
            if (isGhostValid) {
                canvas.drawRoundRect(ghostRect, cornerRadius, cornerRadius, ghostBorderPaint);
            }
        }
    }
}
