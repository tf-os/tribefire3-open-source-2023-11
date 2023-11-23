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
package tribefire.extension.hibernate.leadership_edr2cc.wire.space;

import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.leadership.Candidate;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.PlatformBindIds;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformHardwiredExpertsContract;

/**
 * Configures mappings on the {@link HibernateAccess} behind the system leadership manager (recognized by bindId
 * {@value PlatformBindIds#TRIBEFIRE_LEADERSHIP_DB_BIND_ID}.
 */
@Managed
public class HibernateLeadershipEdr2ccModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformHardwiredExpertsContract hardwiredExperts;

	@Override
	public void bindHardwired() {
		tfPlatform.hardwiredExperts().denotationTransformationRegistry() //
				.registerEnricher("HibernateLeadershiAccesspEdr2ccEnricher", HibernateAccess.T, this::enrichSystemAccess);
	}

	private DenotationEnrichmentResult<HibernateAccess> enrichSystemAccess(//
			DenotationTransformationContext context, HibernateAccess access) {

		// TODO this was replaced. Later remove this entire module
		// if (!PlatformBindIds.TRIBEFIRE_LEADERSHIP_DB_BIND_ID.equals(context.denotationId()))
		if (!"tribefire-leadership-db".equals(context.denotationId()))
			return DenotationEnrichmentResult.nothingNowOrEver();

		GmMetaModel model = access.getMetaModel();
		if (model == null)
			return DenotationEnrichmentResult.nothingYetButCallMeAgain();

		ModelMetaDataEditor mdEditor = tfPlatform.modelApi().newMetaDataEditor(model).done();

		MetaData mappedEntity = context.getEntityByGlobalId("hbm:mapped-entity");
		MetaData unmappedEntity = context.getEntityByGlobalId("hbm:unmapped-entity");

		mdEditor.onEntityType(GenericEntity.T) //
				.addMetaData(unmappedEntity);

		mdEditor.onEntityType(InstanceId.T) //
				.addMetaData(mappedEntity);
		mdEditor.onEntityType(Candidate.T) //
				.addMetaData(mappedEntity) //
				.addPropertyMetaData(Candidate.instanceId, instanceIdMapping(context));

		// un-map globalId and partition
		GmMetaModel basicHbmConfigModel = context.getEntityByGlobalId("model:tribefire.extension.hibernate:basic-hbm-configuration-model");
		model.getDependencies().add(basicHbmConfigModel);

		return DenotationEnrichmentResult.allDone(access, "Configured hbm mappings.");
	}

	private PropertyMapping instanceIdMapping(DenotationTransformationContext context) {
		PropertyMapping result = context.create(PropertyMapping.T);
		result.setGlobalId("edr2cc:hbm:leadership:Candidate.instanceId");
		result.setMapToDb(true);
		result.setCascade("all");
		result.setFetch("join");
		return result;
	}

}
