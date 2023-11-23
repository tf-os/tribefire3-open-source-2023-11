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
package com.braintribe.model.accessdeployment;

import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.DeployableComponent;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.service.domain.ServiceDomain;


/**
 * @author gunther.schenk
 * 
 */
@Abstract
@DeployableComponent
public interface IncrementalAccess extends Deployable, ServiceDomain {

	EntityType<IncrementalAccess> T = EntityTypes.T(IncrementalAccess.class);
	
	public final static String metaModel = "metaModel";
	public final static String workbenchAccess = "workbenchAccess";
	public final static String simulated = "simulated";
	/**
	 * @deprecated This property is not used as of tribefire 2.0 and will be soon removed.
	 */
	@Deprecated
	public final static String resourceFolderPath = "resourceFolderPath";
	public final static String aspectConfiguration = "aspectConfiguration";

	/**
	 * @deprecated This property is not used as of tribefire 2.0 and will be soon removed.
	 */
	@Deprecated
	public String getResourceFolderPath();
	/**
	 * @deprecated This property is not used as of tribefire 2.0 and will be soon removed.
	 */
	@Deprecated
	public void setResourceFolderPath(String resourceFolderPath);
	
	public AspectConfiguration getAspectConfiguration();
	public void setAspectConfiguration( AspectConfiguration aspectConfiguration);
	
	GmMetaModel getMetaModel();
	void setMetaModel(GmMetaModel metaModel);

	IncrementalAccess getWorkbenchAccess();
	void setWorkbenchAccess(IncrementalAccess workbenchAccess);

	/**
	 * If set to true, the actual type of the access will be ignored during deployment and a {@code SmoodAccess} will be
	 * deployed instead (as a proxy).
	 */
	boolean getSimulated();
	void setSimulated(boolean simulated);

}
