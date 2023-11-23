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
package com.braintribe.devrock.zed.forensics;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.core.CachingZedRegistry;
import com.braintribe.devrock.zed.api.forensics.ModelForensics;
import com.braintribe.devrock.zed.commons.Comparators;
import com.braintribe.devrock.zed.commons.ZedTokens;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ParameterEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasAnnotationsNature;
import com.braintribe.zarathud.model.data.natures.HasFieldsNature;
import com.braintribe.zarathud.model.data.natures.HasGenericNature;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ModelDeclarationForensicsResult;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;
import com.braintribe.zarathud.model.forensics.data.ModelEntityReference;
import com.braintribe.zarathud.model.forensics.data.ModelEnumReference;
import com.braintribe.zarathud.model.forensics.data.ModelPropertyReference;
import com.braintribe.zarathud.model.forensics.findings.ModelDeclarationForensicIssueType;
import com.braintribe.zarathud.model.forensics.findings.ModelForensicIssueType;

/**
 * model forensics
 * <ul>forensics 
 * 	<li>model declaration file 
 * 		<ul>
 * 			<li>missing -> MAJOR_ISSUES</li>
 *  		<li>wrong -> MAJOR_ISSUES</li>
 * 		</ul>
 * 	<li>per property:
 * 		<ul>
 * 			<li>getter/setter naming</li>
 *  		<li>invalid types without @Transient</li>
 * 		</ul>
 * 	</li>
 * <ul>
 * @author pit
 *
 */
public class BasicModelForensics extends ZedForensicsCommons implements ModelForensics, ZedTokens {
	private static final String MODEL_DECLARATION_XML = "model-declaration.xml";
	private static final String COM_BRAINTRIBE_MODEL_GENERIC_EVAL_EVAL_CONTEXT = "com.braintribe.model.generic.eval.EvalContext";
	private static final String COM_BRAINTRIBE_MODEL_GENERIC_EVAL_EVALUATOR = "com.braintribe.model.generic.eval.Evaluator";
	private static final String ENUM_BASE = "com.braintribe.model.generic.base.EnumBase"; 
	private static final String ENUM_TYPE = "com.braintribe.model.generic.reflection.EnumType";
	private static Logger log = Logger.getLogger(BasicModelForensics.class);
	private static final String rootGroup ="com.braintribe.gm";
	private static final String rootArtifact = "root-model";
	private static final String reflectingEntityType = "com.braintribe.model.generic.reflection.EntityType";
	private static final String t_type_function = "T";
	
	private ModelForensicsResult modelForensicsResult;
	
	public BasicModelForensics(ZedForensicsContext context) {
		super(context);
		modelForensicsResult = ModelForensicsResult.T.create();
	}

	@Override
	public ModelForensicsResult runForensics() {
		modelForensicsResult.setArtifact( shallowArtifactCopy(context.terminalArtifact()));
		
		//
		// is it a model itself?
		//
		
		Set<ModelEntityReference> modelEntities = context.terminalArtifact().getEntries()
																				.stream()
																				.filter( z -> Comparators.contains(z.getArtifacts(), context.terminalArtifact()))
																				.map( z -> processModelEntity( context, z))
																				.filter( m -> m != null)
																				.collect(Collectors.toSet());
		
		Set<ModelEnumReference> enums = context.terminalArtifact().getEntries()
																		.stream()
																		.filter( z -> Comparators.contains(z.getArtifacts(), context.terminalArtifact()))
																		.filter( z -> z instanceof EnumEntity)
																		.map( z -> processEnumEntity(context, z))
																		.filter( e -> e != null)
																		.collect(Collectors.toSet());

		
		if (modelEntities.size() > 0) {
			modelForensicsResult.getModelEntityReferences().addAll(modelEntities);			
		}
		
		// currently: a model is an artifact that contains generic entities - hence a model with only Enum (EnumEntities) would not be regarded as one
		if (enums.size() > 0 && modelEntities.size() > 0) {
			modelForensicsResult.getModelEnumEntities().addAll(enums);
			runEnumForensics( context, enums, modelForensicsResult);
		}		
		
		extractDeclarationFileForensics( context, findModelDeclarationFile(), modelForensicsResult);
	
		return modelForensicsResult;
	}

	/**
	 * validate the enums 
	 * @param context - {@link ZedForensicsContext}
	 * @param enums - the {@link EnumEntity} within the model 
	 * @param modelForensicsResult - the {@link ModelForensicsResult} 
	 */
	private void runEnumForensics(ZedForensicsContext context, Set<ModelEnumReference> enums, ModelForensicsResult modelForensicsResult) {
		
		for (ModelEnumReference mer : enums) {
			EnumEntity een = mer.getEnumEntity();
			
			// check if it really derives from the enum base
			Set<TypeReferenceEntity> implementedInterfaces = een.getImplementedInterfaces();			
			boolean enumbaseDerivation = false;
			if (implementedInterfaces != null && implementedInterfaces.size() > 0) {
				for (TypeReferenceEntity tfe : implementedInterfaces) {
					enumbaseDerivation = tfe.getReferencedType().getName().equals( ENUM_BASE);
					if (enumbaseDerivation)
						break;
				}				
			}
			if (!enumbaseDerivation) {
				// finger print missing implementation
				FingerPrint fp = FingerPrintExpert.build( mer, ModelForensicIssueType.EnumTypeNoEnumbaseDerivation.name());
				modelForensicsResult.getFingerPrintsOfIssues().add( fp);
			}
			
			// find type() method declaration 
			Set<MethodEntity> methods = een.getMethods();
			boolean typeMethodFound = false;			
			for (MethodEntity me : methods ) {
				String name = me.getName();			
				if (name.equals( "type")) {														
					String returnTypeName = me.getReturnType().getReferencedType().getName();
					typeMethodFound = returnTypeName.equals(ENUM_TYPE);
					if (typeMethodFound)
						break;
				}
			}
 
			if  (!typeMethodFound) {
				// fingerprint missing type functions 
				FingerPrint fp = FingerPrintExpert.build( mer, ModelForensicIssueType.EnumTypeNoTypeFunction.name());
				modelForensicsResult.getFingerPrintsOfIssues().add( fp);
			}
			
			// find T field			
			List<FieldEntity> fields = een.getFields();
			boolean tFieldFound = false;
			for (FieldEntity fe : fields) {
				String name = fe.getName();
				if (name.equals(t_type_function)) {
					String fieldType = fe.getType().getReferencedType().getName();
					tFieldFound = fieldType.equals( ENUM_TYPE); 
					mer.setEnumTypesDeclaration(fe);
				}
			}
			if (!tFieldFound) {
				// fingerprint missing/incorrect T field
				FingerPrint fp = FingerPrintExpert.build( mer, ModelForensicIssueType.EnumTypeNoTField.name());
				modelForensicsResult.getFingerPrintsOfIssues().add( fp);
			}			
		}
	}

	/**
	 * find the correct model declaration file in the classloader's URL
	 * @return - the correct {@link URL} matched to the terminal artifact's model-declaration.xml
	 */
	private URL findModelDeclarationFile() {		
		Enumeration<URL> resources;
		try {
			resources = context.classloader().getResources(MODEL_DECLARATION_XML);
			String key = context.terminalArtifact().toJarRepresentation();
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				String externalRepresentation = url.toExternalForm();
				if (externalRepresentation.contains(key)) {
					return url;			
				}
			}			
		} catch (IOException e) {
			// equals not found, so what
			;
		}
		return null;
	}
	
	
	/**
	 * extract declaration file forensic result (model-declaration.xml) 
	 * @param context - the {@link ZedAnalyzerContext}
	 * @param resource - {@link URL} of the model declaration file with the jar
	 * @param result - the {@link ModelForensicsResult} to extract the data from 
	 * @return
	 */
	private ModelDeclarationForensicsResult extractDeclarationFileForensics(ZedForensicsContext context, URL resource, ModelForensicsResult result) {
		
		ModelDeclarationForensicsResult declarationResult = ModelDeclarationForensicsResult.T.create();
				
		result.setDeclarationResult(declarationResult);
		
		// no resource found at all 
		if (resource == null) {
			FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ModelDeclarationForensicIssueType.MissingDeclarationFile.toString());
			declarationResult.getFingerPrintsOfIssues().add( fp);
			return declarationResult;
		}
		try ( 
				InputStream in = resource.openStream();
				DigestInputStream dig = new DigestInputStream(in, MessageDigest.getInstance("MD5"));
			) {
			Document doc = DomParser.load().from( dig);
			Element documentElement = doc.getDocumentElement();
		
			
			// b) dependencies
			List<String> dependencies = getTagValues(documentElement, "dependencies", "dependency");
			if (dependencies == null || dependencies.size() == 0) {
				// terminal would need to be root.. 
				if (
						!rootGroup.equalsIgnoreCase(result.getArtifact().getGroupId()) ||
						!rootArtifact.equalsIgnoreCase(result.getArtifact().getArtifactId())
					) {
					FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ModelDeclarationForensicIssueType.MissingDeclaredDependencyDeclarations.name());
					declarationResult.getFingerPrintsOfIssues().add( fp);
				}
			}
			else {
				List<String> present = new ArrayList<>();
				List<String> missing = new ArrayList<>();
				
				// check dependencies ... fit, missing, excess
				List<Artifact> actualDependencies = context.terminalArtifact().getActualDependencies();
				for (Artifact dependency : actualDependencies) {
					if (dependency.getGroupId() == null && dependency.getArtifactId().equalsIgnoreCase( "unknown")) {
						continue;
					}
					// filter runtime ..  
					if (context.artifacts().runtimeArtifact(context).compareTo(dependency) == 0) {
						continue;
					}
					// filter out valid implict references (gm-core-api for instance)
					if (context.validImplicitArtifactReferenceFilter().test( dependency))
						continue;
					String key = dependency.getGroupId() + ":" + dependency.getArtifactId();
					if (dependencies.contains( key)) {
						present.add( key);
					}
					else {
						missing.add( key);
					}
				}
				List<String> excess = new ArrayList<>( dependencies);
				excess.removeAll(present);
				
				if (missing.size() > 0) {			
					FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ModelDeclarationForensicIssueType.MissingDeclaredDependencyDeclarations.name());
					declarationResult.getFingerPrintsOfIssues().add( fp);					
					declarationResult.setMissingDependencyDeclarations(missing);
				}
				
				if (excess.size() > 0) {
					FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ModelDeclarationForensicIssueType.ExcessDeclaredDependencyDeclarations.name());
					declarationResult.getFingerPrintsOfIssues().add( fp);										
					declarationResult.setExcessDependencyDeclarations(excess);
				}								
				
				
				
			}
			
			// c) types 
			List<String> types = getTagValues( documentElement, "types", "type");
			if (types == null || types.size() == 0) {
				FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ModelDeclarationForensicIssueType.MissingTypeDeclarations.toString());
				declarationResult.getFingerPrintsOfIssues().add( fp);														
			}
			else {				
				List<String> present = new ArrayList<>();
				List<String> missing = new ArrayList<>();
				Map<String, ZedEntity> nameToZedMap = new HashMap<>();
			
				// extract all declared types
				result.getModelEntityReferences().stream().forEach( mer -> {
					ZedEntity zedEntity = mer.getType();
					String name = zedEntity.getName();
					nameToZedMap.put(name, zedEntity);
					// check if listed
					if (types.contains( name)) {
						if (zedEntity.getDefinedInTerminal()) {
							present.add(name);
						}
						else {
							missing.add(name);
							
						}
					}
				});
				
				// filter enums 
				context.terminalArtifact().getEntries().stream().forEach( z -> {
					if (z instanceof EnumEntity && Comparators.contains(z.getArtifacts(), result.getArtifact())){
						String name = z.getName();
						if (types.contains( name)) {
							present.add( z.getName());
						}
						else {
							missing.add( name);
						}
					}
				});
				List<String> excess = new ArrayList<>( types);
				excess.removeAll( present);
								
				
				if (missing.size() > 0) {
					FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ModelDeclarationForensicIssueType.MissingTypeDeclarations.toString());
					declarationResult.getFingerPrintsOfIssues().add( fp);																		
					declarationResult.setMissingTypeDeclarations(missing);
				}
				
				if (excess.size() > 0) {
					FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ModelDeclarationForensicIssueType.ExcessTypeDeclarations.toString());
					declarationResult.getFingerPrintsOfIssues().add( fp);				
					declarationResult.setExcessTypeDeclarations(excess);
				}								
			}

			// d) add contents of file 
			declarationResult.setModelDeclarationContents( DomParser.write().from(doc).to());
			
		} catch (Exception e) {
			log.error("cannot access model-declaration file via ["+ resource.toExternalForm() + "]", e);
			FingerPrint fp = FingerPrintExpert.build(context.terminalArtifact(), ModelDeclarationForensicIssueType.DeclarationFileInvalid.toString());
			declarationResult.getFingerPrintsOfIssues().add( fp);								
		}
		
	
		return declarationResult;
	}
	
	/**
	 * get the text content of the children of the passed parent
	 * @param parent - the parent {@link Element} to navigate from
	 * @param path - the path to follow
	 * @param tag - the tag of the children 
	 * @return - a {@link List} of the collected text content 
	 */
	private List<String> getTagValues( Element parent, String path, String tag) {
		Element dependencies = DomUtils.getElementByPath(parent, path, false);
		if (dependencies == null) {
			return null;
		}
		List<String> values = new ArrayList<>();
		Iterator<Element> iterator = DomUtils.getElementIterator(dependencies, tag);
		while (iterator.hasNext()) {
				Element element = iterator.next();
				values.add( element.getTextContent());			
		}
		return values;		
	}



	private enum MethodKind { GETTER, SETTER, CONFORM, NON_CONFORM};
	
	/**
	 * classify a method as either conform or non-conform
	 * @param m - {@link MethodEntity} to test
	 * @return - the determined {@link MethodKind}
	 */
	private MethodKind classifyNonAccessMethod( MethodEntity m) {
		ZedEntity zed = (ZedEntity) m.getOwner();
		
		HasAnnotationsNature annotNature = (HasAnnotationsNature) zed;
		if (Comparators.contains( annotNature.getAnnotations(), GMSYSTEMINTERFACE_ANNOTATION_SIGNATURE, ABSTRACT_ANNOTATION_SIGNATURE)) {
			return MethodKind.CONFORM;	
		}
		
		// statics are ok 
		if (m.getStaticNature()) {
			return MethodKind.CONFORM;
		}
		// transients?? here?? 
		if (Comparators.contains( m.getAnnotations(), TRANSIENT_ANNOTATION_SIGNATURE)) 
			return MethodKind.CONFORM;
		
		if (m.getIsDefault()) {
			return MethodKind.CONFORM;
		}
		
		// check eval signature.. 
		if (m.getName().equalsIgnoreCase("eval")) {
			ZedEntity argumentType=null, returnType=null, returnTypeParameterType = null;
			if (m.getArgumentTypes().size() == 1) {
				TypeReferenceEntity tf = m.getArgumentTypes().get(0);
				argumentType = tf.getReferencedType();
			}
			TypeReferenceEntity tf = m.getReturnType();
			returnType = tf.getReferencedType();

			TypeReferenceEntity returnTypeReference = null;
			if (tf.getParameterization().size() == 1) {
				returnTypeReference = tf.getParameterization().get(0);
				returnTypeParameterType = returnTypeReference.getReferencedType();
			}			
			if (argumentType != null && returnType != null && returnTypeParameterType != null) {
				if (
						argumentType.getName().equalsIgnoreCase( COM_BRAINTRIBE_MODEL_GENERIC_EVAL_EVALUATOR) && 
						returnType.getName().equalsIgnoreCase( COM_BRAINTRIBE_MODEL_GENERIC_EVAL_EVAL_CONTEXT)
					) {
					// might be a collection type 
					List<ZedEntity> typesToCheck = extractRelevantParameterType(returnTypeReference); 
					for (ZedEntity typeToCheck : typesToCheck) {
						if (typeToCheck instanceof HasGenericNature == false) {
							
							if (
									!typeToCheck.getName().equalsIgnoreCase("java.lang.Object") &&
									typeToCheck instanceof EnumEntity == false && 
									!CachingZedRegistry.simpleTypes.contains(typeToCheck.getName())) {						
								return MethodKind.NON_CONFORM;
							}
							else {
								return MethodKind.CONFORM;
							}
						}
						HasGenericNature geNature =(HasGenericNature) typeToCheck; 					
						if (geNature.getGenericNature()) {					
							return MethodKind.CONFORM;
						}													
					}
					return MethodKind.NON_CONFORM;
				}
			}
									
			log.warn("couldn't determine whether eval function found (" + m.getSignature() + "] in [" + zed.getName() + "] is a valid eval function");
			
		}
		
		// nada nope 
		return MethodKind.NON_CONFORM;
				
	}
		
	
	
	/**
	 * if a collection, it extracts the element type(s), otherwise the type's returned
	 * @param ref
	 * @return
	 */
	private List<ZedEntity> extractRelevantParameterType(TypeReferenceEntity ref) {
		ZedEntity type = ref.getReferencedType();
		String typeName = type.getName();
		if (CachingZedRegistry.linearCollectionTypes.contains( typeName)) {
			return Collections.singletonList(ref.getParameterization().get(0).getReferencedType());
		}
		else if (CachingZedRegistry.nonlinearCollectionTypes.contains( typeName)) {		
			List<ZedEntity> result = new ArrayList<>();
			result.add( ref.getParameterization().get(0).getReferencedType());
			result.add( ref.getParameterization().get(1).getReferencedType());
			return result;
		}
		return Collections.singletonList(type);
	}

	/**
	 * determine the method kind, i.e. either getter/setter/conform/non-conform
	 * @param m - the {@link MethodEntity}
	 * @return - the {@link MethodKind} determined
	 */
	private MethodKind determineMethodType( MethodEntity m) {
		String name = m.getName();
		if (name.length() <= 3) {
			return classifyNonAccessMethod(m);
		}
		
		MethodKind kind = null;
		boolean isCandidateForGetterSetter = !m.getStaticNature() && !m.getIsDefault();  
		
		String prefix = name.substring(0, 3);		
		if (prefix.equalsIgnoreCase(GETTER_PREFIX) && m.getArgumentTypes().size() == 0) {
			if (isCandidateForGetterSetter) {
				kind = MethodKind.GETTER;
			} else {
				// may not mimic a real getter if implemented
				kind = MethodKind.NON_CONFORM;
			}
			
		}
		else if (prefix.equalsIgnoreCase(SETTER_PREFIX) && m.getArgumentTypes().size() == 1 && m.getReturnType().getReferencedType().getName().equalsIgnoreCase("void")) {
			if (isCandidateForGetterSetter) {
				kind = MethodKind.SETTER;
			}
			else {
				// may not mimic a real setter/getter if implemented
				kind = MethodKind.NON_CONFORM;
			}
		}
		else {
			kind = classifyNonAccessMethod(m);
		}
		
		return kind;
	}

	/**
	 * build a {@link ModelEntityReference} from the {@link ZedEntity}
	 * @param context - the {@link ZedAnalyzerContext}
	 * @param z - the {@link ZedEntity}
	 * @return - a {@link ModelEntityReference}
	 */
	private ModelEntityReference processModelEntity(ZedForensicsContext context, ZedEntity z) {
		// don't do non-interfaces 
		if (z instanceof InterfaceEntity == false) {
			return null;
		}
		InterfaceEntity ie = (InterfaceEntity) z;
		// don't do non-generic-entities
		if (Boolean.TRUE.equals(ie.getGenericNature()) == false) {
			return null;
		}		
		ModelEntityReference mer = ModelEntityReference.T.create();		
		mer.setType(z);
		mer.setArtifact( context.terminalArtifact());
		//
		// detect methods		
		Map<String, Pair<List<MethodEntity>,List<MethodEntity>>> pairs = new HashMap<>();
		for (MethodEntity m : ie.getMethods()) {
			MethodKind kind = determineMethodType(m);
			switch (kind) {
				case GETTER: {
					String key = m.getName().substring(3);					
					Pair<List<MethodEntity>, List<MethodEntity>> ref = pairs.computeIfAbsent( key, k -> new Pair<>(new ArrayList<>(), new ArrayList<>()));
					ref.first().add(m);
					break;
				}
				case SETTER: {
					String key = m.getName().substring(3);					
					Pair<List<MethodEntity>, List<MethodEntity>> ref = pairs.computeIfAbsent( key, k -> new Pair<>(new ArrayList<>(), new ArrayList<>()));
					ref.second().add(m);
					break;
				}
				case CONFORM: {
					mer.getConformOtherMethods().add(m);
					// static initializer for GenericEntities is ok 
					if (!m.getName().equalsIgnoreCase("<clinit>")) {
							FingerPrint fp = FingerPrintExpert.build(m, ModelForensicIssueType.ConformMethods.toString());
							modelForensicsResult.getFingerPrintsOfIssues().add( fp);
					}
					break;
				}
				default:
				case NON_CONFORM: {
					mer.getNonConformOtherMethods().add(m);
					FingerPrint fp = FingerPrintExpert.build(m, ModelForensicIssueType.NonConformMethods.toString());
					modelForensicsResult.getFingerPrintsOfIssues().add( fp);					
					break;			
				}
			}							
		}
		
		// check how the pairs look like, i.e. only one method present. 
		// if so, try to find the corresponding method from the supertype 		
		ensurePairs( ie, pairs);
		 
		// detect properties
		List<ModelPropertyReference> properties = processMethods( mer, pairs);
		
		// validate property tags
		processPropertyTags( (InterfaceEntity) z, properties);
		
		// detect unexpected fields				
		List<FingerPrint> unexpectedFields = processUnexpectedFields( z, properties);
		modelForensicsResult.getFingerPrintsOfIssues().addAll( unexpectedFields);
						
		// attach properties
		mer.getPropertyReferences().addAll( properties);
		
		// process entity type T literal declaration 
		processEntityTypesDeclaration(context, ie, mer);		
		
		return mer;
	}
	
	private void ensurePairs(InterfaceEntity ie, Map<String, Pair<List<MethodEntity>, List<MethodEntity>>> pairs) {
		
		// first is getter, second is setter
		for (Map.Entry<String, Pair<List<MethodEntity>, List<MethodEntity>>> entry : pairs.entrySet()) {
			String key = entry.getKey();
			Pair<List<MethodEntity>, List<MethodEntity>> methods = entry.getValue();
			
			if (methods.first.size() == 0) {
				String methodKey = "get" + key;
				MethodEntity foundGetter = findMethod( ie.getSuperInterfaces(), methodKey);
				if (foundGetter != null) {
					methods.first.add(foundGetter);
				}
			}
			if (methods.second.size() == 0) {
				String methodKey = "set" + key;
				MethodEntity foundsetter = findMethod( ie.getSuperInterfaces(), methodKey);
				if (foundsetter != null) {
					methods.second.add(foundsetter);
				}			
			}
			
		}
	}
	

	private MethodEntity findMethod(Set<TypeReferenceEntity> superInterfaces, String methodKey) {
		for (TypeReferenceEntity tre : superInterfaces) {
			InterfaceEntity ie = (InterfaceEntity) tre.getReferencedType();
			MethodEntity me = ie.getMethods().stream().filter( m -> m.getName().equals(methodKey)).findFirst().orElse(null);
			if (me != null) {
				return me;	
			}
			me = findMethod( ie.getSuperInterfaces(), methodKey);
			if (me != null) {
				return me;
			}
		}
		return null;
	}
	
	

	private ModelEnumReference processEnumEntity(ZedForensicsContext context, ZedEntity z) {
		// don't do non-interfaces 
		if (z instanceof EnumEntity == false) {
			return null;
		}
		ModelEnumReference mer = ModelEnumReference.T.create();		
		mer.setType(z);
		mer.setEnumEntity((EnumEntity) z);
		mer.setArtifact( context.terminalArtifact());		
		return mer;
	}

	/**
	 * detects unexpected fields in an GenericEntity and produces finger prints 
	 * @param z - the GenericEntity 
	 * @param properties - the extracted {@link ModelPropertyReference}
	 * @return - a {@link List} of {@link FingerPrint} with or without {@link FingerPrint}
	 */
	private List<FingerPrint> processUnexpectedFields(ZedEntity z, List<ModelPropertyReference> properties) {
		List<FingerPrint> result = new ArrayList<>();
		// get all property name literals from the properties
		List<FieldEntity> propertyNameLiteralFields = properties.stream().map( p -> {
			return p.getNameMember();
		}).collect(Collectors.toList());
		
		// get all fields and filter-out the property literals 
		List<FieldEntity> unexpected = ((HasFieldsNature)z).getFields().stream().filter( f -> {
			// a property field's fine
			if (propertyNameLiteralFields.contains( f))
				return false;
			// the T literal field's fine too 
			if (f.getName().equalsIgnoreCase(t_type_function))
				return false;
			// other's are not ok
			return true;
		}).collect( Collectors.toList());
						
		
		// any unfiltered remaining? 
		if (unexpected.size() > 0) {
			// only an issue if not a 'special' GE
			HasAnnotationsNature annotNature = (HasAnnotationsNature) z;
			if (!Comparators.contains( annotNature.getAnnotations(), GMSYSTEMINTERFACE_ANNOTATION_SIGNATURE, ABSTRACT_ANNOTATION_SIGNATURE)) {
				// 
				for (FieldEntity f : unexpected) {
					FingerPrint fp = FingerPrintExpert.build(f, ModelForensicIssueType.UnexpectedField.toString());
					result.add( fp);
				}
			}
		}
		return result;
	}

	/**
	 * forensics on the property literals 
	 * @param owner - the owning interface, aka {@link GenericEntity}
	 * @param properties - a list of {@link ModelPropertyReference}
	 */
	private void processPropertyTags(InterfaceEntity owner, List<ModelPropertyReference> properties) {
		for (ModelPropertyReference propertyReference : properties) {
		// field
			String property = propertyReference.getName();
			FieldEntity tagField = findTagField((InterfaceEntity) owner, property);
			if (tagField == null) {
				FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.PropertyNameLiteralMissing.toString());
				modelForensicsResult.getFingerPrintsOfIssues().add( fp);
			}
			else {
				propertyReference.setNameMember(tagField);
				Object obj = tagField.getInitializer();
				if (obj == null || obj instanceof String == false) {
					FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.PropertyNameLiteralTypeMismatch.toString());
					modelForensicsResult.getFingerPrintsOfIssues().add( fp);										
				}
				else {
					String initValue = (String) obj;
					if (!initValue.equalsIgnoreCase( property)) {
						FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.PropertyNameLiteralMismatch.toString());
						modelForensicsResult.getFingerPrintsOfIssues().add( fp);
					}
				}
			}
		}
							
	}

	/**
	 * process the collected data to validate the 'T literal' 
	 * @param context
	 * @param ie
	 * @param mer
	 */
	private void processEntityTypesDeclaration(ZedForensicsContext context, InterfaceEntity ie, ModelEntityReference mer) {
		//entity type field
		
		boolean foundTLiteral = false;
		for (FieldEntity field : ie.getFields()) {
			if (field.getName().equalsIgnoreCase(t_type_function)) {
				mer.setEntityTypesDeclaration(field);
				TypeReferenceEntity typeReference = field.getType();
				// validate here .. 
				ZedEntity referencedType = typeReference.getReferencedType();
				if (!referencedType.getName().equalsIgnoreCase(reflectingEntityType)) {
					FingerPrint fp = FingerPrintExpert.build( ie, ModelForensicIssueType.InvalidEntityTypeDeclaration.toString());
					modelForensicsResult.getFingerPrintsOfIssues().add(fp);
					return;
				}
				// check entity type
				List<TypeReferenceEntity> parameterization = typeReference.getParameterization();
				if (parameterization.size() != 1) {
					FingerPrint fp = FingerPrintExpert.build( ie, ModelForensicIssueType.InvalidEntityTypeDeclaration.toString());
					modelForensicsResult.getFingerPrintsOfIssues().add(fp);
					return;
				}
				else {
					TypeReferenceEntity parameterType = parameterization.get(0);
					if (parameterType.getReferencedType() != ie) {
						FingerPrint fp = FingerPrintExpert.build( ie, ModelForensicIssueType.InvalidEntityTypeDeclaration.toString());
						modelForensicsResult.getFingerPrintsOfIssues().add(fp);
						return;
					}
				}
				
				// actually assigned type - as read from the constructor
				TypeReferenceEntity entityTypesParameter = field.getEntityTypesParameter();				
				if (entityTypesParameter == null ||entityTypesParameter.getReferencedType() != ie) {
					FingerPrint fp = FingerPrintExpert.build( ie, ModelForensicIssueType.InvalidEntityTypeDeclaration.toString());
					modelForensicsResult.getFingerPrintsOfIssues().add(fp);
					return;
				}
							
				foundTLiteral = true;
				break;
			}		
		}
		// no T literal found -> finger print it
		if (foundTLiteral == false) {
			FingerPrint fp = FingerPrintExpert.build( ie, ModelForensicIssueType.MissingEntityTypeDeclaration.toString());
			modelForensicsResult.getFingerPrintsOfIssues().add(fp);
		}
	}

	

	/**
	 * identify, combine getter/setter into properties, validate 
	 * @param pairs - getter/setter combinations 
	 * @return - a {@link List} of {@link ModelPropertyReference} deduced from the pairs 
	 */
	private List<ModelPropertyReference> processMethods(ModelEntityReference mer, Map<String, Pair<List<MethodEntity>, List<MethodEntity>>> pairs) {
		List<ModelPropertyReference> result = new ArrayList<>();
		
		for (Entry<String, Pair<List<MethodEntity>,List<MethodEntity>>> entry : pairs.entrySet()) {
			String property = entry.getKey();

			ModelPropertyReference propertyReference = ModelPropertyReference.T.create();
			propertyReference.setName(property);
			propertyReference.setOwner(mer);
			
			result.add( propertyReference);

			Pair<List<MethodEntity>, List<MethodEntity>> value = entry.getValue();
			List<MethodEntity> getters = value.first();
			List<MethodEntity> setters = value.second();
			
			// 
			getters = filter( getters);
			setters = filter( setters);
			
					
			// no setter/getter at all 	
			if (getters == null) {	
				FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.MissingGetter.toString());
				modelForensicsResult.getFingerPrintsOfIssues().add(fp);
				continue;
			}
			else if (getters.size() > 1) {
				FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.AmbiguousGetter.toString());
				modelForensicsResult.getFingerPrintsOfIssues().add(fp);
				continue;
			}
			
			if (setters == null) {	
				FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.MissingSetter.toString());
				modelForensicsResult.getFingerPrintsOfIssues().add(fp);
				continue;
			}
			else if (getters.size() > 1) {
				FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.AmbiguousSetter.toString());
				modelForensicsResult.getFingerPrintsOfIssues().add(fp);
				continue;
			}
			
			MethodEntity getter = getters.get(0);

			propertyReference.setGetter(getter);
			
			
			TypeReferenceEntity returnTypeReference = getter.getReturnType();
			ZedEntity getterType = returnTypeReference.getReferencedType();
			propertyReference.setType(getterType);
			
			// validate 
			// check transient
			if (Comparators.contains( getter.getAnnotations(), TRANSIENT_ANNOTATION_SIGNATURE)) {
				propertyReference.setTransientNature(true);
			}
			else {
				if (Arrays.asList( collectionTypes).contains( getterType.getName())) {
					// collection
					boolean mismatch = false;
					for (TypeReferenceEntity ref : returnTypeReference.getParameterization()) {
						ZedEntity z = ref.getReferencedType();
						// no collections withhin collections 
						if (Arrays.asList( collectionTypes).contains( z.getName())) {
							mismatch = true;
							break;
						}
						else {
							// all parameter types must be valid
							if (!checkType( ref)) {
								mismatch = true;
								break;
							}
						}
					}
					// either collection-within-collection or invalid element type 
					if(mismatch) {						
						FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.InvalidTypes.toString());
						modelForensicsResult.getFingerPrintsOfIssues().add(fp);						
						continue;
					}					
				}
				else {
					// single type, check 
					if (!checkType(returnTypeReference)) {
						FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.InvalidTypes.toString());
						modelForensicsResult.getFingerPrintsOfIssues().add(fp);												
						continue;
					}
				}					
			}
			
			MethodEntity setter = setters.get(0);
									
			ZedEntity setterType = setter.getArgumentTypes().get(0).getReferencedType();
			if (getterType instanceof ParameterEntity && property.equalsIgnoreCase("Id")) {
				System.out.println("template parameter in ID property");
			}
			else {
				if (Comparators.entity().compare(getterType, setterType) != 0) {
					// types do not match of this pairs.. 
					FingerPrint fp = FingerPrintExpert.build(propertyReference, ModelForensicIssueType.MissingSetter.toString());
					modelForensicsResult.getFingerPrintsOfIssues().add(fp);											
					continue;
				}
			}
					
			propertyReference.setSetter(setter);
								
		}

		return result;
	}
	

	/**
	 * filter the list of possible getter/setter functions so that only one remains
	 * @param methods - the list of setters
	 * @return
	 */
	private List<MethodEntity> filter(List<MethodEntity> methods) {
		if (methods.size() == 0)
			return null;
		if (methods.size() == 1)
			return methods;
		
		ZedEntity z = (ZedEntity) methods.get(0).getOwner();
		
		String name = methods.get(0).getName();
		if ( z instanceof InterfaceEntity == false) {
			throw new IllegalStateException( "expected [" + z.getName() + "] to be an interface");
		}
		InterfaceEntity owner = (InterfaceEntity) z;
		
		List<String> descs = new ArrayList<>();
		List<MethodEntity> mes = new ArrayList<>();
		for (MethodEntity me : methods) {
			String desc = me.getDesc();			
			descs.add( desc);
			if (!isInheritedMethod(owner, name, desc)) {
				mes.add( me);
			}
		}		
		return mes;
	}

	private boolean isInheritedMethod(InterfaceEntity owner, String name, String desc) {
		for (TypeReferenceEntity tfr : owner.getSuperInterfaces()) {
			InterfaceEntity sie = (InterfaceEntity) tfr.getReferencedType();
			for ( MethodEntity sme : sie.getMethods()) {
				if (sme.getName().equalsIgnoreCase( name) && sme.getDesc().equalsIgnoreCase(desc)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * valid types for GEs are simple types
	 * @param zed
	 * @return
	 */
	private boolean checkType( TypeReferenceEntity ref) {
		ZedEntity zed = ref.getReferencedType();
		if (Arrays.asList(simpleTypes).contains( zed.getName())) {
			return true;
		}
		else if (Arrays.asList( collectionTypes).contains( zed.getName())) {
			return true;
		} 
		else if (zed instanceof InterfaceEntity) {
			InterfaceEntity ie = (InterfaceEntity) zed;
			if (ie.getGenericNature()) {					
				return true;
			}
		}	
		else if (zed instanceof EnumEntity) {
			return true;
		}
		else if (zed instanceof ParameterEntity) {
			List<TypeReferenceEntity> parameterization = ref.getParameterization();
			for (TypeReferenceEntity tref : parameterization) {
				if (!checkType(tref)) {
					return false;
				}
			}
			log.debug("parameter entity [" + zed.getName() + "]");
			return true;
		}
		System.out.println( "invalid type [" + zed.getName() + "]");
		return false;
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
