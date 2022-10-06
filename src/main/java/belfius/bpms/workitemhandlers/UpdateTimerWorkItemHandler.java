/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package belfius.bpms.workitemhandlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.admin.TimerInstance;
import org.jbpm.services.api.service.ServiceRegistry;
import org.jbpm.process.workitem.core.util.WidMavenDepends;

@Wid(widfile="UpdateTimerDefinitions.wid", name="UpdateTimer",
        displayName="UpdateTimer",
        defaultHandler="mvel: new belfius.bpms.workitemhandlers.UpdateTimerWorkItemHandler()",
        documentation = "update-timer-workitemhandler/index.html",
        category = "update-timer-workitemhandler",
        icon = "UpdateTimer.png",
        parameters={
            @WidParameter(name="timerId", required = true),
            @WidParameter(name="timerDelay"),
            @WidParameter(name="timerDelayFromCurrentDate"),
            @WidParameter(name="timerNewDate"),
        },
        mavenDepends={
            @WidMavenDepends(group="belfius.bpms.workitemhandlers", artifact="update-timer-workitemhandler", version="1.0.0-SNAPSHOT")
        },
        serviceInfo = @WidService(category = "update-timer-workitemhandler", description = "Updates a timer given its id and target delay",
                keywords = "",
                action = @WidAction(title = "Updates a timer given its id")
        )
)
public class UpdateTimerWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {
	
	private KieSession ksession;
	private Logger logger = LoggerFactory.getLogger(UpdateTimerWorkItemHandler.class);

    public UpdateTimerWorkItemHandler(KieSession ksession){
            this.ksession = ksession;
        }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        try {
            RequiredParameterValidator.validate(this.getClass(), workItem);

            // sample parameters
            String timerId = (String) workItem.getParameter("timerId");
            String timerDelay = (String) workItem.getParameter("timerDelay");
            String timerDelayFromCurrentDate = (String) workItem.getParameter("timerDelayFromCurrentDate");
            
//            org.jbpm.process.instance.InternalProcessRuntime processRuntime = ksession.getKieRuntime(org.jbpm.process.instance.InternalProcessRuntime.class);
//            org.jbpm.process.instance.timer.TimerManager timerManager = processRuntime.getTimerManager();
//			int timerCnt = timerManager.getTimers().size();
//			logger.info("    found " + timerCnt + " active timer(s)");
//			for (org.jbpm.process.instance.timer.TimerInstance ti: timerManager.getTimers()) {
//			  logger.info("Timer found: " + ti.getName() + " - " + ti.getTimerId());
//			}  
			
			
			ProcessInstanceAdminService processAdminService = (ProcessInstanceAdminService) ServiceRegistry.get().service(ServiceRegistry.PROCESS_ADMIN_SERVICE);
			Collection<TimerInstance> timerList = processAdminService.getTimerInstances(workItem.getProcessInstanceId());
			
			logger.info("*********** BEFORE TIMER UPDATE Iterating over timer instances ****************");
			for (Iterator iterator = timerList.iterator(); iterator.hasNext();) {
				TimerInstance timerInstance = (TimerInstance) iterator.next();
				logger.info("TimerInstance found with id={}, timer-id={} and name={}", timerInstance.getId(), timerInstance.getTimerId(), timerInstance.getTimerName());
				logger.info("TimerInstance expiration date is {}", timerInstance.getNextFireTime());
				
			}
			logger.info("*********** BEFORE TIMER UPDATE Ending iteration over timer instances ****************");
			
			if(timerDelay != null && !timerDelay.isEmpty()) {
				logger.info("About to delay {} seconds timer with id {} ", Long.valueOf(timerDelay), Long.valueOf(timerId));
				processAdminService.updateTimer(workItem.getProcessInstanceId(), Long.valueOf(timerId), Long.valueOf(timerDelay), 0, 0);
			}
			else if(timerDelayFromCurrentDate != null && !timerDelayFromCurrentDate.isEmpty()) {
				logger.info("About to delay {} seconds timer with id {} ", Long.valueOf(timerDelayFromCurrentDate), Long.valueOf(timerId));
				processAdminService.updateTimerRelative(workItem.getProcessInstanceId(), Long.valueOf(timerId), Long.valueOf(timerDelayFromCurrentDate), 0, 0);
			}
			
			logger.info("Timer with id {} delayed", Long.valueOf(timerId));
			
			timerList = processAdminService.getTimerInstances(workItem.getProcessInstanceId());
			
			logger.info("*********** AFTER TIMER UPDATE Iterating over timer instances ****************");
			for (Iterator iterator = timerList.iterator(); iterator.hasNext();) {
				TimerInstance timerInstance = (TimerInstance) iterator.next();
				logger.info("TimerInstance found with id={}, timer-id={} and name={}", timerInstance.getId(), timerInstance.getTimerId(), timerInstance.getTimerName());
				logger.info("TimerInstance expiration date is {}", timerInstance.getNextFireTime());
				
			}
			logger.info("*********** AFTER TIMER UPDATE Ending iteration over timer instances ****************");
            
            
            Map<String, Object> results = new HashMap<String, Object>();


            manager.completeWorkItem(workItem.getId(), results);
        } catch(Throwable cause) {
            handleException(cause);
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
        // stub
    }
}


