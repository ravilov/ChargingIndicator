package hr.ravilov.charging;

import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class MainReceiver extends Base.Receiver {
	private static final Map<String, Class<? extends Activity>> handlers = new HashMap<String, Class<? extends Activity>>();
	static {
		handlers.put("BOOT_COMPLETED", OnBootCompleted.class);
		handlers.put("POWER_CONNECTED", OnPowerConnected.class);
		handlers.put("POWER_DISCONNECTED", OnPowerDisconnected.class);
	}
	public static final int INTENT_FLAGS =
		Intent.FLAG_ACTIVITY_NEW_TASK |
		Intent.FLAG_ACTIVITY_NO_HISTORY |
		Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
		Intent.FLAG_FROM_BACKGROUND |
		Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
	;
	protected static final String EXTRA_BASE = "action_base";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		final String base = getBaseAction(action);
		Utils.Log.i(TAG, "got action %1$s -> %2$s", action, base);
		final Class<? extends Activity> activity = handlers.get(base);
		if (activity != null) {
			final Intent launch = new Intent(context, activity);
			launch.setAction(intent.getAction());
			launch.setFlags(INTENT_FLAGS);
			launch.putExtras(intent);
			launch.putExtra(EXTRA_BASE, base);
			context.startActivity(launch);
		} else {
			Utils.Log.e(TAG, "don't know how to handle action '%1$s'", intent.getAction());
		}
	}

	private String getBaseAction(final String action) {
		final String last = Utils.getFinalComponent(action);
		final String pfx = "ACTION_";
		if (last.substring(0, pfx.length()).equals(pfx)) {
			return last.substring(pfx.length());
		}
		return last;
	}

	public static abstract class LocalActivity extends Base.Activity implements View.OnClickListener {
		protected static final float IMG_SIZE = 0.7f;	// percentage
		protected static final int DELAY_MS = 1500;
		protected final Handler ui = new Handler();
		protected ImageView image = null;

		protected static enum State {
			UNKNOWN,
			OFF,
			ON,
		}

		@Override
		public void onCreate(final Bundle saved) {
			super.onCreate(saved);
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.popup);
			image = (ImageView)findViewById(R.id.splash);
			if (image != null) {
				image.setOnClickListener(this);
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			ui.postDelayed(new Runnable() {
				@Override
				public void run() {
					finish();
				}
			}, DELAY_MS);
		}

		@Override
		public void onPause() {
			super.onPause();
			finish();
		}

		@Override
		public void onClick(final View view) {
			finish();
		}

		protected void setState(final State state) {
			int img1 = 0;
			int img2 = 0;
			CharSequence msg = null;
			switch (state) {
				case ON: {
					img1 = R.drawable.on;
					img2 = R.drawable.on_icon;
					msg = getText(R.string.desc_charging);
					break;
				}
				case OFF: {
					img1 = R.drawable.off;
					img2 = R.drawable.off_icon;
					msg = getText(R.string.desc_discharging);
					break;
				}
				default: {
					img1 = 0;
					img2 = 0;
					msg = getText(R.string.desc_unknown);
					break;
				}
			}
			if (image != null) {
				final DisplayMetrics dm = getResources().getDisplayMetrics();
				final int dim = Math.round(Math.min(dm.widthPixels, dm.heightPixels) * IMG_SIZE);
				image.setImageBitmap((img1 > 0) ? resizeBitmap(img1, dim, dim) : null);
				image.setContentDescription(msg);
			}
			final NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancelAll();
			final Notification ntf = new Notification((img2 > 0) ? img2 : R.drawable.icon, msg, System.currentTimeMillis());
			switch (state) {
				case ON: {
					ntf.flags |= Notification.FLAG_ONGOING_EVENT;
					break;
				}
				default: {
					ntf.flags |= Notification.FLAG_AUTO_CANCEL;
					break;
				}
			}
			try {
				ntf.setLatestEventInfo(
					this,
					msg,
					"",
					PendingIntent.getActivity(
						this,
						0,
						new Intent(),
						PendingIntent.FLAG_UPDATE_CURRENT
					)
				);
			}
			catch (final Throwable ignore) { }
			nm.notify(Process.myPid(), ntf);
			if (state == State.OFF) {
//				nm.cancelAll();
			}
		}
	}

	public static class OnBootCompleted extends LocalActivity {
		@Override
		public void onCreate(final Bundle saved) {
			super.onCreate(saved);
			try {
				final Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				final int plugged = i.getIntExtra("plugged", -1);
				setState((plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB) ? State.ON : State.OFF);
			}
			catch (final Throwable ignore) { }
		}
	}

	public static class OnPowerConnected extends LocalActivity {
		@Override
		public void onCreate(final Bundle saved) {
			super.onCreate(saved);
			setState(State.ON);
		}
	}

	public static class OnPowerDisconnected extends LocalActivity {
		@Override
		public void onCreate(final Bundle saved) {
			super.onCreate(saved);
			setState(State.OFF);
		}
	}
}
