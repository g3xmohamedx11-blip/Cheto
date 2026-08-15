package com.vodka.cheto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
importimport android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {

    private List<PointF> trajectory = new ArrayList<>();
    private Paint linePaint = new Paint();
    private Paint glowPaint = new Paint();

    public OverlayView(Context context) {
        super(context);
        setFocusable(false);
        setClickable(false);
        setLongClickable(false);

        linePaint.setColor(Color.argb(220, 0, 200, 255));
        linePaint.setStrokeWidth(5);
        linePaint.setAntiAlias(true);

        glowPaint.setColor(Color.argb(60, 0, 150, 255));
        glowPaint.setStrokeWidth(20);
        glowPaint.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void setTrajectory(List<PointF> trajectory) {
        this.trajectory = trajectory;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (trajectory == null || trajectory.size() < 2) return;

        for (int i = 1; i < trajectory.size(); i++) {
            PointF p1 = trajectory.get(i - 1);
            PointF p2 = trajectory.get(i);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, glowPaint);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, linePaint);
        }
    }
}
