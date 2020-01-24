package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public class DrawThread extends Thread {

        private SurfaceHolder surfaceHolder;
        private Paint backgroundPaint = new Paint();

        {
            backgroundPaint.setColor(Color.BLUE);
        }

        private Paint p = new Paint();

        {
            p.setColor(Color.YELLOW);
        }

        private int towardPointX;
        private int towardPointY;
        private int radius;
        private volatile boolean running = true, touch;//флаг для остановки потока

        public DrawThread(Context context, SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void requestStop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    try {
                        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
                        if (touch) {
                            canvas.drawCircle(towardPointX, towardPointY, radius, p);
                            radius += 5;
                        }
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void setTowardPoint(int x, int y) {
            running = false;
            towardPointX = x;
            towardPointY = y;
            radius = 0;
            touch = true;
            running = true;
        }
    }

    public TestSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private DrawThread drawThread;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getContext(), getHolder());
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawThread.requestStop();
        boolean retry = true;
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        drawThread.setTowardPoint((int) event.getX(), (int) event.getY());
        return false;
    }

}
