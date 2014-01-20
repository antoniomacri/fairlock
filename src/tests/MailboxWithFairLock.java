package tests;

import tests.fairlock.Condition;
import tests.fairlock.FairLock;

public class MailboxWithFairLock implements IMailbox {
	private final String[] buffer;
	private final int capacity;

	private int front;
	private int rear;
	private int count;

	private final FairLock lock = new FairLock();
	private final Condition notFull = lock.newCondition();
	private final Condition notEmpty = lock.newCondition();

	public MailboxWithFairLock(int capacity) {
		this.capacity = capacity;
		this.buffer = new String[capacity];
	}

	public void deposit(String message) throws InterruptedException {
		lock.lock();
		Logger.log("request to deposit");
		try {
			if (count == capacity) {
				Logger.log("mailbox is full");
				notFull.await();
			}

			buffer[rear] = message;
			rear = (rear + 1) % capacity;
			count++;
			Logger.log("deposit done");

			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	public String fetch() throws InterruptedException {
		lock.lock();
		Logger.log("request to fetch");
		try {
			if (count == 0) {
				Logger.log("mailbox is empty");
				notEmpty.await();
			}

			String message = buffer[front];
			front = (front + 1) % capacity;
			count--;
			Logger.log("fetch done");

			notFull.signal();

			return message;
		} finally {
			lock.unlock();
		}
	}
}
