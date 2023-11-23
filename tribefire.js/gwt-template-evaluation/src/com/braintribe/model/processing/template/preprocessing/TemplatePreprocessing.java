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
package com.braintribe.model.processing.template.preprocessing;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.DeferredExecutor;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.vde.clone.async.DeferredExecutorAspect;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.aspects.RootModelPathAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SelectedModelPathsAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SessionAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.UserNameAspect;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.DynamicPropertyMetaDataAssignment;
import com.braintribe.model.template.meta.TemplateMetaData;

public class TemplatePreprocessing {

	private List<ModelPath> modelPaths;
	private ModelPath rootModelPath;
	private Supplier<String> userNameProvider = () -> null;
	private List<Variable> variables;
	private Map<String, Object> variableValues;
	private Boolean valuesSatisfiedFromModelPath;
	private DeferredExecutor deferredExecutor = DeferredExecutor.gwtDeferredExecutor();
	private final Map<String, Priority> priorityMap = new HashMap<>();
	private PersistenceGmSession gmSession;
	private Map<String, Object> configuredVariableValues;

	@Required
	public void setTemplate(Template template) {
		fillPriorityMap(template);
		loadVariables(template);
	}

	private void fillPriorityMap(Template template) {
		Set<TemplateMetaData> metaData = template.getMetaData();
		for (TemplateMetaData md : metaData) {
			if (!(md instanceof DynamicPropertyMetaDataAssignment))
				continue;

			DynamicPropertyMetaDataAssignment mdAssignment = (DynamicPropertyMetaDataAssignment) md;
			String name = mdAssignment.getVariable().getName();

			mdAssignment.getMetaData().stream() //
					.filter(pmd -> pmd instanceof Priority) //
					.forEach(pmd -> priorityMap.put(name, (Priority) pmd));

		}
	}

	private void loadVariables(Template template) {
		variables = findVariables(template);
		Collections.sort(variables, this::compareVariables);
	}

	public void setModelPaths(List<ModelPath> modelPaths) {
		this.modelPaths = modelPaths;
	}

	public void setRootModelPath(ModelPath rootModelPath) {
		this.rootModelPath = rootModelPath;
	}

	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}

	public void setDeferredExecutor(DeferredExecutor deferredExecutor) {
		this.deferredExecutor = deferredExecutor;
	}

	// This is also needed for service request evaluation
	public void setSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Variable values can be configured externally and may be set here.
	 */
	public void setConfiguredVariableValues(Map<String, Object> configuredVariableValues) {
		this.configuredVariableValues = configuredVariableValues;
	}

	public Future<TemplatePreprocessing> run() {
		valuesSatisfiedFromModelPath = true;
		variableValues = new HashMap<>();

		List<ModelPath> reversedModelPaths = resolveReversedModelPaths();

		List<ValueDescriptor> vdsToEvaluate = newList(variables.size());
		for (Variable variable : variables)
			vdsToEvaluate.add(noteVariableValueAndMaybeReturnVdToEvaluate(variable, reversedModelPaths));

		return resolveVd(vdsToEvaluate) //
				.andThenMap(values -> {
					for (int i = 0; i < values.size(); i++) {
						if (vdsToEvaluate.get(i) != null)
							markVariableValue(variables.get(i).getName(), values.get(i));
					}

					return this;
				});
	}

	private List<ModelPath> resolveReversedModelPaths() {
		if (isEmpty(modelPaths))
			return null;

		List<ModelPath> reversedModelPaths = newList(modelPaths);
		Collections.reverse(reversedModelPaths);

		return reversedModelPaths;
	}

	private ValueDescriptor noteVariableValueAndMaybeReturnVdToEvaluate(Variable variable, List<ModelPath> reversedModelPaths) {
		String varName = variable.getName();

		boolean foundVariable = false;

		if (reversedModelPaths != null) {
			GenericModelType varType = GMF.getTypeReflection().getType(variable.getTypeSignature());
			for (ModelPath modelPath : reversedModelPaths) {
				ModelPathElement modelPathElement = modelPath.last();
				GenericModelType modelPathType = modelPathElement.getType();
				if (modelPathType instanceof CollectionType)
					modelPathType = ((CollectionType) modelPathType).getCollectionElementType();

				if (varType.isAssignableFrom(modelPathType)) {
					Object value = modelPathElement.getValue();
					/* The lines bellow cause problems when for example the entity is wrapped with another VD if (template.getPrototype() instanceof
					 * Query && value instanceof GenericEntity) { GenericEntity entity = (GenericEntity) value; value = entity.reference(); } */

					if (!valueAlreadyUsed(value)) {
						variableValues.put(varName, value);
						foundVariable = true;
						break;
					}
				}

				if (!(varType instanceof LinearCollectionType))
					continue;

				// TODO support collection coercion

				CollectionType varCollectionType = (CollectionType) varType;
				GenericModelType variableElementType = varCollectionType.getCollectionElementType();
				if (!variableElementType.isAssignableFrom(modelPathType))
					continue;

				Collection<Object> collection = null;
				Object currentValue = variableValues.get(varName);
				if (currentValue == null || !(currentValue instanceof Collection)) {
					currentValue = varCollectionType.createPlain();
					variableValues.put(varName, currentValue);
				}
				collection = (Collection<Object>) currentValue;

				Object value = modelPathElement.getValue();
				if (!valueAlreadyUsed(value)) {
					collection.add(value);
					foundVariable = true;
				}
			}
		}

		if (foundVariable)
			return null;

		Object value = configuredVariableValues != null ? configuredVariableValues.get(varName) : null;
		if (value == null)
			value = variable.getDefaultValue();
		if (value instanceof ValueDescriptor)
			return (ValueDescriptor) value;

		markVariableValue(varName, value);
		return null;
	}

	private void markVariableValue(String varName, Object value) {
		variableValues.put(varName, value);
		if (value == null)
			valuesSatisfiedFromModelPath = false;
	}

	public Map<String, Object> getVariableValues() {
		return variableValues;
	}

	private boolean valueAlreadyUsed(Object value) {
		if (variableValues.containsValue(value))
			return true;

		// TODO what does this mean? Why only check the first collection among variable values?
		for (Object valueDescriptorValue : variableValues.values()) {
			if (valueDescriptorValue instanceof Collection)
				return ((Collection<?>) valueDescriptorValue).contains(value);
		}

		return false;
	}

	public boolean getValuesSatisfiedFromModelPath() {
		return valuesSatisfiedFromModelPath;
	}

	public void setValuesSatisfiedFromModelPath(boolean valuesSatisfiedFromModelPath) {
		this.valuesSatisfiedFromModelPath = valuesSatisfiedFromModelPath;
	}

	private Future<List<Object>> resolveVd(List<ValueDescriptor> vds) {
		return Future.fromAsyncCallbackConsumer(asyncCallback -> VDE.evaluate() //
				.withEvaluationMode(VdeEvaluationMode.Preliminary) //
				.with(UserNameAspect.class, userNameProvider) //
				.with(SelectedModelPathsAspect.class, modelPaths) //
				.with(RootModelPathAspect.class, rootModelPath) //
				.with(DeferredExecutorAspect.class, deferredExecutor) //
				.with(SessionAspect.class, gmSession) //
				.forValue(vds, asyncCallback));
	}

	private int compareVariables(Variable o1, Variable o2) {
		String v1Name = o1.getName();
		String v2Name = o2.getName();

		Priority priority1 = priorityMap.get(v1Name);
		Priority priority2 = priorityMap.get(v2Name);

		Double p1 = priority1 != null ? priority1.getPriority() : Double.MIN_VALUE;
		Double p2 = priority2 != null ? priority2.getPriority() : Double.MIN_VALUE;

		int priorityComparison = p2.compareTo(p1);
		if (priorityComparison == 0)
			return v1Name.compareToIgnoreCase(v2Name);

		return priorityComparison;
	}

	public static List<Variable> findVariables(Template template) {
		List<Variable> result = newList();

		EntityVisitor vdsCollector = EntityVisitor.onVisitEntity((entity, criterion, traversingContext) -> {
			if (entity instanceof Variable)
				result.add((Variable) entity);
		});

		Template.T.traverse(template, null, vdsCollector);

		return result;
	}

}
