// Author(s): Mikael Rothin (mikro464), Johan Uppman (johup261)   


package rt;

import soccorob.rt.*;
import soccorob.rt.util.*;
import soccorob.rt.time.*;
import soccorob.rt.etst.*;
import soccorob.ai.*;
import soccorob.ai.agent.*;


/**
 * Earliest First Deadline Scheduler.
 */
public class RateMonotonicScheduler extends Scheduler{
	
	
  private PrioritySortedList readyList;
  private EarliestReleaseSortedList  suspendedList;
  
  
  public RateMonotonicScheduler() { 
	
    /* Don't wait until JIT optimizes execution*/
    super(false);		
		
    System.out.println("Rate-monotonic scheduler initiated.");
	
    RLThread[] threadList = (RLThread[]) getThreads(); 
    RMThreadNode threadNode;
		
    /* Create a new ready list. Sorted with respect to shortest period. */
    readyList = new PrioritySortedList();
		
    /* Create a new suspended list. Sorted with respect to earliest release time. */
    suspendedList = new EarliestReleaseSortedList();	
	
    /* Move processes to the suspended list */
    for (int i = 0; i < threadList.length; i++) {
      threadNode = new RMThreadNode(threadList[i]);			
	    
      /* set the first release time */
      threadNode.nextRelease.setTime(((PeriodicParameters)threadList[i].getReleaseParameters()).getStart());
	    
      /* set the first deadline */
      threadNode.nextRelease.add(threadNode.thread.getReleaseParameters().getDeadline(),
				 threadNode.deadline);
      
      /* Set priority = period. */
      threadNode.thread.setSchedulingParameters(new PriorityParameters(2147483647 -													//Number is the biggest integer
    		  				((PeriodicParameters) threadNode.thread.getReleaseParameters()).getPeriod().getMicroSeconds() + 
    		  				1000000*((PeriodicParameters) threadNode.thread.getReleaseParameters()).getPeriod().getSeconds() + 
    		  				60*1000000*((PeriodicParameters) threadNode.thread.getReleaseParameters()).getPeriod().getMinutes()));
      
      /* Insert into suspended list */
      suspendedList.insert(threadNode);
      
    }
    //System.out.println(readyList.toString());
  }
		
  public String getPolicyName() {
    return "Shortest Period First";
  }
    
  public boolean isFeasible() {
    return true;
  }

  /**
   * By calling this function the execution stops for the amount of
   * time specified in timeToSleep.
   * 
   * This function should be used somewhere in the scheduler's big
   * loop, in order to prevent "busy loops" (monopolisation of the CPU
   * by the scheduler). That is, if there are no tasks ready to run,
   * then the scheduler should sleep until some taks become so.
   *
   * Note that the resolution of this sleep function is in
   * miliseconds, and that the accuracy of the sleeping time can
   * vary with a couple of miliseconds.
   */
  public void sleep(RelativeTime timeToSleep) {
    
    long microDelta = timeToSleep.toMicroSeconds();

    //resolution is in miliseconds so the 
    //microseconds are divided by 1000
    if((long)(microDelta/1000) > 0) {
      try {
	synchronized (this) {
	  this.wait(microDelta/1000);
	}
      }
      catch (InterruptedException ie){
	System.out.println("The wait was interrupted");
	ie.printStackTrace();
      }
    }
  }
  
  private int rescheduleFirst(AbsoluteTime nextReleaseTime, AbsoluteTime absTime, int misses) {

	RMThreadNode threadNode;
	threadNode = (RMThreadNode) readyList.removeFirst();
	
	/* Set release time and deadline, add periods until deadline is after current time. */
	do {
		/* Set next release time */
		threadNode.nextRelease.add(
			((PeriodicParameters)threadNode.thread.getReleaseParameters()).getPeriod(), 
			nextReleaseTime);
		threadNode.nextRelease.setTime(nextReleaseTime);
		
		/* Set next deadline */
		threadNode.nextRelease.add(threadNode.thread.getReleaseParameters().getDeadline(),
				 threadNode.deadline);
		misses++;	
	} while (absTime.isGreater(threadNode.deadline));
		
	suspendedList.insert(threadNode);
	return misses;
}
  
  /**
   * Priority driven scheduling. Your task is to make a scheduler by
   * filling our the part within the while loop.
   */
  public void start() {		
		
    /* Create some objects needed */
    AbsoluteTime absTime = new AbsoluteTime();
    AbsoluteTime nextReleaseTime = new AbsoluteTime();
    RelativeTime sleepTime = new RelativeTime();
    
    RMThreadNode readyThreadNode;
    RLThread readyThread;
	
    RMThreadNode suspendedThreadNode;
    RLThread suspendedThread;
    
    int missedDeadlines;
    
    int counter = 0;
    double failedThreads = 0;
    double finishedThreads = 0;
	
    /* Initialize the clock */
    HighResolutionClock.resetClock();
		
    while (true) {
    	HighResolutionClock.getTime(absTime);
    	missedDeadlines = 0;

    	/* Move threads to ready queue. */
    	while (!(suspendedList.isEmpty()) && 
    		   !(((RMThreadNode)suspendedList.getFirst()).nextRelease.isGreater(absTime))) {
    		suspendedThreadNode = (RMThreadNode) suspendedList.removeFirst();
    		suspendedThread = suspendedThreadNode.thread;
    		suspendedThread.setReady();
    		readyList.insert(suspendedThreadNode);
    	}
    	
    	/* Reschedule tasks with missed deadlines */
    	while ((!(readyList.isEmpty())) && 
    			(!(((RMThreadNode) readyList.getFirst()).deadline.isGreater(absTime)))) {
        	missedDeadlines = 0;
    		((RMThreadNode) readyList.getFirst()).thread.getDeadlineMissHandler().handleAsyncEvent();
    		missedDeadlines = rescheduleFirst(nextReleaseTime, absTime, missedDeadlines);
    		failedThreads += missedDeadlines;
    	}
    	
    	/* Run task with shortest period. */
    	if ((!(readyList.isEmpty())) & missedDeadlines == 0) {	//Run task if no releasing needs to be done due to missed deadlines.
	    	
	    	readyThreadNode = (RMThreadNode) readyList.getFirst();
	    	readyThread = readyThreadNode.thread;
	    	
	    	fireThread(readyThread);
	       	/* Task running... */
	    	
	    	/* Reschedule if finished. */
	    	if (readyThread.isFinished()) {
	    		rescheduleFirst(nextReleaseTime, absTime, 0);
	    		finishedThreads++;
	    	}
    	
    	} else if (missedDeadlines == 0) {	// Sleep if no releasing needs to be done due to missed deadlines.
    		((RMThreadNode) suspendedList.getFirst()).nextRelease.subtract(absTime, sleepTime);
        	this.sleep(sleepTime);
    	}
    	counter++;
    	if (counter == 10000) {
    		counter = 0;
    		System.out.println("Miss ratio: " + failedThreads/(failedThreads+finishedThreads));
    	}
    }
  }
}	


