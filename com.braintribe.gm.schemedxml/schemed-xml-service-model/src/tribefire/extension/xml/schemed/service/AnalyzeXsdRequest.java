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
package tribefire.extension.xml.schemed.service;

import java.util.List;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.DomainRequest;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.BidirectionalLink;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.CollectionOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.MappingOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ShallowSubstitutingModel;

@Abstract
public interface AnalyzeXsdRequest extends DomainRequest {
	
	EntityType<AnalyzeXsdRequest> T = EntityTypes.T(AnalyzeXsdRequest.class);	
	
	public static final String DOMAIN_ID = "serviceDomain.schemed-xml";
		
	
	@Initializer("'"+DOMAIN_ID + "'")
	@Override
	String getDomainId();
	
	
	/** 
	 * @return - name of the skeleton model, others are derived 
	 */
	@Description("name of the skeleton model, others are derived")
	@Alias("n")
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
	 
	 * @return -  auto-determination {@link CollectionOverride} for multiple entries (maxOccurs > 1) 
	 */
	@Description("auto-determination overrides for multiple entries (maxOccurs > 1)")
	@Alias("oc")
	List<CollectionOverride> getCollectionOverrides();
	void setCollectionOverrides( List<CollectionOverride> overrides);
	
	/**
	 *  
	 * @return overrides - list of auto-determination {@link MappingOverride} for names
	 */
	@Description("auto-determination overrides for names")
	@Alias("om")
	List<MappingOverride> getMappingOverrides();
	void setMappingOverrides( List<MappingOverride> overrides);
	
	
	/**	 
	 * @return - list of {@link BidirectionalLink}
	 */
	@Description("bidirectional links")
	@Alias("bi")
	List<BidirectionalLink> getBidirectionalLinks();
	void setBidirectionalLinks( List<BidirectionalLink> links);
	
	/** 
	 * @return - a list of {@link ShallowSubstitutingModel}
	 */
	@Description("type substitutions" )
	@Alias("ot")
	List<ShallowSubstitutingModel> getShallowSubstitutingModels();
	void setShallowSubstitutingModels( List<ShallowSubstitutingModel> substitutions);
	
	
	/** 
	 * @return - the local output directory
	 */
	@Description("the output directory where the different output should be put into")
	@Alias( "o")
	String getOutputDir();
	void setOutputDir(String outputDir);
	
	/**
	 * @return - true if an exchange package with the 4 models is made
	 */ 
	@Description("set if you want to have an exchange package produced in addition")
	@Alias("e")
	boolean getExchangePackageOutput();
	void setExchangePackageOutput( boolean output);
	
	/**
	 * @return - true if the skeleton model should also be made into an artifact
	 */
	@Description("set if you want to the skeleton produced as a full artifact")
	@Alias("j")
	boolean getJarOutput();
	void setJarOutput( boolean output);
	
}
