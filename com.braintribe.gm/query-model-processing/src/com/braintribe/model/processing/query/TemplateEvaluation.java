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
package com.braintribe.model.processing.query;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.query.Query;

/**
 * 
 * 
 * @deprecated use standard template evaluation mechanisms instead
 */
@Deprecated
public class TemplateEvaluation {
	
	private Query query;
	private GenericModelTypeReflection typeReflection;
	private EntityType<Query> queryType;
	

	public TemplateEvaluation(Query query){
		this.query = query;
	}

	public void setTypeReflection(GenericModelTypeReflection typeReflection) {
		this.typeReflection = typeReflection;
	}
	
	public GenericModelTypeReflection getTypeReflection() {
		if(typeReflection == null)
			typeReflection = GMF.getTypeReflection();
		return typeReflection;
	}

	/**
	 * @return the queryType
	 */
	public EntityType<Query> getQueryType() {
		if (queryType == null) {
			queryType = getTypeReflection().getEntityType(query);
		}
		return queryType;
	}
	
	public Map<String, Variable> getVariables(){
		final Map<String, Variable> variables = new HashMap<String, Variable>();
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
		getTypeReflection().getEntityType(query).traverse(query, null, traversingVisitor);
		return variables;
	}

	public Query evaluate(Map<String, Object> values) throws TemplateEvaluationException{
		VariableReplacingEvolutionContext evaluationContext = new VariableReplacingEvolutionContext();
		evaluationContext.setValues(values);
		return evaluate(evaluationContext);
	}
	
	public Query evaluate(VariableReplacingEvolutionContext context) throws TemplateEvaluationException {
		return (Query) getQueryType().clone(context, query, null);
	}
	
}
