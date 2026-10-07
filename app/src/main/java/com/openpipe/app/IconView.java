package com.openpipe.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Hand-drawn vector icons (no drawables, no libraries). */
final class IconView extends View {
    static final int RULES = 0, HISTORY = 1, SETTINGS = 2, PLUS = 3, PLAY = 4, BACK = 5;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final int type;

    IconView(Context c, int type, int color) {
        super(c);
        this.type = type;
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        float s = Math.min(getWidth(), getHeight());
        float u = s / 24f;
        c.save();
        c.translate((getWidth() - s) / 2f, (getHeight() - s) / 2f);
        c.scale(u, u);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        switch (type) {
            case RULES:
                c.drawLine(4, 7, 20, 7, paint);
                c.drawLine(4, 12, 20, 12, paint);
                c.drawLine(4, 17, 20, 17, paint);
                paint.setStyle(Paint.Style.FILL);
                c.drawCircle(8, 7, 2.6f, paint);
                c.drawCircle(16, 12, 2.6f, paint);
                c.drawCircle(10, 17, 2.6f, paint);
                break;
            case HISTORY:
                c.drawCircle(12, 12, 8f, paint);
                c.drawLine(12, 7, 12, 12, paint);
                c.drawLine(12, 12, 15.5f, 14, paint);
                break;
            case SETTINGS:
                c.drawCircle(12, 12, 6.5f, paint);
                c.drawCircle(12, 12, 2.2f, paint);
                paint.setStrokeWidth(3f);
                for (int k = 0; k < 8; k++) {
                    double a = Math.PI / 4 * k;
                    float cs = (float) Math.cos(a), sn = (float) Math.sin(a);
                    c.drawLine(12 + cs * 8.2f, 12 + sn * 8.2f, 12 + cs * 10f, 12 + sn * 10f, paint);
                }
                break;
            case PLUS:
                paint.setStrokeWidth(2.4f);
                c.drawLine(12, 5, 12, 19, paint);
                c.drawLine(5, 12, 19, 12, paint);
                break;
            case PLAY:
                paint.setStyle(Paint.Style.FILL);
                path.reset();
                path.moveTo(8, 5);
                path.lineTo(19, 12);
                path.lineTo(8, 19);
                path.close();
                c.drawPath(path, paint);
                break;
            case BACK:
                paint.setStrokeWidth(2.4f);
                c.drawLine(15, 5, 8, 12, paint);
                c.drawLine(8, 12, 15, 19, paint);
                break;
            default:
                break;
        }
        c.restore();
    }
}
