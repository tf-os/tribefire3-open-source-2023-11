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
package tribefire.extension.okta.processing.crud;

import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.crud.api.read.EntityReader;
import com.braintribe.model.access.crud.api.read.EntityReadingContext;
import com.braintribe.model.access.crud.api.read.PopulationReader;
import com.braintribe.model.access.crud.api.read.PopulationReadingContext;
import com.braintribe.model.access.crud.support.read.DispatchingPredicates;
import com.braintribe.model.access.crud.support.read.DispatchingReader;
import com.braintribe.model.access.crud.support.read.EmptyReader;
import com.braintribe.model.access.crud.support.read.IdReaderBridge;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.okta.api.model.AuthorizedOktaRequest;
import tribefire.extension.okta.api.model.user.GetGroup;
import tribefire.extension.okta.api.model.user.ListGroups;
import tribefire.extension.okta.model.OktaGroup;
import tribrefire.extension.okta.common.OktaCommons;

public class OktaGroupReader extends OktaReader
		implements EntityReader<OktaGroup>, PopulationReader<OktaGroup>, DispatchingPredicates, InitializationAware, OktaCommons {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OktaGroupReader.class);

	protected DispatchingReader<OktaGroup> dispatcher = new DispatchingReader<>();
	private Evaluator<ServiceRequest> evaluator;
	private String domainId = DEFAULT_OKTA_ACCESS_EXTERNALID;

	// ***************************************************************************************************
	// Setters
	// ***************************************************************************************************

	@Required
	@Configurable
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Configurable
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	// ***************************************************************************************************
	// Initializations
	// ***************************************************************************************************

	@Override
	public void postConstruct() {
		this.dispatcher
		//@formatter:off
				// Population Readers
				.registerPopulationReader(isExclusiveIdCondition(), IdReaderBridge.instance(this))
				.registerPopulationReader(isAlwaysTrue(), this::findEntries)
				// Property Readers
				.registerPropertyReader(isAlwaysTrue(), EmptyReader.instance()); // Default (last registration)
			//@formatter:on
	}

	// ***************************************************************************************************
	// EntityReader
	// ***************************************************************************************************

	@Override
	public OktaGroup getEntity(EntityReadingContext<OktaGroup> context) {
		GetGroup request = GetGroup.T.create();
		request.setGroupId(context.getId());

		OktaGroup oktaGroup = evaluateOktaRequest(request);
		return oktaGroup;
	}

	// ***************************************************************************************************
	// PopulationReader
	// ***************************************************************************************************

	@Override
	public Iterable<OktaGroup> findEntities(PopulationReadingContext<OktaGroup> context) {
		return this.dispatcher.findEntities(context);
	}

	protected Iterable<OktaGroup> findEntries(PopulationReadingContext<? extends OktaGroup> context) {
		ListGroups request = ListGroups.T.create();

		List<OktaGroup> oktaGroups = evaluateOktaRequest(request);
		return oktaGroups;
	}

	// ***************************************************************************************************
	// Helpers
	// ***************************************************************************************************

	@SuppressWarnings("unchecked")
	private <R> R evaluateOktaRequest(AuthorizedOktaRequest request) {
		request.setDomainId(this.domainId);
		return (R) request.eval(this.evaluator).get();
	}

}
