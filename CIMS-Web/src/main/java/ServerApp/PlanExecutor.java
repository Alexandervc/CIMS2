/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.NetworkException;
import Shared.Tasks.IPlan;

/**
 *
 * @author Alexander
 */
public class PlanExecutor {
    private IPlan plan;

    /**
     * 
     * @param plan cannot be null or empty
     */
    public PlanExecutor(IPlan plan) {
        if(plan == null) {
            throw new IllegalArgumentException("Plan cannot be null or empty");
        }
        this.plan = plan;
    }
    
    public void executeNextStep() throws NetworkException {
        if(this.plan.nextStep()) {
            //push
            ServerMain.pushHandler.pushTaskToService(this.plan.getSteps().get(this.plan.getCurrentStep() - 1));
            ServerMain.pushHandler.pushTaskToChief(this.plan.getSteps().get(this.plan.getCurrentStep() - 1));
        }
        System.out.println(this.plan.getCurrentStep());
        // write to db
        ServerMain.tasksDatabaseManager.updatePlan(this.plan);
    }
}
