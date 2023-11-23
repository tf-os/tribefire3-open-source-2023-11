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
package com.braintribe.gwt.processdesigner.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ProcessDesignerResources extends ClientBundle {
	
	public static final ProcessDesignerResources INSTANCE = GWT.create(ProcessDesignerResources.class);
	
	@Source("Add_16x16.png")
	public ImageResource add();
	@Source("Add_32x32.png")
	public ImageResource addBig();
	
	@Source("Delete_32x32.png")
	public ImageResource remove();
	
	@Source("zoomOut_32x32.png")
	public ImageResource zoomOut();
	@Source("zoomIn_32x32.png")
	public ImageResource zoomIn();
	
	@Source("home_16x16.png")
	public ImageResource home();
	@Source("processDef_32x32.png")
	public ImageResource homeBig();
	
	@Source("refresh_16x16.png")
	public ImageResource restart();
	@Source("refresh_32x32.png")
	public ImageResource restartBig();
	
	@Source("user_16x16.png")
	public ImageResource user();
	@Source("undefined_16x16.png")
	public ImageResource undefined();	
	@Source("worker_32x32.png")
	public ImageResource worker();
	@Source("workers_32x32.png")
	public ImageResource workers();	
	@Source("end_16x16.png")
	public ImageResource end();
	
	@Source("init_32x32.png")
	public ImageResource init();
	public ImageResource select();
	public ImageResource connect();
	
	@Source("condition.edge.png")
	public ImageResource conditionalEdge();	
	@Source("edge.png")
	public ImageResource edge();
	@Source("error.node.png")
	public ImageResource errorNode();	
	@Source("overdue.node.png")
	public ImageResource overdueNode();

}
