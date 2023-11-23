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
package com.braintribe.devrock.zed.analyze;



import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.asm.Type;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.model.reasons.UrlNotFound;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerProcessContext;
import com.braintribe.devrock.zed.api.core.ZedEntityResolver;
import com.braintribe.devrock.zed.commons.Commons;
import com.braintribe.devrock.zed.commons.ZedException;
import com.braintribe.devrock.zed.context.BasicZedAnalyzerProcessContext;
import com.braintribe.devrock.zed.scan.asm.AnnotationTuple;
import com.braintribe.devrock.zed.scan.asm.AsmClassDepScanner;
import com.braintribe.devrock.zed.scan.asm.ClassData;
import com.braintribe.devrock.zed.scan.asm.EntityTypeInitializerData;
import com.braintribe.devrock.zed.scan.asm.FieldData;
import com.braintribe.devrock.zed.scan.asm.MethodBodyTypeReference;
import com.braintribe.devrock.zed.scan.asm.MethodData;
import com.braintribe.devrock.zed.scan.asm.MethodSignature;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.AccessModifier;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.AnnotationValueContainer;
import com.braintribe.zarathud.model.data.AnnotationValueContainerType;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ScopeModifier;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasAnnotationsNature;
import com.braintribe.zarathud.model.data.natures.HasFieldsNature;
import com.braintribe.zarathud.model.data.natures.HasMethodsNature;
import com.braintribe.zarathud.model.data.natures.HasTemplateParameters;

public class AsmAnalyzer implements ZedEntityResolver{
	private static Logger log = Logger.getLogger(AsmAnalyzer.class);
	private Map<ZedEntity, ZedAnalyzerProcessContext> zedToContextMap = new HashMap<>();
	
	private URL runtimeJarUrl;
	
	public AsmAnalyzer( URL runtimeJarUrl) {
		this.runtimeJarUrl = runtimeJarUrl;
	}
	
	/**
	 * processes the {@link MethodData} attached
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 */
	public void processMethodNature(ZedAnalyzerProcessContext context) {
		List<MethodData> methodList = context.methods();
		if (methodList != null && methodList.size()>0) {
			Set<MethodEntity> entries = ((HasMethodsNature) context.entity()).getMethods();
			for (MethodData methodData : methodList) {
				entries.add( buildMethodEntry( context, (HasMethodsNature) context.entity(), methodData));
			}							
		}		
	}


	/**
	 * processes the attached {@link ClassData}'s {@link FieldData}
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 */
	public void processFieldNature(ZedAnalyzerProcessContext context) {
			
		List<FieldData> data = context.classData().getFieldData();
		if (
				data != null && 
				data.size() > 0
			) {								
			String desc = context.entity().getDesc();
			for (FieldData field : data) {
				String name = field.getName();
				if (context.entity() instanceof EnumEntity) {
					String fieldDesc = field.getDesc();
					boolean match = desc.equals(fieldDesc);
					if (name.equalsIgnoreCase( "$VALUES") == false && match) {
						
						((EnumEntity)context.entity()).getValues().add( name);
					}
					else {
						FieldEntity fieldEntity = buildFieldEntry( context, field);
						fieldEntity.setOwner( ((HasFieldsNature) context.entity()));	
						field.getAnnotationTuples();
						
						((HasFieldsNature)context.entity()).getFields().add( fieldEntity);
					}
				}
				else {
					FieldEntity fieldEntity = buildFieldEntry( context, field);
					fieldEntity.setOwner( ((HasFieldsNature) context.entity()));	
					field.getAnnotationTuples();
					
					((HasFieldsNature)context.entity()).getFields().add( fieldEntity);
				}
			}																		
		}		
		
	}


	/**
	 * process the annotations if found (gets it actually)
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 */
	public void processAnnotationNature(ZedAnalyzerProcessContext context) {
		List<AnnotationTuple> annotationTuples = null;
		try {
			annotationTuples = AsmClassDepScanner.getClassAnnotationMembers( ensureInputStream(context.resourceURL()));
		} catch (Exception e) {
			throw new ZedException( "cannot extract possible annotation data of [" + context.resourceURL().toExternalForm() + "]", e);
		}
		
		if (annotationTuples != null && annotationTuples.size() > 0) {
			Set<TypeReferenceEntity> annotations = ((HasAnnotationsNature) context.entity()).getAnnotations();
			annotations.addAll( processAnnotationTuples(context, context.entity(), annotationTuples));																				
		}
	}
	
	/**
	 * sub anno nature : process the collected data from the {@link AsmClassDepScanner} 
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 * @param tuples - a {@link Collection} of {@link AnnotationTuple}
	 * @return
	 */
	private Collection<TypeReferenceEntity> processAnnotationTuples( ZedAnalyzerProcessContext context, GenericEntity owner, Collection<AnnotationTuple> tuples) {
		return tuples.stream().map( t -> {			
			AnnotationEntity ae = acquireAnnotation(context, t);
			ae.setOwner(owner);
			TypeReferenceEntity tf = TypeReferenceEntity.T.create();
			tf.setReferencedType(ae);
			return tf;
		}).collect(Collectors.toList());
	}
		
	/**
	 * sub anno nature: build an {@link AnnotationEntity} from an {@link AnnotationTuple}
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 * @param tuple - the {@link AnnotationTuple}
	 * @return - a fresh {@link AnnotationEntity}
	 * @throws ZedException
	 */
	private AnnotationEntity acquireAnnotation( ZedAnalyzerProcessContext context, AnnotationTuple tuple) throws ZedException {
		String desc = tuple.getDesc();
		
		TypeReferenceEntity declaringInterface = context.signatures().processType( context, desc, InterfaceEntity.T);
		AnnotationEntity annotationEntity = Commons.create(context.context(), AnnotationEntity.T);
		annotationEntity.setDeclaringInterface(declaringInterface);			
		annotationEntity.setOwner( context.entity());
		annotationEntity.getArtifacts().addAll( context.entity().getArtifacts());
		annotationEntity.setName( declaringInterface.getReferencedType().getName());
		
		Map<String, Object> members = tuple.getValues();
		if (members != null && members.size() > 0) {			
			for (Entry<String, Object> member : members.entrySet()) {
				String key = member.getKey();
				Object value = member.getValue();
				AnnotationValueContainer container = acquireAnnotationValueContainer(context, value);
				annotationEntity.getMembers().put(key, container);
			}						
		}
		return annotationEntity;
	}

	/**
	 * sub anno nature : build a {@link AnnotationValueContainer} from the passed {@link Object}
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 * @param value - the value of the annotation as an {@link Object}
	 * @return - a fresh {@link AnnotationValueContainer}
	 * @throws ZedException
	 */
	private AnnotationValueContainer acquireAnnotationValueContainer( ZedAnalyzerProcessContext context, Object value) throws ZedException {
		AnnotationValueContainer result = AnnotationValueContainer.T.create();
		
		if (value instanceof String) {
			result.setContainerType( AnnotationValueContainerType.s_string);
			result.setSimpleStringValue( (String) value);
			return result;
		}

		if (value instanceof Boolean) {
			result.setContainerType( AnnotationValueContainerType.s_boolean);
			result.setSimpleBooleanValue( (Boolean) value);
			return result;
		}

		if (value instanceof Integer) {
			result.setContainerType( AnnotationValueContainerType.s_int);
			result.setSimpleIntegerValue( (Integer) value);
			return result;
		}
		
		if (value instanceof Long) {
			result.setContainerType( AnnotationValueContainerType.s_long);
			result.setSimpleLongValue( (Long) value);
			return result;
		}

		if (value instanceof Float) {
			result.setContainerType( AnnotationValueContainerType.s_float);
			result.setSimpleFloatValue( (Float) value);
			return result;
		}
		
		if (value instanceof Double) {
			result.setContainerType( AnnotationValueContainerType.s_double);
			result.setSimpleDoubleValue( (Double) value);
			return result;
		}
		if (value instanceof Date) {
			result.setContainerType( AnnotationValueContainerType.s_date);
			result.setSimpleDateValue( (Date) value);
			return result;
		}
		
		if (value.getClass().isArray()) {
			Object [] array = (Object[]) value;
			result.setContainerType( AnnotationValueContainerType.collection);
			List<AnnotationValueContainer> contents = result.getChildren();
			for (Object obj : array) {
				contents.add( acquireAnnotationValueContainer(context, obj));
			}			
			return result;
		}
		
		if (value instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>) value;
			result.setContainerType( AnnotationValueContainerType.collection);
			List<AnnotationValueContainer> contents = result.getChildren();
			for (Object obj : collection) {
				contents.add( acquireAnnotationValueContainer(context, obj));
			}			
			return result;
		}
	
		if (value instanceof AnnotationTuple) {
			AnnotationEntity annotation = acquireAnnotation( context, (AnnotationTuple) value);
			result.setContainerType( AnnotationValueContainerType.annotation);
			result.setOwner(annotation);
			return result;
		}
		
		if (value instanceof Type) {
			result.setContainerType( AnnotationValueContainerType.s_string);
			Type valueType = (Type) value;
			result.setSimpleStringValue( valueType.toString());
			return result;
		}
		
		log.warn( "type [" + value.getClass() + "] is not supported");
		return null;	
	}
	
	
	
	/**
	 * builds a {@link FieldEntity} from the {@link FieldData} as defined by the {@link AsmClassDepScanner}
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 * @param data - the {@link FieldData}
	 * @return - the corresponding {@link FieldEntity}
	 */
	private FieldEntity buildFieldEntry( ZedAnalyzerProcessContext context, FieldData data) {
		FieldEntity fieldEntity = Commons.create( context.context(), FieldEntity.T);
		fieldEntity.setName( data.getName());
		String desc = data.getDesc();
		fieldEntity.setDesc( desc);
		String signature = data.getSignature();
		// if no signature comes from ASM, we must generate it
		if (signature == null) {
			signature = desc;
		}
		fieldEntity.setSignature( signature);
		fieldEntity.setInitializer( data.getIntializer());
		
		// type
		TypeReferenceEntity ref = context.signatures().processType(context, signature, ZedEntity.T);
		
		fieldEntity.setType( ref);
		
		// access modifiers
		fieldEntity.setAccessModifier( AccessModifier.valueOf( data.getAccessModifier().toString()));
		fieldEntity.setScopeModifier( ScopeModifier.valueOf( data.getScopeModifier().toString()));		
		fieldEntity.setStaticNature( data.getIsStatic());	
		// final
		fieldEntity.setIsFinal( data.getisFinal());
		
		// annotations
		if (data.getAnnotationTuples() != null && data.getAnnotationTuples().size() > 0) {
			Collection<TypeReferenceEntity> processAnnotationTuples = processAnnotationTuples(context, fieldEntity, data.getAnnotationTuples());
			fieldEntity.getAnnotations().addAll( processAnnotationTuples);
		}
		return fieldEntity;
	}
	/**
	 * builds a {@link MethodEntity} from the {@link MethodData} as defined by the {@link AsmClassDepScanner}
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 * @param data -the {@link MethodData}
	 * @return - the corresponding {@link MethodEntity}
	 * @throws ZedException - arrgh
	 */
	private MethodEntity buildMethodEntry( ZedAnalyzerProcessContext context, HasMethodsNature owner, MethodData data) throws ZedException {
		MethodEntity method = Commons.create( context.context(), MethodEntity.T);
		method.setName(data.getMethodName());
		method.setDesc( data.getDesc());
		method.setSignature( data.getSignature());
		method.setOwner(owner);
		
		String signature = method.getSignature();
		if (signature == null || signature.contains( "TACTUAL")) {
			signature = method.getDesc();
		}
		//signature = method.getDesc();
		MethodSignature ms = context.signatures().extractMethodSignature( context, signature);
			
		if (ms == null) {
			log.warn("cannot process [" + ((ZedEntity) owner).getName() + "]'s function [" + data.getMethodName() + "] signature : [" + data.getSignature() + "]");
			return method;
		}
		// 
		String returnValue = data.getReturnType();
		if (returnValue != null) {			
			method.setReturnType( ms.returnType);			
		}
		
		if (ms.argumentTypes != null) {
			method.getArgumentTypes().addAll(ms.argumentTypes);
		}
		
		// access modifiers and natures 
		method.setAccessModifier( AccessModifier.valueOf( data.getAccessModifier().toString()));
		method.setStaticNature( data.getIsStatic());
		method.setAbstractNature( data.getIsAbstract());
		method.setSynchronizedNature( data.getIsSynchronized());
		
		// annotations
		if (data.getAnnotationTuples() != null && data.getAnnotationTuples().size() > 0) {
			method.getAnnotations().addAll( processAnnotationTuples(context, method, data.getAnnotationTuples()));
		}

		// exceptions 
		Collection<String> exceptions = data.getExceptions();
		if (exceptions != null && exceptions.size() > 0) {
			Set<ClassEntity> exceptionEntities = method.getExceptions(); 			
			for (String exception : exceptions) {
				exception = exception.replace("/", ".");
				exception = ensureStandardNotation(exception);				
				exceptionEntities.add( context.context().registry().acquire(context.context(), exception, ClassEntity.T));
			}
		}		
		
		
		// body types
		List<MethodBodyTypeReference> extractedReferences = data.getBodyTypes();
		if (extractedReferences != null && extractedReferences.size() > 0) {
			for (MethodBodyTypeReference extractedReference : extractedReferences) {
				String resourceName;
				String bodyType = extractedReference.getOwner();
				if (!bodyType.startsWith( "L") && !bodyType.endsWith( ";")) {
					resourceName = "L" + bodyType + ";";
				}
				else {
					resourceName = bodyType;
				}
				// desc to analyze parameter? 
				String methodDesc = extractedReference.getDesc();
				if (methodDesc != null) {
					// analyze that one...??  
					//System.out.println(methodDesc);
				}
				// or simply use ?? 
				String methodName = extractedReference.getInvokedMethodName();
												 		
				TypeReferenceEntity bodyTypeReference = context.signatures().processType(context, resourceName, ZedEntity.T);
				bodyTypeReference.setBodyReferenceMethodName( methodName);
				method.getBodyTypes().add(bodyTypeReference);
			}
		}
		
		// initializers for entity type?? 
		EntityTypeInitializerData entityTypeInitializerData = data.getEntityTypeInitializerData();
		if (entityTypeInitializerData != null && context.entity() instanceof HasFieldsNature) {
			HasFieldsNature hasFieldsNature = (HasFieldsNature) context.entity();
			String expectedFieldName = entityTypeInitializerData.getField();
			for (FieldEntity fe : hasFieldsNature.getFields()) {
				if (expectedFieldName.equalsIgnoreCase( fe.getName())) {
					String value = entityTypeInitializerData.getValue();
					String resourceName;
					if (!value.startsWith( "L") && !value.endsWith( ";")) {
						resourceName = "L" + value + ";";
					}
					else {
						resourceName = value;
					}
					TypeReferenceEntity bodyTypeReference = context.signatures().processType(context, resourceName, ZedEntity.T);
					fe.setEntityTypesParameter(bodyTypeReference);
					break;
				}
			}
		}
		
		if (owner instanceof InterfaceEntity) {
			method.setIsDefault( data.getContainsBody());
		}
		
		return method;
	}
	
	private String ensureStandardNotation( String in) {
		String out = in;		
		if (!out.startsWith( "L")) {
			out = "L" + out;					
		}
		if (!out.endsWith( ";")) {
			out = out + ";";
		}
		return out;
	}
	
	/**
	 * processes the super interfaces of the attached data 
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 * @return - a collection of {@link TypeReferenceEntity} pointing to the super interfaces 
	 */
	private Collection<TypeReferenceEntity> implementingInterfaces( ZedAnalyzerProcessContext context) {
		List<String> interfaces = context.inheritanceData().getInterfaces();
		if (
				(interfaces != null) &&
				(interfaces.size() > 0)
			) {
			Set<TypeReferenceEntity> interfaceEntities = new HashSet<>();
			for (String interfaceName : interfaces) {
				interfaceName = ensureStandardNotation(interfaceName);
				//TypeReferenceEntity acquiredInterfaceEntity = context.signatures().processType(context, interfaceName.replace( "/", "."), InterfaceEntity.T);
				TypeReferenceEntity acquiredInterfaceEntity = context.signatures().processType(context, interfaceName, InterfaceEntity.T);
				interfaceEntities.add( acquiredInterfaceEntity);
			}
			return interfaceEntities;
		}
		return Collections.emptySet();
	}
	

	/**
	 * process the attached {@link ClassData} and attach to context's {@link ClassEntity}
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 */
	public void processClass(ZedAnalyzerProcessContext context) {
		ClassEntity classEntity = (ClassEntity) context.entity();
		classEntity.setAccessModifier( AccessModifier.valueOf( context.classData().getAccessNature().toString()));
		classEntity.setStaticNature( context.classData().getIsStatic());
		classEntity.setAbstractNature( context.classData().getIsAbstract());
		classEntity.setSynchronizedNature( context.classData().getIsSynchronized());
		
		String superSuspect = context.inheritanceData().getSuperClass();
		superSuspect = ensureStandardNotation(superSuspect);
		
		if (superSuspect.equalsIgnoreCase("Ljava.lang.Object;") == false) {
			TypeReferenceEntity superTypeRef = context.signatures().processType(context, superSuspect.replace( '.', '/'), ClassEntity.T); 			
			classEntity.setSuperType( superTypeRef);
		}
		((BasicZedAnalyzerProcessContext) context).setAnnotations(context.classData().getAnnotationTuples());
		classEntity.getImplementedInterfaces().addAll( implementingInterfaces(context));
	}
	
	private InputStream open( URL url) {
		try {
			return url.openStream();
		} catch (IOException e) {
			throw new ZedException("cannot open stream for [" + url.toExternalForm() + "]");
		}
	}

	/**
	 * process the inferfaces of attached {@link InterfaceEntity}
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 */
	public void processInterface(ZedAnalyzerProcessContext context) {
				
		((InterfaceEntity) context.entity()).getSuperInterfaces().addAll( implementingInterfaces(context));
		((BasicZedAnalyzerProcessContext) context).setAnnotations(context.classData().getAnnotationTuples());
	}
	
	enum JarType { system, custom}
		
	private JarType examineUrl( URL jarUrl) {
		String urlAsString = jarUrl.toExternalForm();
		
		if (urlAsString.startsWith( "jrt:")) {
			return JarType.system;
		}
		else if (urlAsString.startsWith( "jre:")) {
			return JarType.system;
		}
		else if (urlAsString.startsWith( "jar")){
			if (urlAsString.contains("rt.jar!"))
				return JarType.system;
		}		
		return JarType.custom;
	}
	/**
	 * process the attached enum
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 */
	public void processEnum(ZedAnalyzerProcessContext proContext) {
	
		EnumEntity enumEntity = (EnumEntity) proContext.entity();
		enumEntity.setAccessModifier( AccessModifier.valueOf( proContext.classData().getAccessNature().toString()));
		enumEntity.setStaticNature( proContext.classData().getIsStatic());
		enumEntity.setAbstractNature( proContext.classData().getIsAbstract());
		enumEntity.setSynchronizedNature( proContext.classData().getIsSynchronized());
		
		String superSuspect = proContext.inheritanceData().getSuperClass();
		superSuspect = ensureStandardNotation(superSuspect);

		if (superSuspect.equalsIgnoreCase("Ljava.lang.Object;") == false) {
			TypeReferenceEntity superTypeRef = proContext.signatures().processType( proContext, superSuspect.replace('.','/'), ClassEntity.T); 			
			enumEntity.setSuperType( superTypeRef);
		}
		((BasicZedAnalyzerProcessContext) proContext).setAnnotations(proContext.classData().getAnnotationTuples());

		enumEntity.getImplementedInterfaces().addAll( implementingInterfaces( proContext));
	
	
	}
	
	@Override
	public Maybe<ZedEntity> acquireClassResource(ZedAnalyzerContext context, String className) {
		String resourceName = className.replace('.', '/') + ".class";
		
		if (context.classloader() == null) {
			throw new ZedException( "No classloader defined to find [" + resourceName + "]");
		}
		// jrt:/java.base/
		URL resourceUrl = null;
			
		List<Artifact> artifacts = new ArrayList<>();
		try {
			Enumeration<URL> resourceUrls = context.classloader().getResources( resourceName);
			URL systemUrl = null;
			while (resourceUrls.hasMoreElements()) {
				URL url = resourceUrls.nextElement();	
				// check if it's a jrt or jre URL				
				Artifact artifact = null;			
				switch (examineUrl(url)) {
					case system:
						artifact = context.artifacts().runtimeArtifact(context);
						systemUrl = url;
						break;
					default:
					case custom:
						URL keyResourceUrl = Commons.extractComparableResource( url, className);
						if (runtimeJarUrl != null && keyResourceUrl.sameFile(runtimeJarUrl)) {
							resourceUrl = url;
						}
						else if (resourceUrl == null){
							resourceUrl = url;
						}					
						artifact = context.urlToArtifactMap().get( keyResourceUrl);
						resourceUrl = url;
						break;
					
					}
				if (artifact == null) {
					artifact = context.artifacts().unknownArtifact(context);
				}							
				artifacts.add(artifact);
			}							
			// if one of the found classes comes from the system, it has precedence 
			if (systemUrl != null) {
				resourceUrl = systemUrl;
			}
		} catch (IOException e1) {
			//throw new ZedException( "error while extracting URL of passed resource [" + className + "] and expected resource [" + resourceName + "]", e1);
			String msg = "error while extracting URL of passed resource [" + className + "] and expected resource [" + resourceName + "]";
			Maybe.empty( Reasons.build(com.braintribe.gm.model.reason.essential.InternalError.T).text( msg).enrich(r -> r.setJavaException(e1)).toReason());
		} 
		
		if (resourceUrl == null) {	
			String path = context.currentlyScannedResource().getPath();
			String msg = "no matching URL found for passed resource name [" + className + "] while scanning [" + path + "]";
			log.warn(msg);
			// in order to collect the data, it needs to be reflected..
			String scannedResource;
			String scannedType;
			int p = path.indexOf('!');
			if (p < 0) {
				scannedResource ="runtime";
				scannedType = path;
			}
			else {
				scannedResource = path.substring(0, p);
				scannedType = path.substring(p+1);
			}
			
			return Maybe.empty( Reasons.build(UrlNotFound.T)
										.text(msg)
										.enrich(r -> r.setScanExpression( className))										
										.enrich( r -> r.setScannedResource(  scannedResource))
										.enrich(r -> r.setScannedType( scannedType))
										.enrich( r -> r.setCombinedUrl(path))
										.toReason()
								);
		}
			
		BasicZedAnalyzerProcessContext proContext = new BasicZedAnalyzerProcessContext();		
				
		try {
			proContext.setInheritanceData( AsmClassDepScanner.getInheritanceData( open(resourceUrl)));
		} catch (Exception e) {
			//throw new ZedException( "cannot extract inheritance data of [" + resourceUrl.toExternalForm() + "]", e);
			String msg = "cannot extract inheritance data of [" + resourceUrl.toExternalForm() + "]";
			return Maybe.empty( Reasons.build(com.braintribe.gm.model.reason.essential.InternalError.T).text( msg).enrich(r -> r.setJavaException(e)).toReason());
		} 
		
		String superSuspect = proContext.inheritanceData().getSuperClass();
		ZedEntity entry = null;				
		 									
		
		// TODO : redesign representation of enum... is it a class? 
		if (superSuspect != null && superSuspect.equalsIgnoreCase( "java.lang.Enum")) {
			entry = Commons.create( context, EnumEntity.T);
			try {
				proContext.setClassData( AsmClassDepScanner.getClassData( open( resourceUrl)));
				proContext.setMethods( AsmClassDepScanner.getMethodData( open( resourceUrl)));
			} catch (Exception e) {
				//throw new ZedException( "cannot extract class data from [" + resourceUrl.toExternalForm() + "]", e);
				String msg = "cannot extract enum data from [" + resourceUrl.toExternalForm() + "]";
				return Maybe.empty( Reasons.build(com.braintribe.gm.model.reason.essential.InternalError.T).text( msg).enrich(r -> r.setJavaException(e)).toReason());
			}
		}
		else {
			if (proContext.inheritanceData().isInterfaceClass()) {
				entry = Commons.create( context, InterfaceEntity.T);				
				try {
					proContext.setMethods( AsmClassDepScanner.getMethodData( open( resourceUrl)));
					proContext.setClassData( AsmClassDepScanner.getClassData( open( resourceUrl)));
				} catch (Exception e) {
					//throw new ZedException( "cannot extract class data from [" + resourceUrl.toExternalForm() + "]", e);
					String msg = "cannot extract interface data from [" + resourceUrl.toExternalForm() + "]";
					return Maybe.empty( Reasons.build(com.braintribe.gm.model.reason.essential.InternalError.T).text( msg).enrich(r -> r.setJavaException(e)).toReason());
				}
			}
			else {
				entry = Commons.create( context, ClassEntity.T);
				try {
					proContext.setClassData( AsmClassDepScanner.getClassData( open( resourceUrl)));
					proContext.setMethods( AsmClassDepScanner.getMethodData( open( resourceUrl)));
				} catch (Exception e) {					
					//throw new ZedException( "cannot extract class data from [" + resourceUrl.toExternalForm() + "]", e);
					String msg = "cannot extract class data from [" + resourceUrl.toExternalForm() + "]";
					return Maybe.empty( Reasons.build(com.braintribe.gm.model.reason.essential.InternalError.T).text( msg).enrich(r -> r.setJavaException(e)).toReason());
				} 
			}
		}
		
		//
		entry.setResourceUrl(resourceUrl);
		proContext.setResourceURL(resourceUrl);		
		entry.setArtifacts(artifacts);
		proContext.setEntity( entry);
		proContext.setContext(context);
		
		zedToContextMap.put(entry, proContext);
		
		return Maybe.complete(entry);			
	}
	
	
	private void processClassParameters(  ZedAnalyzerProcessContext context) {
		String signature = context.classData().getSignature();
		String desc = context.entity().getDesc();
		
		if (signature == null || signature.equalsIgnoreCase( desc)) 
			return;		
	
		// split parameters	
		Pair<String,List<String>> pair = extractClassparameterAndInheritenceHelperData(signature);
		List<String> sigParts = pair.getSecond();
		int i = 0;
		
		Map<String, String> typeToParameterExpressionMap = new HashMap<>();
		Map<String, TypeReferenceEntity> typeToReference = new HashMap<>();
		Map<String, String> keyToTypeReference = new HashMap<>();
		
		// analyze signature parts
		for (String sigPart : sigParts) {
			//a ) split type plus parameters
			int pTs = sigPart.indexOf('<');
			String typeExpressionToProcess, typeParameterExpression = null;
			if (pTs > 0) {
				typeExpressionToProcess = sigPart.substring(0, pTs) + ";";
				typeParameterExpression = sigPart.substring( pTs); // <....;....>;
			}
			else {
				typeExpressionToProcess = sigPart;
			}
			
			String actualType;
			// b) if template parameter, add to storage
			int pCs = typeExpressionToProcess.indexOf( ':');
			if (pCs > 0) { 
				String key = "T" + typeExpressionToProcess.substring(0, pCs) + ";";
				int pCs2 = typeExpressionToProcess.lastIndexOf( ':'); 
				actualType = typeExpressionToProcess.substring(pCs2+1);
				keyToTypeReference.put(key, actualType);
				typeParameterExpression = typeExpressionToProcess;
			}
			else {
				actualType = typeExpressionToProcess;
			}
			
			typeToParameterExpressionMap.put( actualType, typeParameterExpression);
			
			TypeReferenceEntity ref = context.signatures().processType(context, actualType,ZedEntity.T);
			typeToReference.put(actualType, ref);
		}
		Map<String, TypeReferenceEntity> parameterization = new HashMap<>();
		// c) post process templates
		for (Entry<String, String> entry : typeToParameterExpressionMap.entrySet()) {
			String typeName = entry.getKey();
			String paramExpression = entry.getValue();
			if (paramExpression == null) {
				continue;
			}
			List<String> params = extractParametersFromSnippet( paramExpression);
			TypeReferenceEntity typeRef = typeToReference.get( typeName);
			for (String param : params) {
				int pCs = param.indexOf( ':');
				if (pCs > 0) { 
					param += ";";					
					// TODO : key is currently wrong.. needs the same as above					
					String key = "T" + param.substring(0, pCs) + ";";
					String matchingType = keyToTypeReference.get( key);
					if (matchingType == null) {
						log.error("cannot find declared template parameter [" + param + "]");
						continue;
					}
					TypeReferenceEntity paramTypeRef = typeToReference.get( matchingType);
					typeRef.getParameterization().add(paramTypeRef);
					// store the key (without ;)
					parameterization.put(key.substring(0, key.length()-1), paramTypeRef);
				}
				else {
					TypeReferenceEntity ref = context.signatures().processType(context, param,ZedEntity.T);
					typeRef.getParameterization().add(ref);
				}
			}
		}
		
		// attach to entity 				
		if (context.entity() instanceof HasTemplateParameters) {
			((HasTemplateParameters) context.entity()).setTemplateParameters(parameterization);
			contextualizeClassparameters(context);
		}
		else {
			log.warn("template parameterization found at unexpected type [" + context.entity().getDesc() + "], ignored");
		}
		
	}
	
	/**
	 * split a parameter string into values - ie. <TS;TA;>; -> TS; TA;
	 * @param paramExpression - the expression 
	 * @return - a list of values
	 */
	private static List<String> extractParametersFromSnippetOld(String paramExpression) {
		// TODO Auto-generated method stub
		String e = paramExpression.substring( 1, paramExpression.length() - 2);
		String [] es = e.split( ";");
		return Arrays.asList(es);
	}
	
	private static List<String> extractParametersFromSnippet(String paramExpression) {
		// remove leading '<' and trailing ';>'
		String e;
		if (paramExpression.startsWith( "<") && paramExpression.endsWith(">")) { 
			e = paramExpression.substring( 1, paramExpression.length() - 2);
		}
		else {
			e = paramExpression;
		}
		int v = e.indexOf( '<');
		if (v < 0) { // no boxed types.. 
			String [] es = e.split( ";");
			return Arrays.asList(es);
		}
		else { // boxed ...
			List<String> ss = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < e.length(); i++) {
				char c = e.charAt(i);
				if (c == '<') {
					if (sb.length() > 0) {
						String s = sb.toString();
						
						ss.addAll( Arrays.asList(s.split(";")));
						sb.setLength(0); // re-init
					}
				}
				else if (c == '>') {
					if (sb.length() > 0) {
						String s = sb.toString();
						if (s.endsWith( ";")) {
							s = s.substring(0, s.length()-1);
						}
						ss.addAll( Arrays.asList(s.split(";")));
						sb.setLength(0); // re-init
					}
				}
				else {
					sb.append(c);
				}					 
			}
			return ss;
		}
		
	}
	
	

	/**
	 * @param context - the {@link ZedAnalyzerProcessContext} to use
	 */
	private void _processClassParameters(  ZedAnalyzerProcessContext context) {
		String signature = context.classData().getSignature();
		String desc = context.entity().getDesc();
		
		if (signature == null || signature.equalsIgnoreCase( desc)) 
			return;
	
		Map<String, TypeReferenceEntity> parameterization = new HashMap<>();
		// process	
		Pair<String,List<String>> pair = extractClassparameterAndInheritenceHelperData(signature);
		List<String> sigParts = pair.getSecond();
		int i = 0;
		for (String sigPart : sigParts) {
			TypeReferenceEntity tt=null;
			// extract parameter, <P>: ... if <P>:: P needs to go into map
			sigPart = sanitizeParameter( sigPart);			
			int pColon = sigPart.indexOf(':');
			String tName ="";
			if (pColon > 0) {							
				tName = sigPart.substring(0, pColon);
				char c = sigPart.charAt( pColon+1);
				if (c == 'T') {
					// reference to previously defined type in chain such as 'C::L<something>;D=TC;'
					String refKey = sigPart.substring( pColon+2, sigPart.indexOf( ';'));
					tt = parameterization.get( refKey);
				} else {
					// no L at all, then it must be a T reference 
					int pLc = sigPart.indexOf( "L::");
					int pL = sigPart.indexOf( 'L', pLc+1);
					sigPart = sigPart.substring( pL);
					if (sigPart.startsWith( ":")) {
						sigPart = sigPart.substring(1);
					}
				}
				 
			}
			else {
				tName = "" + i++;
			}
			if (tt == null) {
				sigPart = ensureStandardNotation(sigPart);
				context.pushSelfReferenceTemplateKey("T" + tName + ";");
				TypeReferenceEntity ref = context.signatures().processType(context, sigPart,ZedEntity.T);
				context.popSelfReferenceTemplateKey();
				parameterization.put( tName, ref);
			}
			else {
				parameterization.put( tName, tt);
			}
			
			
			// attach to entity 				
			if (context.entity() instanceof HasTemplateParameters) {
				((HasTemplateParameters) context.entity()).setTemplateParameters(parameterization);
				contextualizeClassparameters(context);
			}
			else {
				log.warn("template parameterization found at unexpected type [" + context.entity().getDesc() + "], ignored");
			}
			
		}	
	}
		
			
	private String sanitizeParameter(String sigPart) {
		int state = 0;
		for (int i = 0; i < sigPart.length(); i++) {
			char c = sigPart.charAt(i);
			if (c == '<') {
				state++;
			}
			else if (c == '>') {
				if (state == 1) {
					return sigPart.substring(0, i+1);
				}
				state++; 
			}
		}
		return sigPart;
	}


	@Override
	public void qualify(ZedAnalyzerContext context, ZedEntity entry) {
		
		String entryName = entry.getName();

		//System.out.println("pre-qualification check for inner classes [" + entryName + "]");
		
		ensureInnerClassSequence( context, entryName);
		

		System.out.println("qualifying / processing [" + entryName + "]");
		
		if (entry.getQualifiedFlag()) {
			log.debug("reentrancy detected on [" + entryName + "]");
			return;
		}
		
		ZedAnalyzerProcessContext proContext = zedToContextMap.get(entry);
		if (proContext == null) {
			throw new IllegalStateException( "no context found for [" + entry.getDesc() + "]");
		}
		
		if (entry instanceof ClassEntity) {			
			processClass(proContext);
			// determine parameter map 
			processClassParameters(proContext);
			contextualizeClassparameters( proContext);			
		}
		else if (entry instanceof InterfaceEntity) {
			processInterface(proContext);
			// determine parameter map
			processClassParameters(proContext);
			contextualizeClassparameters( proContext);
		}
		else if (entry instanceof EnumEntity) {
			processEnum(proContext); 			
			processClassParameters(proContext);
			contextualizeClassparameters( proContext);
		}
	 

		if (entry instanceof HasAnnotationsNature) {			
			processAnnotationNature(proContext);			
		}
		
		if (entry instanceof HasFieldsNature) {			
			processFieldNature( proContext);
		}
		if (entry instanceof HasMethodsNature) {
			processMethodNature( proContext);
		}
		
		entry.setQualifiedFlag( true);
		entry.setInnersRequireResolving(false);
		entry.setScannedFlag(true);
	}


	private void ensureInnerClassSequence(ZedAnalyzerContext context, String entryName) {
		List<String> innerNames = ensureInnerClassSequenceNames(entryName);
		if (innerNames.size() == 1)
			return;
		//System.out.println("ensuring outer classes for inner [" + entryName +"]");
		
		for (int i = 1; i < innerNames.size() ; i++) {
			String inner = innerNames.get( i);
			ZedEntity parent = null;
			for (ZedEntity entity : zedToContextMap.keySet()) {
				if (entity.getName().equalsIgnoreCase( inner)) {
					parent = entity;
					break;
				}
			}
			if (parent != null) {
				if (!parent.getQualifiedFlag()) {				
					qualify( context, parent);
				}				
			}
			else {
				log.warn("[" + inner + "] in the family of [" + entryName +"] isn't know yet, resolving");
				ZedEntity zedEntity = context.registry().acquire(context, inner, ClassEntity.T);
				if (zedEntity != null) {
					qualify(context, zedEntity);
				}
				else {
					log.error("[" + inner + "] in the family of [" + entryName +"] isn't know yet, could not resolve");
				}
			}
		}
	}
	private List<String> ensureInnerClassSequenceNames(String entryName) {
		String typeName = entryName;
		List<String> containment = new ArrayList<>();
		do {
			int i = typeName.lastIndexOf('$');
			if (i < 0) {
				containment.add( typeName);
				break;
			}
			containment.add( typeName);
			typeName = typeName.substring(0, i);			
		} while (true);
			
		return containment;
		
	}

	/**
	 * contextualizes all template parameters from the ZedEntity via its context (also outer classes)
	 * @param proContext - the {@link ZedAnalyzerProcessContext}
	 */
	private void contextualizeClassparameters(ZedAnalyzerProcessContext proContext) {
		Map<String, TypeReferenceEntity> contextualizedTemplateParameters = extractTemplateParameters(proContext);
		// add outer classes' templates
		contextualizedTemplateParameters.putAll( ensureOuterClassesParameter( proContext.entity()));
		((BasicZedAnalyzerProcessContext) proContext).setClassTemplateTypes( contextualizedTemplateParameters);
	}

	/**
	 * extracts all template parameters from an {@link ZedEntity} via its context 
	 * @param proContext
	 * @return
	 */
	private Map<String, TypeReferenceEntity> extractTemplateParameters(ZedAnalyzerProcessContext proContext) {
		Map<String, TypeReferenceEntity> templateParameters = ((HasTemplateParameters) proContext.entity()).getTemplateParameters();
		Map<String, TypeReferenceEntity> contextualizedTemplateParameters = new HashMap<>();
		for (Entry<String,TypeReferenceEntity> entry : templateParameters.entrySet()) {
			contextualizedTemplateParameters.put( entry.getKey(), entry.getValue());
		}
		return contextualizedTemplateParameters;
	}


	/**
	 * @param zed - the zed entity to ensure it gets all required template parameters 
	 * @return
	 */
	private Map<String, TypeReferenceEntity> ensureOuterClassesParameter(ZedEntity zed) {
		String entityName = zed.getName();
		List<String> outerNames = ensureInnerClassSequenceNames(entityName);
		if (outerNames.size() == 1)
			return Collections.emptyMap();
		Map<String, TypeReferenceEntity> result = new HashMap<>();
		
		for (int i = 1; i < outerNames.size(); i++) {
			String outer = outerNames.get(i);
			ZedEntity parent = null;
			for (ZedEntity entity : zedToContextMap.keySet()) {
				if (entity.getName().equalsIgnoreCase( outer)) {
					parent = entity;
					break;
				}
			}
			if (parent != null) {
				ZedAnalyzerProcessContext zedAnalyzerProcessContext = zedToContextMap.get(parent);
				result.putAll( extractTemplateParameters(zedAnalyzerProcessContext));				
			}
			
		}
		return result;
	}
	
	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private InputStream ensureInputStream( URL url) throws IOException {
		return url.openStream();
	}
	
	private static int findNextTypeDelimiterInExpression( String expr, int p) {
		int l = expr.length();
		int i = 0;
		int state = 0;		
		for (i = p; i < l; i++) {
			char c = expr.charAt(i);
			if (c == '<') {
				state++;
			}
			else if (c == '>') {
				state--;				
			}
			else if (c == ';' && state == 0) {
				break;
			}			
		}
		if (i == l) {
			return -1;
		}
		return i;
	}
	
	private static int findNextTemplateParameterDelimiterInExpression( String expr, int p) {
		int l = expr.length();
		int i = 0;
		int state = 0;		
		for (i = p; i < l; i++) {
			char c = expr.charAt(i);
			if (c == '<') {
				state++;
			}
			else if (c == '>') {
				state--;
				if (state == 0) {
					break;
				}
			}			
		}
		if (i == l) {
			return -1;
		}
		return i;
	}
	
	private static List<String> extractParameterSignatureParts( String expr) {
		if (expr.startsWith( "<")) {
			int pE = expr.lastIndexOf( ">");
			expr = expr.substring(1, pE);
		}
		List<String> result = new ArrayList<>();
		int c = findNextTypeDelimiterInExpression(expr, 0);
		int l = 0;
		while ( c > 0) {
			result.add( expr.substring(l, c+1));			
			l = c+1; 
			c = findNextTypeDelimiterInExpression(expr, l);
		}
		if (l < expr.length()) {
			result.add( expr.substring(l));
		}
		
		return result;
	}
	
	private static Pair<String, List<String>> extractClassparameterAndInheritenceHelperData( String expr) {
		// extract the part within the angle bracket's '<' & '>'
		// part within are parameter types
		// part behind (guess) is the 'inheritence helper data'
		int tpE = findNextTemplateParameterDelimiterInExpression(expr, 0);
		String paramPart;
		String ihd = null;
		if (tpE > 0) {
			paramPart = expr.substring(0, tpE+1);
			ihd = expr.substring(tpE+1);
		}
		else {
			paramPart = expr;
		}
		List<String> params = extractParameterSignatureParts(paramPart);
		 
		return Pair.of(ihd, params);		
	}
	
	public static void main(String[] args) {
		
		
		String p1 = "<TS;TA;>;";
		String p2 = "<Lcom/braintribe/gm/model/reason/Maybe<Lcom/braintribe/devrock/model/repository/RepositoryConfiguration;>;>";
		
		String p3 = "<Lcom/braintribe/devrock/model/repository/Repository;Ljava/util/function/Function<Lcom/braintribe/devrock/model/repository/Repository;Lcom/braintribe/devrock/mc/api/repository/RepositoryProbingSupport;>;>";
		
		List<String> snippet = extractParametersFromSnippet( p3);
		snippet.stream().forEach( s -> System.out.println( s));
			
		
		//String v = "T::Lcom/braintribe/devrock/test/zarathud/commons/CommonParameter;V:Lcom/braintribe/devrock/test/zarathud/six/SingleClassParameter<TT;>;";
		//String v = "<T::Lcom/braintribe/model/generic/GenericEntity;>Ljava/lang/Object;Lcom/braintribe/model/generic/reflection/EntityTypeDeprecations<TT;>;Lcom/braintribe/model/generic/reflection/CustomType;Lcom/braintribe/model/generic/reflection/EnhancableCustomType;";
		//String v = "Ljava/lang/Object;Ljava/util/function/Function<Ljava/util/Set<Lcom/braintribe/model/artifact/Exclusion;>;Lcom/braintribe/build/artifact/walk/multi/exclusion/ExclusionContainer;>;";
		/*
		String [] vs = new String []  {
				"<T::Lcom/braintribe/devrock/test/zarathud/commons/CommonParameter;V:Lcom/braintribe/devrock/test/zarathud/six/SingleClassParameter<TT;>;>Ljava/lang/Object;",				
				"<T:Ljava/lang/Object;>Lcom/braintribe/devrock/test/zarathud/seven/AbstractSevenClass<TT;>;",
		};
				
		for (String v : vs) {
			Pair<String,List<String>> parts = extractClassparameterAndInheritenceHelperData(v);
			String hp = parts.first();
			List<String> params = parts.getSecond();
			System.out.println( v);
			System.out.println( "\t" + hp);
			params.stream().forEach(p -> System.out.println( "\t" + p));
		}
		*/
		/*
		AsmAnalyzer ana = new AsmAnalyzer(null);
		List<String> sequence = ana.ensureInnerClassSequence("ScratchOne$bla$blue");
		System.out.println( sequence.stream().collect(Collectors.joining(",")));
		*/
	}
}
