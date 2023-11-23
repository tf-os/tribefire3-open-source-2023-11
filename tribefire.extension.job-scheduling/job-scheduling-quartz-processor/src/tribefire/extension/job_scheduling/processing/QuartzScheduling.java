// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.job_scheduling.processing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.securityservice.api.UserSessionScopingBuilder;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.job_scheduling.api.api.JobRequest;
import tribefire.extension.job_scheduling.api.api.JobResponse;
import tribefire.extension.job_scheduling.deployment.model.JobCronScheduling;

public class QuartzScheduling implements Worker, InitializationAware {

	private static Logger logger = Logger.getLogger(QuartzScheduling.class);

	private JobCronScheduling deployable;
	private Scheduler scheduler;
	private CronTrigger trigger;
	private static Map<JobKey, JobImpl> jobs = new ConcurrentHashMap<>();
	private UserSessionScoping userSessionScoping;
	private Evaluator<ServiceRequest> requestEvaluator;
	private UserPasswordCredentials credentials = null;
	private boolean credentialsInitialized = false;

	@Required
	public void setUserSessionScoping(UserSessionScoping userSessionScoping) {
		this.userSessionScoping = userSessionScoping;
	}

	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Required
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Required
	public void setDeployable(JobCronScheduling deployable) {
		this.deployable = deployable;
	}

	@Override
	public GenericEntity getWorkerIdentification() {
		return deployable;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {

		try {
			JobImpl job = new JobImpl();
			JobKey key = new JobKey(deployable.getExternalId());

			jobs.put(key, job);
			JobDetail jobDetail = JobBuilder.newJob().ofType(DispatcherJob.class).withIdentity(key).build();

			CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(deployable.getCronExpression());

			trigger = TriggerBuilder.newTrigger().withSchedule(cronSchedule).forJob(jobDetail).build();

			scheduler.scheduleJob(jobDetail, trigger);

		} catch (SchedulerException e) {
			trigger = null;
			throw new WorkerException("Error while scheduling job: " + deployable, e);
		}

	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		if (trigger != null) {
			try {
				JobKey key = trigger.getJobKey();
				scheduler.unscheduleJob(trigger.getKey());
				JobImpl job = jobs.remove(key);

				String deplInfo = (deployable != null) ? deployable.getExternalId() : "<null>";
				if (job != null) {
					logger.debug(() -> "Interrupting and waiting for job with key " + key + ". Deployable is: " + deplInfo);
					job.interruptAndWaitForEnd();
				} else {
					logger.debug(() -> "Could not find a job with key " + key + ". Deployable is: " + deplInfo);
				}
			} catch (SchedulerException e) {
				throw new WorkerException("Error while unscheduling job: " + deployable);
			}
		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	private class JobImpl implements Job {

		private Object monitor = new Object();
		private volatile int runCount = 0;
		private boolean interrupted;

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {

			synchronized (monitor) {
				if (interrupted || runCount > 0 && deployable.getCoalescing()) {
					return;
				}

				runCount++;
			}

			ServiceProcessor job = deployable.getJobRequestProcessor();
			String externalId = job.getExternalId();

			try {
				getUserSessionScopingBuilder().runInScope(() -> {
					JobRequest request = JobRequest.T.create();
					request.setServiceId(externalId);
					Maybe<? extends JobResponse> reasoned = request.eval(requestEvaluator).getReasoned();
					if (reasoned.isUnsatisfied()) {
						logger.error("Error while executing scheduled job [externalId=" + externalId + "]: " + reasoned.whyUnsatisfied().stringify());
					}
				});

			} catch (Exception e) {
				logger.error("Error while executing scheduled job [externalId=" + externalId + "]", e);

			} finally {
				synchronized (monitor) {
					runCount--;
					monitor.notify();
				}
			}

		}

		public void interruptAndWaitForEnd() {
			synchronized (monitor) {
				interrupted = true;

				while (runCount > 0) {
					try {
						monitor.wait();
					} catch (InterruptedException e) {
						// intentionally left empty
					}
				}
			}
		}

	}

	public static class DispatcherJob implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			Job job = jobs.get(context.getJobDetail().getKey());
			job.execute(context);
		}
	}

	private UserSessionScopingBuilder getUserSessionScopingBuilder() {
		Credentials credentials = getCredentials();

		if (credentials != null) {
			return userSessionScoping.forCredentials(credentials);
		} else {
			return userSessionScoping.forDefaultUser();
		}
	}

	public Credentials getCredentials() {
		if (!credentialsInitialized) {
			String userName = deployable.getUser();
			String password = deployable.getPassword();

			if (userName != null && password != null) {
				UserNameIdentification userIdentification = UserNameIdentification.T.create();
				userIdentification.setUserName(userName);
				credentials = UserPasswordCredentials.T.create();
				credentials.setUserIdentification(userIdentification);
				credentials.setPassword(password);
			}

			credentialsInitialized = true;
		}

		return credentials;
	}

	@Override
	public void postConstruct() {
		String cronExpression = deployable.getCronExpression();
		if (cronExpression == null || !CronExpression.isValidExpression(cronExpression)) {
			throw new IllegalStateException(
					(cronExpression == null) ? "cronExpression cannot be null!" : "Invalid cronExpression: " + cronExpression);
		}

		if (deployable.getJobRequestProcessor() == null) {
			throw new IllegalStateException("Deployment not successful, because jobRequestProcessor is not set on: " + this);
		}
		
		if (deployable.getJobRequestProcessor().getExternalId() == null) {
			throw new IllegalStateException("Deployment not successful, because jobRequestProcessor.externalId is not set on: " + deployable.getJobRequestProcessor());
		}
	}
}
