// Author(s): (Put your name here)
// Notes:     


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
public class EarliestDeadlineFirstScheduler extends Scheduler{
	
	
  private EarliestDeadlineSortedList readyList;
  private EarliestReleaseSortedList  suspendedList;
	
  
  public EarliestDeadlineFirstScheduler() { 
	
    /* Don't wait until JIT optimises execution*/
    super(false);		
		
    System.out.println("Earliest Deadline First scheduler initiated.");
	
    RLThread[] threadList = (RLThread[]) getThreads(); 
    RMThreadNode threadNode;
		
    /* Create a new ready list. Sorted with respect to earliest deadline. */
    readyList = new EarliestDeadlineSortedList();
		
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
			
      /* Insert into suspended list */
      suspendedList.insert(threadNode);
      
    }
    //System.out.println(readyList.toString());
  }
		
  public String getPolicyName() {
    return "Earliest Deadline First";
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
  
  private void rescheduleFirst(AbsoluteTime nextReleaseTime) {

	RMThreadNode threadNode;
	threadNode = (RMThreadNode) readyList.removeFirst();
	
	/* Set next release time */
	threadNode.nextRelease.add(
			((PeriodicParameters)threadNode.thread.getReleaseParameters()).getPeriod(), 
			nextReleaseTime);
	threadNode.nextRelease.setTime(nextReleaseTime);
	
	/* Set next deadline */
	threadNode.nextRelease.add(threadNode.thread.getReleaseParameters().getDeadline(),
			 threadNode.deadline);
		
	suspendedList.insert(threadNode);
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
	
    //AbsoluteTime debugTime = new AbsoluteTime();
    //AbsoluteTime debugTime2 = new AbsoluteTime();
    
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
    		

	    	//HighResolutionClock.getTime(debugTime);
	    	//System.out.println(debugTime.toString() + " Release: " + suspendedThread.getName() + " , Deadline: " + suspendedThreadNode.deadline.toString());
    		
/*    		System.out.println("Release task: " + suspendedThread.getName() + 
					" Time: " + absTime.toString() + 
					" Release time: " + suspendedThreadNode.nextRelease.toString() +
					" Deadline: " + suspendedThreadNode.deadline.toString()); */
    		suspendedThread.setReady();
    		readyList.insert(suspendedThreadNode);
    	}
    	
    	/* Reschedule tasks with missed deadlines */
    	while ((!(readyList.isEmpty())) && 
    			(!(((RMThreadNode) readyList.getFirst()).deadline.isGreater(absTime)))) {
    		

	    	//HighResolutionClock.getTime(debugTime);
	    	//System.out.println(debugTime.toString() + " Missed: " + readyList.getFirst().thread.getName() + 
	    	//		" , Deadline: " + ((RMThreadNode) readyList.getFirst()).deadline.toString());
    		
  /*  		System.out.println("Missed deadline: " + readyList.getFirst().thread.getName() + 
    							" Time: " + absTime.toString() + 
    							" Deadline: " + ((RMThreadNode) readyList.getFirst()).deadline.toString()); */
    		((RMThreadNode) readyList.getFirst()).thread.getDeadlineMissHandler().handleAsyncEvent();
    		rescheduleFirst(nextReleaseTime);
    		missedDeadlines = 1;
    		failedThreads++;
    	}
    	
    	/* Run task with closest deadline. */
    	if ((!(readyList.isEmpty())) & missedDeadlines == 0) {
	    	
	    	readyThreadNode = (RMThreadNode) readyList.getFirst();
	    	readyThread = readyThreadNode.thread;
	    	
	    	//HighResolutionClock.getTime(debugTime);
	    	
	       	fireThread(readyThread);
	       	
	       	/* Task running... */
	       	
	    	//HighResolutionClock.getTime(debugTime2);
	    	//System.out.println(debugTime.toString() + " Start: " + readyThread.getName());
	    	//System.out.println(debugTime2.toString() + " Stop: " + readyThread.getName());
	    	
	       	
	       	
	    	/* Reschedule if finished. */
	    	if (readyThread.isFinished()) {
	    		
		    	//System.out.println(" Finished ");
	    		
	    		
	    		/*System.out.println("Task finished: " + readyThread.getName() + 
						" Time: " + absTime.toString() + 
						" Deadline: " + ((RMThreadNode) readyList.getFirst()).deadline.toString()); */
	    		rescheduleFirst(nextReleaseTime);
	    		finishedThreads++;
	    	}
    	
    	} else if (missedDeadlines == 0) {
    		((RMThreadNode) suspendedList.getFirst()).nextRelease.subtract(absTime, sleepTime);
    		//System.out.println("sleeping: " + sleepTime.toString());
        	this.sleep(sleepTime);
    	}
    	counter++;
    	if (counter == 200) {
    		counter = 0;
    		System.out.println("Miss ratio: " + failedThreads/(failedThreads+finishedThreads));
    	}
    }
  }
}	


