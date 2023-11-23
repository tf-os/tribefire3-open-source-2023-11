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
package tribefire.extension.appconfiguration.processing.services;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.lcd.StringTools;

import tribefire.extension.appconfiguration.model.AppConfiguration;
import tribefire.extension.appconfiguration.model.api.GetAppConfiguration;
import tribefire.extension.appconfiguration.model.api.GetAppConfigurationResponse;

public class AppConfigurationProcessor implements ServiceProcessor<GetAppConfiguration, GetAppConfigurationResponse> {

	private static final Logger logger = Logger.getLogger(AppConfigurationProcessor.class);

	private Supplier<PersistenceGmSession> sessionSupplier;

	//@formatter:off
	protected static TraversingCriterion ALL_TC =
		TC.create()
			.negation()
				.joker()
		.done();
	//@formatter:on

	//@formatter:off
	protected static TraversingCriterion TC_WithoutDescriptors = TC.create()
			.disjunction()
				.pattern()
					.entity(AppConfiguration.T)
					.disjunction()
						.property(AppConfiguration.descriptors)
					.close()
				.close()
			.close()
			.done();	
	//@formatter:on

	@Configurable
	@Required
	public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
		this.sessionSupplier = sessionSupplier;
	}

	@Override
	public GetAppConfigurationResponse process(ServiceRequestContext requestContext, GetAppConfiguration request) {
		Boolean includeDescriptors = request.getIncludeDescriptors();
		if (includeDescriptors == null) {
			includeDescriptors = true;
		}

		AppConfiguration config = getAppConfiguration(request.getName(), sessionSupplier.get(), includeDescriptors);

		GetAppConfigurationResponse response = GetAppConfigurationResponse.T.create();
		response.setConfiguration(config);

		return response;
	}

	static AppConfiguration getAppConfiguration(String appConfigurationName, PersistenceGmSession session, boolean includeDescriptors) {

		final EntityQuery query;
		TraversingCriterion tc = includeDescriptors ? ALL_TC : TC_WithoutDescriptors;

		if (!StringTools.isBlank(appConfigurationName)) {
			logger.debug("Fetching app configuration named '" + appConfigurationName + "'.");
			query = EntityQueryBuilder.from(AppConfiguration.T).where().property(AppConfiguration.name).eq(appConfigurationName).tc(tc).done();

		} else {
			logger.debug("Fetching first app configuration.");
			query = EntityQueryBuilder.from(AppConfiguration.T).tc(tc).limit(1).done();
		}

		AppConfiguration config = session.query().entities(query).first();

		return config;
	}

}