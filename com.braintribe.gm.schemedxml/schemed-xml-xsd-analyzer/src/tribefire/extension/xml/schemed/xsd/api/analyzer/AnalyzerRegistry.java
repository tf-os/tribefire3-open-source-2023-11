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
package tribefire.extension.xml.schemed.xsd.api.analyzer;

import java.util.List;
import java.util.Map;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.model.xsd.Schema;

public interface AnalyzerRegistry {
	/**
	 * get the {@link SchemaRegistry} that is responsible for the {@link Schema}
	 * @param schema - the {@link Schema}
	 * @return - the corresponding {@link SchemaRegistry}
	 */
	SchemaRegistry getBackingRegistryForSchema( Schema schema);
	
	/**
	 * connect the {@link SchemaRegistry} to the  {@link Schema} it is responsible for
	 * @param schema - the {@link Schema}
	 * @param registry - the {@link SchemaRegistry}
	 */
	void setBackingRegistryForSchema( Schema schema, SchemaRegistry registry);
	
	
	/**
	 * returns all {@link GmType} that were collected during the run
	 * @return - {@link Map} of type signature to {@link GmType}
	 */
	Map<String, GmType> getExtractedTypes();
	
	List<GmMetaModel> getActualSubstitutionModels();
	
	/**
	 * returns the top level elements that were collected plus their associated {@link GmType}, they'll be the possible root types
	 * @return - {@link Map} of {@link Element}'s name to linked {@link GmType}
	 */
	Map<String, GmType> getExtractedTopLevelElements();
	
	
	String getTargetNamespace();
	
	Map<String, String> getPrefixToNamespacesMap();
	
	public Map<String, Namespace> getNamespaces();
	
	boolean getElementQualified();
	
	boolean getAttributeQualified();
	
}
