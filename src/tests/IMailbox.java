package tests;

public interface IMailbox {
	void deposit(String message) throws InterruptedException;

	String fetch() throws InterruptedException;
}
