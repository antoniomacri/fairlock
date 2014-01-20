package tests.fairlock;

import java.util.ArrayDeque;

import tests.Logger;

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
				Logger.log("owner is " + thisThread.getName());
				return;
			}
			node = new QueueNode(thisThread);
			entryQueue.add(node);
			Logger.log("added thread " + thisThread.getName()
					+ " to the entry queue");
		}
		try {
			node.doWait();
			assert (owner == thisThread);
			assert (entryQueue.peekFirst() == node);
		} finally {
			synchronized(mutex) {
				entryQueue.remove(node);
				Logger.log("removed thread " + thisThread.getName()
						+ " from the entry queue");
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
				Logger.log("owner is null");
				return;
			}
			owner = node.thread;
			Logger.log("owner is " + node.thread.getName());
			node.doNotify();
		}
	}

	public Condition newCondition() {
		return new Condition(this);
	}
}
