package tests;

public class Main {
	/**
	 * Specify whether to use the FairLock or Java's ReentrantLock.
	 */
	private static boolean useFairLock = true;

	/**
	 * The product {@code numberOfProducers*producerCount} must be equal to
	 * {@code numberOfConsumers*consumerCount}, otherwise a consuming thread may
	 * block forever.
	 */
	private static int numberOfProducers = 4;
	private static int numberOfConsumers = 5;
	private static int producerCount = 5;
	private static int consumerCount = 4;
	private static int mailboxCapacity = 3;

	/**
	 * {@code numberOfSwappers} must be an even value, otherwise a thread will
	 * block forever.
	 */
	private static int numberOfSwappers = 6;

	public static void main(String[] args) {
		IMailbox mailbox;
		if (useFairLock) {
			mailbox = new MailboxWithFairLock(mailboxCapacity);
		} else {
			mailbox = new MailboxWithJavaLock(mailboxCapacity);
		}
		// Logger.showStackTrace = true;
		testMailboxSingleThread(mailbox);
		System.out.println();
		// Logger.showStackTrace = true;
		testMailboxManyThreads(mailbox);

		IScambiatore scambiatore;
		if (useFairLock) {
			scambiatore = new ScambiatoreConFairLock();
		} else {
			scambiatore = new ScambiatoreConJavaLock();
		}
		System.out.println();
		// Logger.showStackTrace = true;
		testScambiatore(scambiatore);
	}

	private static void testMailboxSingleThread(IMailbox mailbox) {
		System.out.println("Starting testMailboxSingleThread...");
		try {
			mailbox.deposit("Zero");
			assert (mailbox.fetch() == "Zero");
			mailbox.deposit("First");
			mailbox.deposit("Second");
			assert (mailbox.fetch() == "First");
			assert (mailbox.fetch() == "Second");
		} catch (InterruptedException e) {
			System.out.println("Interrupted.");
		}
		System.out.println("Completed testMailboxSingleThread.");
	}

	private static void testMailboxManyThreads(IMailbox mailbox) {
		System.out.println("Starting testMailboxManyThreads...");
		MailboxProducer[] producers = new MailboxProducer[numberOfProducers];
		MailboxConsumer[] consumers = new MailboxConsumer[numberOfConsumers];
		for (int i = 0; i < numberOfConsumers; i++) {
			(consumers[i] = new MailboxConsumer(mailbox, consumerCount)).start();
		}
		for (int i = 0; i < numberOfProducers; i++) {
			(producers[i] = new MailboxProducer(mailbox, producerCount)).start();
		}
		System.out.println("All threads created.");
		try {
			System.out.println("Waiting for producers to complete...");
			for (int i = 0; i < numberOfProducers; i++) {
				producers[i].join();
			}
			System.out.println("Waiting for consumers to complete...");
			for (int i = 0; i < numberOfConsumers; i++) {
				consumers[i].join();
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted.");
		}
		System.out.println("Completed testMailboxManyThreads.");
	}

	private static void testScambiatore(IScambiatore scambiatore) {
		System.out.println("Starting testScambiatore...");
		ScambiatoreThread[] scambiatori = new ScambiatoreThread[numberOfSwappers];
		for (int i = 0; i < numberOfSwappers; i++) {
			(scambiatori[i] = new ScambiatoreThread(scambiatore)).start();
		}
		System.out.println("All threads created.");
		try {
			System.out.println("Waiting for threads to complete...");
			for (int i = 0; i < numberOfSwappers; i++) {
				scambiatori[i].join();
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted.");
		}
		System.out.println("Completed testScambiatore.");
	}
}
