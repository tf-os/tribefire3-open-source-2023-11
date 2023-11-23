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
package com.braintribe.devrock.zarathud.runner.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLocators;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationLocatorContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * helper to run 'real life' environment-based classpath resolving..
 * uses a {@link EnvironmentSensitiveConfigurationWireModule} which you can influence via 
 * location (of the current directory) and/or environment-variables 
 * 
 * @author pit
 *
 */
public abstract class ClasspathResolvingUtil {

	
	public static TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
					
	private static YamlMarshaller marshaller;
	static {
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	
	/**
	 * builds a simple {@link OverridingEnvironment} with the passed *environment* overrides
	 * @param overrides - a {@link Map} of env-variables and their values 
	 * @return - an {@link OverridingEnvironment}
	 */
	public static OverridingEnvironment buildVirtualEnvironment(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}						
		return ove;		
	}
	
	
	/**
	 * helper function to resolve a {@link CompiledDependencyIdentification}
	 * @param cdi - the {@link CompiledDependencyIdentification}
	 * @param overrides - a {@link Map} of env-variables
	 * @return - a {@link Maybe} of the resolved {@link CompiledArtifactIdentification}
	 */
	public static Maybe<CompiledArtifactIdentification> resolve( CompiledDependencyIdentification cdi, Map<String,String> overrides) {
		EnvironmentSensitiveConfigurationWireModule environmentSensitiveConfigurationWireModule = new EnvironmentSensitiveConfigurationWireModule( buildVirtualEnvironment(overrides));
		RepositoryConfigurationLocator locator = RepositoryConfigurationLocators.buildDefault().done();
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, environmentSensitiveConfigurationWireModule)
																						.bindContract(RepositoryConfigurationLocatorContract.class, () -> locator)
																						.build();
			) {
			DependencyResolver dependencyResolver = resolverContext.contract().transitiveResolverContract().dataResolverContract().dependencyResolver();
			Maybe<CompiledArtifactIdentification> resolvedMaybe = dependencyResolver.resolveDependency(cdi);
			return resolvedMaybe;
		}
		catch( Exception e) {
			return Maybe.empty( TemplateReasons.build(InternalError.T).assign( InternalError::setJavaException,e).toReason());
		}		
	}
	
	
	/**
	 * runs a CPR run on the terminal as DEPENDENCY (the jar of the terminal appears in the solutions, no optionals)
	 * @param terminal - the qualified name of the dependency 
	 * @param resolutionContext - the {@link ClasspathResolutionContext}
	 * @param overrides - a {@link Map} of environment-variables (may be null) 
	 * @return a {@link Maybe} with the resultng {@link AnalysisArtifactResolution}
	 */
	public static Maybe<AnalysisArtifactResolution> runAsDependency(String terminal, ClasspathResolutionContext resolutionContext, Map<String,String> overrides) {
		RepositoryConfigurationLocator locator = RepositoryConfigurationLocators.buildDefault().done();
		EnvironmentSensitiveConfigurationWireModule environmentSensitiveConfigurationWireModule = new EnvironmentSensitiveConfigurationWireModule( buildVirtualEnvironment(overrides));
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, environmentSensitiveConfigurationWireModule)
																					.bindContract(RepositoryConfigurationLocatorContract.class, () -> locator)
																					.build();
			) {
			
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));			
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);					
			return Maybe.complete( artifactResolution);													
		}		
		catch( Exception e) {
			return Maybe.empty( TemplateReasons.build(InternalError.T).assign( InternalError::setJavaException,e).toReason());
		}		
	}
	/**
	 * runs a CPR run on the terminal as an ARTIFACT (the jar of the terminal does NOT appear in the solutions, optionals are considered if set)
	 * @param terminal - the qualified name of the artifact 
	 * @param resolutionContext - the {@link ClasspathResolutionContext}
	 * @param overrides - a {@link Map} of environment-variables (may be null) 
	 * @return a {@link Maybe} with the resultng {@link AnalysisArtifactResolution}
	 */
	public static  Maybe<AnalysisArtifactResolution> runAsArtifact(String terminal, ClasspathResolutionContext resolutionContext, Map<String,String> overrides) {
		
		EnvironmentSensitiveConfigurationWireModule environmentSensitiveConfigurationWireModule = new EnvironmentSensitiveConfigurationWireModule( buildVirtualEnvironment(overrides));
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, environmentSensitiveConfigurationWireModule).build();
			) {
			
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(terminal);
			Maybe<CompiledArtifact> compiledArtifactOptional = resolverContext.contract().transitiveResolverContract().dataResolverContract().directCompiledArtifactResolver().resolve( cai);
						
			CompiledTerminal cdi = compiledArtifactOptional.get();			
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
			return Maybe.complete( artifactResolution);													
		}		
		catch( Exception e) {
			return Maybe.empty( TemplateReasons.build(InternalError.T).assign( InternalError::setJavaException,e).toReason());
		}		

	}
		
	/**
	 * gets all parts of type ':jar' of an artifact
	 * @param artifact - the {@link AnalysisArtifact}
	 * @return - a {@link Stream} of {@link Part}
	 */
	public static Stream<Part> getCpJarParts(AnalysisArtifact artifact) {
		return artifact.getParts().entrySet().stream().filter(e -> e.getKey().endsWith(":jar")).map(Map.Entry::getValue);
	}
	/**
	 * gets all parts of type ':jar' of all artifacts passed
	 * @param artifacts - a {@link List} of {@link AnalysisArtifact}
	 * @return - a {@link Stream} of {@link Part}
	 */
	public static Stream<Part> getCpJarParts(List<AnalysisArtifact> artifacts) {
		return artifacts.stream().flatMap( a -> a.getParts().entrySet().stream()).filter(e -> e.getKey().endsWith(":jar")).map(Map.Entry::getValue);
	}
		
	/**
	 * dumps the resolution as a YAML file
	 * @param file - the {@link File} to write to
	 * @param resolution - the {@link AnalysisArtifactResolution} to write
	 */
	public static void dump(File file, AnalysisArtifactResolution resolution) {
		try (OutputStream out = new FileOutputStream(file)) {	
				marshaller.marshall(out, resolution);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "can't dump resolution to [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		}
		
	}
	
}
