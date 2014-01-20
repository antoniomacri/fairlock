package tests;

public class MailboxConsumer extends Thread {
	private IMailbox mailbox;
	private int count;

	public MailboxConsumer(IMailbox mailbox, int count) {
		this.mailbox = mailbox;
		this.count = count;
	}

	public void run() {
		for (int i = 0; i < count; i++) {
			try {
				mailbox.fetch();
			} catch (InterruptedException e) {
				Logger.log("interrupted");
			}
		}
	}
}
