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
package tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmProperty;

import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.model.xsd.Any;
import tribefire.extension.xml.schemed.model.xsd.Choice;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.Group;
import tribefire.extension.xml.schemed.model.xsd.Sequence;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.ResolverCommons;

/**
 * a {@link Sequence} is part of a type definition, it can be repeated in a {@link Sequence} itself. 
 * A {@link Sequence} is actually a proper type (as it can occur multiple times and is sorted within itself.
 * Therefore, it multiple or required to be a proper type (i.e. not be transparent), it can either take over the 
 * type definition (turn into the complex type it occurs in, yet if within a sequence, it must be turned into a virtual
 * property (such as the value property) with a unique name (would be sufficient to be unique within the structure).<br/>
 * if called by a {@link ComplexType}'s expert, the virtual type may be remapped into the actual type, but this is not decided here.
 *   
 * @author pit
 *
 */
public class SequenceResolver {
	private static Logger log = Logger.getLogger(SequenceResolver.class);
	
	public static List<GmProperty> resolve(SchemaMappingContext context, Sequence sequence, boolean overrideMultiple) {
		context.currentEntityStack.push( sequence);
		
		try {
			int maxOccurs = sequence.getMaxOccurs();
			boolean multiple = (maxOccurs < 0 || maxOccurs > 1);
			int indexWithinSequence = 0;
		
			List<GmProperty> propertiesFromElements = new ArrayList<>();
			List<GmProperty> propertiesFromSequences = new ArrayList<>();
			List<GmProperty> propertiesFromChoices = new ArrayList<>();
			List<GmProperty> propertiesFromGroups = new ArrayList<>();
			List<GmProperty> propertiesFromAny = new ArrayList<>();
				
			for (GenericEntity entity : sequence.getNamedItemsSequence()) {
				
				if (entity instanceof Element) {
					// element -> property 			
					GmProperty property = ElementResolver.resolve(context, (Element) entity, multiple || overrideMultiple);
					// attach sequence number to metadata 
					mapPropertyIndexInSequence(context, property, indexWithinSequence);
					propertiesFromElements.add(property);
				}
				else if (entity instanceof Sequence) {	
					// sequence -> properties
					List<GmProperty> properties = SequenceResolver.resolve(context, (Sequence) entity, multiple || overrideMultiple);
					// attach sequence number to metadata
					for (GmProperty property : properties) {
						mapPropertyIndexInSequence(context, property, indexWithinSequence);
					}
					propertiesFromSequences.addAll( properties);										
				}
				else if (entity instanceof Group) {
					// group -> properties, contents of the group
					List<GmProperty> properties = GroupResolver.resolve(context,  (Group) entity, multiple || overrideMultiple);
					// attach sequence number to metadata
					for (GmProperty property : properties) {
						mapPropertyIndexInSequence(context, property, indexWithinSequence);
					}
					propertiesFromGroups.addAll( properties);
				}
				else if (entity instanceof Choice) {
					// choice -> properties, contents of the choice : always a virtual type 
					List<GmProperty> properties = ChoiceResolver.resolve(context, (Choice) entity, multiple || overrideMultiple);
					// attach sequence number to metadata
					for (GmProperty property : properties) {
						mapPropertyIndexInSequence(context, property, indexWithinSequence);
					}
					propertiesFromChoices.addAll( properties);				
				}
				else if (entity instanceof Any) {
					// any -> anything goes
					GmProperty property = AnyResolver.resolve( context, (Any) entity, multiple || overrideMultiple);
					mapPropertyIndexInSequence(context, property, indexWithinSequence);
					propertiesFromAny.add(property);
					log.warn( "structural type [" + entity.entityType().getTypeSignature() + "] is not supported at this point in [" + context.print() + "]");
				}
				else {
					log.warn("structural type [" + entity.entityType().getTypeSignature() + "] is not supported at this point in [" + context.print() + "]");
					continue;
				}
				// increment sequence number
				indexWithinSequence++;			
			}
			
			return ResolverCommons.combine( propertiesFromSequences.stream(), propertiesFromChoices.stream(), propertiesFromGroups.stream(), propertiesFromElements.stream(), propertiesFromAny.stream());
			
			/*
			
			// if not required to create a special type (not multiple, not required by settings), just return the collected properties)
			if (!multiple && context.mappingContext.sequenceMappingMode != MappingMode.structured) {			
				return ResolverCommons.combine( propertiesFromSequences.stream(), propertiesFromChoices.stream(), propertiesFromGroups.stream(), propertiesFromElements.stream(), propertiesFromAny.stream());
			}
			
										
			// 
			// NOW as the decision to create a virtual type has been taken, it might be that we can collapse the type:
			// but only if a single property from the sequences or a single property of the choices has been collected (XOR) 
			// 			
			GmProperty propertyToPropagate;
			if (propertiesFromElements.isEmpty() && propertiesFromGroups.isEmpty()) {
				if (propertiesFromSequences.size() == 1 && propertiesFromChoices.isEmpty()) {
					propertyToPropagate = propertiesFromSequences.get(0);
					return Collections.singletonList( propertyToPropagate);
				}
				else if (propertiesFromChoices.size() == 1 && propertiesFromSequences.isEmpty()) {
					propertyToPropagate = propertiesFromChoices.get(0);
					return Collections.singletonList( propertyToPropagate);
				}					
			}
						
			// otherwise, do create a special type for the sequence 
			QPath qpath = context.qpathGenerator.generateQPathForSchemaEntity(sequence.getDeclaringSchema());
			String name = context.mappingContext.nameMapper.generateJavaCompatibleTypeNameForVirtualSequenceType();
			name = AnalyzerCommons.assertNonCollidingTypeName( context, name);
			String propertyName = context.mappingContext.nameMapper.generateJavaCompatiblePropertyName( name);
			GmProperty finalProperty = null;

			// otherwise, we must create a single type for this sequence
			List<GmProperty> properties = ResolverCommons.combine( propertiesFromSequences.stream(), propertiesFromChoices.stream(), propertiesFromGroups.stream(), propertiesFromElements.stream(), propertiesFromAny.stream());
			if (!multiple)  {
				GmEntityType gmEntityType = context.mappingContext.typeMapper.generateGmEntityType( qpath, null, name);				
				for (GmProperty property : properties) {
					gmEntityType.getProperties().add(property);
					property.setDeclaringType(gmEntityType);
					property.setGlobalId( JavaTypeAnalysis.propertyGlobalId( gmEntityType.getTypeSignature(), property.getName()));
				}			
				finalProperty = context.mappingContext.typeMapper.generateGmProperty(propertyName);
				finalProperty.setType(gmEntityType);
				PropertyMappingMetaData propertyMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(finalProperty);
				propertyMappingMetaData.setIsVirtual(true);				
			}
			else {				
				GmEntityType gmEntityType = context.mappingContext.typeMapper.generateGmEntityType(qpath, null, name);				
				for (GmProperty property : properties) {
					gmEntityType.getProperties().add(property);
					property.setDeclaringType(gmEntityType);
					property.setGlobalId( JavaTypeAnalysis.propertyGlobalId( gmEntityType.getTypeSignature(), property.getName()));
				}
				String collectionPropertyName = context.mappingContext.nameMapper.generateCollectionName(propertyName);
				finalProperty = context.mappingContext.typeMapper.generateGmProperty( collectionPropertyName);					
				// TODO : possible override as GmSetType  
				GmCollectionType gmCollectionType = context.mappingContext.typeMapper.acquireCollectionType( gmEntityType);				
				finalProperty.setType(gmCollectionType);			
				PropertyMappingMetaData propertyMappingMetaData = context.mappingContext.metaDataMapper.acquireMetaData(finalProperty);
				propertyMappingMetaData.setIsVirtual(true);
				propertyMappingMetaData.setIsMultiple( true);
			}	
			return Collections.singletonList( finalProperty);
			*/
		}
	
		finally {
			context.currentEntityStack.pop();
		}		
	}
	private static PropertyMappingMetaData mapPropertyIndexInSequence(SchemaMappingContext context, GmProperty gmProperty, int sequenceIndex) {
		PropertyMappingMetaData metaData = context.mappingContext.metaDataMapper.acquireMetaData(gmProperty);
		metaData.getIndex().add(0, sequenceIndex);
		return metaData;
	}


}
