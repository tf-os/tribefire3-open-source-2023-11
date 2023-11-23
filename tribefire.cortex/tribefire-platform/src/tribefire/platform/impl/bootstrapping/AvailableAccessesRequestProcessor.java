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
package tribefire.platform.impl.bootstrapping;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.bapi.AvailableAccesses;
import com.braintribe.model.bapi.AvailableAccessesRequest;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.utils.lcd.NullSafe;

/**
 * TODO: Move to the upcoming PersistenceReflectionServiceProcessor
 */
public class AvailableAccessesRequestProcessor implements ServiceProcessor<AvailableAccessesRequest, AvailableAccesses> {

	private Supplier<com.braintribe.model.access.IncrementalAccess> cortexAccessSupplier;
	private com.braintribe.model.access.IncrementalAccess cortexAccess;
	private ReentrantLock cortexAccessLock = new ReentrantLock();

	@Required
	public void setCortexAccessSupplier(Supplier<com.braintribe.model.access.IncrementalAccess> cortexAccessSupplier) {
		this.cortexAccessSupplier = cortexAccessSupplier;
	}

	@Override
	public AvailableAccesses process(ServiceRequestContext requestContext, AvailableAccessesRequest request) throws ServiceProcessorException {
		boolean includeHardwired = request.getIncludeHardwired();

		EntityQueryResult result = queryAccesses();

		AvailableAccesses availableAccesses = AvailableAccesses.T.create();
		List<IncrementalAccess> accesses = availableAccesses.getAccesses();

		for (GenericEntity access : result.getEntities())
			if (includeHardwired || !(access instanceof HardwiredAccess))
				accesses.add((IncrementalAccess) access);

		accesses.sort((a1, a2) -> {
			if (includeHardwired) {
				int res = isHw(a2) - isHw(a1);
				if (res != 0)
					return res;
			}

			return NullSafe.compare(a1.getName(), a2.getName());
		});

		return availableAccesses;
	}

	private EntityQueryResult queryAccesses() {
		//@formatter:off
		TraversingCriterion tc = TC.create()
			.pattern()
				.typeCondition(TypeConditions.isAssignableTo(IncrementalAccess.T))
				.negation()
					.disjunction()
						.property("name")
						.property("externalId")
						.property("id")
						.property("hardwired")
						.property("deploymentState")
					.close()
			.close()
			.done();
		
		EntityQuery entityQuery = EntityQueryBuilder
				.from(IncrementalAccess.T)
				.where()
				//TODO: originally checked for getDeployed() -> if this check actually needs to be aware of the deployment state an according DDSA request needs to be performed				
					.property(Deployable.autoDeploy).eq(true) 
			    .tc(tc)
				.done();
		//@formatter:on

		try {
			return getCortexAccess().queryEntities(entityQuery);
		} catch (ModelAccessException e) {
			throw new ServiceProcessorException("Error while querying for accesses", e);
		}
	}

	private com.braintribe.model.access.IncrementalAccess getCortexAccess() {
		if (cortexAccess == null)
			loadCortexAccessSync();

		return cortexAccess;
	}

	private void loadCortexAccessSync() {
		if (cortexAccess == null) {
			cortexAccessLock.lock();
			try {
				if (cortexAccess == null) {
					cortexAccess = cortexAccessSupplier.get();
				}
			} finally {
				cortexAccessLock.unlock();
			}
		}
	}

	private int isHw(IncrementalAccess a) {
		return a instanceof HardwiredAccess ? 1 : 0;
	}
}
