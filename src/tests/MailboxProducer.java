package tests;

public class MailboxProducer extends Thread {
	private IMailbox mailbox;
	private int count;

	public MailboxProducer(IMailbox mailbox, int count) {
		this.mailbox = mailbox;
		this.count = count;
	}

	public void run() {
		for (int i = 0; i < count; i++) {
			try {
				String message = "Hello " + Thread.currentThread().getId();
				mailbox.deposit(message);
			} catch (InterruptedException e) {
				Logger.log("interrupted");
			}
		}
	}
}
