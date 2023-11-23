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
package com.braintribe.model.processing.cortex.service.connection;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.common.attribute.TypeSafeAttributeEntry;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.check.service.ParameterizedCheckRequest;
import com.braintribe.model.cortexapi.connection.TestConnectionRequest;
import com.braintribe.model.cortexapi.connection.TestConnectionResponse;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContextAspect;
import com.braintribe.model.processing.service.api.ServiceRequestContextBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;

public abstract class AbstractConnectionTester<R extends ParameterizedCheckRequest & AccessRequest> extends ServiceBase {

	protected ParameterizedAccessCheckProcessor<R> checkProcessor;
	protected AccessRequestContext<? extends TestConnectionRequest> context;
	
	
	public AbstractConnectionTester(AccessRequestContext<? extends TestConnectionRequest> context, ParameterizedAccessCheckProcessor<R> checkProcessor) {
		this.context = context;
		this.checkProcessor = checkProcessor;
	}
	
	public TestConnectionResponse run() {

		
		/*
		CompositeCheck check = CompositeCheck.T.create();
		check.setParameterizedChecks(Collections.singletonList(createCheckRequest()));
		
		CompositeCheckResult compositeCheckResult = check.eval(context.getSession()).get();
		CheckResult checkResult = compositeCheckResult.getParameterizedCheckResults().get(0);
		*/
		
		R checkRequest = createCheckRequest();
		
		if (checkRequest == null) {
			return createResponse("Could not perform connection check.", Level.ERROR, TestConnectionResponse.T);
		}
		
		
		InternalCheckContext internalCheckContext = new InternalCheckContext(context, checkRequest);
		CheckResult checkResult = checkProcessor.check(internalCheckContext);
		List<CheckResultEntry> entries = checkResult.getEntries();
		
		if (entries.isEmpty()) {
			return createConfirmationResponse("The internal check didn't provide any results", Level.WARNING, TestConnectionResponse.T);
		}
		
		NotificationsBuilder builder = Notifications.build();
		int errorCount = 0;
		int warningCount = 0;
		for (CheckResultEntry entry : entries) {
			
			Level level = getNotificationLevel(entry);
			String message = entry.getMessage();
			String details = entry.getDetails();

			
			if(level == Level.ERROR) {
				errorCount++;
			}
			if(level == Level.WARNING) {
				warningCount++;
			}
			
			builder
			  .add()
				.message()
					.message(message)
					.level(level)
					.details(details)
				.close()
			  .close();
		}
		
		addNotifications(builder.list());
		
		String message = createFinalMessage(errorCount, warningCount);
		
		
		Level level = Level.SUCCESS;
		if (errorCount > 0) {
			level = Level.ERROR;
		} else if (warningCount > 0) {
			level = Level.WARNING;
		}
		return createResponse(message, level, TestConnectionResponse.T);
		
	}
	
	abstract protected R createCheckRequest();
	
	protected String createFinalMessage(int errorCount, int warningCount) {
		if (errorCount == 0 && warningCount == 0) {
			return "Connection(s) successfully tested with no errors or warnings";
		}
		return "Connection(s) tested with "+errorCount+" errors and "+warningCount+" warnings. Click to see further details...";
	}
	
	private Level getNotificationLevel(CheckResultEntry entry) {
		CheckStatus checkStatus = entry.getCheckStatus();
		
		if (checkStatus == null) {
			entry.setCheckStatus(CheckStatus.fail);
			String message = entry.getMessage();
			String additionalMessage = "FrameworkError: Check didn't return a status";
			
			entry.setMessage((message != null) ? message + " (" +additionalMessage + ")" : additionalMessage);
			checkStatus = entry.getCheckStatus();
		}
		
		
		switch (checkStatus) {
			case ok:
				return Level.INFO;
			case warn:
				return Level.WARNING;
			case fail:
				return Level.ERROR;
		}
		throw new UnsupportedOperationException("Unsupported checkStatus: "+checkStatus);
	}
	
	public class InternalCheckContext implements AccessRequestContext<R> {
		private final AccessRequestContext<?> delegate;
		private final R checkRequest;

		
		public InternalCheckContext(AccessRequestContext<?> delegate, R checkRequest) {
			this.delegate = delegate;
			this.checkRequest = checkRequest;
		}
		
		@Override
		public PersistenceGmSession getSession() {
			return delegate.getSession();
		}

		@Override
		public PersistenceGmSession getSystemSession() {
			return delegate.getSystemSession();
		}
		
		@Override
		public String getDomainId() {
			return delegate.getDomainId();
		}

		@Override
		public R getRequest(){
			return checkRequest;
		}

		@Override
		public R getSystemRequest(){
			return checkRequest;
		}

		@Override
		public R getOriginalRequest(){
			return checkRequest;
		}

		@Override
		public void setAutoInducing(boolean autoInducing) {
			delegate.setAutoInducing(autoInducing);
		}

		@Override
		public <T> EvalContext<T> eval(ServiceRequest evaluable) {
			return delegate.eval(evaluable);
		}

		@Override
		public String getRequestorAddress() {
			return delegate.getRequestorAddress();
		}

		@Override
		public String getRequestorId() {
			return delegate.getRequestorId();
		}

		@Override
		public String getRequestorSessionId() {
			return delegate.getRequestorSessionId();
		}

		@Override
		public String getRequestorUserName() {
			return delegate.getRequestorUserName();
		}

		@Override
		public String getRequestedEndpoint() {
			return delegate.getRequestedEndpoint();
		}

		@Override
		public boolean isAuthorized() {
			return delegate.isAuthorized();
		}

		@Override
		public boolean isTrusted() {
			return delegate.isTrusted();
		}

		@Override
		public ServiceRequestSummaryLogger summaryLogger() {
			return delegate.summaryLogger();
		}

		@Override
		public void notifyResponse(Object response) {
			delegate.notifyResponse(response);
		}

		@Override
		public ServiceRequestContextBuilder derive() {
			return delegate.derive();
		}

		@Override
		public <T, A extends ServiceRequestContextAspect<? super T>> T findAspect(Class<A> aspect) {
			return delegate.findAspect(aspect);
		}

		@Override
		public void transferAttributes(Map<Class<? extends TypeSafeAttribute<?>>, Object> target) {
			delegate.transferAttributes(target);
		}

		@Override
		public AttributeContext parent() {
			return delegate.parent();
		}

		@Override
		public Stream<TypeSafeAttributeEntry> streamAttributes() {
			return delegate.streamAttributes();
		}

		@Override
		public <A extends TypeSafeAttribute<V>, V> Optional<V> findAttribute(Class<A> attribute) {
			return delegate.findAttribute(attribute);
		}
	}
}
