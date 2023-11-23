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
package tribefire.extension.ldap.initializer.wire.space;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.ldap.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.ldap.initializer.wire.contract.LdapInitializerModuleModelsContract;
import tribefire.extension.ldap.templates.api.LdapTemplateContext;
import tribefire.extension.ldap.templates.wire.contract.LdapMetaDataContract;
import tribefire.extension.ldap.templates.wire.contract.LdapTemplatesContract;

@Managed
public class LdapInitializerModuleModelsSpace extends AbstractInitializerSpace implements LdapInitializerModuleModelsContract {

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private LdapTemplatesContract ldapTemplates;

	@Import
	private LdapMetaDataContract ldapMetaData;

	@Import
	private LdapInitializerModuleSpace ldapInitializer;

	@Override
	public void metaData() {
		LdapTemplateContext context = ldapInitializer.defaultContext();
		ldapMetaData.metaData(context);
	}

	@Override
	public GmMetaModel deploymentModel() {
		LdapTemplateContext context = ldapInitializer.defaultContext();
		return ldapMetaData.deploymentModel(context);
	}

	@Override
	public GmMetaModel serviceModel() {
		LdapTemplateContext context = ldapInitializer.defaultContext();
		return ldapMetaData.serviceModel(context);
	}

	@Override
	public void registerModels() {
		LdapTemplateContext context = ldapInitializer.defaultContext();
		ldapMetaData.registerModels(context);
	}
}
