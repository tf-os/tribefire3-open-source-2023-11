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
package com.braintribe.model.processing.xmi.converter.coding.differentiator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

/**
 * compares two models to determine the differences.. 
 * @author pit
 *
 */
public class ModelDifferentiator {

	/**
	 * extract all GmTypes of a {@link GmMetaModel}
	 * @param model - the {@link GmMetaModel}
	 * @return - Map of 'type signature' to {@link GmType}
	 */
	private static Map<String, GmType> extractTypes( GmMetaModel model) {
		Map<String,GmType> result = new HashMap<>();
		model.getTypes().stream().forEach( t -> result.put( t.getTypeSignature(), t));
		return result;
	}
	
	/**
	 * extract all properties of a {@link GmEntityType}
	 * @param type - the {@link GmEntityType}
	 * @return - a Map of 'property name' to {@link GmProperty}
	 */
	private static Map<String, GmProperty> extractProperties( GmEntityType type) {
		Map<String, GmProperty> result = new HashMap<>();
		type.getProperties().stream().forEach( p -> result.put( p.getName(), p));
		return result;
	}
	
	private static Map<String, GmEnumConstant> extractConstants( GmEnumType type) {
		Map<String, GmEnumConstant> result = new HashMap<>();
		type.getConstants().stream().forEach( c -> result.put( c.getName(), c));
		return result;
	}
	
	/**
	 * differentiates a {@link GmProperty}
	 * @param property - the {@link GmProperty} that is new
	 * @param decodedProperty - the {@link GmProperty} that is old
	 */
	private static void differentiate( ModelDifferentiatorContext context, GmProperty property, GmProperty decodedProperty) {
		String signature = property.getType().getTypeSignature();
		String decodedSignature = decodedProperty.getType().getTypeSignature();
		if (!signature.equals( decodedSignature)) {
			context.setDiffering(true);
			context.getReasons().add( new DifferentiationReason( property, "type has changed from [" + decodedSignature + "] to [" + signature + "]"));
		}		
	}
	
	/**
	 * differentiates a {@link GmEntityType}
	 * @param type - the {@link GmEntityType} that is new
	 * @param decodedType - the {@link GmEntityType} that is old
	 */
	private static void differentiate( ModelDifferentiatorContext context, GmEntityType type, GmEntityType decodedType) {
		Map<String, GmProperty> properties = extractProperties(type);
		Map<String, GmProperty> decodedProperties = extractProperties(decodedType);
		Set<GmProperty> newProperties = new HashSet<>();
		Set<GmProperty> matchedProperties = new HashSet<>();
		
		for (Map.Entry<String, GmProperty> entry : properties.entrySet()) {
			GmProperty decodedProperty = decodedProperties.get( entry.getKey());
			GmProperty property = entry.getValue();
			if (decodedProperty == null) {
				newProperties.add(property);
				continue;
			}
			matchedProperties.add( decodedProperty);
			differentiate(context, property, decodedProperty);			
		}
		Set<GmProperty> removedProperties = new HashSet<>( decodedProperties.values());
		removedProperties.removeAll(matchedProperties);
		if (newProperties.size() > 0) {
			context.setDiffering(true);
			context.getReasons().add( new DifferentiationReason(type, "new properties added : " + newProperties.stream().map( p -> p.getName()).collect(Collectors.joining(","))));
		}
		if (removedProperties.size() > 0) {
			context.setDiffering(true);
			context.getReasons().add( new DifferentiationReason(type, "properties removed : " + removedProperties.stream().map( p -> p.getName()).collect(Collectors.joining(","))));
		}
		
	}
	/**
	 * differentiates a {@link GmEnumType}
	 * @param type - the {@link GmEnumType that is new
	 * @param decodedType - the {@link GmEnumType} that is already present
	 */
	private static void differentiate( ModelDifferentiatorContext context, GmEnumType type, GmEnumType decodedType) {
		Map<String, GmEnumConstant> constants = extractConstants(type);
		Map<String, GmEnumConstant> decodedConstants = extractConstants(decodedType);
		
		Set<GmEnumConstant> matchedConstants = new HashSet<>();
		Set<GmEnumConstant> newConstants = new HashSet<>();
		
		for (Map.Entry<String, GmEnumConstant> entry : constants.entrySet()) {
			GmEnumConstant decodedConstant = decodedConstants.get( entry.getKey());
			GmEnumConstant constant = entry.getValue();
			
			if (decodedConstant == null) {
				newConstants.add(constant);
				continue;
			}
			matchedConstants.add(constant);									
		}
		
		Set<GmEnumConstant> removedConstants = new HashSet<>( decodedConstants.values());
		removedConstants.removeAll( matchedConstants);
		
		if (newConstants.size() > 0) {
			context.setDiffering(true);
			context.getReasons().add( new DifferentiationReason(type, "new types added : " + newConstants.stream().map( c -> c.getName()).collect(Collectors.joining(","))));
		}
		if (removedConstants.size() > 0) {
			context.setDiffering(true);
			context.getReasons().add( new DifferentiationReason(type, "types removed : " + removedConstants.stream().map( c -> c.getName()).collect(Collectors.joining(","))));
		}
		
	}
	
	
	/**
	 * differentiates a {@link GmType}
	 * @param type - the new {@link GmType}
	 * @param decodedType - the already present {@link GmType}
	 */
	private static void differentiate( ModelDifferentiatorContext context, GmType type, GmType decodedType) {
		if (type instanceof GmEntityType) {
			if (decodedType instanceof GmEntityType) {
				differentiate( context, (GmEntityType) type, (GmEntityType) decodedType);
			}
			else {
				// changed type nature
				context.setDiffering(true);
				DifferentiationReason reason = new DifferentiationReason(type, "This type has changed is nature, it used to be GmEntityType");
				context.getReasons().add(reason);
			}
		}
		else if (type instanceof GmEnumType) {
			if (decodedType instanceof GmEnumType) {
				differentiate( context, (GmEnumType) type, (GmEnumType) decodedType);
			}
			else {
				context.setDiffering(true);
				DifferentiationReason reason = new DifferentiationReason(type, "This type has changed is nature, it used to be GmEnumyType");
				context.getReasons().add(reason);
			}
		}
	}

	/**
	 * differentiates a model 
	 * @param model - the new {@link GmMetaModel}
	 * @param decodedModel - the {@link GmMetaModel} that is already present 
	 */
	public static void differentiate( ModelDifferentiatorContext context, GmMetaModel model, GmMetaModel decodedModel) {
		Map<String, GmType> types = extractTypes(model);
		Map<String, GmType> decodedTypes = extractTypes( decodedModel);
		Set<GmType> matchedTypes = new HashSet<>();
		Set<GmType> newTypes = new HashSet<>();
		
		for (Map.Entry<String, GmType> entry : types.entrySet()) {
			GmType decodedType = decodedTypes.get(entry.getKey());
			GmType type = entry.getValue();
			if (decodedType == null) {
				// new type
				newTypes.add( type);
				continue;
			}
			// matching per signature
			matchedTypes.add(decodedType);
			differentiate( context, type, decodedType);						
		}
		
		Set<GmType> removedTypes = new HashSet<>( decodedTypes.values());
		removedTypes.removeAll( matchedTypes);
		
		if (newTypes.size() > 0) {
			context.setDiffering(true);
			context.getReasons().add( new DifferentiationReason(model, "new types added : " + newTypes.stream().map( t -> t.getTypeSignature()).collect(Collectors.joining(","))));
		}
		if (removedTypes.size() > 0) {
			context.setDiffering(true);
			context.getReasons().add( new DifferentiationReason(model, "types removed : " + removedTypes.stream().map( t -> t.getTypeSignature()).collect(Collectors.joining(","))));
		}
		
	}
}
