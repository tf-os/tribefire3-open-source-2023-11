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
package com.braintribe.model.processing.resource.filesystem.path;

import java.nio.file.Path;

import com.braintribe.cfg.Required;

/**
 * @author peter.gazdik
 */
public class StaticFsPathResolver extends AbstractFsPathResolver {

	public static StaticFsPathResolver newInstance(Path basePath) {
		StaticFsPathResolver result = new StaticFsPathResolver();
		result.setBasePath(basePath);
		return result;
	}

	protected Path basePath;

	@Required
	public void setBasePath(Path basePath) {
		this.basePath = basePath.normalize();
	}

	@Override
	public Path resolveDomainPath(String accessId) {
		return basePath;
	}

}
