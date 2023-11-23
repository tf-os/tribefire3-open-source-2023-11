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
package com.braintribe.template.processing.projection.support;

import com.braintribe.template.processing.projection.StopTemplateProjectionException;

/**
 * 
 * Allows templates to control their projection.
 * 
 */
public class TemplateHandler {

	private String relocationTarget;

	/**
	 * Ignores the template projection.
	 */
	public void ignore() throws StopTemplateProjectionException{
		throw new StopTemplateProjectionException();
	}

	/**
	 * 
	 * Relocates the template projection in the installation directory. 
	 * If not set, the template is projected based on the current path in the 'projected' directory with the template extension stripped.
	 * 
	 */
	public void relocate(String relocationTarget) {
		this.relocationTarget = relocationTarget;
	}

	
	public String getRelocationTarget() {
		return relocationTarget;
	}
	
}