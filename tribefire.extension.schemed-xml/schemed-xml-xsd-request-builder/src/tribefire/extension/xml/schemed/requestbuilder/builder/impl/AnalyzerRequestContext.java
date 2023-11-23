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
package tribefire.extension.xml.schemed.requestbuilder.builder.impl;

import java.util.function.Supplier;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.resource.Resource;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.BidirectionalLink;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.CollectionOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.MappingOverride;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ReferencedSchemata;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ShallowSubstitutingModel;
import tribefire.extension.xml.schemed.requestbuilder.resource.ResourceProvidingSession;

/**
 * a context to build a {@link SchemedXmlXsdAnalyzerRequest}
 * 
 * @author pit
 *
 */
public class AnalyzerRequestContext implements 	MappingOverrideConsumer, 
												CollectionOverrideConsumer,
												XsdResourceConsumer,
												SchemaReferencesConsumer,
												BidirectionalLinkReceiver,
												ShallowSubstitutionModelConsumer,
												Supplier<ResourceProvidingSession>{
	
	private SchemedXmlXsdAnalyzerRequest request = SchemedXmlXsdAnalyzerRequest.T.create();
	private ResourceProvidingSession session;
	
	
	 
	/**
	 * access the {@link XsdContext} to specify the input XSD
	 * @return - the {@link XsdContext} specialized for handling input files
	 */
	public XsdContext<AnalyzerRequestContext> xsd() {
		return new XsdContext<AnalyzerRequestContext>(this, this);
	}	
	
	@Override
	public void accept(Resource xsdResource, String terminal) {
		request.setContainerResource(xsdResource);
		request.setContainerTerminalSchemaUri(terminal);
	}
	@Override
	public void accept(Resource xsdResource) {		
		request.setSchema(xsdResource);
	}

	/**
	 * access the {@link SchemaReferencesContext} to declare referenced schemata,
	 * see the {@link XsdContext} for alternatives
	 * @return
	 */
	public SchemaReferencesContext<AnalyzerRequestContext> references() {
		return new SchemaReferencesContext<AnalyzerRequestContext>(this, this);
	}
	
	@Override
	public void accept(ReferencedSchemata references) {
		request.setReferencedSchemata(references);					
	}
	
	/**
	 * access the {@link NameOverrideContext} to influence the name mapping
	 * @return - the {@link NameOverrideContext} specialized for name mapping overrides
	 */
	public NameOverrideContext<AnalyzerRequestContext> nameOverride() {
		return new NameOverrideContext<AnalyzerRequestContext>(this);
	}		
	@Override
	public void accept(MappingOverride override) {
		request.getMappingOverrides().add(override);		
	}

	/**
	 * access the {@link CollectionOverrideContext} to influence the collection types that
	 * are used for 'maxOccur > 1' cases 
	 * @return - the {@link CollectionOverrideContext} specialized for collection type generation
	 */
	public CollectionOverrideContext<AnalyzerRequestContext> collectionOverride() {
		return new CollectionOverrideContext<AnalyzerRequestContext>(this);
	}		
	@Override
	public void accept(CollectionOverride override) {
		request.getCollectionOverrides().add(override);		
	}


	
	/**
	 * access the {@link BidirectionalLinkContext} to add bi-directional linking of properties and their owners
	 * @return
	 */
	public BidirectionalLinkContext<AnalyzerRequestContext> bidirectional() {
		return new BidirectionalLinkContext<AnalyzerRequestContext>( this);
	}
	@Override
	public void accept(BidirectionalLink link) {
		request.getBidirectionalLinks().add(link);
		
	}
	
	/**
	 * access the {@link ShallowSubstitutionModelContext} to add substitutions
	 * @return
	 */
	public ShallowSubstitutionModelContext< AnalyzerRequestContext> substitutionModel() {
		return new ShallowSubstitutionModelContext<AnalyzerRequestContext>( this);
	}
	@Override
	public void accept(ShallowSubstitutingModel model) {
		request.getShallowSubstitutingModels().add(model);		
	}
	

	/**
	 * specify the fully qualified model name, i.e. groupid, artifactId and version,
	 * in the condensed form <groupId>:<artifactId>#<version>
	 * @param modelName - the fully qualified name of the model 
	 * @return - the {@link AnalyzerRequestContext} 
	 */
	public AnalyzerRequestContext modelName( String modelName) {
		request.setSkeletonModelName(modelName);
		return this;
	}	
	/**
	 * specify the name of the top package in the model, i.e. the prefix of the type-signature of 
	 * the {@link GmEntityType} created, and in consequence the top package name in the JAVA form
	 * @param packageName - a valid JAVA style package name
	 * @return - the {@link AnalyzerRequestContext}
	 */
	public AnalyzerRequestContext packageName( String packageName) {
		request.setTopPackageName(packageName);
		return this;
	}
	
	/**
	 * override the standard {@link ResourceProvidingSession} that is used to generate
	 * the file system based resources 
	 * @param session
	 * @return
	 */
	public AnalyzerRequestContext session( ResourceProvidingSession session) {
		this.session = session;
		return this;	
	} 
	

	@Override
	public ResourceProvidingSession get() {	
		if (session == null) {
			session = new ResourceProvidingSession();
		}
		return session;
	}
	
	
	

	/**
	 * finish and package the {@link SchemedXmlXsdAnalyzerRequest}, ready to be processed
	 * @return
	 */
	public SchemedXmlXsdAnalyzerRequest build() {
		return request;
	}
	
	
	
}
