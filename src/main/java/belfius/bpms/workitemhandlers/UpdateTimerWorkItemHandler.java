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

import org.jbpm.kie.services.impl.admin.TimerInstanceImpl;
import org.jbpm.process.instance.command.RelativeUpdateTimerCommand;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.admin.TimerInstance;
import org.jbpm.services.api.service.ServiceRegistry;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            String timerId = (String) workItem.getParameter("timerId");
            String timerName = (String) workItem.getParameter("timerName");
            String timerDelay = (String) workItem.getParameter("timerDelay");
            String timerDelayFromCurrentDate = (String) workItem.getParameter("timerDelayFromCurrentDate");
            
            long timerIdLv = Long.valueOf(timerId).longValue();
            long timerDelayLv = Long.valueOf(timerDelay).longValue();
            long timerDelayFromCurrentDateLv = Long.valueOf(timerDelayFromCurrentDate).longValue();			
			
			ProcessInstanceAdminService processAdminService = (ProcessInstanceAdminService) ServiceRegistry.get().service(ServiceRegistry.PROCESS_ADMIN_SERVICE);
			Collection<TimerInstance> timerList = processAdminService.getTimerInstances(workItem.getProcessInstanceId());
			
			for (Iterator<TimerInstance> iterator = timerList.iterator(); iterator.hasNext();) {
				TimerInstanceImpl timerInstance = (TimerInstanceImpl) iterator.next();
				logger.debug("TimerInstance found with id={}, timer-id={} and name={}", timerInstance.getId(), timerInstance.getTimerId(), timerInstance.getTimerName());
				logger.debug("TimerInstance expiration date is {}", timerInstance.getNextFireTime());
			}
			
			if(timerDelay != null && !timerDelay.isEmpty()) {
				logger.info("About to delay {} milliseconds the timer with id {} from ProcessInstance {}",
						timerDelayLv, timerIdLv, workItem.getProcessInstanceId());
				
				ksession.execute(new RelativeUpdateTimerCommand(workItem.getProcessInstanceId(), timerIdLv, timerDelayLv));
			}
			else if(timerDelayFromCurrentDate != null && !timerDelayFromCurrentDate.isEmpty()) {
				logger.info("About to relative delay {} milliseconds the timer with id {} from ProcessInstance {}",
						timerDelayFromCurrentDateLv, timerIdLv, workItem.getProcessInstanceId());

				ksession.execute(new RelativeUpdateTimerCommand(workItem.getProcessInstanceId(), timerIdLv, timerDelayFromCurrentDateLv));
			}
			
			logger.info("Timer with id {} delayed", timerIdLv);

            manager.completeWorkItem(workItem.getId(), new HashMap<String, Object>());
        } catch(Throwable cause) {
            handleException(cause);
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }
}