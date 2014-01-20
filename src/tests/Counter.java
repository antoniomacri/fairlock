package tests;

import tests.fairlock.Condition;
import tests.fairlock.FairLock;

public class Counter {
	public static int limit = 5;

	private int x = 0;
	private FairLock lock = new FairLock();
	private Condition condition = lock.newCondition();

	public void count() throws InterruptedException {
		lock.lock();
		try {
			if (++x < limit) {
				condition.await();
			}
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

	public static class Thread extends java.lang.Thread {
		private Counter counter;

		public Thread(Counter counter) {
			this.counter = counter;
			try {
				sleep(200);
			} catch (InterruptedException e) {
			}
		}

		public void run() {
			try {
				counter.count();
			} catch (InterruptedException e) {
				Logger.log("interrupted");
			}
		}
	}
}
