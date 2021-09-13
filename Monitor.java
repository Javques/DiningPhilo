import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
//https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/Condition.html
//A3 monitor class Alexis bolduc 40126092
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	 enum status {EATING, HUNGRY, THINKING};
		private final int NumberPhil;
		 final status state[];
		private boolean isTalking = false;
		private Lock conditionLock = null;
		private Condition self[];
		private Condition talkingIS;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		//initialize values and mutexes 
		self = new Condition[piNumberOfPhilosophers];
		NumberPhil = piNumberOfPhilosophers;
		state = new status[piNumberOfPhilosophers];
		conditionLock = new ReentrantLock();
		for(int i=0; i<NumberPhil;i++) {
			state[i] = status.THINKING;
			self[i] = conditionLock.newCondition();
		}
		talkingIS = conditionLock.newCondition();
		isTalking = false;
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 * @throws InterruptedException 
	 */
	public  void test(final int piTID) {
		conditionLock.lock();
		try {
		if((state[piTID]==status.HUNGRY)
				&&(state[(piTID+1)%NumberPhil]!=status.EATING)
				&&(state[(piTID-1+NumberPhil)%NumberPhil]!=status.EATING)) {
			
			state[piTID] = status.EATING;
			
			self[piTID].signal();
		}
		}finally {
			conditionLock.unlock();
		}
	}
	public  void pickUp(final int piTID) 
	{	
		
		conditionLock.lock();
		try {
		state[piTID] = status.HUNGRY;
		test(piTID);
		
		if(state[piTID]!=status.EATING) {
			self[piTID].await();
		}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
		
			conditionLock.unlock();
		}
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public  void putDown(final int piTID)
	{
		conditionLock.lock();
		try {
		state[piTID]=status.THINKING;
		test((piTID+1+NumberPhil)%NumberPhil);
		test((piTID+NumberPhil-1)%NumberPhil);
		}finally {
			conditionLock.unlock();
		}
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public  void requestTalk()
	{
		conditionLock.lock();
		try{
			if(isTalking) {
				talkingIS.await();
			}
			isTalking=true;
		}catch(InterruptedException e) {
			e.printStackTrace();
		}finally {
			conditionLock.unlock();
		}
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public void endTalk()
	{
		conditionLock.lock();
		try {
			isTalking = false;
			talkingIS.signal();
		}finally {
			conditionLock.unlock();
		}
	}
}

// EOF
