package com.taihuoniao.shop.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ClipCircleImageView extends ImageView {
	public ClipCircleImageView(Context context) {
		super(context);
	}

	public ClipCircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ClipCircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private boolean set = false;
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(getCroppedBitmap(bm));
		set = true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (!set) {
			set = true;
			super.setImageBitmap(getCroppedBitmap(((BitmapDrawable) getDrawable())
					.getBitmap()));
			return;
		}
		super.onDraw(canvas);
	}

	public Bitmap getCroppedBitmap(Bitmap bitmap) {
		if (bitmap == null)
			return bitmap;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int r = width < height ? width:height;
		Bitmap output = Bitmap.createBitmap(r, r, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rectSrc = new Rect((width - r)/2, (height - r)/2, (width + r)/2, (height + r)/2);
		final Rect rectDest = new Rect(0, 0, width, width);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawCircle(r/2, r / 2, r / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rectSrc, rectDest, paint);
		return output;
	}
}