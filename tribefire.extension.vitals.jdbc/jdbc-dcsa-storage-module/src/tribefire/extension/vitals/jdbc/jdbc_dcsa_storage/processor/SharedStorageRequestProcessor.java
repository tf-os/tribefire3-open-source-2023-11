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
package tribefire.extension.vitals.jdbc.jdbc_dcsa_storage.processor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.Unsatisfied;

import tribefire.extension.jdbc.gmdb.dcsa.TemporaryJdbc2GmDbSharedStorage;
import tribefire.extension.vitals.jdbc.model.migration.DowngradeSharedStorage;
import tribefire.extension.vitals.jdbc.model.migration.InternalPollSharedStorageState;
import tribefire.extension.vitals.jdbc.model.migration.PollSharedStorageState;
import tribefire.extension.vitals.jdbc.model.migration.SharedStorageRequest;
import tribefire.extension.vitals.jdbc.model.migration.SharedStorageState;
import tribefire.extension.vitals.jdbc.model.migration.SharedStorageStatus;
import tribefire.extension.vitals.jdbc.model.migration.UpgradeSharedStorage;

/**
 * @author peter.gazdik
 */
public class SharedStorageRequestProcessor extends AbstractDispatchingServiceProcessor<SharedStorageRequest, Object> {

	private Supplier<TemporaryJdbc2GmDbSharedStorage> sharedStorageSupplier;
	private ResourceBuilder resourceBuilder;

	private SharedStorageStatusReporter statusReporter;

	public void setSharedStorageSupplier(Supplier<TemporaryJdbc2GmDbSharedStorage> sharedStorageSupplier) {
		this.sharedStorageSupplier = sharedStorageSupplier;
	}

	public void setResourceBuilder(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<SharedStorageRequest, Object> dispatching) {
		dispatching.register(UpgradeSharedStorage.T, (c, r) -> processUpgrade(r));
		dispatching.register(DowngradeSharedStorage.T, (c, r) -> processDowngrade(r));
		dispatching.register(PollSharedStorageState.T, (c, r) -> processPollSharedStorageState(c));
		dispatching.register(InternalPollSharedStorageState.T, (c, r) -> processInternalPollSharedStorageState());
	}

	@Override
	public Object process(ServiceRequestContext context, SharedStorageRequest request) {
		TemporaryJdbc2GmDbSharedStorage sharedStorage = getSharedStorage(request);

		if (statusReporter == null)
			synchronized (this) {
				if (statusReporter == null)
					this.statusReporter = new SharedStorageStatusReporter(sharedStorage, resourceBuilder);
			}

		return super.process(context, request);
	}

	private String processUpgrade(UpgradeSharedStorage request) {
		new Thread(new Jdbc2GmDbUpgrader(getSharedStorage(request), statusReporter)).start();
		return "DCSA Upgrade triggered. Use " + PollSharedStorageState.class.getSimpleName() + " to see the status.";
	}

	private String processDowngrade(DowngradeSharedStorage request) {
		new Jdbc2GmDbDowngrader(getSharedStorage(request), statusReporter).doDowngrade();
		return "DCSA Downgraded";
	}

	private SharedStorageState processInternalPollSharedStorageState() {
		return statusReporter.getState();
	}

	private SharedStorageState processPollSharedStorageState(ServiceRequestContext context) {
		MulticastRequest mRequest = MulticastRequest.T.create();
		mRequest.setServiceRequest(InternalPollSharedStorageState.T.create());

		MulticastResponse mResponse = mRequest.eval(context.getEvaluator()).get();

		Map<InstanceId, ServiceResult> responses = mResponse.getResponses();

		SharedStorageState state = null;
		for (Entry<InstanceId, ServiceResult> e : responses.entrySet()) {
			ServiceResult value = e.getValue();
			if (value instanceof ResponseEnvelope) {
				SharedStorageState _state = (SharedStorageState) ((ResponseEnvelope) value).getResult();
				if (state == null || state.getDate().before(_state.getDate()))
					state = _state;

			} else {
				state = processInternalPollSharedStorageState();
				state.getMigratedAccesses().clear();
				state.setStatus(SharedStorageStatus.POLLING_ERROR);
				state.setDetails(statusReporter.errorResource(getErrorText(value)));

				return state;
			}
		}

		return state;
	}

	private String getErrorText(ServiceResult value) {
		if (value instanceof Failure)
			return ((Failure) value).getMessage();
		else if (value instanceof Unsatisfied)
			return ((Unsatisfied) value).getWhy().stringify();
		else
			return value.toString();
	}

	private TemporaryJdbc2GmDbSharedStorage getSharedStorage(SharedStorageRequest request) {
		TemporaryJdbc2GmDbSharedStorage sharedStorage = sharedStorageSupplier.get();
		if (sharedStorage == null)
			throw new IllegalStateException("Cannot execute request of type " + request.entityType().getShortName() + ". No shared storage used.");
		return sharedStorage;
	}

}
