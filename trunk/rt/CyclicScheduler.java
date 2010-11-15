// Author(s): Mikael Rothin (mikro464), Johan Uppman (johup261)   

package rt;

import soccorob.ai.agent.*;
import soccorob.rt.*;
import soccorob.rt.time.*;


public class CyclicScheduler extends BaseCyclicScheduler {
    
  private RLThread[] threadList;
	
  public CyclicScheduler() {
    /* Don't wait until JIT optimizes execution*/
    super(false);

    System.out.println("Cyclic scheduler initiated");

    /* Load all tasks in a threadList array */
    this.threadList = (RLThread[])getThreads();		
  }
		
  public String getPolicyName() {
    return "Cyclic";
  }
	
  public boolean isFeasible() {
    return true; //not implemented
  }
		
  /**
   * Static scheduling. All tasks are run in a predefined order.  Your
   * task is to make a cyclic scheduler by filling out the part within
   * the while loop.
   *
   * Do not forget to set the length of the minor and major cycles
   * right after the start of the Scheduler.
   *
   * Tasks are in the threadList as follows:
   * threadList[0] - image processing
   * threadList[1] - planner for robot 1
   * threadList[2] - planner for robot 2
   * threadList[3] - planner for robot 3
   * threadList[4] - reactor for robot 1
   * threadList[5] - reactor for robot 2
   * threadList[6] - reactor for robot 3
   * threadList[7] - actuator
   */

  public void start() {
      
    /* Minor and major cycles should be initialized here ... */ 
	setMinorCycle(new RelativeTime(0, 0, 50000));
	setMajorCycle(new RelativeTime(0, 1, 0));
    
    /* Next code signals to the infrastructure the start time of this
       Scheduler */
    AbsoluteTime sstrt = new AbsoluteTime();
    HighResolutionClock.getTime(sstrt);
    setSchedulerStart(sstrt);
    
    while (true) {			
	    
      /* Minor cycle 1-3 */
    	for (int c = 1; c <= 3; c ++) {
		    fireUntilFinished(threadList[0]);
		    fireThread(threadList[1]);
		    fireThread(threadList[1]);
		    fireThread(threadList[2]);
		    fireThread(threadList[2]);
		    fireThread(threadList[3]);
		    fireThread(threadList[3]);
		    fireUntilFinished(threadList[4]);
		    fireUntilFinished(threadList[5]);
		    fireUntilFinished(threadList[6]);
		    fireUntilFinished(threadList[7]);
	    	waitForCycleInterrupt();
    	}
	   
      /* Minor cycle 4-20 */
    	for (int c = 4; c <= 20; c ++) {
		    fireUntilFinished(threadList[0]);
		    fireUntilFinished(threadList[4]);
		    fireUntilFinished(threadList[5]);
		    fireUntilFinished(threadList[6]);
		    fireUntilFinished(threadList[7]);
	    	waitForCycleInterrupt();
    	}
    	
    }	
  }
}

