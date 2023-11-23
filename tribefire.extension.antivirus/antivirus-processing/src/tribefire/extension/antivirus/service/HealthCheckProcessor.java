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
package tribefire.extension.antivirus.service;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.ReflectionTools;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.antivirus.connector.api.AntivirusConnector;
import tribefire.extension.antivirus.model.deployment.repository.configuration.ClamAVSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.CloudmersiveSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.ProviderSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.VirusTotalSpecification;
import tribefire.extension.antivirus.model.service.result.AbstractAntivirusResult;
import tribefire.extension.antivirus.service.connector.clamav.ClamAVExpert;
import tribefire.extension.antivirus.service.connector.cloudmersive.CloudmersiveExpert;
import tribefire.extension.antivirus.service.connector.virustotal.VirusTotalExpert;

public class HealthCheckProcessor implements CheckProcessor {

	private static final Logger logger = Logger.getLogger(HealthCheckProcessor.class);

	private Supplier<PersistenceGmSession> cortexSessionSupplier;

	private String providerSpecificationType;
	private String providerModuleSpecificationMethod;
	private String providerActivatedMethod;

	@Override
	public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {
		CheckResult response = CheckResult.T.create();

		PersistenceGmSession cortexSession = cortexSessionSupplier.get();

		List<ProviderSpecification> providers = fetchProviderSpecification(cortexSession);

		List<CheckResultEntry> entries = response.getEntries();
		Resource resource = Resource.T.create();
		resource.assignTransientSource(() -> new ByteArrayInputStream("healthcheck".getBytes()));
		for (ProviderSpecification provider : providers) {
			AntivirusConnector<? extends AbstractAntivirusResult> expert;
			if (provider instanceof ClamAVSpecification) {
				expert = ClamAVExpert.forHealthCheck((ClamAVSpecification) provider, resource);
			} else if (provider instanceof CloudmersiveSpecification) {
				expert = CloudmersiveExpert.forHealthCheck((CloudmersiveSpecification) provider, resource);
			} else if (provider instanceof VirusTotalSpecification) {
				expert = VirusTotalExpert.forHealthCheck((VirusTotalSpecification) provider, resource);
			} else {
				throw new IllegalStateException("ProviderSpecification: '" + provider + "' not supported");
			}

			CheckResultEntry antivirusConnectorHealth = expert.actualHealth();
			entries.add(antivirusConnectorHealth);
		}

		logger.debug(() -> "Executed antivirus health check");

		return response;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	/**
	 * If the {@link #providerSpecificationType} is set then the configuration ({@link ProviderSpecification}) is
	 * fetched externally (e.g. from another module). There a query is done to fetch the types specified in
	 * {@link #providerSpecificationType}. Then there is a check if the provider should even be executed
	 * ({@link #providerActivatedMethod}). If so, {@link #providerModuleSpecificationMethod} will be executed to fetch
	 * the module related {@link ProviderSpecification}.
	 * 
	 * If the {@link #providerSpecificationType} is not set then the configuration will be queried by type
	 * {@link ProviderSpecification}
	 * 
	 * @param cortexSession
	 *            {@link PersistenceGmSession} - cortex session
	 * @return List of {@link ProviderSpecification} from the module
	 */
	/**
	 * @param cortexSession
	 * @return
	 */
	private List<ProviderSpecification> fetchProviderSpecification(PersistenceGmSession cortexSession) {
		List<ProviderSpecification> providers;
		if (CommonTools.isEmpty(providerSpecificationType)) {
			EntityQuery query = EntityQueryBuilder.from(ProviderSpecification.T).done();
			providers = cortexSession.query().entities(query).list();
		} else {
			EntityQuery query = EntityQueryBuilder.from(GMF.getTypeReflection().findEntityType(providerSpecificationType)).done();
			List<GenericEntity> externalProviders = cortexSession.query().entities(query).list();

			providers = new ArrayList<>();
			for (GenericEntity externalProvider : externalProviders) {
				Class<GenericEntity> javaType = externalProvider.entityType().getJavaType();
				Method[] methods = javaType.getMethods();
				// Method providerActivatedMethodMethod = ReflectionTools.getMethod(providerActivatedMethod, javaType);

				boolean activated;
				try {
					Method providerActivatedMethodMethod = javaType.getMethod(providerActivatedMethod);
					activated = (boolean) providerActivatedMethodMethod.invoke(externalProvider);
				} catch (Exception e) {
					throw Exceptions.unchecked(e,
							"Could not call method: '" + providerActivatedMethod + "' for externalProvider: '" + externalProvider + "'");
				}
				if (!activated) {
					continue;
				}

				Method providerModuleSpecificationMethodMethod = ReflectionTools.getMethod(providerModuleSpecificationMethod, javaType);
				ProviderSpecification providerSpecification;
				try {
					providerSpecification = (ProviderSpecification) providerModuleSpecificationMethodMethod.invoke(externalProvider);
					providers.add(providerSpecification);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw Exceptions.unchecked(e,
							"Could not call method: '" + providerModuleSpecificationMethod + "' for externalProvider: '" + externalProvider + "'");
				}
			}
		}
		return providers;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}

	@Required
	@Configurable
	public void setProviderSpecificationType(String providerSpecificationType) {
		this.providerSpecificationType = providerSpecificationType;
	}

	@Required
	@Configurable
	public void setProviderModuleSpecificationMethod(String providerModuleSpecificationMethod) {
		this.providerModuleSpecificationMethod = providerModuleSpecificationMethod;
	}

	@Required
	@Configurable
	public void setProviderActivatedMethod(String providerActivatedMethod) {
		this.providerActivatedMethod = providerActivatedMethod;
	}

}
