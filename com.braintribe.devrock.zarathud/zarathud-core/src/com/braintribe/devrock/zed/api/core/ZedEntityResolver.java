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
package com.braintribe.devrock.zed.api.core;

import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * 
 * @author pit
 *
 */
public interface ZedEntityResolver {

	/**
	 * @param context - the {@link ZedAnalyzerContext} 
	 * @param desc - the desc as a {@link String}
	 * @return - a {@link ZedEntity}, either fully qualified or shallow
	 */
	public Maybe<ZedEntity> acquireClassResource(ZedAnalyzerContext context, String desc);
	
	/**
	 * @param context - the {@link ZedAnalyzerContext}
	 * @param zed - the {@link ZedEntity} to qualify (analyze etc)
	 */
	void qualify(ZedAnalyzerContext context, ZedEntity zed);
}
