package com.vodka.cheto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/** Offline screenshot training view. It never reads or controls another app. */
public class AnalysisView extends View {
    private Bitmap image;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<PointF> balls = new ArrayList<>();
    private PointF cue;
    private boolean predictions;
    private RectF table = new RectF();

    public AnalysisView(Context c) {
        super(c);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(5f);
        setBackgroundColor(Color.rgb(18,18,18));
    }

    public void setImage(Bitmap b) {
        image = b;
        predictions = false;
        detectBalls();
        invalidate();
    }

    public void setPredictionsEnabled(boolean enabled) {
        predictions = enabled;
        invalidate();
    }

    public void clear() {
        image = null;
        balls.clear();
        cue = null;
        predictions = false;
        invalidate();
    }

    private void detectBalls() {
        balls.clear(); cue = null;
        if (image == null) return;
        int w = image.getWidth(), h = image.getHeight();
        int sx = Math.max(0, w / 12), sy = Math.max(0, h / 12);
        int ex = w - sx, ey = h - sy;
        table.set(sx, sy, ex, ey);

        // Lightweight offline color scan. It is intentionally approximate.
        int step = Math.max(4, Math.min(w,h) / 180);
        for (int y = sy; y < ey; y += step) {
            for (int x = sx; x < ex; x += step) {
                int c = image.getPixel(x,y);
                int r=Color.red(c), g=Color.green(c), b=Color.blue(c);
                boolean greenTable = g > r + 25 && g > b + 20 && g > 70;
                if (greenTable) continue;
                int brightness=r+g+b;
                if (brightness < 90 || brightness > 745) continue;
                if (!looksCircular(x,y,w,h)) continue;
                PointF q = new PointF(x,y);
                if (r>190 && g>190 && b>190) {
                    if (cue == null) cue=q;
                } else {
                    boolean near=false;
                    for(PointF old:balls) if(dist(old,q)<22){near=true;break;}
                    if(!near && balls.size()<16) balls.add(q);
                }
            }
        }
        if (cue == null && !balls.isEmpty()) {
            // Offline fallback only: choose the brightest candidate as cue-like.
            float best=-1; PointF bestP=null;
            for(PointF q:balls){ int c=image.getPixel((int)q.x,(int)q.y); float v=Color.red(c)+Color.green(c)+Color.blue(c); if(v>best){best=v;bestP=q;} }
            cue=bestP;
            if(cue!=null) balls.remove(cue);
        }
    }

    private boolean looksCircular(int x,int y,int w,int h){
        int[][] d={{10,0},{-10,0},{0,10},{0,-10}};
        int good=0;
        for(int[] a:d){int xx=x+a[0], yy=y+a[1]; if(xx<0||yy<0||xx>=w||yy>=h) continue; int c=image.getPixel(xx,yy); int r=Color.red(c),g=Color.green(c),b=Color.blue(c); if(!(g>r+25&&g>b+20&&g>70)) good++;}
        return good>=3;
    }
    private float dist(PointF a,PointF b){float dx=a.x-b.x,dy=a.y-b.y;return (float)Math.hypot(dx,dy);}

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if(image==null){p.setColor(Color.LTGRAY);p.setTextSize(18);c.drawText("Load a billiards screenshot to begin",30,50,p);return;}
        float scale=Math.min(getWidth()/(float)image.getWidth(), getHeight()/(float)image.getHeight());
        float ox=(getWidth()-image.getWidth()*scale)/2f, oy=(getHeight()-image.getHeight()*scale)/2f;
        c.drawBitmap(image,null,new RectF(ox,oy,ox+image.getWidth()*scale,oy+image.getHeight()*scale),p);
        if(cue==null) return;
        drawPoint(c,cue,Color.CYAN,18,"CUE",scale,ox,oy);
        for(int i=0;i<balls.size();i++) drawPoint(c,balls.get(i),Color.YELLOW,14,String.valueOf(i+1),scale,ox,oy);
        if(predictions) drawPrediction(c,scale,ox,oy);
    }

    private void drawPoint(Canvas c,PointF q,int color,float r,String label,float s,float ox,float oy){
        float x=ox+q.x*s,y=oy+q.y*s;
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(color);c.drawCircle(x,y,r,p);
        p.setStyle(Paint.Style.FILL);p.setTextSize(14);p.setColor(Color.WHITE);c.drawText(label,x+8,y-8,p);
    }

    private void drawPrediction(Canvas c,float s,float ox,float oy){
        if(cue==null) return;
        PointF target=null;float best=Float.MAX_VALUE;
        for(PointF b:balls){float d=dist(cue,b);if(d<best){best=d;target=b;}}
        if(target==null) return;
        float dx=target.x-cue.x,dy=target.y-cue.y;float len=(float)Math.hypot(dx,dy);if(len<1)return;
        float ux=dx/len,uy=dy/len;
        float endX=target.x+ux*260,endY=target.y+uy*260;
        line.setColor(Color.argb(220,0,220,255));line.setStrokeWidth(5);c.drawLine(ox+cue.x*s,oy+cue.y*s,ox+endX*s,oy+endY*s,line);
        line.setColor(Color.argb(90,0,220,255));line.setStrokeWidth(18);c.drawLine(ox+cue.x*s,oy+cue.y*s,ox+endX*s,oy+endY*s,line);
        p.setColor(Color.CYAN);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawCircle(ox+target.x*s,oy+target.y*s,22,p);p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);p.setTextSize(16);c.drawText("PREDICTION",ox+cue.x*s+10,oy+cue.y*s-20,p);
    }
}
