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
package com.braintribe.gwt.querymodeleditor.client.queryform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.gwt.gmview.client.TemplateSupport;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CollectionManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedMap;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluation;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.query.Query;
import com.braintribe.model.template.Template;
import com.sencha.gxt.core.shared.FastMap;

public class QueryFormTemplate implements ManipulationListener, DisposableBean {

	/********************************** Variables **********************************/
	protected static final String SEARCH_TEXT_VARIABLE = "searchText";

	private GenericEntity parentEntity;
	private List<Variable> variables = null;
	private List<ValueDescriptor> valueDescriptors = null;
	private Map<String, Variable> variableCache = null;
	private Map<String, Object> valueDescriptorValues = null;

	private ModelEnvironmentDrivenGmSession persistenceSession = null;
	private ModelEnvironmentDrivenGmSession workbenchPersistenceSession = null;
	
	private List<GenericEntity> entitiesWithListener;

	private Supplier<String> userNameProvider = () -> null;
	
	private boolean useEntityReferences;
	/******************************** Getter/Setter ********************************/

	@Required
	public void setPersistenceSession(final ModelEnvironmentDrivenGmSession persistenceSession) {
		this.persistenceSession = persistenceSession;
	}

	@Configurable
	public void setWorkbenchPersistenceSession(final ModelEnvironmentDrivenGmSession workbenchPersistenceSession) {
		this.workbenchPersistenceSession = workbenchPersistenceSession;
	}
	
	@Configurable
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}
	
	/**
	 * Configures whether we should use entity references when handling entities in the {@link QueryFormTemplate}.
	 */
	public void configureUseEntityReferences(boolean useEntityReferences) {
		this.useEntityReferences = useEntityReferences;
	}

	public boolean getTemplateHasVariables() {
		return this.variables != null && this.variables.size() > 0;
	}
	
	public Variable getSearchTextVariable() {
		if (variables == null)
			return null;
		
		return variables.stream().filter(v -> v.getName().equals(SEARCH_TEXT_VARIABLE)).findAny().orElse(null);
	}
	
	public String getCurrentVariableValue(String variableName) {
		if (valueDescriptorValues == null)
			return "";
		
		return (String) valueDescriptorValues.get(variableName);
	}
	
	public void updateVariableValue(String variableName, Object newValue) {
		if (valueDescriptorValues == null)
			return;
		
		if (parentEntity != null) {
			Property property = parentEntity.entityType().findProperty(variableName);
			if (property != null)
				property.set(parentEntity, newValue);
		}
	}

	public boolean getTemplatehasValueDescriptors() {
		return this.valueDescriptors != null && this.valueDescriptors.size() > 0;
	}
	
	protected ModelEnvironmentDrivenGmSession getWorkbenchPersistenceSession() {
		return this.workbenchPersistenceSession;
	}
	
	public void clearVariables() {
		variables = null;
		valueDescriptorValues = null;
		variableCache = null;
		parentEntity = null;
	}

	public ModelPath prepareVariableWrapperEntity(Query query) {
		return prepareVariableWrapperEntity(createQueryTemplate(query));
	}

	public ModelPath prepareVariableWrapperEntity(Template queryTemplate) {
		TemplateEvaluation templateEvaluation = new TemplateEvaluation();
		templateEvaluation.setTargetSession(workbenchPersistenceSession);
		templateEvaluation.setTemplate(queryTemplate);
		
		valueDescriptors = (List<ValueDescriptor>) templateEvaluation.collectValueDescriptors(true, null);
		//@formatter:off
		variables = 
			valueDescriptors
				.stream()
				.filter(vd -> vd instanceof Variable)
				.map(vd -> (Variable) vd)
				.collect(Collectors.toList());
		//@formatter:on

		valueDescriptorValues = new FastMap<>();
		variableCache = new FastMap<>();

		// Create dynamic type for the template, create an instance of it, and set it's display info
		DynamicEntityType variableWrapperEntityType = TemplateSupport.createTypeForTemplate(queryTemplate, variables, getClass());
		GenericEntity variableWrapperEntity = workbenchPersistenceSession.create(variableWrapperEntityType);
		
		parentEntity = variableWrapperEntity;
		
		for (Variable variable : variables) {
			variableCache.put(variable.getName(), variable);
			valueDescriptorValues.put(variable.getName(), handleEntityReference(handleCollection(variable.getDefaultValue())));
		}

		for (Variable variable : variables) {
			Object value = variable.getDefaultValue();

			if (value instanceof EntityReference) {
				EntityReference entityReference = (EntityReference) value;

				EntityType<GenericEntity> variableType = GMF.getTypeReflection().getEntityType(variable.getTypeSignature());
				EntityType<GenericEntity> referenceType = GMF.getTypeReflection().getEntityType(entityReference.getTypeSignature());

				if (referenceType.isAssignableFrom(variableType))
					value = resolveEntityReference(entityReference);
			}

			variableWrapperEntityType.getProperty(variable.getName()).set(variableWrapperEntity, value);
		}

		workbenchPersistenceSession.listeners().entity(variableWrapperEntity).add(this);
		
		if (entitiesWithListener == null)
			entitiesWithListener = new ArrayList<>();
		entitiesWithListener.add(variableWrapperEntity);

		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(variableWrapperEntityType, variableWrapperEntity));

		return modelPath;
	}

	public Query evaluateQuery(final Query query, Template template) throws TemplateEvaluationException {
		if (template == null)
			template = createQueryTemplate(query);
		
		return evaluateQueryTemplate(template);
	}

	public Query evaluateQueryTemplate(final Template queryTemplate) throws TemplateEvaluationException {
		final TemplateEvaluation templateEvaluation = new TemplateEvaluation();

		// Evaluate query template
		templateEvaluation.setTemplate(queryTemplate);
		templateEvaluation.setTargetSession(workbenchPersistenceSession);
		templateEvaluation.setVariableValues(valueDescriptorValues);
		templateEvaluation.setUserNameProvider(userNameProvider);

		// Return evaluated query
		Query query = templateEvaluation.evaluateTemplateSync(false);
		
		return replaceReferences(query);
	}

	private Query replaceReferences(Query query) {
		return Query.T.clone(new StandardCloningContext(), query, StrategyOnCriterionMatch.reference);
	}
	
	/***************************** ManipulationListener ****************************/

	@Override
	public void noticeManipulation(final Manipulation manipulation) {
		if (!(manipulation instanceof PropertyManipulation))
			return;
		
		Owner owner = ((PropertyManipulation) manipulation).getOwner();
		if (!(owner instanceof LocalEntityProperty))
			return;
		
		String propertyName = ((LocalEntityProperty) owner).getPropertyName();
		Variable variable = variableCache.get(propertyName);
		if (variable == null)
			return;
		
		if (manipulation instanceof ChangeValueManipulation) {
			valueDescriptorValues.put(propertyName, handleEntityReference(((ChangeValueManipulation) manipulation).getNewValue()));
			return;
		}
		
		if (!(manipulation instanceof CollectionManipulation))
			return;
		
		Object collection = valueDescriptorValues.get(propertyName);
		
		if (collection == null) {
			GenericEntity entity = ((LocalEntityProperty) owner).getEntity();
			GenericModelType type = entity.entityType().getProperty(propertyName).getType();
			if (type instanceof ListType)
				collection = new ArrayList<>();
			else if (type instanceof SetType)
				collection = new HashSet<>();
			else
				collection = new HashMap<>();
			valueDescriptorValues.put(propertyName, collection);
		}
		
		if (manipulation instanceof AddManipulation) {
			Map<Object, Object> itemsToAdd = ((AddManipulation) manipulation).getItemsToAdd();
			if (collection instanceof List) {
				for (Map.Entry<Object, Object> entry : itemsToAdd.entrySet())
					((List<Object>) collection).add((Integer) entry.getKey(), handleEntityReference(entry.getValue()));
			} else if (collection instanceof Set)
				((Set<Object>) collection).addAll(handleEntityReference(itemsToAdd.values()));
			else if (collection instanceof Map)
				((Map<Object,Object>) collection).putAll(handleEntityReference(itemsToAdd));
		} else if (manipulation instanceof RemoveManipulation) {
			Map<Object, Object> itemsToRemove = ((RemoveManipulation) manipulation).getItemsToRemove();
			if (collection instanceof List) {
				for (Object index : itemsToRemove.keySet())
					((List<?>) collection).remove((int) index);
			} else if (collection instanceof Set) {
				for (Object object : itemsToRemove.values())
					((Set<?>) collection).remove(handleEntityReference(object));
			} else if (collection instanceof Map) {
				for (Object key : itemsToRemove.keySet())
					((Map<?, ?>) collection).remove(handleEntityReference(key));
			}
		} else {
			if (collection instanceof Collection)
				((Collection<?>) collection).clear();
			else
				((Map<?, ?>) collection).clear();
		}
	}

	/******************************** Helper Methods *******************************/

	private static Template createQueryTemplate(final Query query) {
		final Template queryTemplate = Template.T.create();

		// Prepare query template
		queryTemplate.setPrototypeTypeSignature(query.type().getTypeSignature());
		queryTemplate.setPrototype(query);

		return queryTemplate;
	}

	private GenericEntity resolveEntityReference(final EntityReference entityReference) {
		try {
			return this.persistenceSession.queryCache().entity(entityReference).require();
		} catch (final Exception e) {
			throw new RuntimeException("Entity not available.", e);
		}
	}
	
	private Object handleCollection(Object possibleCollection) {
		if (possibleCollection instanceof EnhancedList)
			return new ArrayList<>((List<?>) possibleCollection);
		
		if (possibleCollection instanceof EnhancedSet)
			return new HashSet<>((Set<?>) possibleCollection);
		
		if (possibleCollection instanceof EnhancedMap)
			return new HashMap<>((Map<?,?>) possibleCollection);
		
		return possibleCollection;
	}
	
	private Object handleEntityReference(Object possibleEntity) {
		if (!useEntityReferences)
			return possibleEntity;
		
		if (!(possibleEntity instanceof GenericEntity) || possibleEntity instanceof EntityReference)
			return possibleEntity;
		
		return ((GenericEntity) possibleEntity).reference();
	}
	
	private Set<Object> handleEntityReference(Collection<Object> possibleEntities) {
		Set<Object> collection = new LinkedHashSet<>();
		for (Object possibleEntity : possibleEntities)
			collection.add(handleEntityReference(possibleEntity));
		
		return collection;
	}
	
	private Map<Object, Object> handleEntityReference(Map<Object, Object> possibleEntities) {
		Map<Object, Object> map = new HashMap<>();
		for (Map.Entry<Object, Object> entry : possibleEntities.entrySet())
			map.put(handleEntityReference(entry.getKey()), handleEntityReference(entry.getValue()));
		
		return map;
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (variableCache != null) {
			variableCache.clear();
			variableCache = null;
		}
		
		if (valueDescriptorValues != null) {
			valueDescriptorValues.clear();
			valueDescriptorValues = null;
		}
		
		if (valueDescriptors != null) {
			valueDescriptors.clear();
			valueDescriptors = null;
		}
		
		if (variables != null) {
			variables.clear();
			variables = null;
		}
		
		if (entitiesWithListener != null) {
			for (GenericEntity entity : entitiesWithListener)
				workbenchPersistenceSession.listeners().entity(entity).remove(this);
			entitiesWithListener.clear();
			entitiesWithListener = null;
		}
	}
}
