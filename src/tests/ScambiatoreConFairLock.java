package tests;

import tests.fairlock.Condition;
import tests.fairlock.FairLock;

public class ScambiatoreConFairLock implements IScambiatore {
	private int buffer1, buffer2;
	private boolean primo = true;
	private FairLock lock = new FairLock();
	private Condition attesa_secondo = lock.newCondition();

	public int scambia(int x) throws InterruptedException {
		lock.lock();
		Logger.log("request to swap with value " + x);
		try {
			if (primo) {
				primo = false;
				buffer1 = x;
				Logger.log("waiting for second thread");
				attesa_secondo.await();
				primo = true;
				Logger.log("swap done (" + x + " <-> " + buffer2 + ")");
				return buffer2;
			} else {
				buffer2 = x;
				Logger.log("signaling first thread");
				attesa_secondo.signal();
				return buffer1;
			}
		} finally {
			lock.unlock();
		}
	}
}
