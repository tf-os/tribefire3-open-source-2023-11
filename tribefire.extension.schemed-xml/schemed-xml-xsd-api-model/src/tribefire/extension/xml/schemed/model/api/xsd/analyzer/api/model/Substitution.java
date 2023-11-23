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
package tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmType;

/**
 * a {@link Substitution} declare a type switch.
 * If only simple types are used, the SchemedXmlMarshaller still can un-/marshall an XML following the XSD,
 * but cannot handle complex types without specific handler. 
 * @author pit
 *
 */
@Description("replaces a type as extracted from the XSD with a already existing GmType")
public interface Substitution extends GenericEntity {
	
	final EntityType<Substitution> T = EntityTypes.T(Substitution.class);

	/**
	 * identifies the type, element or attribute in the xsd whose type is to be substituted,
	 * if the {@link SchemaAddress} element is null, then type itself if switched. 
	 * @return- the {@link SchemaAddress} pointing the place
	 */
	@Description("identifies the type, element or attribute in the xsd whose type is to be substituted, if the element is null, then type itself if switched.")
	@Alias("a")
	@Mandatory
	SchemaAddress getSchemaAddress();
	void setSchemaAddress( SchemaAddress address);

	/** 
	 * @return - the signature of the {@link GmType} to replace the XSD type with
	 */
	@Description("the signature of the GmType to replace the XSD type with")
	@Alias("s")
	@Mandatory
	String getReplacementSignature();
	void setReplacementSignature(String signature);
	
	
	/**  
	 * @return - the global id of the replacing type (null if auto naming as in JTA should happen) 
	 */
	@Description("the global id of the replacing type (null if auto naming as in JTA should happen)")
	@Alias("g")
	String getReplacementGlobalId();
	void setReplacementGlobalId( String globalId);
}
