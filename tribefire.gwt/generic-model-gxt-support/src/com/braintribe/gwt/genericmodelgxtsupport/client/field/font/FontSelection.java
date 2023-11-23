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
package com.braintribe.gwt.genericmodelgxtsupport.client.field.font;

public class FontSelection {

	  private String id;
	  private String name;
	 
	  public FontSelection() {	 
	  }
	 
	  public FontSelection(String id, String name) {
	    this();
	    setId(id);
	    setName(name);
	  }
	 
	  public String getId() {
	    return id;
	  }
	 
	  public void setId(String id) {
	    this.id = id;
	  }
	 
	  public String getName() {
	    return name;
	  }
	 
	  public void setName(String name) {
	    this.name = name;
	  }
 }
