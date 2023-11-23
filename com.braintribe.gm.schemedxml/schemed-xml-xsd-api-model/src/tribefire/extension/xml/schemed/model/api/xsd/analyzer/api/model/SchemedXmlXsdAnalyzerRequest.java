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

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * a {@link SchemedXmlXsdAnalyzerRequest} contains all the information the SchemedXmlMarshallerPrimer requires
 * to do its job.
 * 
 * @author pit
 *
 */
@Description("a SchemedXmlXsdAnalyzerRequest contains all the information the Analyzer requires to do its job.")
public interface SchemedXmlXsdAnalyzerRequest extends GenericEntity {
	
	final EntityType<SchemedXmlXsdAnalyzerRequest> T = EntityTypes.T(SchemedXmlXsdAnalyzerRequest.class);

	/** 
	 * @return - the resource that contains the XSD that should be parsed 
	 */
	@Description("the resource that contains the XSD that should be parsed")
	@Alias("s")
	@Mandatory
	Resource getSchema();
	void setSchema( Resource resource);
	
	/** 
	 * @return - name of the skeleton model, others are derived 
	 */
	@Description("name of the skeleton model, others are derived")
	@Alias("n")
	@Mandatory
	String getSkeletonModelName();
	void setSkeletonModelName(String name);
	
	/**	
	 * @return -  name of the top package (imported are attached)
	 */
	@Description("name of the top package (imported are attached)")
	@Alias("p")
	String getTopPackageName();
	void setTopPackageName( String name);
	
	
	/**
	 * @return - {@link ReferencedSchemata} that contains all referenced Schemata - if any
	 */
	@Description("all referenced schema - if any")
	@Alias("r")
	ReferencedSchemata getReferencedSchemata();
	void setReferencedSchemata( ReferencedSchemata schemata);

	/** 
	 * @return - the name of the schema (aka the name of the ZipEntry in the container resource if a container is used)
	 */
	@Description("the name of the schema (aka the name of the ZipEntry in the container resource if a container is used)")
	@Alias("t")
	String getContainerTerminalSchemaUri();
	void setContainerTerminalSchemaUri( String name);
	
	/**  
	 * @return - the ZIP resource (of the container) 
	 */
	@Description("the ZIP resource (of the container)")
	@Alias("c")
	Resource getContainerResource();
	void setContainerResource( Resource resource);

	
	/**
 
	 * @return -  auto-determination {@link CollectionOverride} for multiple entries (maxOccurs > 1) 
	 */
	@Description("auto-determination overrides for multiple entries (maxOccurs > 1)")
	@Alias("o")
	List<CollectionOverride> getCollectionOverrides();
	void setCollectionOverrides( List<CollectionOverride> overrides);
	
	/**
	 *  
	 * @return overrides - list of auto-determination {@link MappingOverride} for names
	 */
	@Description("auto-determination overrides for names")
	@Alias("m")
	List<MappingOverride> getMappingOverrides();
	void setMappingOverrides( List<MappingOverride> overrides);
	
	
	/**	 
	 * @return - list of {@link BidirectionalLink}
	 */
	@Description("bidirectional links")
	@Alias("b")
	List<BidirectionalLink> getBidirectionalLinks();
	void setBidirectionalLinks( List<BidirectionalLink> links);
	
	/** 
	 * @return - a list of {@link ShallowSubstitutingModel}
	 */
	@Description("type substitutions" )
	@Alias("u")
	List<ShallowSubstitutingModel> getShallowSubstitutingModels();
	void setShallowSubstitutingModels( List<ShallowSubstitutingModel> substitutions);
	
	/**  
	 * @return - true : maxOccurs == 1 sequences are appearing as types, false : they don't 
	 */
	@Description("experimental feature to expose 'sequence' structural types")
	boolean getExposeSequence();	
	void setExposeSequence( boolean expose);
	
	/**
	 * @return - true - maxOccurs == 1 choices are appearing as types, MD to XOR them not required, false they don't exist
	 */
	@Description("experimental feature to expose 'choice' as a type for simple model validation")
	boolean getExposeChoice();
	void setExposeChoice( boolean expose);
	
			 
}

