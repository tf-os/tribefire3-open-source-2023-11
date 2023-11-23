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
package tribefire.extension.xml.schemed.model.xsd;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.xml.schemed.model.xsd.archetypes.HasAll;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasAttributeGroups;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasAttributes;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasChoice;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasElements;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasGroup;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasSequence;

public interface ComplexType extends Type, SequenceAware, HasAttributes, HasAttributeGroups, HasAll, HasGroup, HasSequence, HasChoice, HasElements {
	
	final EntityType<ComplexType> T = EntityTypes.T(ComplexType.class);
	
	boolean getAbstract();
	void setAbstract( boolean value);
	
	boolean getAbstractSpecified();
	void setAbstractSpecified( boolean value);
	
	boolean getMixed();
	void setMixed( boolean value);
	
	boolean getMixedSpecified();
	void setMixedSpecified( boolean value);
	
	Block getBlock();
	void setBlock( Block value);
	
	Final getFinal();
	void setFinal( Final value);
	
	
	ComplexContent getComplexContent();
	void setComplexContent( ComplexContent content);
	
	SimpleContent getSimpleContent();
	void setSimpleContent( SimpleContent content);
	
}
