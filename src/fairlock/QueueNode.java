package fairlock;

class QueueNode {
	private boolean notified = false;

	final Thread thread;

	public QueueNode(Thread thread) {
		this.thread = thread;
	}

	public synchronized void doWait() throws InterruptedException {
		while (!notified) {
			wait();
		}
		notified = false;
	}

	public synchronized void doNotify() {
		notified = true;
		notify();
	}
}
