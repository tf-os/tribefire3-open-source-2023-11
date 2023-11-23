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
package tribefire.extension.opentracing.service;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.utils.lcd.StopWatch;

import tribefire.extension.opentracing.model.service.EnableOpentracing;
import tribefire.extension.opentracing.model.service.EnableOpentracingResult;
import tribefire.extension.opentracing.model.service.OpentracingRequest;
import tribefire.extension.opentracing.model.service.OpentracingResult;

/**
 * Service processing related to OPENTRACING
 * 
 * 
 *
 */
public class OpentracingServiceProcessor extends AbstractOpentracingServiceProcessor
		implements AccessRequestProcessor<OpentracingRequest, OpentracingResult> {

	private static final Logger logger = Logger.getLogger(OpentracingServiceProcessor.class);

	private GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private tribefire.extension.opentracing.model.deployment.service.OpentracingServiceProcessor deployable;

	// -----------------------------------------------------------------------
	// DELEGATES
	// -----------------------------------------------------------------------

	private AccessRequestProcessor<OpentracingRequest, OpentracingResult> delegate = AccessRequestProcessors.dispatcher(dispatching -> {
		dispatching.register(EnableOpentracing.T, this::openWopiSession); // in workbench
	});

	@Override
	public OpentracingResult process(AccessRequestContext<OpentracingRequest> context) {

		OpentracingRequest request = context.getRequest();

		StopWatch stopWatch = new StopWatch();

		OpentracingResult result;
		try {
			result = delegate.process(context);
		} catch (IllegalArgumentException | IllegalStateException e) {
			// rollback session
			PersistenceGmSession session = context.getSession();
			PersistenceGmSession systemSession = context.getSystemSession();
			rollback(session);
			rollback(systemSession);

			Failure failure = FailureCodec.INSTANCE.encode(e);

			String typeSignature = request.entityType().getTypeSignature();
			// the assumption is that every request fits to the naming convention
			typeSignature = typeSignature + "Result";
			EntityType<? extends OpentracingResult> responseEntityType = typeReflection.getEntityType(typeSignature);

			//@formatter:off
			result = responseBuilder(responseEntityType, context.getRequest())
				.notifications(builder -> 
					builder	
					.add()
						.message().confirmError(failure.getMessage(), e)
					.close()
				).build();
			//@formatter:on 
			result.setFailure(failure);
		}
		long elapsedTime = stopWatch.getElapsedTime();

		LogLevel logLevel = LogLevel.TRACE;
		if (elapsedTime > deployable.getLogWarningThresholdInMs()) {
			logLevel = LogLevel.WARN;
		} else if (elapsedTime > deployable.getLogErrorThresholdInMs()) {
			logLevel = LogLevel.ERROR;
		}
		if (logger.isLevelEnabled(logLevel)) {
			logger.log(logLevel, "Executed request: '" + request + "' with failure: '" + result.getFailure() + "' in '" + elapsedTime + "'ms");
		}
		return result;
	}

	private void rollback(PersistenceGmSession session) {
		try {
			Transaction t = session.getTransaction();
			if (t.hasManipulations()) {
				t.undo(t.getManipulationsDone().size());
			}
		} catch (Exception e) {
			logger.warn(() -> "Rollback of session did not succeed", e);
		}
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	public EnableOpentracingResult openWopiSession(AccessRequestContext<EnableOpentracing> context) {

		logger.info(() -> "Dummy Enable Opentracing.... - SERVICE");

		EnableOpentracingResult result = EnableOpentracingResult.T.create();
		return result;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDeployable(tribefire.extension.opentracing.model.deployment.service.OpentracingServiceProcessor deployable) {
		this.deployable = deployable;
	}

}
