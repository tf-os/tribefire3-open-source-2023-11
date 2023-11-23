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
package com.braintribe.gwt.smartmapper.client.util;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAsIs;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public class ConversionRendering {
	
	private static Map<EntityType<? extends GenericEntity>, String> renderMap;
	
	static{
		renderMap = new HashMap<EntityType<? extends GenericEntity>, String>();
		
		renderMap.put(PropertyAsIs.T, "=");
		renderMap.put(QualifiedPropertyAssignment.T, "=");
		renderMap.put(KeyPropertyAssignment.T, "join");
		renderMap.put(InverseKeyPropertyAssignment.T, "^join");
		renderMap.put(LinkPropertyAssignment.T, "join+");
		renderMap.put(OrderedLinkPropertyAssignment.T, "ordered join+");
		renderMap.put(CompositeKeyPropertyAssignment.T, "joins");
		renderMap.put(CompositeInverseKeyPropertyAssignment.T, "^joins");
	}
	
	public static String renderConversion(EntityType<GenericEntity> type){
		if(renderMap.containsKey(type)){
			return renderMap.get(type);
		}else
			return "?";
	}

}
