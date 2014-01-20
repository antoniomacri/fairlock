package tests;

import java.io.StringWriter;

public class Logger {
	private static class Log extends Throwable {
		private static final long serialVersionUID = 1L;

		public Log(String message) {
			super(message);
		}

		public String toString() {
			return super.getMessage();
		}

		public String getStackTraceString() {
			StringWriter sw = new StringWriter();
			sw.write(this.toString() + "\n");
			StackTraceElement[] trace = getStackTrace();
			for (int i = 1; i < trace.length; i++) {
				sw.write("\tat " + trace[i] + "\n");
			}
			return sw.toString();
		}
	}

	public static boolean showStackTrace = false;

	public static void log(String message) {
		String threadName = Thread.currentThread().getName();
		message = "In thread " + threadName + ": " + message + ".";
		if (showStackTrace) {
			System.out.println(new Log(message).getStackTraceString());
		} else {
			System.out.println(message);
		}
	}
}
