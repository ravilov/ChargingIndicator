package hr.ravilov.charging;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import android.content.Context;

public class Utils {
	public static final boolean LOGGING = true;

	private static String myVersion = null;
	private static String myBuild = null;

	public static final String myPackage() {
		try {
			return Utils.class.getPackage().getName();
		}
		catch (final Throwable ex) { }
		return null;
	}

	public static String getClassName(Class<?> c) {
		return (c == null) ? null : getFinalComponent(c.getName());
	}

	public static String getFinalComponent(final String str) {
		if (str == null) {
			return null;
		}
		final List<String> l = Arrays.asList(str.split("\\."));
		return (l == null) ? null : l.get(l.size() - 1);
	}

	public static String getMyVersion(final Context context) {
		if (myVersion == null) {
			try {
				myVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			}
			catch (final Throwable ex) { }
		}
		return myVersion;
	}

	public static String getMyBuild(final Context context) {
		if (myBuild == null) {
			try {
				myBuild = context.getResources().getString(R.string.class.getField("auto_build").getInt(0));
			}
			catch (final Throwable ex) { }
		}
		return myBuild;
	}

	public static String getStackTrace(final Throwable ex) {
		final ByteArrayOutputStream s = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(s);
		ex.printStackTrace(pw);
		pw.close();
		return s.toString();
	}

	public static String getExceptionMessage(final Throwable ex) {
		if (ex == null) {
			return null;
		}
		Throwable t = ex;
		while (t != null) {
			if (ex.getMessage() != null) {
				return ex.getMessage();
			}
			t = t.getCause();
		}
		return ex.toString();
	}

	public static final String join(final String sep, final Object[] list) {
		if (list == null || list.length <= 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final Object s : list) {
			if (!first) {
				sb.append(sep);
			}
			sb.append((s == null) ? s : ((s instanceof String) ? s : s.toString()));
			first = false;
		}
		return sb.toString();
	}

	public static <T> T coalesce(T... list) {
		if (list == null) {
			return null;
		}
		for (final T item : list) {
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	public static class Log {
		public static void i(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.i(tag, String.format(Locale.US, msg, args));
		}

		public static void w(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.w(tag, String.format(Locale.US, msg, args));
		}

		public static void d(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.d(tag, String.format(Locale.US, msg, args));
		}

		public static void v(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.v(tag, String.format(Locale.US, msg, args));
		}

		public static void e(final String tag, final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.e(tag, String.format(Locale.US, msg, args));
		}

		public static void x(final String msg, final Object... args) {
			if (!LOGGING) {
				return;
			}
			android.util.Log.d("<>", String.format(Locale.US, msg, args));
		}
	}
}
