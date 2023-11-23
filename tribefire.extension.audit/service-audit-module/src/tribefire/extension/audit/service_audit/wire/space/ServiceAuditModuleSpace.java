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
package tribefire.extension.audit.service_audit.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.audit.processing.ServiceAuditInterceptor;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class ServiceAuditModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(tribefire.extension.audit.model.deployment.AuditInterceptor.T) //
			.component(tfPlatform.binders().serviceAroundProcessor()) //
			.expertFactory(this::auditInterceptor);
	}
	
	private ServiceAuditInterceptor auditInterceptor(ExpertContext<tribefire.extension.audit.model.deployment.AuditInterceptor> context) {
		ServiceAuditInterceptor bean = new ServiceAuditInterceptor();
		
		bean.setAuditAccessId(context.getDeployable().getAuditAccess().getExternalId());
		bean.setModelAccessoryFactory(tfPlatform.requestUserRelated().modelAccessoryFactory());
		bean.setMarshallerRegistry(tfPlatform.marshalling().registry());
		bean.setSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setSystemEvaluator(tfPlatform.systemUserRelated().evaluator());
		
		return bean;
	}

}
