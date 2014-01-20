package tests.fairlock;

import java.util.ArrayDeque;

import tests.Logger;

public class Condition {
	private FairLock lock;
	private ArrayDeque queue = new ArrayDeque();

	Condition(FairLock lock) {
		this.lock = lock;
	}

	public void await() throws InterruptedException {
		Thread thisThread = Thread.currentThread();
		if (lock.owner != thisThread) {
			throw new IllegalMonitorStateException(
					"The current thread does not hold the lock "
							+ "associated with this Condition");
		}
		QueueNode node = new QueueNode(thisThread);
		synchronized(queue) {
			queue.add(node);
			Logger.log("added thread " + thisThread.getName()
					+ " to the condition queue");
		}
		lock.unlock();
		try {
			node.doWait();
			assert (lock.owner == thisThread);
			assert (queue.peekFirst() == node);
		} finally {
			synchronized(queue) {
				queue.remove(node);
				Logger.log("removed thread " + thisThread.getName()
						+ " from the condition queue");
			}
		}
	}

	public void signal() throws InterruptedException {
		Thread thisThread = Thread.currentThread();
		if (lock.owner != thisThread) {
			throw new IllegalMonitorStateException(
					"The current thread does not hold the lock "
							+ "associated with this Condition");
		}
		QueueNode node = (QueueNode) queue.peekFirst();
		if (node != null) {
			QueueNode urgentNode = new QueueNode(thisThread);
			assert (lock.urgentQueue.isEmpty());
			lock.urgentQueue.add(urgentNode);
			Logger.log("added thread " + thisThread.getName()
					+ " to the urgent queue");
			lock.owner = node.thread;
			Logger.log("owner is " + node.thread.getName());
			node.doNotify();
			try {
				urgentNode.doWait();
				assert (lock.owner == thisThread);
			} finally {
				assert (lock.urgentQueue.peekFirst() == urgentNode);
				lock.urgentQueue.pop();
				Logger.log("removed thread " + thisThread.getName()
						+ " from the urgent queue");
			}
		}
	}
}
