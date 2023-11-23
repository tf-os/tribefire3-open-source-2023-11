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
package com.braintribe.gwt.processdesigner.client;

import com.braintribe.gwt.processdesigner.client.vector.Point;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;

public class ProcessDesignerConfigurator implements ManipulationListener{
	
	private ProcessDesignerConfiguration pdc;
	//private ProcessDesignerRenderer processDesignerRenderer;
	
	/*public void setProcessDesignerRenderer(ProcessDesignerRenderer processDesignerRenderer) {
		this.processDesignerRenderer = processDesignerRenderer;
	}*/
	
	public void setProcessDesignerConfiguration(ProcessDesignerConfiguration processDesignerConfiguration) {
		this.pdc = processDesignerConfiguration;
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		configure();
	}
	
	public void configure(){
		Point defaultStartingPoint = Point.T.create();
		defaultStartingPoint.setX(0 + pdc.getDefaultOffset());
		defaultStartingPoint.setY(0 + pdc.getDefaultOffset());
//		pdc.setDefaultStartingPoint(defaultStartingPoint);
		
//		processDesignerRenderer.render();
	}

}
