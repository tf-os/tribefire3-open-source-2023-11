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
package com.braintribe.gwt.gmview.client;

import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.model.meta.data.prompt.AutoExpand;

/**
 * This interface should be implemented by views which are interested in receiving metadata defined within templates.
 * @author michel.docouto
 *
 */
public interface GmTemplateMetadataViewSupport {
	
	/**
     * Sets the optional display paths. Any path which is not included in this list, if it is set, should be hidden by default. 
     * @param columnData - the list of paths to be shown.
     */
    public void setColumnData(ColumnData columnData);
	
	public void setAutoExpand(AutoExpand autoExpand);

}
