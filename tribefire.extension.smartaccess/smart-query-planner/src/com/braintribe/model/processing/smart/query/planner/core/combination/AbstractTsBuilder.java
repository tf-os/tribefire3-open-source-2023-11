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
package com.braintribe.model.processing.smart.query.planner.core.combination;

import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.hasSubType;
import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.isLinearCollectionInstance;
import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.newCompatibleLinearCollection;

import java.util.Collection;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EmUseCase;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.EntitySignature;

/**
 * @author peter.gazdik
 */
abstract class AbstractTsBuilder {

	protected final SmartQueryPlannerContext context;
	protected final ModelExpert modelExpert;

	protected AbstractTsBuilder(SmartQueryPlannerContext context) {
		this.context = context;
		this.modelExpert = context.modelExpert();

	}


	protected Object convertToDelegateIfNeeded(Object constantOperand, Object otherOperand) {
		if (constantOperand instanceof PersistentEntityReference)
			return convertToDelegateReference((PersistentEntityReference) constantOperand, otherOperand);

		if (isLinearCollectionInstance(constantOperand))
			return convertCollectionIfNeeded((Collection<?>) constantOperand, otherOperand);

		SmartConversion conversion = context.findConversion(otherOperand);
		if (conversion != null)
			return context.convertToDelegateValue(constantOperand, conversion);

		if (otherOperand instanceof EntitySignature) {
			Object nestedSignatureOperand = ((EntitySignature) otherOperand).getOperand();
			if (representsUnmappedTypeNode(nestedSignatureOperand))
				return constantOperand;

			// TODO RE-UNDERSTAND: check + explain what is going on here - I s
			EntitySourceNode sn = (EntitySourceNode) findNodeForOperand(nestedSignatureOperand);
			GmEntityType operandEntityType = context.modelExpert().resolveSmartEntityType((String) constantOperand);
			EntityMapping em = context.modelExpert().resolveEntityMapping(operandEntityType, sn.getAccess(), null);

			return em != null ? em.getDelegateEntityType().getTypeSignature() : null;
		}

		return constantOperand;
	}

	/**
	 * The use-case is relevant for comparisons. In that case, the otherOperand is probably source or property, which means there is node
	 * that represents it. This node has to tell us the use-case, so we know how to convert the signature. In normal use-case, the signature
	 * will be turned to the delegate one. In smartReference it remains what it was.
	 */
	private PersistentEntityReference convertToDelegateReference(PersistentEntityReference reference, Object otherOperand) {
		if (representsUnmappedTypeNode(otherOperand))
			return reference;
		else
			return context.localizeReference(reference);
	}

	private Object convertCollectionIfNeeded(Collection<?> collection, Object otherOperand) {
		Collection<Object> result = newCompatibleLinearCollection(collection);

		for (Object o: collection)
			result.add(convertToDelegateIfNeeded(o, otherOperand));

		return result;
	}

	private boolean representsUnmappedTypeNode(Object operand) {
		SourceNode sn = findNodeForOperand(operand);
		EmUseCase useCase = sn instanceof EntitySourceNode ? ((EntitySourceNode) sn).getEmUseCase() : null;

		return useCase != null;
	}

	private SourceNode findNodeForOperand(Object operand) {
		QueryPlanStructure planStructure = context.planStructure();
		
		if (operand instanceof Source)
			return planStructure.getSourceNode((Source) operand);

		if (operand instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) operand;

			return po.getPropertyName() == null ? planStructure.getSourceNode(po.getSource()) : null;
		}

		return null;
	}


	/**
	 * In general, we only need to select the signature from the delegate if our smart-type has sub-types, otherwise we know the signature.
	 * However, due to a special case of a query like <tt>SELECT entitySignature(p) FROM SmartPerson p</tt>, i.e. when only signature is
	 * selected (i.e. selectedDelegateProperties is empty), we select the signature so that our delegate query does have at least this one
	 * select. Otherwise, the query would be: <tt>FROM DelegatePreson p</tt>, which would return instances of <tt>DelegatePerson</tt> back,
	 * and this EntityType might not even exist on our side...
	 */
	protected boolean needsTypeSignature(EntitySourceNode node) {
		return node.isSignatureSelected() && (hasSubType(node, context) || node.selectedDelegateProperties().isEmpty());
	}

}
