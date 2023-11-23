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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntityReferenceComparator;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext.CccBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.VdeRegistry;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.aspects.RootModelPathAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SelectedModelPathsAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.UserNameAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.VariableProviderAspect;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.Presentation;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.model.template.vd.ResolveVariables;

@SuppressWarnings("deprecation")
public class TemplateEvaluation{
	
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private PersistenceGmSession targetSession;
	private Template template;
	private Map<String, Object> valueDescriptorValues;
	private List<ModelPath> modelPaths;
	private ModelPath rootModelPath;
	private Supplier<String> userNameProvider = ()->null;

	
	public void setModelPaths(List<ModelPath> modelPaths) {
		this.modelPaths = modelPaths;
	}
	
	public void setRootModelPath(ModelPath rootModelPath) {
		this.rootModelPath = rootModelPath;
	}
	
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}

	public void setTargetSession(PersistenceGmSession targetSession) {
		this.targetSession = targetSession;
	}
	
	public void setTemplate(Template template) {
		this.template = template;
	}
	
	public void setValueDescriptorValues(Map<String, Object> valueDescriptorValues) {
		this.valueDescriptorValues = valueDescriptorValues;
	}
	
	public Map<String, Object> getValueDescriptorValues() {
		return valueDescriptorValues;
	}

	public Template getTemplate() {
		return template;
	}
	
	public <T> T evaluateTemplate(final boolean cloneToPersistenceSession) throws TemplateEvaluationException{
		try{
			final Object prototypeClone;
			Manipulation scriptClone = null;
			
			//creation prototype entity network within target session. translationMap set up. replacing persistentEntityReference with preliminary ones...
			final Map<PersistentEntityReference, GenericEntity> translationMap = new TreeMap<>(new EntityReferenceComparator());		
			final Set<String> notFoundVariableNames = new HashSet<String>();

			//check if available
			GenericModelType prototypeType = null;
			if(template.getPrototypeTypeSignature() != null)
				prototypeType = typeReflection.getType(template.getPrototypeTypeSignature());
			else
				prototypeType = typeReflection.getType(template.getPrototype());
			
			prototypeClone = prototypeType.clone(prepareCloningContext(cloneToPersistenceSession, translationMap), template.getPrototype(), null);

			//adapting script to targetSession reference and variable value resolution...
			if(template.getScript() != null){
				EntityType<?> scriptType = template.getScript().entityType();
				scriptClone = (Manipulation) scriptType.clone(prepareScriptCloningContext(cloneToPersistenceSession, translationMap),template.getScript(),null);
			}
			
			if(!notFoundVariableNames.isEmpty())
				throw new TemplateEvaluationException("missing values for following variables " + notFoundVariableNames);

			//script application
			if(!cloneToPersistenceSession && targetSession != null)
				targetSession.suspendHistory();
			if(scriptClone != null && targetSession != null)
				targetSession.manipulate().apply(scriptClone);
			if(!cloneToPersistenceSession && targetSession != null)
				targetSession.resumeHistory();	
						
			T result = (T) prototypeClone;
			return result;
		}catch(GmSessionException ex){
			throw new TemplateEvaluationException("error while evaluation template", ex);
		}
	}

	private StandardCloningContext prepareScriptCloningContext(final boolean cloneToPersistenceSession, final Map<PersistentEntityReference, GenericEntity> translationMap) {
		return new StandardCloningContext(){	
			
			@Override
			public Object postProcessCloneValue(GenericModelType propertyType, Object clonedValue) {
				if(clonedValue instanceof LocalEntityProperty){
					LocalEntityProperty localEntityProperty = (LocalEntityProperty) clonedValue;
					GenericEntity entity = ((LocalEntityProperty) clonedValue).getEntity();
					GenericEntity entityReference = translationMap.get(entity.reference());
					localEntityProperty.setEntity(entityReference != null ? entityReference : entity);
				}												
				else if(clonedValue instanceof PersistentEntityReference){
					PropertyCriterion pc = (PropertyCriterion)getTraversingStack().peek();
					GenericModelType gmt = pc.entityType();
					EntityType<EntityReference> refType = EntityReference.T;
					GenericEntity entity = translationMap.get(clonedValue);							
					if(entity != null){
						if(refType.isAssignableFrom(gmt))
							return entity.reference();
						else
							return entity;
					}
					else
						return entity;
				}
				else if (clonedValue instanceof ValueDescriptor) {
					// Check if the ValueDescriptor is a variable (which will be resolvd in any case) or not embedded in other ValueDescriptors. 
					// If the VD is embedded in other VDs we skip the evaluation here and let the top level VD do the full evaluation.
					if (clonedValue instanceof Variable || !isEmbeddedVd(getObjectStack())) {
						Object evaluated = evaluate(clonedValue);
						clonedValue = postProcessEvaluatedEntity(cloneToPersistenceSession, evaluated); 
					}
				}
				return super.postProcessCloneValue(propertyType, clonedValue);
			}
							
		};
	}
	
	private Object postProcessEvaluatedEntity(final boolean cloneToPersistenceSession, Object object) {
		if(cloneToPersistenceSession && object instanceof GenericEntity){
			GenericEntity entity = (GenericEntity)object;
			if (entity.getId() == null) {
				CccBuilder builder = 
						ConfigurableCloningContext
							.build()
							.supplyRawCloneWith(targetSession);
				object = entity.clone(builder.done());
			}								
		}
		return object;
	}
	
	private Object evaluate(Object object) {
		//@formatter:off
		VdeRegistry registry = 
			VDE.registryBuilder()
			.addRegistry(VDE.registryBuilder().defaultSetup())
			.withConcreteExpert(ResolveVariables.class, this::resolveVariables)
			.done();
		
		return VDE.evaluate()
				.withRegistry(registry)
				.withEvaluationMode(VdeEvaluationMode.Preliminary)
				.with(UserNameAspect.class, this.userNameProvider)
				.with(SelectedModelPathsAspect.class, this.modelPaths)
				.with(RootModelPathAspect.class, this.rootModelPath)
				.with(VariableProviderAspect.class, this::resolveVariable)
				.forValue(object);
		//@formatter:on
	}
	
	private VdeResult resolveVariables(VdeContext context, ResolveVariables vd) {
		Object value = context.evaluate(vd.getValue());
		
		//@formatter:off
		ConfigurableCloningContext cc = 
			ConfigurableCloningContext.build()
			.withClonedValuePostProcesor(this::postProcessClonedVariable)
			.done();
		//@formatter:on
		Object result = BaseType.INSTANCE.clone(cc, value, null);
		
		return new VdeResultImpl(result,false);
	}
	
	private Object postProcessClonedVariable(@SuppressWarnings("unused") GenericModelType propertyOrElementType, Object clonedValue) {
		return (clonedValue instanceof Variable) ? resolveVariable((Variable) clonedValue) : clonedValue;
	}

	private Object resolveVariable(Variable variable) {
		Object defaultValue = variable.getDefaultValue();
		return valueDescriptorValues!= null ? valueDescriptorValues.getOrDefault(variable.getName(), defaultValue) : defaultValue;
		
	}

	private StandardCloningContext prepareCloningContext(final boolean cloneToPersistenceSession, final Map<PersistentEntityReference, GenericEntity> translationMap) {
		return new StandardCloningContext(){
			
			@Override
			public GenericEntity supplyRawClone(EntityType<?> entityType, GenericEntity instanceToBeCloned) {
				
				EntityReference entityReference = instanceToBeCloned.reference();
				GenericEntity entityClone = (cloneToPersistenceSession) ? targetSession.create(entityType) : entityType.create();
				if (entityReference instanceof PersistentEntityReference) {
					PersistentEntityReference persistentEntityReference = (PersistentEntityReference) entityReference;				
					translationMap.put(persistentEntityReference, entityClone);
				}
				return entityClone;
				
			}
			
			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned,
					GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				return !(property.isIdentifying() || property.isGlobalId()) && !isServiceMetadata(instanceToBeCloned, property);
			}
			
			private boolean isServiceMetadata(GenericEntity instanceToBeCloned, Property property) {
				return property.getName().equalsIgnoreCase("metadata") && instanceToBeCloned instanceof ServiceRequest;
			}

			@Override
			public Object postProcessCloneValue(GenericModelType propertyType, Object clonedValue) {
				if (clonedValue instanceof ValueDescriptor) {
					// Check if the ValueDescriptor is a variable (which will be resolvd in any case) or not embedded in other ValueDescriptors. 
					// If the VD is embedded in other VDs we skip the evaluation here and let the top level VD do the full evaluation.
					if (clonedValue instanceof Variable || !isEmbeddedVd(getObjectStack())) {
						Object evaluated = evaluate(clonedValue);
						clonedValue = postProcessEvaluatedEntity(cloneToPersistenceSession, evaluated);
					}
				}
				return super.postProcessCloneValue(propertyType, clonedValue);
			}

		};
	}
	
	public Collection<Variable> collectVariables(){
		return collectVariables(false);
	}
	
	public Collection<Variable> collectVariables(boolean sorted){
		return collectValueDescriptors(sorted, vd -> vd instanceof Variable);
	}

	public <T extends ValueDescriptor> Collection<T> collectValueDescriptors(boolean sorted, Predicate<ValueDescriptor> filter){
		final Collection<T> collectedVds = sorted ? new ArrayList<T>() : new HashSet<T>();
		EntityVisitor vdsCollector = new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion,TraversingContext traversingContext) {
				if(entity instanceof ValueDescriptor) {
					ValueDescriptor vd = (ValueDescriptor) entity;
					if ((filter == null || filter.test(vd)) && !collectedVds.contains(vd))
						collectedVds.add((T)vd);
				}
			}
		};
		Template.T.traverse(template,null,vdsCollector);
		return collectedVds;
	}

	public Set<Presentation> getPresentationMetaData(){
		final Set<Presentation> presentationMetaData = new HashSet<Presentation>();
		for(TemplateMetaData metaData : template.getMetaData()){
			if(metaData instanceof Presentation)
				presentationMetaData.add((Presentation) metaData);
		}
		return presentationMetaData;
	}
	
	private boolean isEmbeddedVd(Stack<Object> objectStack) {

		int stackSize = objectStack.size();
		
		int valueDescriptorCount = 0;
		if (stackSize > 0) {
			
			for (int i = stackSize-1; i >= 0; i-- ) {
				Object object = objectStack.elementAt(i);
				if (object instanceof ValueDescriptor) {
					valueDescriptorCount++;
					if (valueDescriptorCount > 1) {
						// There's another ValueDescriptor before. Skip evaluation
						return true;
					}
				}				
			}
		}
		return false;
	}


}