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
package com.braintribe.xml.sax.parser.test.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Container extends GenericEntity{
	final EntityType<Container> T = EntityTypes.T(Container.class);
	
	void setStringValue( String value);
	String getStringValue();
	
	void setBooleanValue( boolean value);
	boolean getBooleanValue();
	
	void setIntegerValue( int value);
	int getIntegerValue();
	
	void setStringSet( Set<String> stringSet);
	Set<String> getStringSet();
	
	void setStringList( List<String> stringList);
	List<String> getStringList();
	
	void setProcessingInstruction( String value);
	String getProcessingInstruction();
	
	void setGrouping( Grouping grouping);
	Grouping getGrouping();
	
	/*
	void setProperties(Map<String,String> map);
	Map<String,String> getProperties();
	
	void setPropertiesL(List<String> list);	
	List<String> getPropertiesL();
	*/
	void setAutoValue( String autoValue);
	String getAutoValue();
	
	void setEntries( List<Entry> entries);
	List<Entry> getEntries();
	
}
