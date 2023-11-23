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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerProcessContext;
import com.braintribe.devrock.zed.api.core.CachingZedRegistry;
import com.braintribe.devrock.zed.api.core.ZedSignatureAnalyzer;
import com.braintribe.devrock.zed.commons.Commons;
import com.braintribe.devrock.zed.scan.asm.MethodSignature;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.zarathud.model.data.ArrayEntity;
import com.braintribe.zarathud.model.data.ParameterEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;

public class BasicSignatureAnalyzer implements ZedSignatureAnalyzer {
	
	@Override
	public MethodSignature extractMethodSignature(ZedAnalyzerProcessContext context, String oSignature) {
		
		if (oSignature == null) {
			return null;
		}
				
		String signature = oSignature;
		MethodSignature methodSignature = new MethodSignature();
		context.pushTypeReferences( new HashMap<>());
		
		// template definition 
		if (signature.startsWith( "<")) {
			int endGen = signature.indexOf( "(");
			String genParam = signature.substring(0, endGen);
			// fills the pushed type references (due to internal relations inside function)
			extractGenericParameters( context, genParam);	
			signature = signature.substring( endGen);			
		}
		else {
		}
		
		// arguments 
		try {
			if (signature.startsWith( "(")) {
				int index = signature.indexOf(')');
				String argumentPart = signature.substring(1, index);
				String remainder = signature.substring( index+1);			 
				String returnTypeDesc = remainder;
				
				// return type handling 
						
				TypeReferenceEntity tf = processType(context, returnTypeDesc, ZedEntity.T);
				methodSignature.returnType = tf;				
				
				methodSignature.argumentTypes = new ArrayList<>();

				List<String> argumentDescs = extractMethodParameters( argumentPart);
				for (String argumentDesc : argumentDescs) { 
					TypeReferenceEntity ref = processType(context, argumentDesc, ZedEntity.T);
					methodSignature.argumentTypes.add(ref);
				}																
			}
		} 
		finally {		
			context.popTypeReferences();
		}
		return methodSignature;
	}
	
	/**
	 * @param context
	 * @param typeDesc
	 * @return
	 */
	public TypeReferenceEntity processType(ZedAnalyzerProcessContext context, String typeDesc, EntityType<? extends ZedEntity> expectation) {		
		TypeReferenceEntity tf = getTemplateDefinedType(context, typeDesc);
		if (tf == null) {
			tf = analyzeReference(context, typeDesc, expectation);
		}
		return tf;
	}
	
	
	/**
	 * extract all arguments to a method (i.e. split them into a list)
	 * @param argumentPart
	 * @return
	 */
	private static List<String> extractMethodParameters(String argumentPart) {
		if (argumentPart.length() == 0)
			return Collections.emptyList();
		List<String> result = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		int stateGenerics = 0;
		int stateType = 0;
		
		for (int i = 0; i < argumentPart.length(); i++) {
			char c = argumentPart.charAt(i);
			sb.append(c);			
			
			if (c == '<') {
				stateGenerics++;		
			}
			else if (c == '>'){
				stateGenerics--;								
			}			
			else if (c == ';') {
				if (stateGenerics == 0) {					
					result.add( sb.toString());
					sb.delete(0, sb.length());
					stateType = 0;
				}			
			}
			else if (c == 'L' || c == 'T') {
					stateType = 1;			
			}
			else if (stateType == 0 && isSimpleType( c)) {									
					result.add( "" + c);
					sb.delete(0, sb.length());
			}
		}
							
		if (sb.length() > 0)
			result.add( sb.toString());
		return result;
	}
	
	
	/**
	 * check if it's a simple type (as expected in the setup)
	 * @param c
	 * @return
	 */
	private static boolean isSimpleType(char c) {
		String s = "" + c;
		if (CachingZedRegistry.simpleTypeDescs.containsKey( s)) {
			return true;
		}
		return false;
	}
	/**
	 * build a map of template type parameter name to type 
	 * @param context - the {@link ZedAnalyzerProcessContext}
	 * @param signature - the signature to extract the map from 
	 * @return - a {@link Map} of template-type-placeholder name to {@link TypeReferenceEntity} 
	 */
	private Map<String, TypeReferenceEntity> extractGenericParameters( ZedAnalyzerProcessContext context, String orgSignature) {
		String signature = orgSignature;
		if (signature.startsWith( "<") && signature.endsWith( ">")) {
			signature = signature.substring(1, signature.length() -1);
		}
		StringBuilder builder = new StringBuilder();
		int generics = 0;
		List<String> params = new ArrayList<>();
		for (int i = 0; i < signature.length(); i++) {
			char c = signature.charAt(i);
			builder.append(c);
			
			if (c == '<') {
				generics++;
			}
			else if (c == '>') {
				generics--; 
			}
			else if (c == ';' && generics == 0) {
				params.add( builder.toString());
				builder = new StringBuilder();
			}			
		}
		if (builder.length() > 0) {
			params.add( builder.toString());
		}
					
		Map<String, TypeReferenceEntity> result = context.templateTypes();

		for (int i = params.size()-1; i >= 0; i--) {
			String param = params.get(i);
			if (param.startsWith( "<")) {
				param = param.substring(1);
			}
			if (param.endsWith( ">")) {
				param = param.substring(0, param.length() -1);
			}
			
			String keyName = param.substring( 0, param.indexOf( ':'));			
			String typeName = param.substring( keyName.length()+1); 
			typeName = sanitizeTypeName(typeName);			
			TypeReferenceEntity ttype = createParameter(context, keyName, typeName);		
			result.put( "T"+keyName+";", ttype);
		}
		return result;
	}
	private String sanitizeTypeName(String typeName) {
		if (	typeName.startsWith( ":") ||
				typeName.startsWith( "+") || 
				typeName.startsWith( "-") ||
				typeName.startsWith( "*")
			) {
			typeName = typeName.substring(1);				                                                    
		}
		return typeName;
	}

	private TypeReferenceEntity createParameter(ZedAnalyzerProcessContext context, String keyName, String typeName) {
		TypeReferenceEntity ref = Commons.create(context.context(), TypeReferenceEntity.T);		
		ParameterEntity pE = Commons.create(context.context(), ParameterEntity.T);
		pE.setName(keyName);
		pE.setScannedFlag(true);
		pE.getArtifacts().add( context.context().artifacts().runtimeArtifact(context.context()));
		ref.setReferencedType(pE);
		if (typeName.length() > 0) {
			context.pushSelfReferenceTemplateKey("T" + keyName + ";");
			ref.getParameterization().add(analyzeReference(context, typeName, ZedEntity.T));
			context.popSelfReferenceTemplateKey();
		}

		return ref;
	}
	
	/**
	 * extracts all types from the passed desc
	 * @param desc - the desc
	 * @return - a {@link List} of single type desc
	 */
	public static List<String> extractMultipleSignatures( String desc) {
		List<String> result = new ArrayList<>();
		StringBuilder builder	 = new StringBuilder();
		int generics = 0;
		for (int i = 0; i < desc.length(); i++) {
			char c = desc.charAt(i);
			if (c == '<') {
				generics++;
			}
			if (c == '>') {
				generics--;
			}
			builder.append( c);
			if (c == ';' && generics == 0) {
				// closing
				// simple, toplevel argument desc
				String argumentDesc = builder.toString();			
				result.add( argumentDesc);
				builder = new StringBuilder();
			}
		} 
		return result;	
	}
	
	/**
	 * looks up a type either from the template types or class parameterization template types
	 * @param context
	 * @param desc
	 * @return
	 */
	private TypeReferenceEntity getTemplateDefinedType( ZedAnalyzerProcessContext context, String desc) {
		String key = desc;
		if (key.endsWith(";")) {
			key = key.substring(0, key.length() -1);
		}
		if (context.templateTypes().containsKey(key)) {
			return context.templateTypes().get(key);
		}
		if (context.classTemplateTypes().containsKey(key)) {
			return context.classTemplateTypes().get(key);
		}
		return null;
		
	}
		
	@Override
	public TypeReferenceEntity analyzeReference(ZedAnalyzerProcessContext context, String desc, EntityType<? extends ZedEntity> expectation) {
		
		TypeReferenceEntity result = getTemplateDefinedType(context, desc);
		if (result != null)
			return result;
				
				
		result = TypeReferenceEntity.T.create();
		
		if (desc.equalsIgnoreCase("TSELF;")) {
			ZedEntity itself = context.entity();
			result.setReferencedType(itself);
			return result;
		}
	
		
		// simple type
		ZedAnalyzerContext analyzerContext = context.context();
		analyzerContext.setCurrentlyScannedResource( context.resourceURL());
		if (!desc.contains( ">") && !desc.contains( "[")) {
			desc = redirectToInnerType(desc);
			
			ZedEntity analyzedType = analyzerContext.registry().acquire( analyzerContext, desc, expectation);
			result.setReferencedType(analyzedType);			
			return result;
		}
		// array 
		if (desc.startsWith( "[")) {
			ArrayEntity collection = Commons.create( analyzerContext, ArrayEntity.T);			
			collection.setDesc(desc);
			collection.getArtifacts().add( analyzerContext.artifacts().runtimeArtifact(analyzerContext));
			collection.setIsRuntime(true);
			String arrayElementDesc;
			if (desc.startsWith( "[[")) {
				arrayElementDesc = desc.substring(2);				
				collection.setName("1D-array");
			}
			else {
				arrayElementDesc = desc.substring(1);
				collection.setName("2D-array");
			}
			// 
			TypeReferenceEntity elementTypeRef = analyzeReference(context, arrayElementDesc, ZedEntity.T);
			result.setReferencedType(collection);
			result.setParameterization( Collections.singletonList( elementTypeRef));
			return result;			
		}
		Pair<String,String> processedDesc =  determineContainerDesc( desc);
		String containerDesc = processedDesc.first;
		containerDesc = redirectToInnerType( containerDesc);
		ZedEntity containerType = analyzerContext.registry().acquire(analyzerContext, containerDesc, ParameterEntity.T);
		result.setReferencedType(containerType);

		// contained types 					
		String containedDesc = processedDesc.second;
		List<String> containedDescs = extractMultipleSignatures(containedDesc);
		for (String d : containedDescs) {			
				d = sanitizeTypeName(d);
				// check context for self key 
				if (!d.equalsIgnoreCase( context.selfReferenceTemplateKey())) {
					d = redirectToInnerType(d);
					TypeReferenceEntity parameterRef = analyzeReference( context, d, ZedEntity.T);
					result.getParameterization().add(parameterRef);
					
				}
				else {
					TypeReferenceEntity tref = TypeReferenceEntity.T.create();
					tref.setReferencedType(containerType);
					tref.getParameterization().add( result);
					result.getParameterization().add( tref);
				}
		}
		return result;
	}
	
	/**
	 * if the container type itself is an outer/inner chain, it must be dealt with, 
	 * otherwise not
	 * @param desc
	 * @return
	 */
	private Pair<String,String> determineContainerDesc(String signature) {
		int insideTemplate = 0;
		boolean requiresRedirect = false;
		for (int i = 0 ; i < signature.length(); i++) {
			char c = signature.charAt(i);
			
			if (c == '<') {
				insideTemplate++;
				continue;
			}
			if (c == '>') {
				insideTemplate--;
				continue;
			}
			if (c == '.' && insideTemplate == 0) {
				requiresRedirect = true;
				break;
			}						
		}
		String desc = signature;
		if (requiresRedirect) {
			desc = redirectToInnerType( desc);
		}
		// type with parameter
		int startGenChar = desc.indexOf( '<');
		int endGenChar = desc.lastIndexOf( '>');
		if (startGenChar < 0 || endGenChar < 0) {
			return Pair.of( desc, "");
		}

		// container type 
		String containerDesc = desc.substring(0,  startGenChar) + ";";
		String containedDesc = desc.substring( startGenChar + 1, endGenChar);
		return Pair.of( containerDesc, containedDesc);
	}

	/**
	 * build an address to innermost type that was passed 
	 * @param desc 
	 * @return
	 */
	private String redirectToInnerType(String desc) {
		String [] tks = desc.split( "\\.");
		int l = tks.length;
		if (l == 1)
			return desc;
		
		StringBuilder sb  = new StringBuilder();
		for (int i = 0; i < l; i++) {
			String tk = tks[i];
			if (sb.length() > 0) {
				sb.append( "$");
			}
			if (i < l-1) {
				tk = tk.substring( 0, tk.indexOf( '<'));
			}			
			sb.append( tk);
		}		
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
		//String t = "Ljava/lang/Object;I[Ljava/lang/Object;I";
		String [] ts = new String [] {
				//"La<b;>;Lc<d;>;",
				//"<E:Ljava/lang/Enum<TE;>;>(Ljava/lang/String;)TE;",
				"Ljava/util/List<Lcom/braintribe/devrock/test/zarathud/four/FourParameter;>;Ljava/util/List<Lcom/braintribe/devrock/test/zarathud/two/TwoClass;>;"
		};
		for (String t : ts) {
			List<String> ps = extractMethodParameters( t);
			ps.stream().forEach( p -> System.out.println(p));
		}
	}
}
