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
package tribefire.extension.modelling.common.wire.space;

import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.modelling.common.wire.contract.CommonIconContract;
import tribefire.extension.modelling.common.wire.contract.CommonResourceContract;

@Managed
public class CommonIconSpace extends AbstractInitializerSpace implements CommonIconContract {

	@Import
	CommonResourceContract resources;

	@Managed
	@Override
	public AdaptiveIcon newIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("New Icon");
		bean.setRepresentations(set(resources.new16Png(), resources.new32Png(), resources.new64Png()));
		return bean;
	}
	
	@Managed
	@Override
	public AdaptiveIcon deleteIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		
		bean.setName("Delete Icon");
		bean.setRepresentations(set(resources.delete16Png(), resources.delete32Png(), resources.delete64Png()));
		return bean;
	}
	
	@Managed
	@Override
	public AdaptiveIcon openIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		
		bean.setName("Open Icon");
		bean.setRepresentations(set(resources.open16Png(), resources.open32Png(), resources.open64Png()));
		return bean;
	}
	
	@Managed
	@Override
	public AdaptiveIcon infoIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		
		bean.setName("Info Icon");
		bean.setRepresentations(set(resources.info16Png(), resources.info32Png(), resources.info64Png()));
		return bean;
	}
	
	@Managed
	@Override
	public AdaptiveIcon modelIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		
		bean.setName("Model Icon");
		bean.setRepresentations(set(resources.model16Png(), resources.model32Png(), resources.model64Png()));
		return bean;
	}

}
