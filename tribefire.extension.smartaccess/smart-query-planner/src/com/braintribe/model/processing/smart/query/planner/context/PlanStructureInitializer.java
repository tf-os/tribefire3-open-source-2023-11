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

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveDelegateProperty;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveId;

/**
 * Initializes all the {@link EntitySourceNode} which we will need for the evaluation of the query. By "initializes" we
 * mean it acquires such node (i.e. making sure an instance of it exists) and it marks all the properties we need for
 * the evaluation, so these will later be retrieved from the delegate accesses (either because they are needed for the
 * selection, or some condition which cannot be delegated).
 */
class PlanStructureInitializer {

	private final SelectQuery query;
	private final SmartQueryFunctionManager functionManager;
	private final SmartQueryPlannerContext context;
	private final QueryPlanStructure planStructure;

	public static void initialize(SelectQuery query, SmartQueryFunctionManager functionManager, SmartQueryPlannerContext context) {
		new PlanStructureInitializer(query, functionManager, context).initialize();
	}

	private PlanStructureInitializer(SelectQuery query, SmartQueryFunctionManager functionManager, SmartQueryPlannerContext context) {
		this.query = query;
		this.functionManager = functionManager;
		this.context = context;
		this.planStructure = context.planStructure();
	}

	private void initialize() {
		initializeExplicitSources();
		markPropertiesForSelectClause();
		markPropertiesForOrdering();
	}

	/**
	 * Make sure we have a {@link EntitySourceNode} for every {@link Source} from the query "from" clause.
	 */
	private void initializeExplicitSources() {
		for (From from : nullSafe(query.getFroms()))
			initializeSources(from);
	}

	private void initializeSources(Source source) {
		SourceNode sourceNode = planStructure.acquireSourceNodeLeniently(source);
		if (sourceNode == null)
			// If there is not node for given source (i.e. the property is not mapped), we do not look for further nodes
			return;

		for (Source joinedSource : nullSafe(source.getJoins()))
			initializeSources(joinedSource);
	}

	// ####################################
	// ## . . . . . . SELECT . . . . . . ##
	// ####################################

	private void markPropertiesForSelectClause() {
		for (Object operand : query.getSelections())
			analyzeOperand(operand, true);
	}

	private void markPropertiesForOrdering() {
		Ordering ordering = query.getOrdering();
		if (ordering == null)
			return;

		if (ordering instanceof SimpleOrdering)
			analyzeOrdering((SimpleOrdering) ordering);

		else if (ordering instanceof CascadedOrdering)
			applyOrdering((CascadedOrdering) ordering);

		else
			throw new SmartQueryPlannerException("Unknown ordering type. Ordering: " + ordering);
	}

	private void applyOrdering(CascadedOrdering ordering) {
		for (SimpleOrdering simpleOrdering : ordering.getOrderings())
			analyzeOrdering(simpleOrdering);
	}

	private void analyzeOrdering(SimpleOrdering ordering) {
		analyzeOperand(ordering.getOrderBy());
	}

	/** Also used by {@link SmartQueryFunctionManager} */
	void analyzeOperand(Object operand) {
		analyzeOperand(operand, false);
	}

	private void analyzeOperand(Object operand, boolean ignoreUnmapped) {
		if (!(operand instanceof Operand) || context.isEvaluationExclude(operand)
				|| (ignoreUnmapped && context.isUnmappedSourceRelatedOperand((Operand) operand)))
			return;

		if (operand instanceof PropertyOperand) {
			PropertyOperand propertyOperand = (PropertyOperand) operand;
			markProperty(propertyOperand.getSource(), propertyOperand.getPropertyName());

		} else if (operand instanceof JoinFunction) {
			JoinFunction joinFunction = (JoinFunction) operand;
			Join join = joinFunction.getJoin();

			planStructure.acquireSourceNode(join).markJoinFunction();

		} else if (operand instanceof AggregateFunction) {
			throw new UnsupportedOperationException("Method 'SelectionStructureAnalyzer.analyzeOperand' is not implemented for this case!");

		} else if (operand instanceof QueryFunction) {
			if (operand instanceof ResolveId) {
				ResolveId ri = (ResolveId) operand;
				planStructure.<EntitySourceNode> acquireSourceNode(ri.getSource()).markEntityId();

			} else if (operand instanceof ResolveDelegateProperty) {
				ResolveDelegateProperty rdp = (ResolveDelegateProperty) operand;
				planStructure.<EntitySourceNode> acquireSourceNode(rdp.getSource()).markDelegatePropertyForSelection(rdp.getDelegateProperty());

			} else {
				QueryFunction queryFunction = (QueryFunction) operand;
				functionManager.noticeQueryFunction(queryFunction, this);
			}

		} else if (operand instanceof Source) {
			markProperty((Source) operand, null);

		} else {
			throw new RuntimeQueryPlannerException("Unsupported operand: " + operand + " of type: " + operand.getClass().getName());
		}
	}

	private void markProperty(Source source, String propertyName) {
		SourceNode sourceNode = planStructure.acquireSourceNode(source);

		if (propertyName == null) {
			sourceNode.markNodeForSelection();
			return;
		}

		/**
		 * We know the property has a simple type, because all other properties were forced into being joined.
		 */
		((EntitySourceNode) sourceNode).markSimpleSmartPropertyForSelection(propertyName);
	}

}
