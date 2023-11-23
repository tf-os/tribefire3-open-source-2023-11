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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.processing.valuedescriptor.api.ValueDescriptorResolver;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.aspects.RootModelPathAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SelectedModelPathsAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.UserNameAspect;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.DynamicPropertyMetaDataAssignment;
import com.braintribe.model.template.meta.TemplateMetaData;

public class TemplatePreprocessing {
	
	private Template template;
	private List<ModelPath> modelPaths;
	private ModelPath rootModelPath;
	private Supplier<String> userNameProvider = ()->null;
	private List<ValueDescriptor> valueDescriptors;
	private Map<Class<? extends ValueDescriptor>, ValueDescriptorResolver<? extends ValueDescriptor, ?>> valueDescriptorResolverMap;
	private Map<String, Object> valueDescriptorValues;
	private Boolean valuesSatisfiedFromModelPath;
	private Comparator<ValueDescriptor> comparator;
	private Map<String, Priority> priorityMap = new HashMap<>();
	
	public void setTemplate(Template template) {
		this.template = template;
		
		Set<TemplateMetaData> metaData = template.getMetaData();
		for (TemplateMetaData md : metaData) {
			if (!(md instanceof DynamicPropertyMetaDataAssignment))
				continue;
			
			DynamicPropertyMetaDataAssignment mdAssignment = (DynamicPropertyMetaDataAssignment) md;
			String name = mdAssignment.getVariable().getName();
			
			mdAssignment.getMetaData().stream().filter(pmd -> pmd instanceof Priority).forEach(pmd -> priorityMap.put(name, (Priority) pmd));
		}
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
	
	
	/**
	 * Please remove any call to this. It is no longer needed since VDE is taking care of the resolvers directly now.
	 */
	@Deprecated
	public void setValueDescriptorResolverMap(Map<Class<? extends ValueDescriptor>, ValueDescriptorResolver<? extends ValueDescriptor, ?>> valueDescriptorResolverMap) {
		this.valueDescriptorResolverMap = valueDescriptorResolverMap;
	}
	
	public List<ValueDescriptor> getValueDescriptors() {
		if (valueDescriptors != null)
			return valueDescriptors;
		
		valueDescriptors = collectValueDescriptors();
		return valueDescriptors;
	}
		
	public Map<String, Object> getValueDescriptorValues() {
		if (valueDescriptorValues != null)
			return valueDescriptorValues;
		
		valuesSatisfiedFromModelPath = true;
		valueDescriptorValues = new HashMap<>();
		
		List<ModelPath> reversedModelPaths = null;
		if (modelPaths != null && !modelPaths.isEmpty()) {
			reversedModelPaths = new ArrayList<>(modelPaths);
			Collections.reverse(reversedModelPaths);
		}
		
		for (ValueDescriptor valueDescriptor : getValueDescriptors()) {
			if (!(valueDescriptor instanceof Variable))
				continue;
			
			Variable variable = (Variable) valueDescriptor;
			boolean foundVariable = false;
			
			if (reversedModelPaths != null) {
				GenericModelType variableType = GMF.getTypeReflection().getType(variable.getTypeSignature());
				for (ModelPath modelPath : reversedModelPaths) {
					ModelPathElement modelPathElement = modelPath.last();
					GenericModelType modelPathType = modelPathElement.getType();
					if (modelPathType instanceof CollectionType)
						modelPathType = ((CollectionType) modelPathType).getCollectionElementType();
					if (variableType.isAssignableFrom(modelPathType)) {
						Object value = modelPathElement.getValue();
						/*The lines bellow cause problems when for example the entity is wrapped with another VD
						if (template.getPrototype() instanceof Query && value instanceof GenericEntity) {	
							GenericEntity entity = (GenericEntity) value;
							value = entity.reference();
						}*/
						
						if (!valueAlreadyUsed(value)) {
							valueDescriptorValues.put(variable.getName(), value);
							foundVariable = true;
							break;
						}
					}
					
					if (!(variableType instanceof CollectionType))
						continue;
					
					GenericModelType variableElementType = ((CollectionType)variableType).getCollectionElementType();
					if (!variableElementType.isAssignableFrom(modelPathType))
						continue;
					
					Collection<Object> valueCollection = null;
					Object valueCollectionCandidate = valueDescriptorValues.get(variable.getName());
					if (valueCollectionCandidate == null || !(valueCollectionCandidate instanceof Collection<?>)) {
						valueCollectionCandidate = ((CollectionType) variableType).createPlain();
						valueDescriptorValues.put(variable.getName(), valueCollectionCandidate);
						valueCollection = (Collection<Object>) valueCollectionCandidate;
					}
					valueCollection = (Collection<Object>) valueCollectionCandidate;
					
					Object value = modelPathElement.getValue();
					if (!valueAlreadyUsed(value)) {
						valueCollection.add(value);
						foundVariable = true;
					}
				}
			}
			
			if (!foundVariable) {
				Object value = variable.getDefaultValue();
				if (value instanceof ValueDescriptor)
					value = resolveValue((ValueDescriptor) value);
				valueDescriptorValues.put(variable.getName(), value);
				if (value == null)
					valuesSatisfiedFromModelPath = false;
			}
		}
		
		return valueDescriptorValues;
	}
	
	private boolean valueAlreadyUsed(Object value) {
		if (valueDescriptorValues.containsValue(value))
			return true;
		
		for (Object valueDescriptorValue : valueDescriptorValues.values()) {
			if (valueDescriptorValue instanceof Collection)
				return ((Collection<?>) valueDescriptorValue).contains(value);
		}
		
		return false;
	}
	
	public boolean getValuesSatisfiedFromModelPath() {
		if (valuesSatisfiedFromModelPath == null)
			getValueDescriptorValues();
		return valuesSatisfiedFromModelPath;
	}

	public void setValuesSatisfiedFromModelPath(boolean valuesSatisfiedFromModelPath) {
		this.valuesSatisfiedFromModelPath = valuesSatisfiedFromModelPath;
	}

	private List<ValueDescriptor> collectValueDescriptors() {
		List<ValueDescriptor> collectedValueDescriptors = new ArrayList<>();
		EntityVisitor valueDescriptorCollector = new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion,TraversingContext traversingContext) {
				if (entity instanceof ValueDescriptor)
					collectedValueDescriptors.add((ValueDescriptor) entity);
			}
		};
		Template.T.traverse(template,null,valueDescriptorCollector);
		
		Collections.sort(collectedValueDescriptors, getValueDescriptorComparator());
		
		return collectedValueDescriptors;
	}
	
	private <D extends ValueDescriptor> Object resolveValue(D valueDescriptor) {
		if (valueDescriptorResolverMap != null) {
			ValueDescriptorResolver<ValueDescriptor, ?> valueDescriptorResolver = (ValueDescriptorResolver<ValueDescriptor, ?>) valueDescriptorResolverMap
					.get(valueDescriptor.getClass());
			if (valueDescriptorResolver != null) {
				Object value = valueDescriptorResolver.resolve(null, valueDescriptor);
				if (value instanceof ValueDescriptor )
					return resolveValue((D) value);
				return value;
			}
		}
		return evaluate(valueDescriptor);
	}
	
	private Object evaluate(Object object) {
		//@formatter:off
		return VDE.evaluate()
				.withRegistry(VDE.registryBuilder().defaultSetup())
				.withEvaluationMode(VdeEvaluationMode.Preliminary)
				.with(UserNameAspect.class, this.userNameProvider)
				.with(SelectedModelPathsAspect.class, this.modelPaths)
				.with(RootModelPathAspect.class, this.rootModelPath)
				.forValue(object);
		//@formatter:on
	}

	private Comparator<ValueDescriptor> getValueDescriptorComparator() {
		if (comparator != null)
			return comparator;
		
		comparator = (o1, o2) -> {
			if (!(o1 instanceof Variable) || !(o2 instanceof Variable))
				return 0;
			
			String v1Name = ((Variable) o1).getName();
			String v2Name = ((Variable) o2).getName();
			
			Priority priority1 = priorityMap.get(v1Name);
			Priority priority2 = priorityMap.get(v2Name);
			
			Double p1 = priority1 != null ? priority1.getPriority() : Double.MIN_VALUE;
			Double p2 = priority2 != null ? priority2.getPriority() : Double.MIN_VALUE;
			
			int priorityComparison = p2.compareTo(p1);
			if (priorityComparison == 0)
				return v1Name.compareToIgnoreCase(v2Name);
			
			return priorityComparison;
		};
		
		return comparator;
	}

}
