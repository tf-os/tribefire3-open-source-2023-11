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
package tribefire.extension.xml.schemed.processing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.service.api.OutputConfig;
import com.braintribe.model.processing.service.api.OutputConfigAspect;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.stream.ReferencingFileInputStream;

import tribefire.extension.xml.schemed.marshaller.commons.ModelPersistenceExpert;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerResponse;
import tribefire.extension.xml.schemed.service.AnalyzeXsd;
import tribefire.extension.xml.schemed.service.AnalyzeXsdContainer;
import tribefire.extension.xml.schemed.service.AnalyzeXsdRequest;
import tribefire.extension.xml.schemed.service.AnalyzedXsd;
import tribefire.extension.xml.schemed.xsd.analyzer.SchemedXmlXsdAnalyzer;

/**
 * @author pit
 *
 */
public class XsdAnalyzingProcessor extends AbstractDispatchingServiceProcessor<AnalyzeXsdRequest, AnalyzedXsd> {
	private static Logger log = Logger.getLogger(XsdAnalyzingProcessor.class);
	
	
	@Override
	protected void configureDispatching(DispatchConfiguration<AnalyzeXsdRequest, AnalyzedXsd> dispatching) {
		dispatching.register(AnalyzeXsd.T, this::analyzeXsdFile);
		dispatching.register(AnalyzeXsdContainer.T, this::analyzeXsdContainer);
	}

	/**
	 * creates a new {@link SchemedXmlXsdAnalyzerRequest} based on the common data 
	 * @param from - the basic {@link AnalyzeXsdRequest}
	 * @return - a fresh {@link SchemedXmlXsdAnalyzerRequest}
	 */
	private SchemedXmlXsdAnalyzerRequest from( AnalyzeXsdRequest from) {
		SchemedXmlXsdAnalyzerRequest to = SchemedXmlXsdAnalyzerRequest.T.create();
		to.setBidirectionalLinks( from.getBidirectionalLinks());
		to.setCollectionOverrides(from.getCollectionOverrides());
		to.setMappingOverrides( from.getMappingOverrides());
		
		String skeletonModelName = from.getSkeletonModelName();
		to.setSkeletonModelName( skeletonModelName);
		
		String topPackageName = from.getTopPackageName();
		if (topPackageName == null) {
			topPackageName = skeletonModelName.substring(0, skeletonModelName.indexOf(':'));
		}
		to.setTopPackageName( topPackageName);
		to.setShallowSubstitutingModels( from.getShallowSubstitutingModels());
				
		return to;
	}
	
	/**
	 * creates a new {@link SchemedXmlXsdAnalyzerRequest} based on the {@link AnalyzeXsd}
	 * @param from - the {@link AnalyzeXsd}
	 * @return - a fresh {@link SchemedXmlXsdAnalyzerRequest}
	 */
	private SchemedXmlXsdAnalyzerRequest from( AnalyzeXsd from) {
		SchemedXmlXsdAnalyzerRequest to = from( (AnalyzeXsdRequest) from);
		to.setSchema( from.getSchema());
		to.setReferencedSchemata( from.getReferencedSchemata());
		return to;
	}
	
	/**
	 * creates a new {@link SchemedXmlXsdAnalyzerRequest} based on the {@link AnalyzeXsdContainer}
	 * @param from - the {@link AnalyzeXsdContainer}
	 * @return - a fresh {@link SchemedXmlXsdAnalyzerRequest}
	 */
	private SchemedXmlXsdAnalyzerRequest from( AnalyzeXsdContainer from) {
		SchemedXmlXsdAnalyzerRequest to = from( (AnalyzeXsdRequest) from);
		to.setContainerResource( from.getContainerResource());
		to.setContainerTerminalSchemaUri( from.getContainerTerminalSchemaUri());
		return to;
	}
	
	
	
	/**
	 * process the {@link AnalyzeXsd} request
	 * @param requestContext - {@link ServiceRequestContext}
	 * @param request - the {@link AnalyzeXsd}
	 * @return - answer as {@link AnalyzedXsd}
	 */
	public AnalyzedXsd analyzeXsdFile( ServiceRequestContext requestContext, AnalyzeXsd request) {
		SchemedXmlXsdAnalyzerRequest ar = from( request);		
		return analyze(requestContext, request, ar);
	}

	/**
	 * process the {@link AnalyzeXsdContainer} request
	 * @param requestContext - {@link ServiceRequestContext}
	 * @param request - the {@link AnalyzeXsdContainer}
	 * @return - answer as {@link AnalyzedXsd}
	 */
	public AnalyzedXsd analyzeXsdContainer( ServiceRequestContext requestContext, AnalyzeXsdContainer request) {
		SchemedXmlXsdAnalyzerRequest ar = from( request);
		return analyze(requestContext, request, ar);
	}
	
	/**
	 * call the analyzer with the {@link SchemedXmlXsdAnalyzerRequest} and post process the its result
	 * @param request - the {@link AnalyzeXsdRequest} 
	 * @param xmlRequest - the {@link SchemedXmlXsdAnalyzerRequest}
	 * @return - {@link AnalyzedXsd} answer
	 */
	private AnalyzedXsd analyze(ServiceRequestContext requestContext, AnalyzeXsdRequest request, SchemedXmlXsdAnalyzerRequest xmlRequest) {
		
		File output = new File(request.getOutputDir());
		
		output.mkdirs();

		SchemedXmlXsdAnalyzer analyzer = new SchemedXmlXsdAnalyzer();
		if (requestContext != null) {
			boolean verbose = requestContext.getAspect(OutputConfigAspect.class, OutputConfig.empty).verbose();		
			analyzer.setVerbose( verbose);
		}
		
		// call analyzer
		SchemedXmlXsdAnalyzerResponse analyzerResponse = analyzer.process(xmlRequest);
		
		
		// convert back 					
		AnalyzedXsd analyzedXsd = from( output, analyzerResponse);	
		
		// jar output requested
		if (request.getJarOutput()) {
			Resource archive = produceSkeletonJava(output, analyzerResponse.getSkeletonModel());
			analyzedXsd.setArtifact(archive);
		}

		// exchange package requested
		if (request.getExchangePackageOutput()) {
			Resource exchangePackage = produceExchangePackage(output, analyzerResponse);
			analyzedXsd.setExchangePackage(exchangePackage);
		}
		
		return analyzedXsd;
	}
	/**
	 * produce an {@link AnalyzedXsd} from what the actual processor returned
	 * @param output - the output directory, i.e. where to write to
	 * @param in - the {@link SchemedXmlXsdAnalyzerResponse} as received
	 * @return - the proper and initialized {@link AnalyzeXsd}
	 */
	private AnalyzedXsd from(File output, SchemedXmlXsdAnalyzerResponse in) {
		AnalyzedXsd out = AnalyzedXsd.T.create();
		//simply transfer
		out.setSkeletonModel( in.getSkeletonModel());
		out.setConstraintModel(in.getConstraintModel());
		out.setMappingModel( in.getMappingModel());
		out.setVirtualModel( in.getVirtualModel());

		// produce the resources 
		out.setSkeletonResource( processResource(output, in.getSkeletonModel()));
		out.setConstraintResource( processResource(output, in.getConstraintModel()));
		out.setMappingResource( processResource(output, in.getMappingModel()));
		
		return out;
	}
	
	private Resource produceSkeletonJava(File output, GmMetaModel model) {
		File dump;
		try {
			dump = Files.createTempDirectory(buildValidFile(model.getName() + "#" + model.getVersion())).toFile();
		} catch (IOException e1) {
			log.error("cannot create temporary directory",e1);
			return null;
		}				
				
		ModelPersistenceExpert.dumpModelJar( model, dump);
		
		File artifactArchive = new File( output, buildValidFile(model.getName() + "#" + model.getVersion()) + ".zip");
		
		try {
			Archives.zip().pack( dump).to( artifactArchive);
		} catch (ArchivesException e) {
			log.error("cannot create zip file from directory",e);
			return null;
		}
		// delete dump?
		List<File> filesToDelete = new ArrayList<>( Arrays.asList(dump.listFiles()));
		filesToDelete.add(dump);
		try {
			filesToDelete.stream().forEach( f -> f.delete());
		} catch (Exception e) {
			log.debug("couldn't clean temp directory [" + dump.getAbsolutePath() + "]");			
		}
		
		
		return  Resource.createTransient(() -> new ReferencingFileInputStream( artifactArchive));
	}
	
	private Resource produceExchangePackage( File output, SchemedXmlXsdAnalyzerResponse in) {
		// 
		String name = buildValidFile( in.getSkeletonModel().getName() + "#" + in.getSkeletonModel().getVersion()) + ".pck.xml";
	
		List<GmMetaModel> models = new ArrayList<>();				
		models.add( in.getConstraintModel());
		models.add( in.getMappingModel());
		models.add( in.getVirtualModel());
		
		File exchangePackageFile = ModelPersistenceExpert.dumpExchangePackage( output, name, in.getShallowTypes(), in.getSkeletonModel(), models.toArray( new GmMetaModel[0]));		

		return  Resource.createTransient(() -> new ReferencingFileInputStream( exchangePackageFile));
	}
	
	private Resource processResource( File outputDir, GmMetaModel model) {
		
		String fileName = buildValidFile(model.getName() + "#" + model.getVersion() + ".xml");
		File file = ModelPersistenceExpert.dumpMappingModel(model, outputDir, fileName);
		Resource resource = Resource.createTransient(() -> new ReferencingFileInputStream(file));;
		
		return resource;
	}
	
	private String buildValidFile(String name) {
		return name.replaceAll( "[:]", ".");
		
	}

}
