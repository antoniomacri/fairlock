package tests;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ScambiatoreConJavaLock implements IScambiatore {
	private int buffer1, buffer2;
	private boolean primo = true;
	private boolean secondo = true;
	private ReentrantLock lock = new ReentrantLock();
	private Condition attesa_secondo = lock.newCondition();
	private Condition fine_primo = lock.newCondition();

	public int scambia(int x) throws InterruptedException {
		lock.lock();
		Logger.log("request to swap with value " + x);
		try {
			while (!primo && !secondo) {
				fine_primo.await();
			}
			if (primo) {
				primo = false;
				buffer1 = x;
				Logger.log("waiting for second thread");
				attesa_secondo.await();
				primo = true;
				secondo = true;
				fine_primo.signalAll();
				Logger.log("swap done (" + x + " <-> " + buffer2 + ")");
				return buffer2;
			} else {
				buffer2 = x;
				secondo = false;
				Logger.log("signaling first thread");
				attesa_secondo.signalAll();
				return buffer1;
			}
		} finally {
			lock.unlock();
		}
	}
}
