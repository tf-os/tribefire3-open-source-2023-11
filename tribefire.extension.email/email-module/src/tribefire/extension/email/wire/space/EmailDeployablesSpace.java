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
package tribefire.extension.email.wire.space;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.braintribe.execution.ThreadPoolBuilder;
import com.braintribe.execution.queue.LimitedQueue;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.email.EmailProcessor;
import com.braintribe.model.processing.email.HealthCheckProcessor;
import com.braintribe.model.processing.email.cache.MailerCache;
import com.braintribe.model.processing.email.connection.ImapConnectorImpl;
import com.braintribe.model.processing.email.connection.Pop3ConnectorImpl;
import com.braintribe.model.processing.email.connection.SmtpConnectorImpl;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.ModuleReflectionContract;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class EmailDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModuleReflectionContract reflection;

	@Import
	private ResourceProcessingContract resourceProcessing;

	@Import
	private SystemUserRelatedContract systemUserRelated;

	@Managed
	public SmtpConnectorImpl smtpConnector(ExpertContext<? extends com.braintribe.model.email.deployment.connection.SmtpConnector> context) {
		com.braintribe.model.email.deployment.connection.SmtpConnector deployable = context.getDeployable();

		SmtpConnectorImpl bean = new SmtpConnectorImpl();
		bean.setConnector(deployable);
		bean.setMailerCache(mailerCache());

		return bean;
	}

	@Managed
	public SmtpConnectorImpl gmailSmtpConnector(ExpertContext<com.braintribe.model.email.deployment.connection.GmailSmtpConnector> context) {
		com.braintribe.model.email.deployment.connection.GmailSmtpConnector deployable = context.getDeployable();

		SmtpConnectorImpl bean = new SmtpConnectorImpl();
		bean.setConnector(deployable);
		bean.setMailerCache(mailerCache());

		return bean;
	}

	@Managed
	public ImapConnectorImpl gmailImapConnector(ExpertContext<com.braintribe.model.email.deployment.connection.GmailImapConnector> context) {
		@SuppressWarnings("unused")
		com.braintribe.model.email.deployment.connection.GmailImapConnector deployable = context.getDeployable();

		ImapConnectorImpl bean = new ImapConnectorImpl();

		return bean;
	}

	@Managed
	public ImapConnectorImpl imapConnector(ExpertContext<com.braintribe.model.email.deployment.connection.ImapConnector> context) {
		@SuppressWarnings("unused")
		com.braintribe.model.email.deployment.connection.ImapConnector deployable = context.getDeployable();

		ImapConnectorImpl bean = new ImapConnectorImpl();

		return bean;
	}

	@Managed
	public Pop3ConnectorImpl pop3Connector(ExpertContext<com.braintribe.model.email.deployment.connection.Pop3Connector> context) {
		@SuppressWarnings("unused")
		com.braintribe.model.email.deployment.connection.Pop3Connector deployable = context.getDeployable();

		Pop3ConnectorImpl bean = new Pop3ConnectorImpl();

		return bean;
	}

	@Managed
	public EmailProcessor emailServiceProcessor() {
		EmailProcessor bean = new EmailProcessor();

		bean.setCortexSessionProvider(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setModuleClassLoader(reflection.moduleClassLoader());
		bean.setMailerCache(mailerCache());
		bean.setPipeStreamFactory(resourceProcessing.streamPipeFactory());
		bean.setHealthCheckExecutor(healthCheckExecutor());

		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor(ExpertContext<com.braintribe.model.email.deployment.service.HealthCheckProcessor> context) {
		@SuppressWarnings("unused")
		com.braintribe.model.email.deployment.service.HealthCheckProcessor deployable = context.getDeployable();

		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setSystemServiceRequestEvaluator(tfPlatform.systemUserRelated().evaluator());

		return bean;

	}

	@Managed
	private ExecutorService healthCheckExecutor() {
		int workerThreads = 5;
		//@formatter:off
		return ThreadPoolBuilder.newPool()
				.poolSize(workerThreads, workerThreads)
				.keepAliveTime(60L, TimeUnit.SECONDS)
				.workQueue(new LimitedQueue<>(1))
				.rejectionHandler(new ThreadPoolExecutor.CallerRunsPolicy()) 
				.waitForTasksToCompleteOnShutdown(true)
				.threadNamePrefix("EmailConnectionCheck")
				.description("Email Check Executor") 
				.build();
		//@formatter:on
	}

	@Managed
	private MailerCache mailerCache() {
		MailerCache bean = new MailerCache();
		return bean;
	}
}
