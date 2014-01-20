package tests;

import java.util.Random;

public class ScambiatoreThread extends Thread {
	private IScambiatore scambiatore;

	public ScambiatoreThread(IScambiatore scambiatore) {
		this.scambiatore = scambiatore;
	}

	public void run() {
		Random random = new Random();
		try {
			scambiatore.scambia(random.nextInt(100));
		} catch (InterruptedException e) {
			Logger.log("interrupted");
		}
	}
}
