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
package tribefire.extension.xmi.argo.exchange.processor;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.common.CallerEnvironment;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.generic.mdec.ModelDeclaration;
import com.braintribe.model.jvm.reflection.ModelDeclarationParser;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.zargo.MetaModelToZargoConverterWorker;
import com.braintribe.model.processing.zargo.wire.ZargoConverterModule;
import com.braintribe.model.processing.zargo.wire.contract.WorkerContract;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.classloader.ReverseOrderURLClassLoader;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.xmi.model.exchange.api.ArgoExchangeRequest;
import tribefire.extension.xmi.model.exchange.api.ExportModelToZargo;

public class ArgoExchangeProcessor extends AbstractDispatchingServiceProcessor<ArgoExchangeRequest, Object> {
	private VirtualEnvironment virtualEnvironment;
	
	@Required
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<ArgoExchangeRequest, Object> dispatching) {
		dispatching.register(ExportModelToZargo.T, this::exportModelToZargo);
	}
	
	private GmMetaModel exportModelToZargo(@SuppressWarnings("unused") ServiceRequestContext ctx, ExportModelToZargo request) {
		GmMetaModel terminalModel = buildModelFromRequest(request);
		
		String modelName = terminalModel.getName();
		String artifactId = modelName.substring(modelName.indexOf(':') + 1);
		
		FileResource zargo = request.getZargo();
		
		String zargoFilePath = zargo != null? zargo.getPath(): "./" + artifactId + ".zargo";
		
		File zargoFile = CallerEnvironment.resolveRelativePath(zargoFilePath);
		
		println("\nWriting zargo file " + zargoFile);
		
		// backup file.. input and output streams on the same in some situations doesn't seem to work,
		// so we create a backup and use this copy as source for the differentiation data & id recycling 
		File backedZargoFile = null;
		if  (zargoFile.exists()) {
			try {
				backedZargoFile = new File( zargoFile.getParentFile(), zargoFile.getName() + ".bak");
				if (backedZargoFile.exists()) {
					backedZargoFile.delete();
				}
				Files.copy( zargoFile.toPath(), backedZargoFile.toPath());
			} catch (IOException e) {
				throw Exceptions.uncheckedAndContextualize(e, "cannot prepare backup copy of [" + zargoFile.getAbsolutePath() + "]", IllegalStateException::new);		
			}
		}
		
		InputStream inputStream;
		try {
			inputStream = backedZargoFile != null ?  new FileInputStream( backedZargoFile) : null;
		} catch (Exception e) {
			throw Exceptions.uncheckedAndContextualize(e, "cannot open an input stream to zargofile [" + zargoFile.getAbsolutePath() + "]", IllegalStateException::new);			
		}
		
		try (				
				WireContext<WorkerContract> zargoWorkerContext = Wire.contextBuilder( ZargoConverterModule.INSTANCE).build();
				OutputStream outputStream = new BufferedOutputStream(new FileOutputStream( zargoFile));				
			) {						
				MetaModelToZargoConverterWorker worker = zargoWorkerContext.contract().metaModelToZargoWorker();
				worker.execute(terminalModel, outputStream, inputStream);				
		}
		catch( Exception e) {
			throw Exceptions.uncheckedAndContextualize(e, "cannot export model [" + modelName + "] to zargofile [" + zargoFile.getAbsolutePath() + "]", IllegalStateException::new);
				
		}
		finally {
			if (inputStream != null) {
				IOTools.closeQuietly(inputStream);
			}
		}
		
		return terminalModel;
	}

	private GmMetaModel buildModelFromRequest(ExportModelToZargo request) {
		String model = Objects.requireNonNull(request.getModel(), "ExportModelToZargo.model is a mandatory property");
		Pair<String, URL[]> cpInfo = determineModelClasspath(request, model);
		
	    GmMetaModel terminalModel = buildModelFromCp(cpInfo);
		return terminalModel;
	}

	private GmMetaModel buildModelFromCp(Pair<String, URL[]> cpInfo) {
		println("\nBuilding GmMetaModel");
		
		ClassLoader classLoader = new ReverseOrderURLClassLoader(cpInfo.second, getClass().getClassLoader());
	    
	    Map<String, ModelDeclaration> modelDeclarations = readModelDeclarations(classLoader);
	    
	    JavaTypeAnalysis jta = new JavaTypeAnalysis();
		jta.setClassLoader(classLoader);
		
		ModelIdentityManagement identityManagement = new ModelIdentityManagement();
		
		for (ModelDeclaration modelDeclaration: modelDeclarations.values()) {
			analyzeModel(jta, classLoader, modelDeclaration, identityManagement::lookupModel);
		}

		
		String terminalModelName = cpInfo.first;
		GmMetaModel terminalModel = identityManagement.lookupModel(terminalModelName);
		return terminalModel;
	}
	
	private class ModelIdentityManagement {
		Map<String, GmMetaModel> models = new HashMap<>();
		
		GmMetaModel lookupModel(String name) {
			return models.computeIfAbsent(name, n -> {
				GmMetaModel model = GmMetaModel.T.create();
				model.setGlobalId("model:" + n);
				model.setName(n);
				return model;
			});
		}
	}
	
	private GmMetaModel analyzeModel(JavaTypeAnalysis jta, ClassLoader classLoader, ModelDeclaration modelDeclaration, Function<String, GmMetaModel> modelLookup) {
		GmMetaModel model = modelLookup.apply(modelDeclaration.getName());

		model.setVersion(modelDeclaration.getVersion());

		List<GmMetaModel> dependencies = model.getDependencies();
		
		for (ModelDeclaration modelDeclarationDependency: modelDeclaration.getDependencies()) {
			GmMetaModel modelDependency = modelLookup.apply(modelDeclarationDependency.getName());
			dependencies.add(modelDependency);
		}

		Set<GmType> types = model.getTypes();

		try {
			for (String typeName : modelDeclaration.getTypes()) {
				Class<?> type = Class.forName(typeName, false, classLoader);
				
				GmType gmType = jta.getGmType(type);

				gmType.setDeclaringModel(model);

				types.add(gmType);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while analyzing classpath model " + modelDeclaration.getName());
			
		}

		return model;
	}

	
	private Map<String, ModelDeclaration> readModelDeclarations(ClassLoader classLoader) {
		try {
			Enumeration<URL> declarationUrls = classLoader.getResources("model-declaration.xml");
			
			Map<String, ModelDeclaration> declarations = new HashMap<>();
			
			while (declarationUrls.hasMoreElements()) {
				URL url = declarationUrls.nextElement();
				
				try (InputStream in = url.openStream()) {
					ModelDeclaration modelDeclaration = ModelDeclarationParser.parse(in);
					declarations.put(modelDeclaration.getName(), modelDeclaration);
				}
			}
			
			return declarations;
		}
		catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while reading model declaration", UncheckedIOException::new);
		}
		
	}


	private Pair<String, URL[]> determineModelClasspath(ExportModelToZargo request, String model) {
//		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
//		
//		cfg.setScopes( Scopes.runtimeScopes());
//		cfg.setSkipOptional(false);
//		cfg.setRelevantPartTuples(PartTupleProcessor.createJarPartTuple());
//		cfg.setResolvingInstant( ResolvingInstant.posthoc);
//		cfg.setVirtualEnvironment(virtualEnvironment);

		try (WireContext<ClasspathResolverContract> wireContext = Wire.context(ClasspathResolverWireModule.INSTANCE, new MavenConfigurationWireModule(virtualEnvironment)) ) {
//			String walkScopeId = UUID.randomUUID().toString();
//			WalkerContext walkerContext = new WalkerContext();
//			Walker enrichingWalker = wireContext.contract().enrichingWalker(walkerContext);
			
//			Dependency modelDependency = NameParser.parseCondensedDependencyNameAndAutoRangify(model, true);

			ClasspathResolverContract contract = wireContext.contract();
			DependencyResolver dependencyResolver = contract.transitiveResolverContract().dataResolverContract().dependencyResolver();

			CompiledDependencyIdentification modelDependency = CompiledDependencyIdentification.parseAndRangify(model, true);

		
			println(
					sequence(
							text("Resolving model dependencies for:  "),
							brightBlack(modelDependency.getGroupId() + ":"),
							text(modelDependency.getArtifactId()),
							brightBlack("#"),
							green(modelDependency.getVersion().asString())
							)
					);

			CompiledArtifactIdentification modelSolution = dependencyResolver.resolveDependency(modelDependency).get();

//			Solution modelSolution = wireContext.contract().dependencyResolver().resolveSingleTopDependency(walkScopeId, modelDependency);
			
			if (modelSolution == null)
				throw new IllegalStateException("Could not resolve model: " + modelDependency.asString());
			
//			Collection<Solution> solutions = enrichingWalker.walk(walkScopeId, modelSolution);
			
//			wireContext.contract().enricher().enrich(walkScopeId, Collections.singleton(modelSolution));
//			solutions.add(modelSolution);

			ClasspathResolutionContext cpContext = ClasspathResolutionContext.build().enrichJar(true).done();
			
			AnalysisArtifactResolution resolution = contract.classpathResolver().resolve(cpContext, modelDependency);
			if (resolution.hasFailed()) {
				println(
						sequence(
								text("\nResolution has failed :"),
								text( resolution.getFailure().asFormattedText()),
								text("\n")
						)
				);
				throw new IllegalStateException("Resolution of model has not succeeded: " + modelDependency.asString());
			}
			
			List<URL> cpUrls = new ArrayList<>();
			
			println(
					sequence(
							text("\nContributing models for GmMetaModel "),
							ArtifactOutputs.solution(modelSolution),
							text("\n")
					)
			);
			ModelAnalysis modelAnalysis = new ModelAnalysis();
			for (AnalysisArtifact solution: resolution.getSolutions()) {
				if (modelAnalysis.isDependencyOfRootModel(solution))
					continue;
				
				println(sequence(
						text("  "),
						ArtifactOutputs.solution(solution.getOrigin())
				));
				
				URL jarUrl = getJarFile(solution).toURI().toURL();
				cpUrls.add(jarUrl);
			}

			String modelName = modelSolution.getGroupId() + ":" + modelSolution.getArtifactId();
			return Pair.of(modelName, cpUrls.toArray(new URL[cpUrls.size()]));
			
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while resolving model dependency: " + request.getModel());
		}
	}
	
	private File getJarFile(AnalysisArtifact solution) {
		Part jarPart = solution.getParts().get(PartIdentifications.jar.asString());
		Resource r = jarPart.getResource();

		if (r instanceof FileResource)
			return new File(((FileResource) r).getPath());
		else
			throw new IllegalStateException(
					"Cannot resolve jar file for artifact '" + solution.asString() + "'. The jar resource is not a FileResource, but: " + r);
	}

	private static class ModelAnalysis {
		private final Map<AnalysisArtifact, Boolean> cache = new HashMap<>();
		
		private boolean isDependencyOfRootModel(AnalysisArtifact solution) {
			return cache.computeIfAbsent(solution, this::_isDependencyOfRootModel);
		}
		
		private boolean _isDependencyOfRootModel(AnalysisArtifact solution) {
			for (AnalysisDependency dependerDependency : solution.getDependers()) {
				AnalysisArtifact depender = dependerDependency.getDepender();
				
				if (depender == null)
					return false;
				
				if (depender.getArtifactId().equals("root-model"))
					return true;

				if (_isDependencyOfRootModel(depender))
					return true;
			}

			return false;
		}
	}

}
