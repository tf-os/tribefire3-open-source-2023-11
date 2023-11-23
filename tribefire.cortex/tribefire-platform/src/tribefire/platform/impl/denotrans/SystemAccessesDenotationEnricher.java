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
package tribefire.platform.impl.denotrans;

import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_AUTH;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_AUTH_NAME;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_AUTH;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_TRANSIENT_MESSAGING_DATA;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_USER_SESSIONS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_USER_STATISTICS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_TRANSIENT_MESSAGING_DATA;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_TRANSIENT_MESSAGING_DATA_NAME;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_USER_SESSIONS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_USER_SESSIONS_NAME;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_USER_STATISTICS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_USER_STATISTICS_NAME;
import static tribefire.module.api.DenotationEnrichmentResult.allDone;

import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.common.artifact.StandardArtifactReflection;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.utils.lcd.StringTools;

import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.PlatformBindIds;
import tribefire.module.api.SimpleDenotationEnricher;
import tribefire.platform.impl.initializer.Edr2ccPostInitializer;

/**
 * Sets name, globalId, externalId and model for known system accesses.
 * 
 * @see Edr2ccPostInitializer
 * 
 * @author peter.gazdik
 */
public class SystemAccessesDenotationEnricher extends SimpleDenotationEnricher<IncrementalAccess> {

	private static final Logger log = Logger.getLogger(SystemAccessesDenotationEnricher.class);

	public SystemAccessesDenotationEnricher() {
		super(IncrementalAccess.T);
	}

	@Override
	public DenotationEnrichmentResult<IncrementalAccess> enrich(DenotationTransformationContext context, IncrementalAccess access) {
		if (access.getExternalId() == null)
			return new SystemAccessesDenotationEnriching(context, access).run();
		else
			return DenotationEnrichmentResult.nothingNowOrEver();

	}

	private static class SystemAccessesDenotationEnriching {

		private final DenotationTransformationContext context;
		private final IncrementalAccess access;

		public SystemAccessesDenotationEnriching(DenotationTransformationContext context, IncrementalAccess access) {
			this.context = context;
			this.access = access;
		}

		public DenotationEnrichmentResult<IncrementalAccess> run() {
			switch (context.denotationId()) {
				case PlatformBindIds.AUTH_DB_BIND_ID:
					return enrich(ACCESS_AUTH, ACCESS_MODEL_AUTH, ACCESS_AUTH_NAME);

				case PlatformBindIds.USER_SESSIONS_DB_BIND_ID:
					return enrich(ACCESS_USER_SESSIONS, ACCESS_MODEL_USER_SESSIONS, ACCESS_USER_SESSIONS_NAME);

				case PlatformBindIds.USER_STATISTICS_DB_BIND_ID:
					return enrich(ACCESS_USER_STATISTICS, ACCESS_MODEL_USER_STATISTICS, ACCESS_USER_STATISTICS_NAME);

				case PlatformBindIds.TRANSIENT_MESSAGING_DATA_DB_BIND_ID:
					return enrich(ACCESS_TRANSIENT_MESSAGING_DATA, ACCESS_MODEL_TRANSIENT_MESSAGING_DATA, ACCESS_TRANSIENT_MESSAGING_DATA_NAME);

				default:
					return DenotationEnrichmentResult.nothingNowOrEver();
			}
		}

		private DenotationEnrichmentResult<IncrementalAccess> enrich(String externalId, String rawModelName, String name) {
			log.info("Edr2cc: Enriching " + name + " Access with externalId: " + externalId + ", rawModel: " + rawModelName);

			access.setGlobalId("edr2cc:access:" + externalId);
			access.setName(name);
			access.setExternalId(externalId);
			access.setMetaModel(rawConfiguredModel(rawModelName));

			return allDone(access, "Configured externalId to [" + externalId + "] and configured model based on raw model [" + rawModelName + "]");
		}

		private GmMetaModel rawConfiguredModel(String rawModelName) {
			// ArtifactReflection currentArtifact = _TribefireWebPlatform_.reflection;
			// TODO: THIS MUST BE REVERTED TO REAL ARTIFACT REFLECTION AS SOON AS POSSIBLE.
			ArtifactReflection currentArtifact = new StandardArtifactReflection("tribefire.cortex.services", "tribefire-web-platform", "3.0.81-pc",
					(String) null);

			String name = currentArtifact.groupId() + ":configuration-" + simpleModelName(rawModelName);

			GmMetaModel configuredModel = context.create(GmMetaModel.T);
			configuredModel.setGlobalId(Model.modelGlobalId(name));
			configuredModel.setName(name);
			configuredModel.setVersion(currentArtifact.version());
			configuredModel.getDependencies().add(findModel(rawModelName));

			return configuredModel;
		}

		private GmMetaModel findModel(String rawModelName) {
			return context.getEntityByGlobalId(Model.modelGlobalId(rawModelName));
		}

		private String simpleModelName(String modelName) {
			return StringTools.findSuffix(modelName, ":");
		}

	}
}
