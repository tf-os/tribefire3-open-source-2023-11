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
package com.braintribe.model.processing.traversing.engine.impl.clone;

import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.engine.api.ClonerConfigurer;
import com.braintribe.model.processing.traversing.engine.api.customize.ClonerCustomization;
import com.braintribe.model.processing.traversing.engine.impl.AbstractTraversingConfigurer;

public class ClonerConfigurerImpl extends AbstractTraversingConfigurer<ClonerConfigurerImpl> implements ClonerConfigurer<ClonerConfigurerImpl> {

	ClonerCustomization customizer = new BasicClonerCustomization();

	@Override
	public ClonerConfigurerImpl customize(ClonerCustomization customization) {
		this.customizer = customization;
		return this;
	}

	@Override
	public ClonerConfigurerImpl visitor(GmTraversingVisitor visitor) {
		if (visitor instanceof Cloner) {
			((Cloner) visitor).setCustomizer(customizer);
		}
		return super.visitor(visitor);
	}

}
