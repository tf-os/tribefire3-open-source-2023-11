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
package com.braintribe.devrock.zarathud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.model.ResolvingRunnerContext;
import com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zarathud.model.request.AnalyzeArtifact;
import com.braintribe.devrock.zarathud.model.request.ZedRequest;
import com.braintribe.devrock.zarathud.model.response.AnalyzedArtifact;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.wire.ZedRunnerWireTerminalModule;
import com.braintribe.devrock.zarathud.runner.wire.contract.ZedRunnerContract;
import com.braintribe.devrock.zed.forensics.fingerprint.persistence.FingerPrintMarshaller;
import com.braintribe.devrock.zed.forensics.fingerprint.persistence.FingerprintOverrideContainer;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.DependencyForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;
import com.braintribe.zarathud.model.forensics.ModuleForensicsResult;

/**
 * jinni wrapper for zed
 * @author pit
 *
 */
public class ZedServiceProcessor  extends AbstractDispatchingServiceProcessor<ZedRequest, Object> {
	private static Logger log = Logger.getLogger(ZedServiceProcessor.class);
	private FingerPrintMarshaller fingerPrintMarshaller = new FingerPrintMarshaller();
	private YamlMarshaller marshaller;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	
	
	@Override
	protected void configureDispatching(DispatchConfiguration<ZedRequest, Object> dispatching) {
		dispatching.register( AnalyzeArtifact.T, this::analyze);
	}
	
	/**
	 * run the analysis 
	 * @param context - the {@link ServiceRequestContext}
	 * @param request - the {@link AnalyzeArtifact} request
	 * @return - the resulting {@link AnalyzedArtifact}
	 */
	private AnalyzedArtifact analyze( ServiceRequestContext context, AnalyzeArtifact request) {
		WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE);
		
		ResolvingRunnerContext rrc = ResolvingRunnerContext.T.create();
		rrc.setTerminal( request.getTerminal());
		ConsoleOutputVerbosity consoleOutputVerbosity = request.getVerbosity();
		if (consoleOutputVerbosity == null) {
			consoleOutputVerbosity = ConsoleOutputVerbosity.verbose;
		}
		rrc.setConsoleOutputVerbosity( consoleOutputVerbosity);
		
		ZedWireRunner zedWireRunner = wireContext.contract().resolvingRunner( rrc);
		
		Maybe<Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>>> resultsMaybe = zedWireRunner.run();
		
		Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>> results = resultsMaybe.get(); 
		
		AnalyzedArtifact response = AnalyzedArtifact.T.create();
		if (results.first == null) {
			log.warn("no data available for [" + request.getTerminal() + "]");
			return response;
		}
		
		String outputDirectory = request.getOutputDir();
		if (outputDirectory == null) {
			outputDirectory = ".";
		}
		
		File output = new File( outputDirectory);
		output.mkdirs();
		
		File fingerprintsFile = writeFingerPrints( output, request.getTerminal(), results.second);
		response.setFingerPrints( toResource(fingerprintsFile));
		
		if (request.getWriteAnalysisData()) {
			// write extraction & forensics & add at 
			Artifact analyzedArtifact = zedWireRunner.analyzedArtifact();
			File exFile = writeYaml(output, request.getTerminal(), "extraction", analyzedArtifact);
			response.setDependencyForensics( toResource( exFile));
						
			DependencyForensicsResult dependencyForensicsResult = zedWireRunner.dependencyForensicsResult();
			File depFile = writeYaml(output, request.getTerminal(), "dependency", dependencyForensicsResult);
			response.setDependencyForensics( toResource(depFile));
			
			ClasspathForensicsResult classpathForensicsResult = zedWireRunner.classpathForensicsResult();
			File cpFile = writeYaml(output, request.getTerminal(), "classpath", classpathForensicsResult);
			response.setClasspathForensics( toResource(cpFile));
			
			ModelForensicsResult modelForensicsResult = zedWireRunner.modelForensicsResult();
			File mdlFile = writeYaml(output, request.getTerminal(), "model", modelForensicsResult);
			response.setModelForensics( toResource(mdlFile));
			
			ModuleForensicsResult moduleForensicsResult = zedWireRunner.moduleForensicsResult();
			File moduleFile = writeYaml(output, request.getTerminal(), "module", moduleForensicsResult);
			response.setModelForensics( toResource(moduleFile));
			
		}
		
		return response;
	}
	
	/**
	 * build a resource from the file 
	 * @param file - {@link File} to wrap
	 * @return - resulting {@link Resource}
	 */
	private Resource toResource( File file) {
		Resource resource = Resource.createTransient(() -> new FileInputStream( file));
		return resource;
	}
	
	/**
	 * dumps fingerprints 
	 * @param output - {@link File} pointing to the directory
	 * @param terminalName - the name of the terminal 
	 * @param fingerPrints - the fingerprint data to store 
	 * @return - the {@link File} written
	 */
	private File writeFingerPrints( File output, String terminalName, Map<FingerPrint,ForensicsRating> fingerPrints) {
		FingerprintOverrideContainer fpovrc = new FingerprintOverrideContainer();
		fpovrc.setFingerprintOverrideRatings(fingerPrints);		
		terminalName = terminalName.replace( ':', '.');
		String name = terminalName + ".fpr.txt";
		File target = new File( output, name);
		try (OutputStream out = new FileOutputStream( target)) {
			fingerPrintMarshaller.marshall(out, fpovrc);
			return target;
		}
		catch (Exception e) {
			throw new IllegalStateException("cannot write ", e);
		}
	}
	
	/**
	 * dump data 
	 * @param output - {@link File} pointing to the directory 
	 * @param terminalName - the name of the terminal
	 * @param code - the code (actually a prefix to the suffix of the file)
	 * @param payload - the payload as {@link GenericEntity}
	 * @return - the {@link File} written 
	 */
	private File writeYaml( File output, String terminalName, String code, GenericEntity payload) {
		terminalName = terminalName.replace( ':', '.');
		String name = terminalName + "." + code + ".yaml";
		File target = new File( output, name);
		try (OutputStream out = new FileOutputStream( target)) {
			marshaller.marshall(out, payload);
			return target;
		}
		catch (Exception e) {
			throw new IllegalStateException("boink", e);
		}
		
	}
}
