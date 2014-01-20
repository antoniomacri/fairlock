package fairlock;

import java.util.ArrayDeque;

public class FairLock {
	Thread owner = null;
	private final Object mutex = new Object();
	private ArrayDeque entryQueue = new ArrayDeque();
	ArrayDeque urgentQueue = new ArrayDeque();

	boolean isLocked() {
		return owner != null;
	}

	public void lock() throws InterruptedException {
		Thread thisThread = Thread.currentThread();
		if (owner == thisThread) {
			throw new IllegalMonitorStateException(
					"The current thread has already locked this lock");
		}
		QueueNode node;
		synchronized(mutex) {
			if (!isLocked()) {
				owner = thisThread;
				return;
			}
			node = new QueueNode(thisThread);
			entryQueue.add(node);
		}
		try {
			node.doWait();
		} finally {
			synchronized(mutex) {
				entryQueue.remove(node);
			}
		}
	}

	public void unlock() {
		if (owner != Thread.currentThread()) {
			throw new IllegalMonitorStateException(
					"The current thread has not locked this lock");
		}
		synchronized(mutex) {
			QueueNode node;
			if (!urgentQueue.isEmpty()) {
				node = (QueueNode) urgentQueue.getFirst();
			} else if (!entryQueue.isEmpty()) {
				node = (QueueNode) entryQueue.getFirst();
			} else {
				owner = null;
				return;
			}
			owner = node.thread;
			node.doNotify();
		}
	}

	public Condition newCondition() {
		return new Condition(this);
	}
}
