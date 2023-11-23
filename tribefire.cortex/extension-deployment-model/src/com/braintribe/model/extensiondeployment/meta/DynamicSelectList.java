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
package com.braintribe.model.extensiondeployment.meta;

import com.braintribe.model.extensiondeployment.RequestProcessing;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.PropertyMetaData;

/**
 * This metadata is similar to the VirtualEnum metadata. The only difference is that instead of having the constants defined
 * in the metadata itself, a ServiceRequest can be configured on it, which will then return a list of possible values.
 */
public interface DynamicSelectList extends PropertyMetaData {
	
	EntityType<DynamicSelectList> T = EntityTypes.T(DynamicSelectList.class);
	
	RequestProcessing getRequestProcessing();
	void setRequestProcessing(RequestProcessing rp);
	
	/**
	 * Used for knowing which editor should be used. If false (default), then an editor similar to the one used for the
	 * VirtualEnum should be used, this means a combo box is shown. If true, then a simplified selection UI should be
	 * used.
	 */
	boolean getOutlined();
	void setOutlined(boolean outlined);
	
	/**
	 * Used for disabling the client cache of the loading of the select list.
	 */
	boolean getDisableCache();
	void setDisableCache(boolean disableCache);

}
