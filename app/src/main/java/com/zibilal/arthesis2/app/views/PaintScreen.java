package com.zibilal.arthesis2.app.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmuhamm on 5/24/14.
 */
public class PaintScreen {
    private static final String TAG="PaintScreen";

    public static final int DEFAULT_TEXT_SIZE=22;
    private Paint mPaint;
    private int mTextSize;

    public PaintScreen(int color) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        mTextSize=DEFAULT_TEXT_SIZE;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public void setPaint(int color, int textSize) {
        mPaint.setTextSize(textSize);
        mPaint.setColor(color);
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void circle(Canvas canvas, float x, float y, float radius) {
        Log.d(TAG, String.format("---->> The cirles= x:%.4f y: %.4f radius:%.4f", x, y, radius));
        canvas.drawCircle(x, y, radius, mPaint);
    }

    public float boxedText(Canvas canvas, String str, int maxLen, float x, float y, float padding) {
        float left=x;
        mPaint.setTextSize(mTextSize);
        mPaint.setStyle(Paint.Style.STROKE);
        float top =y +  mPaint.ascent();
        float textWidth=0;
        int textHeight=(int) (-mPaint.ascent() + mPaint.descent());

        if(str.length() > maxLen) {
            String[] splits = str.split(" ");
            StringBuilder row = new StringBuilder("");
            List<String> rows = new ArrayList<String>();
            int offset=0;
            int index=0;
            int i;
            String tmp="";

            while(offset < str.length()) {
                for(i=index; i < splits.length && (row.length() < maxLen); i++) {
                    if(i < splits.length - 1)
                        row.append(tmp + splits[i] + " " );
                    else
                        row.append(tmp + splits[i]);
                }

                offset += row.length();

                if(row.length() > maxLen ) {
                    int m = row.length() - maxLen;
                    tmp = row.substring(row.length() - m, row.length());
                    row.delete(row.length() - m, row.length());
                    offset -= m;
                } else if(tmp != null && tmp.length() > 0){
                    offset += tmp.length();
                    row.append(tmp);
                }

                int tempwidth = (int) mPaint.measureText(row.toString());
                textWidth = tempwidth > textWidth ? tempwidth : textWidth;
                rows.add(row.toString());
                row.delete(0, row.length());
                index=i;
            }

            for(String s : rows) {
                canvas.drawText(s, x, y, mPaint);
                y += textHeight;
            }

            RectF rect = new RectF();
            rect.set(left - padding, top - padding, left + textWidth + padding, y + mPaint.ascent() + padding);
            canvas.drawRect(rect, mPaint);

            return y - mPaint.ascent() + padding;
        } else {
            canvas.drawText(str, x, y, mPaint);
            textWidth = (int) mPaint.measureText(str);
            RectF rect = new RectF();
            rect.set(left - padding, top - padding, left + textWidth + padding, y + padding);
            canvas.drawRect(rect, mPaint);

            return y - mPaint.ascent() + padding;
        }
    }
}
