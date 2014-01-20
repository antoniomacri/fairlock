package fairlock;

import java.util.ArrayDeque;

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
		}
		lock.unlock();
		try {
			node.doWait();
		} finally {
			synchronized(queue) {
				queue.remove(node);
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
			lock.urgentQueue.add(urgentNode);
			lock.owner = node.thread;
			node.doNotify();
			try {
				urgentNode.doWait();
			} finally {
				lock.urgentQueue.pop();
			}
		}
	}
}
