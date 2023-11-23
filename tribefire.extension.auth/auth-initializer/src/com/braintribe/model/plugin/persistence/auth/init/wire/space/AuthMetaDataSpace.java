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
package com.braintribe.model.plugin.persistence.auth.init.wire.space;

import com.braintribe.gm.persistence.initializer.support.integrity.wire.contract.CoreInstancesContract;
import com.braintribe.gm.persistence.initializer.support.wire.space.AbstractInitializerSpace;
import com.braintribe.logging.Logger;
import com.braintribe.model.plugin.persistence.auth.init.wire.contract.AuthMetaDataContract;
import com.braintribe.model.plugin.persistence.auth.init.wire.contract.AuthRuntimePropertyDefinitions;
import com.braintribe.model.plugin.persistence.auth.init.wire.contract.ExistingInstancesContract;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class AuthMetaDataSpace extends AbstractInitializerSpace implements AuthMetaDataContract {

	private static final Logger logger = Logger.getLogger(AuthMetaDataSpace.class);

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private AuthRuntimePropertyDefinitions properties;

	@Import
	private CoreInstancesContract coreInstances;


	@Override
	public void metaData() {

		BasicModelMetaDataEditor deploymentModelEditor = new BasicModelMetaDataEditor(existingInstances.deploymentModel());


	}
}
