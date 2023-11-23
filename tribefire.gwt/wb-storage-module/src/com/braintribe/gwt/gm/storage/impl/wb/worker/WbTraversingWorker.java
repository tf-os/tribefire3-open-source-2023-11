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
package com.braintribe.gwt.gm.storage.impl.wb.worker;

import java.util.Map;

import com.braintribe.gwt.gm.storage.impl.wb.WbStorageRuntimeException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingSkippingCriteria;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.engine.GMT;
import com.braintribe.model.processing.traversing.engine.api.usecase.DefaultSkipUseCase;
import com.braintribe.model.processing.traversing.engine.impl.clone.BasicClonerCustomization;
import com.braintribe.model.processing.traversing.engine.impl.clone.Cloner;
import com.braintribe.model.processing.traversing.engine.impl.skip.conditional.MpcConfigurableSkipper;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.conditions.ValueComparison;
import com.sencha.gxt.core.shared.FastMap;

public class WbTraversingWorker {
	private int variableCounter = 0;
	private Map<String, Variable> variableMap = null;

	/********************************* Traversing Methods *********************************/

	public Query createTemplateQuery(final PersistenceGmSession workbenchSession, final Query query) {
		return createTemplateQuery(workbenchSession, query, false);
	}

	public Query createTemplateQuery(final PersistenceGmSession workbenchSession, final Query query, final boolean createValueVariables) {
		this.variableCounter = 1;
		this.variableMap = new FastMap<>();

		try {
			// Create clone customization with the workbench session
			final BasicClonerCustomization clonerCustomization = new BasicClonerCustomization();
			clonerCustomization.setSession(workbenchSession);

			// Create skip configuration for the $globalId of GenericEntities
			final MpcConfigurableSkipper skipper = new MpcConfigurableSkipper();
			skipper.setSkippingCriteria(GmTraversingSkippingCriteria.skipDescendants);
			skipper.setCondition(MPC.builder().property("$globalId"));
			skipper.setSkipUseCase(DefaultSkipUseCase.INSTANCE);

			// Clone the query
			final Cloner cloner = new Cloner();
			GMT.doClone().customize(clonerCustomization).visitor(skipper).visitor(cloner).doFor(query);
			final Query templateQuery = cloner.getClonedValue();

			// Check: Create value variables?
			if (createValueVariables == true) {
				// Replace values with variables
				GMT.traverse().visitor(new GmTraversingVisitor() {
					@Override
					public void onElementEnter(final GmTraversingContext context, final TraversingModelPathElement pathElement) throws GmTraversingException {
						// Get and check current query element
						final Object currentElement = pathElement.getValue();
						if (currentElement instanceof Variable) {
							// Convert current to variable and check variable name
							final Variable variable = (Variable) currentElement;
							if (isNewVariableNameRequired(variable) == true) {
								variable.setName(getNewVariableName());
							}

							// Set empty default value to prevent bug
							if (variable.getDefaultValue() == null) {
								variable.setDefaultValue("");
							}

							// Add variable to mapping
							putVariableToMapping(variable);
						} else if (currentElement != null && (currentElement instanceof PropertyOperand) == false && (currentElement instanceof Operator) == false) {
							// Get and check parent query element
							final Object parentElement = (pathElement.getPrevious() != null ? pathElement.getPrevious().getValue() : null);
							if (parentElement instanceof ValueComparison) {
								// Convert parent to value comparison and get variable name
								final ValueComparison vc = (ValueComparison) parentElement;
								final String variableName = getNewVariableName();

								// Create variable for the value (replace value with variable)
								final Variable variable = workbenchSession.create(Variable.T);
								variable.setTypeSignature(GMF.getTypeReflection().getType(currentElement).getTypeSignature());
								variable.setDefaultValue(currentElement);
								variable.setName(variableName);

								// Add new variable to mapping
								WbTraversingWorker.this.variableMap.put(variableName, variable);

								// Replace value operand with variable
								if (vc.getLeftOperand() == currentElement) {
									// It was the left operand
									vc.setLeftOperand(variable);
								} else if (vc.getRightOperand() == currentElement) {
									// It was the right operand
									vc.setRightOperand(variable);
								} else {
									// Operand not found, throw Exception
									throw new WbStorageRuntimeException("Could not replace value with variable.");
								}
							}
						}
					}

					@Override
					public void onElementLeave(final GmTraversingContext context, final TraversingModelPathElement pathElement) throws GmTraversingException {
						// leave empty
					}
				}).doFor(templateQuery);
			}

			// Return query
			return templateQuery;
		} catch (final GmTraversingException ex) {
			// Throw traversing exception
			throw new WbStorageRuntimeException(ex.getMessage(), ex);
		}
	}

	/*********************************** Helper Methods ***********************************/

	private boolean isNewVariableNameRequired(final Variable queryVariable) {
		// Get mapped variable from query variable name
		final Variable storedVariable = this.variableMap.get(queryVariable.getName());

		// If Name is not mapped or variable is already added
		if (storedVariable == null || storedVariable == queryVariable) {
			// No new name
			return false;
		} else {
			// New name
			return true;
		}
	}

	private void putVariableToMapping(final Variable queryVariable) {
		// Get name of query variable
		final String variableName = queryVariable.getName();

		// Add name of query variable, if not in map
		if (this.variableMap.get(variableName) == null) {
			this.variableMap.put(variableName, queryVariable);
		}
	}

	private String getNewVariableName() {
		String suggestedName = null;
		Variable variable = null;

		do {
			// Create name and check if name is free
			suggestedName = "var" + this.variableCounter++;
			variable = this.variableMap.get(suggestedName);
		} while (variable != null);

		// Return free name
		return suggestedName;
	}
}
