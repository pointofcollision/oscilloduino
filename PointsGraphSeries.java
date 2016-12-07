package com.mycompany.usbmanagertest;

/**
 * Created by Luke on 12/2/2016.
 */

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.*;

import java.util.Iterator;
//package com.jjoe64.graphview.series;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Canvas.VertexMode;
import android.graphics.Paint.Cap;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPointInterface;
import java.util.Iterator;

public class PointsGraphSeries<E extends DataPointInterface> extends com.mycompany.usbmanagertest.BaseSeries<E> {
    private PointsGraphSeries.Styles mStyles;
    private Paint mPaint;
    private PointsGraphSeries.CustomShape mCustomShape;

    public PointsGraphSeries() {
        this.init();
    }

    public PointsGraphSeries(E[] data) {
        super(data);
        this.init();
    }

    protected void init() {
        this.mStyles = new PointsGraphSeries.Styles();
        this.mStyles.size = 20.0F;
        this.mPaint = new Paint();
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.setShape(PointsGraphSeries.Shape.POINT);
    }

    public void draw(GraphView graphView, Canvas canvas, boolean isSecondScale) {
        this.resetDataPoints();
        double maxX = graphView.getViewport().getMaxX(false);
        double minX = graphView.getViewport().getMinX(false);
        double maxY;
        double minY;
        if(isSecondScale) {
            maxY = graphView.getSecondScale().getMaxY();
            minY = graphView.getSecondScale().getMinY();
        } else {
            maxY = graphView.getViewport().getMaxY(false);
            minY = graphView.getViewport().getMinY(false);
        }

        Iterator values = this.getValues(minX, maxX);
        double lastEndY = 0.0D;
        double lastEndX = 0.0D;
        this.mPaint.setColor(this.getColor());
        double diffY = maxY - minY;
        double diffX = maxX - minX;
        float graphHeight = (float)graphView.getGraphContentHeight();
        float graphWidth = (float)graphView.getGraphContentWidth();
        float graphLeft = (float)graphView.getGraphContentLeft();
        float graphTop = (float)graphView.getGraphContentTop();
        lastEndY = 0.0D;
        lastEndX = 0.0D;
        float firstX = 0.0F;

        for(int i = 0; values.hasNext(); ++i) {
            DataPointInterface value = (DataPointInterface)values.next();
            double valY = value.getY() - minY;
            double ratY = valY / diffY;
            double y = (double)graphHeight * ratY;
            double valX = value.getX() - minX;
            double ratX = valX / diffX;
            double x = (double)graphWidth * ratX;
            boolean overdraw = false;
            if(x > (double)graphWidth) {
                overdraw = true;
            }

            if(y < 0.0D) {
                overdraw = true;
            }

            if(y > (double)graphHeight) {
                overdraw = true;
            }

            float endX = (float)x + graphLeft + 1.0F;
            float endY = (float)((double)graphTop - y) + graphHeight;
            this.registerDataPoint(endX, endY, (E)value);
            if(!overdraw) {
                if(this.mCustomShape != null) {
                    this.mCustomShape.draw(canvas, this.mPaint, endX, endY, value);
                } else if(this.mStyles.shape == PointsGraphSeries.Shape.POINT) {
                    canvas.drawCircle(endX, endY, this.mStyles.size, this.mPaint);
                } else if(this.mStyles.shape == PointsGraphSeries.Shape.RECTANGLE) {
                    canvas.drawRect(endX - this.mStyles.size, endY - this.mStyles.size, endX + this.mStyles.size, endY + this.mStyles.size, this.mPaint);
                } else if(this.mStyles.shape == PointsGraphSeries.Shape.TRIANGLE) {
                    Point[] points = new Point[]{new Point((int)endX, (int)(endY - this.getSize())), new Point((int)(endX + this.getSize()), (int)((double)endY + (double)this.getSize() * 0.67D)), new Point((int)(endX - this.getSize()), (int)((double)endY + (double)this.getSize() * 0.67D))};
                    this.drawArrows(points, canvas, this.mPaint);
                }
            }
        }

    }

    private void drawArrows(Point[] point, Canvas canvas, Paint paint) {
        float[] points = new float[]{(float)point[0].x, (float)point[0].y, (float)point[1].x, (float)point[1].y, (float)point[2].x, (float)point[2].y, (float)point[0].x, (float)point[0].y};
        canvas.drawVertices(Canvas.VertexMode.TRIANGLES, 8, points, 0, (float[])null, 0, (int[])null, 0, (short[])null, 0, 0, paint);
        Path path = new Path();
        path.moveTo((float)point[0].x, (float)point[0].y);
        path.lineTo((float)point[1].x, (float)point[1].y);
        path.lineTo((float)point[2].x, (float)point[2].y);
        canvas.drawPath(path, paint);
    }

    public float getSize() {
        return this.mStyles.size;
    }

    public void setSize(float radius) {
        this.mStyles.size = radius;
    }

    public PointsGraphSeries.Shape getShape() {
        return this.mStyles.shape;
    }

    public void setShape(PointsGraphSeries.Shape s) {
        this.mStyles.shape = s;
    }

    public void setCustomShape(PointsGraphSeries.CustomShape shape) {
        this.mCustomShape = shape;
    }

    private final class Styles {
        float size;
        PointsGraphSeries.Shape shape;

        private Styles() {
        }
    }

    public static enum Shape {
        POINT,
        TRIANGLE,
        RECTANGLE;

        private Shape() {
        }
    }

    public interface CustomShape {
        void draw(Canvas var1, Paint var2, float var3, float var4, DataPointInterface var5);
    }
}
