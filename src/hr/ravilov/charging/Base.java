package hr.ravilov.charging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Base {
	private Base() {
	}

	private static final String TAG_PREFIX = "CHARGE::";

	protected static final String mkTagFromClass(final Class<?> c) {
		if (c == null) {
			return null;
		}
		return TAG_PREFIX + Utils.getClassName(c);
	}

	protected static final String mkTag(final Object obj) {
		if (obj == null) {
			return null;
		}
		return mkTagFromClass(obj.getClass());
	}

	public static Bitmap resizeBitmap(final Context context, final int resId, int w, int h) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), resId, options);
		options.inJustDecodeBounds = false;
		final int width = options.outWidth;
		final int height = options.outHeight;
		if (w >= width && h >= height) {
			return BitmapFactory.decodeResource(context.getResources(), resId);
		}
		final float aspect1 = (float)width / (float)height;
		final float aspect2 = (float)w / (float)h;
		if (aspect1 > aspect2) {
			h = (int)Math.round(Math.floor((float)w / aspect1));
		}
		if (aspect1 < aspect2) {
			w = (int)Math.round(Math.floor((float)h * aspect1));
		}
		final int halfHeight = height / 2;
		final int halfWidth = width / 2;
		options.inSampleSize = 1;
		while ((halfWidth / options.inSampleSize) > w && (halfHeight / options.inSampleSize) > h) {
			options.inSampleSize *= 2;
		}
		return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), resId, options), w, h, true);
	}

	public static abstract class Activity extends android.app.Activity {
		protected final String TAG = mkTag(this);

		protected Bitmap resizeBitmap(final int resId, final int w, final int h) {
			return Base.resizeBitmap(this, resId, w, h);
		}
	}

	public static abstract class Receiver extends android.content.BroadcastReceiver {
		protected final String TAG = mkTag(this);
	}
}
