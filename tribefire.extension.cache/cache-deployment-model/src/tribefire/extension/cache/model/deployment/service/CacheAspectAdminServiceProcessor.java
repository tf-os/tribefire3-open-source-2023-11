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
package tribefire.extension.cache.model.deployment.service;

import java.util.Set;

import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CacheAspectAdminServiceProcessor extends ServiceProcessor {

	final EntityType<CacheAspectAdminServiceProcessor> T = EntityTypes.T(CacheAspectAdminServiceProcessor.class);

	String cacheAspects = "cacheAspects";

	@Name("Cache Aspects")
	@Description("Optional Set of 'CacheAspects' which is used to restrict admin operations to specific 'CacheAspects'")
	Set<CacheAspect> getCacheAspects();
	void setCacheAspects(Set<CacheAspect> cacheAspects);

}
