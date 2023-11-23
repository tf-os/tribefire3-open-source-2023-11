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

import tribefire.extension.xml.schemed.model.xsd.restrictions.Enumeration;
import tribefire.extension.xml.schemed.model.xsd.restrictions.FractionDigits;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Length;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxExclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxInclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxLength;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinExclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinInclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinLength;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Pattern;
import tribefire.extension.xml.schemed.model.xsd.restrictions.TotalDigits;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Whitespace;

public interface SimpleTypeRestriction extends Restriction, SequenceAware {
	
	final EntityType<SimpleTypeRestriction> T = EntityTypes.T(SimpleTypeRestriction.class);
		
	SimpleType getSimpleType();
	void setSimpleType( SimpleType simpleType);
	
	java.util.List<Enumeration> getEnumerations();
	void setEnumerations(java.util.List<Enumeration> values);
	
	Whitespace getWhitespace();
	void setWhitespace( Whitespace value);
	
	Pattern getPattern();
	void setPattern( Pattern pattern);
	
	Length getLength();
	void setLength( Length value);
	
	MinLength getMinLength();
	void setMinLength( MinLength value);
	
	MaxLength getMaxLength();
	void setMaxLength( MaxLength value);
	
	MinInclusive getMinInclusive();
	void setMinInclusive( MinInclusive value);
	
	MinExclusive getMinExclusive();
	void setMinExclusive( MinExclusive value);
	
	MaxInclusive getMaxInclusive();
	void setMaxInclusive( MaxInclusive value);
	
	MaxExclusive getMaxExclusive();
	void setMaxExclusive( MaxExclusive value);
	
	TotalDigits getTotalDigits();
	void setTotalDigits( TotalDigits value);
	
	FractionDigits getFractionDigits();
	void setFractionDigits( FractionDigits value);
	
	
}
