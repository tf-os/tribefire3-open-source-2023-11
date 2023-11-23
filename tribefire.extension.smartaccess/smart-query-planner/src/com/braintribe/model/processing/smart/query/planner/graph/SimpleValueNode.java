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
package com.braintribe.model.processing.smart.query.planner.graph;

import static com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper.inverseOf;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.Map;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper;
import com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools;
import com.braintribe.model.query.Join;

/**
 * 
 */
public class SimpleValueNode extends SourceNode {

	private final Map<ConversionWrapper, Integer> valuePosition = newMap();

	protected EntitySourceNode inverseNode;
	protected String inverseProperty;

	public SimpleValueNode(Join join, GmType smartGmType, SmartQueryPlannerContext context) {

		super(join, smartGmType, context);

		/* This makes sure that getValueConversions returns set containing null, which means in
		 * DelegateQueryBuilder.buildSelectionFor(SimpleValueNode sourceNode, String alias) we will actually retrieve
		 * this node's value. */
		valuePosition.put(null, null);
	}

	public GmType getDelegateCollectionElementType() {
		return SmartQueryPlannerTools.collectionElementType(delegateCollectionGmType);
	}

	public EntitySourceNode getInverseNode() {
		return inverseNode;
	}

	public Collection<ConversionWrapper> getValueConversions() {
		return valuePosition.keySet();
	}

	@Override
	public void markNodeForSelection() {
		this.retrieveEntireNode = true;
	}

	public int acquireValuePosition(ConversionWrapper cw) {
		if (inverseNode != null) {
			return inverseNode.acquirePropertyPosition(inverseProperty, inverseOf(cw));

		} else {
			Integer result = valuePosition.get(cw);

			if (result == null) {
				result = context.allocateTuplePosition();
				valuePosition.put(cw, result);
			}

			return result;
		}
	}

	public int getValuePosition() {
		return getValuePosition(null);
	}

	public int getValuePosition(ConversionWrapper cw) {
		if (inverseNode != null) {
			return inverseNode.getSimpleDelegatePropertyPosition(inverseProperty, cw);
		}

		Integer result = valuePosition.get(cw);

		if (result == null) {
			throw new SmartQueryPlannerException("Collection '" + ((Join) source).getProperty() + "' was not assigned a position." +
					(cw == null ? "" : "[conversion: " + cw.actualConversion() + "]"));
		}

		return result;
	}

	public SmartConversion findSmartConversion() {
		/* we know that if smartJoinProperty is not null, then also explicitJoinMaster is not null */
		return smartJoinProperty != null ? joinMaster.findSmartPropertyConversion(smartJoinProperty) : null;
	}

}
