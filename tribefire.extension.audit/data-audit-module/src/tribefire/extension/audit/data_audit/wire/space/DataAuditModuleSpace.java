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
package tribefire.extension.audit.data_audit.wire.space;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.audit.processing.AuditAspect;
import tribefire.extension.audit.processing.ManipulationRecordCreator;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class DataAuditModuleSpace implements TribefireModuleContract {

	private static final String CORTEX_MODEL_GLOBAL_ID = "model:tribefire.cortex:tribefire-cortex-model";
	private static final String DATA_AUDIT_DEPLOYMENT_MODEL_GLOBAL_ID = "model:tribefire.extension.audit:data-audit-deployment-model";
	
	@Import
	private TribefireWebPlatformContract tfPlatform;

	//
	// Initializers
	//
	
	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind(c -> {
			ManagedGmSession session = c.getSession();
			GmMetaModel cortexAccessModel = session.getEntityByGlobalId(CORTEX_MODEL_GLOBAL_ID);
			GmMetaModel dataAuditDeploymentModel = session.getEntityByGlobalId(DATA_AUDIT_DEPLOYMENT_MODEL_GLOBAL_ID);
			cortexAccessModel.getDependencies().add(dataAuditDeploymentModel);
		});
	}
	
	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(tribefire.extension.audit.model.deployment.AuditAspect.T) //
			.component(tfPlatform.binders().accessAspect()) //
			.expertFactory(this::auditAspect);
	}
	
	
	@Managed
	private AuditAspect auditAspect(ExpertContext<tribefire.extension.audit.model.deployment.AuditAspect> expertContext) {
		tribefire.extension.audit.model.deployment.AuditAspect deployable = expertContext.getDeployable(); 
		AuditAspect bean = new AuditAspect();
		
		IncrementalAccess auditAccess = deployable.getAuditAccess();
		
		if (auditAccess != null) {
			bean.setAuditSessionProvider(() -> tfPlatform.systemUserRelated().sessionFactory().newSession(auditAccess.getExternalId()));
		}
		
		bean.setActive(true);
		bean.setUntrackedRoles(deployable.getUntrackedRoles());
		bean.setUserRolesProvider(tfPlatform.requestUserRelated().userRolesSupplier());
		bean.setManipulationRecordCreator(recordCreator());
		return bean;
	}
	
	@Managed
	private ManipulationRecordCreator recordCreator() {
		ManipulationRecordCreator bean = new ManipulationRecordCreator();
		return bean;
	}

}
