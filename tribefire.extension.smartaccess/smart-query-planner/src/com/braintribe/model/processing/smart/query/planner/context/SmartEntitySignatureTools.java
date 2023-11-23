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

import static com.braintribe.model.processing.query.planner.builder.ValueBuilder.staticValue;
import static com.braintribe.model.processing.smart.query.planner.core.builder.SmartValueBuilder.compositeDiscriminatorBasedSignature;
import static com.braintribe.model.processing.smart.query.planner.core.builder.SmartValueBuilder.simpleDiscriminatorBasedSignature;
import static com.braintribe.model.processing.smart.query.planner.core.builder.SmartValueBuilder.smartEntitySignature;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Map;

import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchy;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchyNode;
import com.braintribe.model.queryplan.value.Value;

/**
 * @author peter.gazdik
 */
public class SmartEntitySignatureTools {

	public static Value smartEntitySignatureFor(EntitySourceNode sourceNode) {
		if (sourceNode.isPolymorphicHierarchy())
			return signatureForPolymorphicHierarchy(sourceNode);
		else
			return signatureForIsomorphicHierarchy(sourceNode);
	}

	private static Value signatureForPolymorphicHierarchy(EntitySourceNode sourceNode) {
		DiscriminatedHierarchy dh = sourceNode.getDiscriminatorHierarchy();

		List<DiscriminatedHierarchyNode> nodes = dh.getNodes();
		if (nodes.size() == 1)
			return staticValue(nodes.get(0).smartType.getTypeSignature());

		if (dh.isSingleDiscriminatorProperty()) {
			int discriminatorPosition = sourceNode.getSimpleDelegatePropertyPosition(dh.getSingleDiscriminatorProperty().getName());
			return simpleDiscriminatorBasedSignature(discriminatorPosition, dh);

		} else {
			List<Integer> discriminatorPositions = newList();
			for (GmProperty p : dh.getCompositeDiscriminatorProperties())
				discriminatorPositions.add(sourceNode.getSimpleDelegatePropertyPosition(p.getName()));

			return compositeDiscriminatorBasedSignature(discriminatorPositions, dh);
		}
	}

	private static Value signatureForIsomorphicHierarchy(EntitySourceNode sourceNode) {
		Map<String, String> typeRules = sourceNode.acquireTypeRules();

		if (typeRules.isEmpty())
			return staticValue(sourceNode.getSmartGmType().getTypeSignature());
		else
			return smartEntitySignature(sourceNode, typeRules);
	}

}
