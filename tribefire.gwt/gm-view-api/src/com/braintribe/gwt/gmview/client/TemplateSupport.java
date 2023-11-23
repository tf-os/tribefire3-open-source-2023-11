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
package com.braintribe.gwt.gmview.client;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.StringTools.isEmpty;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.proxy.DynamicProperty;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.processing.template.preprocessing.TemplatePreprocessing;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.TemplateBase;
import com.braintribe.model.template.meta.DynamicPropertyMetaDataAssignment;
import com.braintribe.model.template.meta.DynamicTypeMetaDataAssignment;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.model.workbench.TemplateBasedAction;

public class TemplateSupport {
	
	public static Set<String> knownCallerClasses = new HashSet<>();
	
	/**
	 * Creates a new {@link DynamicEntityType} for the given parameters.
	 */
	public static DynamicEntityType createTypeForTemplate(Template template, Collection<Variable> variables, Class<?> callerClass) {
		String typeSignature;
		if (isEmpty(template.getTechnicalName()))
			typeSignature = TemplateBase.T.getTypeSignature();
		else
			typeSignature = template.getTechnicalName().replace(" ", "_");
		String callerClassName = callerClass.getSimpleName();
		typeSignature += "_" + callerClassName + "_" + template.getId();
		knownCallerClasses.add(callerClassName + "_");
		
		DynamicEntityType variableWrapperEntityType = new DynamicEntityType(typeSignature);

		addDefaultNameMetaData(variableWrapperEntityType, template);		
		addDefaultDescriptionMetaData(variableWrapperEntityType, template);
		addConfiguredMetaData(variableWrapperEntityType, template);
		
		Hidden hidden = Hidden.T.create();
		addPropertyMetaData(variableWrapperEntityType, GenericEntity.id, hidden);
		addPropertyMetaData(variableWrapperEntityType, GenericEntity.globalId, hidden);
		addPropertyMetaData(variableWrapperEntityType, GenericEntity.partition, hidden);

		for (Variable variable : variables) {
			String variableTypeSignature = variable.getTypeSignature();
			if (variableTypeSignature == null)
				variableTypeSignature = "string";
			
			DynamicProperty property = 
					variableWrapperEntityType
						.addProperty(
							variable.getName(), 
							GMF.getTypeReflection().getType(variableTypeSignature));
			
			addDefaultNameMetaData(property, variable);		
			addDefaultDescriptionMetaData(property, variable);
			addConfiguredMetaData(property, variable, template);
			
		}
		
		return variableWrapperEntityType;
	}
	
	/**
	 * Prepares a new {@link ManipulationListener}, which handle manipulations done in the {@link Template} variables.
	 */
	public static ManipulationListener getManipulationListener(TemplateEvaluationContext templateEvaluationContext, Map<String, Variable> variableCache) {
		return manipulation -> {
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
				templateEvaluationContext.getTemplatePreprocessing().getVariableValues().put(variable.getName(),
						((ChangeValueManipulation) manipulation).getNewValue());
				return;
			}

			GenericModelType type = GMF.getTypeReflection().getType(variable.getTypeSignature());
			if (!type.isCollection())
				return;
			
			Collection<Object> newValues = (Collection<Object>) ((CollectionType) type).createPlain();
			
			if (manipulation instanceof ClearCollectionManipulation) {
				templateEvaluationContext.getTemplatePreprocessing().getVariableValues().put(variable.getName(), newValues);
				return;
			}
			
			Object oldValues = templateEvaluationContext.getTemplatePreprocessing().getVariableValues().get(variable.getName());
			if (oldValues instanceof Collection)
				newValues.addAll((Collection<Object>) oldValues);
			templateEvaluationContext.getTemplatePreprocessing().getVariableValues().put(variable.getName(), newValues);
			
			if (manipulation instanceof AddManipulation) {
				AddManipulation addManipulation = (AddManipulation) manipulation;
				addManipulation.getItemsToAdd().values().forEach(item -> newValues.add(item));
				return;
			}
			
			if (manipulation instanceof RemoveManipulation) {
				RemoveManipulation removeManipulation = (RemoveManipulation) manipulation;
				removeManipulation.getItemsToRemove().values().forEach(item -> newValues.remove(item));
				return;
			}
		};
	}
	
	@SuppressWarnings("deprecation")
	private static void addDefaultNameMetaData(DynamicProperty property, Variable variable) {
		Name md = Name.T.create(); 
		md.setName(variable.getLocalizedName());
		md.setConflictPriority(-1d);
		addPropertyMetaData(property, md);
	}

	@SuppressWarnings("deprecation")
	private static void addDefaultDescriptionMetaData(DynamicProperty property, Variable variable) {
		Description md = Description.T.create(); 
		md.setDescription(variable.getDescription());
		md.setConflictPriority(-1d);
		addPropertyMetaData(property, md);
	}

	private static void addConfiguredMetaData(DynamicProperty dynamicProperty, Variable variable, Template template ) {
		Set<TemplateMetaData> metaData = template.getMetaData();
		for (TemplateMetaData md : metaData) {
			if (md instanceof DynamicPropertyMetaDataAssignment) {
				DynamicPropertyMetaDataAssignment mdAssignment = (DynamicPropertyMetaDataAssignment) md;
				if (mdAssignment.getVariable() == variable)
					dynamicProperty.getMetaData().addAll(mdAssignment.getMetaData());
			}
		}

	}
	
	private static void addConfiguredMetaData(DynamicEntityType dynamicType, Template template) {
		Set<TemplateMetaData> metaData = template.getMetaData();
		for (TemplateMetaData md : metaData) {
			if (md instanceof DynamicTypeMetaDataAssignment) {
				DynamicTypeMetaDataAssignment mdAssignment = (DynamicTypeMetaDataAssignment) md;
				dynamicType.getMetaData().addAll(mdAssignment.getMetaData());
			}
		}
	}

	private static void addDefaultNameMetaData(DynamicEntityType dynamicType, Template template) {
		Name md = Name.T.create(); 
		md.setName(template.getName());
		md.setConflictPriority(-1d);
		addTypeMetaData(dynamicType, md);
	}
	private static void addDefaultDescriptionMetaData(DynamicEntityType dynamicType, Template template) {
		Description md = Description.T.create(); 
		md.setDescription(template.getDescription());
		md.setConflictPriority(-1d);
		addTypeMetaData(dynamicType, md);
	}

	private static void addPropertyMetaData(DynamicEntityType variableWrapperEntityType, String propertyName, MetaData metaData) {
		DynamicProperty property = (DynamicProperty) variableWrapperEntityType.getProperty(propertyName);
		addPropertyMetaData(property, metaData);
	}
	
	private static void addPropertyMetaData(DynamicProperty property, MetaData metaData) {
		property.getMetaData().add(metaData);
	}

	private static void addTypeMetaData(DynamicEntityType dynamicType, MetaData metaData) {
		dynamicType.getMetaData().add(metaData);
	}

	public static List<Variable> getVisibleVariables(Template template) {
		List<Variable> variables = TemplatePreprocessing.findVariables(template);

		return extractVisibleVariables(variables, template);
	}

	public static List<Variable> extractVisibleVariables(Collection<Variable> variables, Template template) {
		List<Variable> result = newList(variables);

		result.removeAll(findVariablesForMd(template, Hidden.T));

		return result;
	}

	public static List<Variable> extractMandatoryVariables(Collection<Variable> variables, Template template) {
		List<Variable> result = newList(variables);

		result.retainAll(findVariablesForMd(template, Mandatory.T));

		return result;
	}

	private static Set<Variable> findVariablesForMd(Template template, EntityType<?> mdType) {
		return template.getMetaData().stream() //
				.filter(md -> md instanceof DynamicPropertyMetaDataAssignment) //
				.map(md -> (DynamicPropertyMetaDataAssignment) md) //
				.filter(md -> containsMdOfType(md, mdType)) //
				.map(DynamicPropertyMetaDataAssignment::getVariable) //
				.collect(Collectors.toSet());
	}

	private static boolean containsMdOfType(DynamicPropertyMetaDataAssignment mda, EntityType<?> mdType) {
		return mda.getMetaData().stream().anyMatch(mdType::isInstance);
	}

	public static Collection<Variable> findVariables(TemplateBasedAction templateAction) {
		return TemplatePreprocessing.findVariables(templateAction.getTemplate());
	}

}
