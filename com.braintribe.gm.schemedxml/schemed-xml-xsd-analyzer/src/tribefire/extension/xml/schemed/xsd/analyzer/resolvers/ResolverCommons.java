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
package tribefire.extension.xml.schemed.xsd.analyzer.resolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;

import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.model.xsd.All;
import tribefire.extension.xml.schemed.model.xsd.Attribute;
import tribefire.extension.xml.schemed.model.xsd.AttributeGroup;
import tribefire.extension.xml.schemed.model.xsd.Choice;
import tribefire.extension.xml.schemed.model.xsd.ComplexType;
import tribefire.extension.xml.schemed.model.xsd.Element;
import tribefire.extension.xml.schemed.model.xsd.Group;
import tribefire.extension.xml.schemed.model.xsd.SchemaEntity;
import tribefire.extension.xml.schemed.model.xsd.Sequence;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasAll;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasAttributeGroups;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasAttributes;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasChoice;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasChoices;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasElements;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasGroup;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasGroups;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasSequence;
import tribefire.extension.xml.schemed.model.xsd.archetypes.HasSequences;
import tribefire.extension.xml.schemed.xsd.analyzer.registry.context.SchemaMappingContext;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure.AttributeGroupResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure.AttributeResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure.ChoiceResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure.ElementResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure.GroupResolver;
import tribefire.extension.xml.schemed.xsd.analyzer.resolvers.structure.SequenceResolver;


/**
 * a collection of helper functions for the directory structural resolvers 
 * @author pit
 *
 */
public class ResolverCommons {

	/**
	 * process a single {@link Sequence}
	 * @param context - the {@link SchemaMappingContext}
	 * @param hasSequence - a {@link SchemaEntity} implementing {@link HasSequence}
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processSequence(SchemaMappingContext context, HasSequence hasSequence, boolean multiple) {
		Sequence sequence = hasSequence.getSequence();
		if (sequence == null)
			return Collections.emptyList();
		return SequenceResolver.resolve( context, sequence, multiple);
	}
	
	/**
	 * process a list of {@link Sequence}s 
	 * @param context - the {@link SchemaMappingContext}
	 * @param hasSequences - a {@link SchemaEntity} implementing {@link HasSequences}
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processSequences(SchemaMappingContext context, HasSequences hasSequences, boolean multiple) {
		List<Sequence> sequences = hasSequences.getSequences();
		if (sequences == null || sequences.isEmpty())
			return Collections.emptyList();
		List<GmProperty> properties = new ArrayList<>();
		for (Sequence sequence : sequences) {
			properties.addAll( SequenceResolver.resolve(context, sequence, multiple));
		}
		return properties;
	}

	/**
	 * process a single {@link Choice}
	 * @param context -  the {@link SchemaMappingContext}
	 * @param hasChoice - a {@link SchemaEntity} implementing {@link HasChoice} 
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processChoice(SchemaMappingContext context, HasChoice hasChoice, boolean multiple) {
		Choice choice = hasChoice.getChoice();
		if (choice == null)
			return Collections.emptyList();
		return ChoiceResolver.resolve( context, choice, multiple);
	}
	/**
	 * process a list of {@link Choice}s
	 * @param context -  the {@link SchemaMappingContext}
	 * @param hasChoices - a {@link SchemaEntity} implementing {@link HasChoices} 
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processChoices(SchemaMappingContext context, HasChoices hasGroups, boolean multiple) {
		List<Choice> choices = hasGroups.getChoices();
		if (choices == null || choices.isEmpty())
			return Collections.emptyList();
		List<GmProperty> properties = new ArrayList<>();
		for (Choice choice : choices) {
			properties.addAll(ChoiceResolver.resolve( context, choice, multiple));
		}
		return properties;
	}
	/**
	 * process a single {@link Group}
	 * @param context -  the {@link SchemaMappingContext}
	 * @param hasGroup - a {@link SchemaEntity} implementing {@link HasGroup} 
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processGroup(SchemaMappingContext context, HasGroup hasGroup, boolean multiple) {
		Group group = hasGroup.getGroup();
		if (group == null)
			return Collections.emptyList();	
		return GroupResolver.resolve( context, group, multiple);	
	}
	/**
	 * process a list of {@link Group}s
	 * @param context -  the {@link SchemaMappingContext}
	 * @param hasGroups - a {@link SchemaEntity} implementing {@link HasGroups} 
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processGroups(SchemaMappingContext context, HasGroups hasGroups, boolean multiple) {
		List<Group> groups = hasGroups.getGroups();
		if (groups == null || groups.isEmpty())
			return Collections.emptyList();
		List<GmProperty> properties = new ArrayList<>();
		for (Group group : groups) {
			properties.addAll(GroupResolver.resolve( context, group, multiple));
		}
		return properties;
	}
	
	/**
	 * process a single {@link All}
	 * @param context -  the {@link SchemaMappingContext}
	 * @param hasAll - a {@link SchemaEntity} implementing {@link HasAll} 
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processAll(SchemaMappingContext context, HasAll hasAll, boolean multiple) {
		All all = hasAll.getAll();
		if (all == null)
			return Collections.emptyList();
		return processElements(context, all, multiple);
	}
	

	/**
	 * process a {@link List} of {@link Attribute} into a {@link List} of {@link GmProperty}
	 * @param context - the {@link SchemaMappingContext}
	 * @param hasAttributes - an instance of {@link HasAttributes}
	 * @return - a {@link List} of the converted {@link GmProperty}
	 */
	public static List<GmProperty> processAttributes(SchemaMappingContext context, HasAttributes hasAttributes) {
		List<Attribute> attributes = hasAttributes.getAttributes();
		if (attributes == null || attributes.isEmpty()) {
			return Collections.emptyList();
		}
		List<GmProperty> properties = new ArrayList<>();
		for (Attribute attribute : attributes) {		
			GmProperty property = AttributeResolver.resolve( context, attribute);
			properties.add( property);		
		}
		return properties;	
	}
	/**
	 * process multiple {@link AttributeGroup}s
	 * @param context -  the {@link SchemaMappingContext}
	 * @param hasAttributeGroups - a {@link SchemaEntity} implementing {@link HasAttributeGroups} 
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processAttributeGroups(SchemaMappingContext context, HasAttributeGroups hasAttributeGroups) {
		List<AttributeGroup> attributeGroups = hasAttributeGroups.getAttributeGroups();
		if (attributeGroups == null || attributeGroups.isEmpty()) {
			return Collections.emptyList();
		}
		List<GmProperty> properties = new ArrayList<>();
		for (AttributeGroup group : attributeGroups) {
			properties.addAll( AttributeGroupResolver.resolve( context, group));
		}
		return properties;
	}
	/**
	 * process multiple {@link Element}s
	 * @param context -  the {@link SchemaMappingContext}
	 * @param hasElements - a {@link SchemaEntity} implementing {@link HasElements} 
	 * @return - a {@link List} of resolved {@link GmProperty}
	 */
	public static List<GmProperty> processElements( SchemaMappingContext context, HasElements hasElements, boolean multiple) {
		List<Element> elements = hasElements.getElements();
		if (elements == null || elements.isEmpty()) {
			return Collections.emptyList();
		}
		List<GmProperty> properties = new ArrayList<>();
		for (Element element : elements) {
			GmProperty property = ElementResolver.resolve( context, element, multiple);
			properties.add( property);
		}
		return properties;
	}
	
	/**
	 * attach the {@link List} of {@link GmProperty} passed to the {@link GmEntityType}
	 * @param gmEntityType - the {@link GmEntityType} to attach to 
	 * @param properties - the list of {@link GmProperty} to attach
	 */
	public static void attachPropertiesToGmEntityType( SchemaMappingContext context, GmEntityType gmEntityType, List<GmProperty> properties) {
		if (properties == null || properties.isEmpty())
			return;
		Set<String> names = new HashSet<>();
		for (GmProperty property : gmEntityType.getProperties()) {
			names.add( property.getName());
		}
		for (GmProperty property : properties) {
			PropertyMappingMetaData metadata = context.mappingContext.metaDataMapper.acquireMetaData(property);
			String propertyName = property.getName();
			
			while ( names.size() > 0 && names.contains(propertyName)){				
				if (propertyName.endsWith( "Attr") == false && metadata.getIsAttribute()) {
					propertyName = propertyName + "Attr";
				}
				else {
					propertyName = "_" + propertyName;
				}
				property.setName(propertyName);
			} 
			names.add(propertyName);
			
			gmEntityType.getProperties().add(property);
			property.setDeclaringType(gmEntityType);
			property.setGlobalId( JavaTypeAnalysis.propertyGlobalId( gmEntityType.getTypeSignature(), property.getName()));
			
			// 
		}
	}
	
	
	/**
	 * combine multiple instances of {@link Stream}s to a single {@link List}
	 * @param streams - the {@link Stream} of {@link GmProperty}
	 * @return - a {@link List} of the combined {@link GmProperty}
	 */
	@SafeVarargs
	public static List<GmProperty> combine( Stream<GmProperty> ...streams) {
		Stream<GmProperty> mainStream = null;
		for (Stream<GmProperty> stream : streams) {
			if (mainStream == null) {
				mainStream = stream;			
			}
			else {
				mainStream = Stream.concat(mainStream, stream);
			}			
		}
		return mainStream.collect( Collectors.toList());
	}

	public static String findParentName(Stack<SchemaEntity> currentEntityStack) {
		Stack<SchemaEntity> entityStack = currentEntityStack;
		// if nothing's in there or we have only the current entity in it (a top level element for instance)		
		int size = entityStack.size();
		if (entityStack.isEmpty() || size == 1) {
			return null;
		}
		for (int i = size-1; i >= 0; i--) {
			SchemaEntity entity = entityStack.get(i);
			if (entity instanceof ComplexType) {
				ComplexType complexType = (ComplexType) entity;
				String name = complexType.getName();
				return name;
			}
		}
		
		return null;
	}
	
	
	public static GmType findParentType(SchemaMappingContext context) {
		Stack<SchemaEntity> entityStack = context.currentEntityStack;
		// if nothing's in there or we have only the current entity in it (a top level element for instance)		
		int size = entityStack.size();
		if (entityStack.isEmpty() || size == 1) {
			return null;
		}
		for (int i = size-1; i >= 0; i--) {
			SchemaEntity entity = entityStack.get(i);
			if (entity instanceof ComplexType) {				
				return context.mappingContext.typeMapper.lookupType( (ComplexType) entity);
			}
		}
		
		return null;
	}
	
}
