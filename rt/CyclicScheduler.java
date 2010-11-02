// Author(s): (Put your name here)
// Notes:     

package rt;

import soccorob.ai.agent.*;
import soccorob.rt.*;
import soccorob.rt.time.*;


public class CyclicScheduler extends BaseCyclicScheduler {
    
  private RLThread[] threadList;
	
  public CyclicScheduler() {
    /* Don't wait until JIT optimises execution*/
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
      
    /* Minor and major cycles should be initialised here ... */ 

    
    /* Next code signals to the infrastructure the start time of this
       Scheduler */
    AbsoluteTime sstrt = new AbsoluteTime();
    HighResolutionClock.getTime(sstrt);
    setSchedulerStart(sstrt);
    
    while (true) {			
	    
      /* Minor cycle 1 */
	    
	   
	    
      /* Minor cycle 2 */
	   
						
      /*     ...       */
	    
      
    }	
  }
}

