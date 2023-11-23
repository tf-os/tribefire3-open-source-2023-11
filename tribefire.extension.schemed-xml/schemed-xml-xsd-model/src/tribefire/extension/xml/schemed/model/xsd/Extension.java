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
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasBase;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasChoice;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasGroup;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasSequence;

public interface Extension extends SchemaEntity, Annoted, SequenceAware, HasAttributes, HasAttributeGroups, HasGroup, HasChoice, HasAll, HasSequence, HasBase {
	
	final EntityType<Extension> T = EntityTypes.T(Extension.class);
			
}
