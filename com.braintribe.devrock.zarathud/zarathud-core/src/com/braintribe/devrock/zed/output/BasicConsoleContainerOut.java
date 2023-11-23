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
package com.braintribe.devrock.zed.output;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.devrock.zarathud.model.reasons.UrlNotFound;
import com.braintribe.devrock.zed.analyze.dependency.StandardFilter;
import com.braintribe.devrock.zed.api.context.ConsoleOutputContext;
import com.braintribe.devrock.zed.api.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zed.api.forensics.DependencyForensics;
import com.braintribe.devrock.zed.api.output.ConsoleContainerOut;
import com.braintribe.devrock.zed.commons.Comparators;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.devrock.zed.forensics.fingerprint.filter.FingerPrintFilter;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.ArtifactForensicsResult;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.DependencyForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.ModelDeclarationForensicsResult;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;
import com.braintribe.zarathud.model.forensics.ModuleForensicsResult;
import com.braintribe.zarathud.model.forensics.data.ArtifactReference;
import com.braintribe.zarathud.model.forensics.data.ClasspathDuplicate;
import com.braintribe.zarathud.model.forensics.data.ImportedModule;
import com.braintribe.zarathud.model.forensics.data.ModelEntityReference;
import com.braintribe.zarathud.model.forensics.data.ModelEnumReference;
import com.braintribe.zarathud.model.forensics.data.ModelPropertyReference;
import com.braintribe.zarathud.model.forensics.data.ModuleReference;
import com.braintribe.zarathud.model.forensics.findings.ClasspathForensicIssueType;
import com.braintribe.zarathud.model.forensics.findings.DependencyForensicIssueTypes;
import com.braintribe.zarathud.model.forensics.findings.ModelDeclarationForensicIssueType;
import com.braintribe.zarathud.model.forensics.findings.ModelForensicIssueType;

/**
 * console output generator 
 * 
 * @author pit
 *
 */
public class BasicConsoleContainerOut extends BasicConsoleContainerOutputCommons implements ConsoleContainerOut {
	
	

	@Override
	public ConsoleOutputContainer processScanErrorReasons(ConsoleOutputContext context, List<UrlNotFound> reasons) {
		ConfigurableConsoleOutputContainer cc = context.consoleOutputContainer();
		cc.append( padL( context, "Scan issues found:"));
		context.pushIndent();
		
		for (UrlNotFound reason : reasons) {			
			cc.append(  ConsoleOutputs.styled( styleForForensicsRating( ForensicsRating.ERROR), padL( context, reason.getText())));
		}
		context.popIndent();
		return cc;
	}


	@Override
	public ConsoleOutputContainer processReasons(ConsoleOutputContext context, List<Reason> reasons) {
		ConfigurableConsoleOutputContainer cc = context.consoleOutputContainer();
		cc.append( padL( context, "Nonspecific issues found"));
		context.pushIndent();
		
		for (Reason reason : reasons) {			
			cc.append(  ConsoleOutputs.styled( styleForForensicsRating( ForensicsRating.ERROR), padL( context, reason.getText())));
		}
		context.popIndent();
		return cc;
	}


	@Override
	public ConsoleOutputContainer processTerminal(ConsoleOutputContext context, Artifact artifact) {	
		ConfigurableConsoleOutputContainer cc = context.consoleOutputContainer();
		// artifact name
		cc.append( padL(context, artifact.toVersionedStringRepresentation()));
		context.pushIndent();
		try {
			// gwt module
			Predicate<ZedEntity> filter = new StandardFilter(artifact);
			if (artifact.getGwtModule() != null) {
				cc.append( padL( context, "gwt module : " + artifact.getGwtModule()));
			}
						
		
			List<ZedEntity> entries = artifact.getEntries().stream().filter( filter).sorted(Comparators.entity()).collect( Collectors.toList());
			entries.stream().forEach( e -> {
				context.pushIndent();
				try {
					processZedEntity( context, e);
				}
				finally {
					context.popIndent();
				}
			});
			
		}
		finally {
			context.popIndent();
		}
		return cc;
	}

	
	@Override
	public ConsoleOutputContainer processDependencyForensics(ConsoleOutputContext context, DependencyForensicsResult dependencyForensicsResult) {	
		List<Artifact> declarations = dependencyForensicsResult.getDeclarations();
		List<Artifact> requiredDeclarations = dependencyForensicsResult.getRequiredDeclarations();
		
		List<Artifact> excessDeclarations = dependencyForensicsResult.getExcessDeclarations();
		List<Artifact> missingDeclarations = dependencyForensicsResult.getMissingDeclarations();
				
		if (declarations.size() > 0) {
			context.consoleOutputContainer().append( padL(context, "declared artifacts"));
			context.pushIndent();		
			processArtifacts(context, declarations, Severity.none, false);
			context.popIndent();
		}
		if (requiredDeclarations != null) {
			requiredDeclarations.remove( context.runtimeArtifact());
		}
		if (requiredDeclarations.size() > 0) {
			context.consoleOutputContainer().append( padL(context, "required artifacts"));
			context.pushIndent();
			processArtifacts(context, requiredDeclarations, Severity.none, false);
			context.popIndent();
		}
		
		if (excessDeclarations != null && excessDeclarations.size() > 0) {
			context.consoleOutputContainer().append( padL(context, "excess declarations"));
			context.pushIndent();
			processArtifacts(context, excessDeclarations, Severity.minor, false);
			context.popIndent();
		}
		
		if (missingDeclarations != null) {
			missingDeclarations.remove( context.runtimeArtifact());
		}
		if (missingDeclarations != null && missingDeclarations.size() > 0) {
			context.consoleOutputContainer().append( padL(  context, "missing declarations"));
			context.pushIndent();
			if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {				
				dependencyForensicsResult.getMissingArtifactDetails().stream().sorted( Comparators.artifactForensicsResult()).forEach( m -> {
					//context.pushIndent();					
					processArtifactForensicsResult(context, m);
					//context.popIndent();
				});
			}
			else if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.terse)) {
				processArtifacts(context, missingDeclarations, Severity.major, false);
			}
			context.popIndent();
		}		
		// forwards
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			Map<ArtifactReference, Artifact> forwardedReferences = dependencyForensicsResult.getForwardedReferences();
			if (forwardedReferences.size() > 0) {
				context.consoleOutputContainer().append( padL(  context, "detected forward declarations:"));
				context.pushIndent();
				processForwardReferences( context, forwardedReferences);
				context.popIndent();
			}
		}
		
		//
		List<FingerPrint> prints = context.fingerprints().getPrints();
		Collection<FingerPrint> printsOfCategory = context.ratings().getFingerPrintsOfCategory(prints, DependencyForensicIssueTypes.class);
		
		// 
		 Map<ForensicsRating, List<FingerPrint>> ratedFingerPrints = context.ratings().rateFingerPrints(printsOfCategory);
		
		ForensicsRating worstRating = context.ratings().getWorstRatingOfFingerPrints( ratedFingerPrints);				
			
		String fingerPrintString = dumpForensicRatingsAndCode( worstRating, ratedFingerPrints);
		context.consoleOutputContainer().append( padL(context, fingerPrintString));
		return context.consoleOutputContainer();
	}

	/**
	 * process the forward declarations as pre processed by the {@link DependencyForensics}
	 * @param context - the {@link ConsoleOutputContext}
	 * @param forwardedReferences - {@link Map} of {@link ArtifactReference} to {@link Artifact} redirection
	 */
	private void processForwardReferences(ConsoleOutputContext context, Map<ArtifactReference, Artifact> forwardedReferences) {
		// sort be artifact
		List<ArtifactReference> keys = forwardedReferences.keySet().stream().sorted( Comparators.artifactReference()).collect( Collectors.toList());
	
		Map<String, Integer> numReferences  = new HashMap<>();
		List<String> lines = new ArrayList<>();
		for (ArtifactReference r : keys) {
			String line = r.getSource().getName() + " -> " + r.getTarget().getName() + ": redirected from " +  dumpArtifactReferences(context, r.getTarget()) + " to " + dumpArtifactReference(context, forwardedReferences.get( r));
			if (lines.contains(line)) {
				numReferences.put( line, numReferences.get(line)+1);
			}
			else {
				lines.add(line);
				numReferences.put(line, 1);
			}
		}
		context.consoleOutputContainer().append( padL(  context, "(" + forwardedReferences.size() + ") references to forward declared entities"));
			
		context.pushIndent();
		lines.stream().sorted().forEach( s -> context.consoleOutputContainer().append( padL( context, s + "(" + numReferences.get(s) + ")")));				
		context.popIndent();
			
		
		
	}


	@Override
	public ConsoleOutputContainer processArtifactForensicsResult(ConsoleOutputContext context, ArtifactForensicsResult artifactForensicsResult) {
		// artifact name
		// number of references by terminal		
		List<ArtifactReference> references = artifactForensicsResult.getReferences();
		
		int size = references.size();
		if (size == 0 && verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) { 
			List<ClasspathDuplicate> duplicates = artifactForensicsResult.getDuplicates();
			if (duplicates.size() == 0) {				
				return context.consoleOutputContainer();
			}
			// 
			context.consoleOutputContainer().append( padL( context, dumpArtifactReference(context, artifactForensicsResult.getArtifact()) + " (" + duplicates.size() + ") shadowing definitions"));
			context.pushIndent();
			duplicates.stream().sorted( Comparators.classpathDuplicate()).forEach( d -> {
				context.consoleOutputContainer().append( padL( context, d.getType().getName() + dumpArtifactReferences(context, d.getType())));
			});
			context.popIndent();
			return context.consoleOutputContainer();
		}
	
		context.consoleOutputContainer().append( padL( context, dumpArtifactReference(context, artifactForensicsResult.getArtifact()) + " (" + size + ") references"));
				
		// map of references 
		if (verbosityLevelAtLeast(context, ConsoleOutputVerbosity.verbose)) {
			
			Map<String, Integer> numReferences  = new HashMap<>();
			List<String> lines = new ArrayList<>();
			for (ArtifactReference r : references) {
				String line = r.getSource().getName() + " -> " + r.getTarget().getName();
				if (lines.contains(line)) {
					numReferences.put( line, numReferences.get(line)+1);
				}
				else {
					lines.add(line);
					numReferences.put(line, 1);
				}
			}
			
			//Set<String> strings = references.stream().map( r -> r.getSource().getName() + " -> " + r.getTarget().getName()).collect(Collectors.toSet());			
			
			context.pushIndent();
			lines.stream().sorted().forEach( s -> context.consoleOutputContainer().append( padL( context, s + "(" + numReferences.get(s) + ")")));				
			context.popIndent();
		}
		return context.consoleOutputContainer();
	}


	@Override
	public ConsoleOutputContainer processClasspathForensicsResult(ConsoleOutputContext context, ClasspathForensicsResult classpathForensicsResult) {
		context.pushIndent();

		List<ClasspathDuplicate> duplicates = classpathForensicsResult.getDuplicates();		
		int size = duplicates.size();
		if (duplicates != null && size > 0) {
			//
			context.consoleOutputContainer().append( padL(context, "(" + size + ") duplicate types in classpath"));
			context.pushIndent();
			duplicates.stream().sorted( Comparators.classpathDuplicate()).forEach( d -> {
				context.consoleOutputContainer().append( padL( context, d.getType().getName() + dumpArtifactReferences(context, d.getType())));
			});
			context.popIndent();
		}	
		List<FingerPrint> prints = context.fingerprints().getPrints();
		
		Collection<FingerPrint> issuesTypesOfCategory = context.ratings().getFingerPrintsOfCategory(prints, ClasspathForensicIssueType.class);
		ForensicsRating worstRating = context.ratings().getWorstRatingOfFingerPrints(prints);
		processForensicRatingsAndCode( context, worstRating, issuesTypesOfCategory);
		context.popIndent();
		
		return context.consoleOutputContainer();
	}


	@Override
	public ConsoleOutputContainer processModelForensicsResult(ConsoleOutputContext context, ModelForensicsResult modelForensicsResult) {
		List<FingerPrint> prints = context.fingerprints().getPrints();
		Collection<FingerPrint> issuesTypesOfCategory = context.ratings().getFingerPrintsOfCategory(prints, ModelForensicIssueType.class);
		ForensicsRating worstRating = context.ratings().getWorstRatingOfFingerPrints(prints);			
		List<ModelEntityReference> modelEntityReferences = modelForensicsResult.getModelEntityReferences();
		context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating( worstRating), padL(context, "(" + modelEntityReferences.size() + ") generic entities in artifact")));
		context.pushIndent();
		modelEntityReferences.stream().forEach( mer -> {			
			processEntityReference(context, mer);
		});
		context.popIndent();
		List<ModelEnumReference> enums = modelForensicsResult.getModelEnumEntities();
		if (enums.size() > 0) {
			context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating( worstRating), padL(context, "(" + enums.size() + ") enums in artifact")));
			context.pushIndent();
			enums.stream().forEach( ee -> {			
				processEnum(context, ee.getEnumEntity());
			});
			context.popIndent();
		}
		
		processForensicRatingsAndCode( context, worstRating, issuesTypesOfCategory);
		return context.consoleOutputContainer();
	}

		

	@Override
	public ConsoleOutputContainer processModuleForensicsResult(ConsoleOutputContext context, ModuleForensicsResult moduleForensicsResult) {
		// the collected information about the import-part of the module of the terminal
		context.consoleOutputContainer().append( padL( context, "Packages imported by the terminal itself"));
		context.pushIndent();
		List<ModuleReference> moduleImports = moduleForensicsResult.getModuleImports();
		for (ModuleReference reference : moduleImports) {
			if (reference.getRequiredPackages().size() > 0) {
				processModuleReference( context, reference);
			}
		}
		context.popIndent();
		//
		// the collected information about the export-part of the module of the dependencies
		context.consoleOutputContainer().append( padL( context, "Packages required to be exported by terminal's dependencies"));
		context.pushIndent();
		List<ImportedModule> requiredModuleExports = moduleForensicsResult.getRequiredImportModules();
		for (ImportedModule importedModule : requiredModuleExports) {
			if (importedModule.getRequiredExports().size() > 0) {
				processImportedModule( context, importedModule);
			}
		}
		context.popIndent();
		return context.consoleOutputContainer();
	}
	
	/**
	 * processes the imports of a dependency of the terminal 
	 * @param context - the {@link ConsoleOutputContext}
	 * @param moduleReference - the {@link ModuleReference}
	 */
	private void processModuleReference(ConsoleOutputContext context, ModuleReference moduleReference) {
		context.consoleOutputContainer().append( padL( context, "Packages imported from " + moduleReference.getArtifactName()));
		context.pushIndent();
		List<String> requiredPackages = moduleReference.getRequiredPackages();
		for (String requiredPackage : requiredPackages) {
			context.consoleOutputContainer().append( padL( context, requiredPackage));
		}
		context.popIndent();
	}

	/**
	 * processes the required exports of a dependency of the terminal
	 * @param context - the {@link ConsoleOutputContext}
	 * @param importedModule - the {@link ImportedModule}
	 */
	private void processImportedModule(ConsoleOutputContext context, ImportedModule importedModule) {
		context.consoleOutputContainer().append( padL( context, "Packages required to be exported from " + importedModule.getArtifactName()));
		context.pushIndent();
		List<String> requiredPackages = importedModule.getRequiredExports();
		for (String requiredPackage : requiredPackages) {
			context.consoleOutputContainer().append( padL( context, requiredPackage));
		}
		context.popIndent();
	}
	
	

	@Override
	public ConsoleOutputContainer processModelDeclarationResult(ConsoleOutputContext context, ModelDeclarationForensicsResult modelDeclarationResult) {
		List<FingerPrint> prints = context.fingerprints().getPrints();
		Collection<FingerPrint> issuesTypesOfCategory = context.ratings().getFingerPrintsOfCategory(prints, ModelDeclarationForensicIssueType.class);
		ForensicsRating worstRating = context.ratings().getWorstRatingOfFingerPrints(prints);
	
				
		//context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(forensicsRating), padL(context, "model declaration")));
		
		context.pushIndent();
		// existence
		if (modelDeclarationResult.getModelDeclarationContents() == null) {
			context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, "no model-declaration.xml detected")));
		}
		
		// format
		if (context.fingerprints().hasPrintOfIssue( ModelDeclarationForensicIssueType.DeclarationFileInvalid)) {
			context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, "model-declaration.xml is of invalid format")));
		}
		
		// validation
				
		// dependencies
		List<String> missingDependencyDeclarations = modelDeclarationResult.getMissingDependencyDeclarations();
		List<String> excessDependencyDeclarations = modelDeclarationResult.getExcessDependencyDeclarations();
		if (missingDependencyDeclarations.size() > 0 || excessDependencyDeclarations.size() > 0) {
			if (missingDependencyDeclarations.size() > 0) {
				context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, "missing dependency declaration")));
				context.pushIndent();
				String missing = missingDependencyDeclarations.stream().collect(Collectors.joining(","));
				context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, missing)));
				context.popIndent();
			}
			if (excessDependencyDeclarations.size() > 0) {
				context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, "excess dependency declarations")));
				context.pushIndent();
				String excess = excessDependencyDeclarations.stream().collect(Collectors.joining(","));
				context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, excess)));
				context.popIndent();
			}
		}
		// types
		List<String> missingTypeDeclarations = modelDeclarationResult.getMissingTypeDeclarations();
		List<String> excessTypeDeclarations = modelDeclarationResult.getExcessTypeDeclarations();
		if (missingTypeDeclarations.size() > 0 || excessTypeDeclarations.size() > 0) {
			if (missingTypeDeclarations.size() > 0) {
				context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, "missing type declarations")));
				context.pushIndent();
				String missing = missingTypeDeclarations.stream().collect(Collectors.joining(","));
				context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, missing)));
				context.popIndent();
			}
			if (excessTypeDeclarations.size() > 0) {
				context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, "excess type declarations")));
				context.pushIndent();
				String excess = excessTypeDeclarations.stream().collect(Collectors.joining(","));
				context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, excess)));
				context.popIndent();
			}
		}
		
		context.popIndent();
	
		processForensicRatingsAndCode( context, worstRating, issuesTypesOfCategory);
		return context.consoleOutputContainer();		
	}

	private void processEnum(ConsoleOutputContext context, EnumEntity ee) {
		FingerPrint fp = FingerPrintExpert.build( ee);
		List<FingerPrint> filtered = context.fingerprints().filter( new FingerPrintFilter(fp));		
		
		Collection<FingerPrint> fingerprints = context.ratings().getFingerPrintsOfCategory(filtered, ModelForensicIssueType.class);
		ForensicsRating worstRating = context.ratings().getWorstRatingOfFingerPrints(fingerprints);
				
		context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, ee.getName())));					
		context.pushIndent();
		
		// what to show? 
		
		context.popIndent();
		processForensicRatingsAndCode( context,  worstRating, fingerprints);
	}

	/**
	 * model forensics output : processing {@link ModelEntityReference}
	 * @param context - the {@link ConsoleOutputContext}
	 * @param mer - the {@link ModelEntityReference} to process 
	 */
	private void processEntityReference(ConsoleOutputContext context, ModelEntityReference mer) {
		
		FingerPrint fp = FingerPrintExpert.build( mer.getType());
		List<FingerPrint> filtered = context.fingerprints().filter( new FingerPrintFilter(fp));		
		
		// TODO : MER -> fingerprint to get only FPs attached to the MER
		Collection<FingerPrint> fingerprints = context.ratings().getFingerPrintsOfCategory(filtered, ModelForensicIssueType.class);
		ForensicsRating worstRating = context.ratings().getWorstRatingOfFingerPrints(fingerprints);
	
		
		context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, mer.getType().getName())));					
		context.pushIndent();
		// non conform
		int numNonConform = mer.getNonConformOtherMethods().size();
		if (numNonConform > 0) {			
			context.pushIndent();
			processMethods(context, mer.getNonConformOtherMethods(), "non conform methods");
			context.popIndent();
		}
		// conform
		int numConform = mer.getConformOtherMethods().size();
		if (numConform > 0) {
			
			context.pushIndent();
			processMethods(context, mer.getConformOtherMethods(), "conform methods");
			context.popIndent();
		}
		// properties
		int numProperties = mer.getPropertyReferences().size();
		if (numProperties > 0) {
			context.pushIndent();
			processProperties(context, mer.getPropertyReferences());
			context.popIndent();
		}
		
		// TODO: process fingerprints of unexpected fields 
		List<FingerPrint> printsOfUnexpectedFields = fingerprints.stream()
														.filter( f -> f.getSlots().containsValue( ModelForensicIssueType.UnexpectedField.name()))														
														.collect(Collectors.toList());
		if (printsOfUnexpectedFields.size() > 0) {
			context.pushIndent();
			processUnexpectedFields(context, printsOfUnexpectedFields);
			context.popIndent();
		}
		
		context.popIndent();
		processForensicRatingsAndCode( context,  worstRating, fingerprints);
	}


	private void processUnexpectedFields(ConsoleOutputContext context, List<FingerPrint> printsOfUnexpectedFields) {
		context.consoleOutputContainer().append( padL(context, "(" + printsOfUnexpectedFields.size() + ") unexpected fields"));
		context.pushIndent();
		for (FingerPrint fp : printsOfUnexpectedFields) {
			Map<String,String> slots = fp.getSlots();
			String fieldName = slots.get( FIELD);						
			context.consoleOutputContainer().append( padL(context, "field : " + fieldName));
		}
		context.popIndent();
	}


	/**
	 * model forensics processing : processing list of properties 
	 * @param context - the {@link ConsoleOutputContext}
	 * @param propertyReferences - a {@link List} of {@link ModelPropertyReference}
	 */
	private void processProperties(ConsoleOutputContext context, List<ModelPropertyReference> propertyReferences) {
		context.consoleOutputContainer().append( padL(context, "(" + propertyReferences.size() + ") properties"));
		context.pushIndent();
		propertyReferences.stream().forEach( pr -> {
			processProperty( context, pr);
		});
		context.popIndent();
	}


	/**
	 * model forensics processing : processing a property 
	 * @param context - the {@link ConsoleOutputContext}
	 * @param pr - the {@link ModelPropertyReference} to process 
	 */
	private void processProperty(ConsoleOutputContext context, ModelPropertyReference pr) {
		FingerPrint fp = FingerPrintExpert.build( pr);
		List<FingerPrint> filtered = context.fingerprints().filter( new FingerPrintFilter(fp));
		Collection<FingerPrint> fingerPrints = context.ratings().getFingerPrintsOfCategory(filtered, ModelForensicIssueType.class);
		ForensicsRating worstRating = context.ratings().getWorstRatingOfFingerPrints(fingerPrints);
		
		ZedEntity type = pr.getType();
		if (type != null) {
			context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, pr.getName() + ":" + type.getName() + dumpArtifactReferences(context, pr.getType()))));
		}
		else {
			context.consoleOutputContainer().append( ConsoleOutputs.styled( styleForForensicsRating(worstRating), padL(context, pr.getName() + ":" + "UNDEFINED")));
		}
		context.pushIndent();
		MethodEntity getter = pr.getGetter();
		if (getter != null) {
			context.consoleOutputContainer().append( padL(context, "getter : " + dumpMethod(context, getter)));
		}
		MethodEntity setter = pr.getSetter();
		if (setter != null) {
			context.consoleOutputContainer().append( padL(context, "setter : " + dumpMethod(context, setter)));
		}
		ModelEntityReference owner = pr.getOwner();
		InterfaceEntity ie = (InterfaceEntity) owner.getType();
		FieldEntity tagField = findTagField( ie, pr.getName());
		if (tagField == null) {
			context.consoleOutputContainer().append( padL(context, "no tag"));
		}
		else {
			context.consoleOutputContainer().append( padL(context, "tag : \"" + tagField.getInitializer() + "\""));
		}
		context.popIndent();
		processForensicRatingsAndCode( context,  worstRating, fingerPrints);
	}


	/**
	 * model forensics processing : find matching tag property 
	 * @param ie - the {@link InterfaceEntity} (aka generic entity)
	 * @param name - the name of the field to search for
	 * @return - the {@link FieldEntity} if found, null otherwise 
	 */
	private FieldEntity findTagField(InterfaceEntity ie, String name) {		
		for (FieldEntity fe : ie.getFields()) {
			if (fe.getName().equalsIgnoreCase( name))
				return fe;
		}
		return null;
	}
	
	

	
	
}
