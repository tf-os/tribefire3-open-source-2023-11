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

import com.braintribe.model.extensiondeployment.ServiceAroundProcessor;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CacheAspect extends ServiceAroundProcessor {

	final EntityType<CacheAspect> T = EntityTypes.T(CacheAspect.class);

	String configuration = "configuration";
	String useCache = "useCache";
	String hashAlgorithm = "hashAlgorithm";

	@Mandatory
	CacheAspectConfiguration getConfiguration();
	void setConfiguration(CacheAspectConfiguration configuration);

	@Initializer("true")
	@Mandatory
	boolean getUseCache();
	void setUseCache(boolean useCache);

	@Mandatory
	@Initializer("enum(tribefire.extension.cache.model.deployment.service.HashAlgorithm,SHA256)")
	HashAlgorithm getHashAlgorithm();
	void setHashAlgorithm(HashAlgorithm hashAlgorithm);

}
