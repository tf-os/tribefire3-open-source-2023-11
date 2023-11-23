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
package com.braintribe.model.processing.template.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.Variable;

public class TemplateEvolution extends EntityVisitor{
	
	private Set<Object> reachablePrototypeTargets = new HashSet<Object>();
	private HashMap<Class<? extends Manipulation>, Set<String>> variablePropertyCandidates;
	private List<Manipulation> manipulations;
	private GmSession session;
	private static int i = 0;
	
	public TemplateEvolution() {
		variablePropertyCandidates = new HashMap<Class<? extends Manipulation>, Set<String>>();
		variablePropertyCandidates.put(ChangeValueManipulation.class, new HashSet<String>(Arrays.asList("newValue")));
		variablePropertyCandidates.put(AddManipulation.class, new HashSet<String>(Arrays.asList("itemsToAdd")));
	}
	
	public void setSession(GmSession session) {
		this.session = session;
	}
	
	private TraversingVisitor variableReplacingEvolutionContext = new TraversingVisitor(){

		@Override
		public void visitTraversing(TraversingContext traversingContext) {
			BasicCriterion criterion = traversingContext.getTraversingStack().peek();
			int index = traversingContext.getTraversingStack().size() - 2;
			BasicCriterion parentCriterion = traversingContext.getTraversingStack().get(Math.max(0, index));
			Object parentObject = traversingContext.getObjectStack().get(Math.max(0,index));
			if(criterion instanceof PropertyCriterion && parentCriterion instanceof EntityCriterion){
				EntityCriterion entityCriterion = (EntityCriterion) parentCriterion;
				PropertyCriterion propertyCriterion = (PropertyCriterion) criterion;			
				EntityType<GenericEntity> entityType = entityCriterion.entityType();				
				Set<String> propertyNames = variablePropertyCandidates.get(entityType.getJavaType());
				if(propertyNames != null){
					if(propertyNames.contains(propertyCriterion.getPropertyName())){
						GenericEntity entity = (GenericEntity) parentObject;
						PropertyManipulation manipulation = (PropertyManipulation) entity;
						EntityProperty entityProperty = (EntityProperty) manipulation.getOwner();
						Object defaultValue = entityType.getProperty(propertyCriterion.getPropertyName()).get(entity);
						if(!(defaultValue instanceof Variable)){
							if(entityType.getJavaType().equals(AddManipulation.class)){
								Map<Object,Object> objectMap = (Map<Object, Object>) defaultValue;
								for(Object key : objectMap.keySet()){
									Variable variable = session.create(Variable.T);
									variable.setDefaultValue(objectMap.get(key));
									variable.setName("v_" + entityProperty.getPropertyName() + "_" + i++);
									
									String typeSignature = "";
									if(defaultValue instanceof EntityReference)
										typeSignature = ((EntityReference)defaultValue).getTypeSignature();
									else{
										EntityType<?> referenceType = entityProperty.entityType();
										Property property = referenceType.getProperty(entityProperty.getPropertyName());
										typeSignature = property.getType().getTypeSignature();
									}
									variable.setTypeSignature(typeSignature);
									
									objectMap.put(key, variable);
								}
							}else{
								Variable variable = session.create(Variable.T);
								variable.setDefaultValue(defaultValue);
								variable.setName("v_" + entityProperty.getPropertyName() + "_" + i++);
								String typeSignature = "";
								if(defaultValue instanceof EntityReference)
									typeSignature = ((EntityReference)defaultValue).getTypeSignature();
								else{
									EntityType<?> referenceType = entityProperty.entityType();
									Property property = referenceType.getProperty(entityProperty.getPropertyName());
									typeSignature = property.getType().getTypeSignature();
								}
								variable.setTypeSignature(typeSignature);
								entityType.getProperty(propertyCriterion.getPropertyName()).set(entity, variable);
							}
						}
					}
				}				
			}
		}
		
	};
	
	public void analyzePrototype(Object protoType){
		GMF.getTypeReflection().getType(protoType).traverse(protoType, null, this);		
	}
	
	public List<Manipulation> filterManipulations(List<Manipulation> manipulationsToFilter){
		List<Manipulation> filteredManipulations = null;
		for(Manipulation manipulation : manipulationsToFilter){
			if(filteredManipulations == null) filteredManipulations = new ArrayList<Manipulation>();
			filteredManipulations.add(manipulation);
		}
		return filteredManipulations;
	}
	
	public List<Manipulation> evoluteManipulations(List<Manipulation> manipualtionsToEvolute){
		this.manipulations = manipualtionsToEvolute;
		if(manipualtionsToEvolute != null){
			TraversingCriterion tc = TC.create()
				.pattern()
					.conjunction()
						.entity()
						.typeCondition(TypeConditions.isAssignableTo(Manipulation.T))
						.close()
					.property("inverseManipulation").close()
				.done();
			
			StandardMatcher matcher = new StandardMatcher();
			matcher.setCriterion(tc);
			GMF.getTypeReflection().getType(manipualtionsToEvolute).traverse(manipualtionsToEvolute, matcher, variableReplacingEvolutionContext);		
			//(variableReplacingEvolutionContext, manipualtionsToEvolute, null);
		}
		return manipualtionsToEvolute;
	}
	
	public Map<String, Variable> getVariables(){
		final Map<String, Variable> variables = new HashMap<String, Variable>();
		if(manipulations != null){
			TraversingVisitor traversingVisitor = new TraversingVisitor() {			
				@Override
				public void visitTraversing(TraversingContext traversingContext) {
					Object candidate = traversingContext.getObjectStack().peek();
					if(candidate instanceof Variable){
						Variable variable = (Variable) candidate;
						variables.put(variable.getName(), variable);					
					}
				}
			};
			GMF.getTypeReflection().getType(manipulations).traverse(manipulations, null, traversingVisitor);
		}
		return variables;
	}
	
	public List<? extends Object> getValues(){
		final List<Object> values = new ArrayList<Object>();
		EntityVisitor entityVisitor = new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				Object candidate = traversingContext.getObjectStack().peek();
				if(candidate instanceof Variable){
					values.add(((Variable) candidate).getDefaultValue());
				}	
			}
		};
		//return Arrays.asList(1,2,3,4,5,6,7,8,"String");
		List<Variable> variables = new ArrayList<Variable>(getVariables().values());
		GMF.getTypeReflection().getType(variables).traverse(variables, null, entityVisitor);
		return values;
	}
	
	@Override
	protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
		reachablePrototypeTargets.add(entity);
	}
	
	public boolean isManipulationRelevant(Manipulation manipulation){
		if(manipulation instanceof PropertyManipulation){
			LocalEntityProperty entityProperty = (LocalEntityProperty) ((PropertyManipulation)manipulation).getOwner();
			return reachablePrototypeTargets.contains(entityProperty.getEntity());
		}
		return false;
	}
}
