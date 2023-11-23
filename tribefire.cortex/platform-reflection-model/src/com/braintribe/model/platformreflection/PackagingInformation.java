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
package com.braintribe.model.platformreflection;

import java.util.List;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.packaging.Packaging;
import com.braintribe.model.platformreflection.request.PlatformReflectionResponse;
import com.braintribe.model.platformreflection.tf.ClasspathContainer;

public interface PackagingInformation extends PlatformReflectionResponse {

	EntityType<PackagingInformation> T = EntityTypes.T(PackagingInformation.class);

	void setPackaging(Packaging packaging);
	Packaging getPackaging();

	void setPlatformAsset(PlatformAsset setupAsset);
	PlatformAsset getPlatformAsset();

	List<ClasspathContainer> getClasspathContainers();
	void setClasspathContainers(List<ClasspathContainer> classpathContainers);
}
