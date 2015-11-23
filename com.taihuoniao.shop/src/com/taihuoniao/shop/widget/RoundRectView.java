package com.taihuoniao.shop.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;


public class RoundRectView extends FrameLayout  {

	public RoundRectView(Context context) {
		super(context);
		Init(context);
	}

	public RoundRectView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		Init(context);
	}

	public RoundRectView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Init(context);
	}
	
	private Paint mPaint;
	private Path mPath;

	public void Init(Context context) {
		this.setWillNotDraw(false);//±ØÐë
		setFocusable(true);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(6);
		mPaint.setTextSize(16);
		mPaint.setTextAlign(Paint.Align.RIGHT);
		mPath = new Path();
	}

	public void onDraw(Canvas canvas) {
		mPath.reset();
		mPath.addRoundRect(new RectF(0, 0, this.getWidth(), this.getHeight()), 5, 5, Path.Direction.CCW);
		canvas.save();
		canvas.drawColor(Color.WHITE);
		canvas.clipPath(mPath, Region.Op.REPLACE);
		super.onDraw(canvas);
		canvas.restore();
	}
}
