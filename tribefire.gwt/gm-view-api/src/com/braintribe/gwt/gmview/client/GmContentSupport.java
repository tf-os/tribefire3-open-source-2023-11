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

public interface GmContentSupport {
	
	public void addGmContentViewListener(GmContentViewListener listener);
    
    public void removeGmContentViewListener(GmContentViewListener listener);
    
    /**
     * @param context - the GmContentContext
     */
    public default void setGmContentContext(GmContentContext context) {
    	//NOP
    }
    
    public default GmContentContext getGmContentContext() {
    		return null;
    }
    
    /**
     * @param listener = the listener to be added.
     */
    public default void addGmViewChangeListener(GmViewChangeListener listener) {
    	//NOP
    }
    
    /**
     * @param listener = the listener to be removed.
     */
    public default void removeGmViewChangeListener(GmViewChangeListener listener) {
    	//NOP
    }

}
