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
package tribefire.extension.demo.demo_wb_initializer.wire.space;

import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerIconContract;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerResourceContract;

@Managed
public class DemoWbInitializerIconSpace extends AbstractInitializerSpace implements DemoWbInitializerIconContract {

	@Import
	DemoWbInitializerResourceContract resources;
	
	@Managed
	@Override
	public SimpleIcon logoIcon() {
		SimpleIcon bean = create(SimpleIcon.T);
		
		bean.setName("Logo Icon");
		bean.setImage(resources.logoPng());
		
		return bean;
	}

	@Managed
	@Override
	public Icon tribefireIcon() {
		SimpleIcon bean = create(SimpleIcon.T);
		
		bean.setName("tribefire Icon");
		bean.setImage(resources.tribefire16Png());
		
		return bean;
	}

	@Managed
	@Override
	public Icon personIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("Person Icon");
		bean.setRepresentations(set(
				resources.person16Png(),
				resources.person32Png(),
				resources.person64Png()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Icon companyIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		
		bean.setName("Company Icon");
		bean.setRepresentations(set(
				resources.company16Png(),
				resources.company32Png(),
				resources.company64Png()
				));
		
		return bean;
	}
	
}
