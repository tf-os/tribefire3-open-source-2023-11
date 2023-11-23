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
package com.braintribe.model.processing.smart.query.planner.context;

import static com.braintribe.model.processing.smart.query.planner.context.SmartEntitySignatureTools.smartEntitySignatureFor;
import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.isScalarOrId;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collections;
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.structure.EntityHierarchyNode;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.functions.AssembleEntity;
import com.braintribe.model.smartqueryplan.functions.PropertyMappingNode;

/**
 * 
 */
class AssembleEntityFunctionBuilder {

	private final Map<EntitySourceNode, AssembleEntity> assembleEntities;
	private final SmartOperandConverter smartOperandConverter;

	public AssembleEntityFunctionBuilder(SmartOperandConverter smartOperandConverter) {
		this.smartOperandConverter = smartOperandConverter;
		this.assembleEntities = newMap();
	}

	public AssembleEntity build(EntitySourceNode sourceNode) {
		AssembleEntity result = assembleEntities.get(sourceNode);

		if (result == null) {
			result = buildHelper(sourceNode);
			assembleEntities.put(sourceNode, result);
		}

		return result;
	}

	private AssembleEntity buildHelper(EntitySourceNode sourceNode) {
		Map<String, PropertyMappingNode> map = newMap();

		EntityHierarchyNode rootNode = sourceNode.resolveHierarchyRootedAtThis();
		buildMappingNodes(rootNode, null, map, sourceNode);

		return build(sourceNode, map);
	}

	private void buildMappingNodes(EntityHierarchyNode hierarchyNode, PropertyMappingNode parentNode, Map<String, PropertyMappingNode> map,
			EntitySourceNode rootSourceNode) {

		Map<String, Value> mappings = newMap();

		EntitySourceNode matchingSourceNode = findMatchingSourceNode(hierarchyNode, rootSourceNode);

		for (Property property: hierarchyNode.getAdditionalProperties()) {
			String propertyName = property.getName();
			GenericModelType propertyType = property.getType();

			if (!matchingSourceNode.isSmartPropertyMapped(propertyName))
				continue;

			if (isScalarOrId(property)) {
				mappings.put(propertyName, smartOperandConverter.convertSmartProperty(matchingSourceNode, propertyName));

			} else if (propertyType.isEntity()) {
				EntitySourceNode jn = matchingSourceNode.getEntityJoin(propertyName);
				if (jn != null && jn.isNodeMarkedForSelection())
					mappings.put(propertyName, ValueBuilder.queryFunction(build(jn), Collections.<Object, Value> emptyMap()));
			}
		}

		PropertyMappingNode mappingNode = newMappingNode(parentNode, mappings);

		map.put(hierarchyNode.getGmEntityType().getTypeSignature(), mappingNode);

		for (EntityHierarchyNode subNode: hierarchyNode.getSubNodes())
			buildMappingNodes(subNode, mappingNode, map, rootSourceNode);
	}

	private PropertyMappingNode newMappingNode(PropertyMappingNode parentNode, Map<String, Value> mappings) {
		PropertyMappingNode mappingNode = PropertyMappingNode.T.createPlain();

		mappingNode.setPropertyMappings(mappings);
		if (parentNode != null)
			mappingNode.setSuperTypeNode(parentNode);

		return mappingNode;
	}

	private EntitySourceNode findMatchingSourceNode(EntityHierarchyNode hierarchyNode, EntitySourceNode rootSourceNode) {
		EntityType<?> entityType = hierarchyNode.getEntityType();

		if (entityType == rootSourceNode.getSmartEntityType())
			return rootSourceNode;

		return rootSourceNode.getSubTypeNode(entityType.getTypeSignature());
	}

	private AssembleEntity build(EntitySourceNode sourceNode, Map<String, PropertyMappingNode> mappings) {
		GmEntityType smartType = sourceNode.getSmartGmType();

		AssembleEntity result = AssembleEntity.T.createPlain();
		result.setEntitySignature(smartType.getTypeSignature());
		result.setSmartEntitySignature(smartEntitySignatureFor(sourceNode));
		result.setSignatureToPropertyMappingNode(mappings);

		return result;
	}

}
