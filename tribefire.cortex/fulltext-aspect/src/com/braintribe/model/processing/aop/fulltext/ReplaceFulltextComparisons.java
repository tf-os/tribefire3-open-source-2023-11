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
package com.braintribe.model.processing.aop.fulltext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.impl.aop.AopAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;
import com.braintribe.model.processing.aop.api.aspect.PointCutConfigurationContext;
import com.braintribe.model.processing.aop.api.context.BeforeContext;
import com.braintribe.model.processing.aop.api.interceptor.BeforeInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.InterceptionException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CollectionTools;



/**
 * This is an interception aspect that inspects incoming queries (queryEntity as well as queryProperty) for {@link FulltextComparison} elements <br>
 * and replaces them with a {@link Disjunction} of {@link ValueComparison}s including properties marked as fulltext-indexed compared to the requested value using the like operator. <br>
 * This could be used as an preparation for implementations of {@link IncrementalAccess} that have no FulltextSupport.
 *    
 * @author gunther.schenk
 *
 */
public class ReplaceFulltextComparisons implements AccessAspect, InitializationAware {
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(ReplaceFulltextComparisons.class);
	private AopAccess access;
	
	private String queryValueTemplate = "*{text}*";
	private int maxIncludedDefaultProperties = 3;

	private boolean autoDetectFulltextProperties = false;
	private Function<EntityType<GenericEntity>, List<FulltextPropertyDescription>> defaultPropertiesProvider = null;
			

	@Override
	public void postConstruct() {
		if (this.defaultPropertiesProvider == null) {
			this.defaultPropertiesProvider =
					/**
					 * The default implementation returns the first property of type string found in the given entity type.
					 * If no string property could be found and empty list is provided.
					 */
					new Function<EntityType<GenericEntity>, List<FulltextPropertyDescription>>() {
				@Override
				public List<FulltextPropertyDescription> apply(EntityType<GenericEntity> entityType) throws RuntimeException {
					List<FulltextPropertyDescription> propertyNames = new ArrayList<FulltextPropertyDescription>();
					for (Property property : entityType.getProperties()) {
						GenericModelType propertyType = property.getType(); 
						if (propertyType == GenericModelTypeReflection.TYPE_STRING) {
							propertyNames.add(new FulltextPropertyDescription(property.getName(), propertyType));
						}
						if (propertyNames.size() >= maxIncludedDefaultProperties) {
							break;
						}
					}
					return propertyNames;
				}
			};
		}
	}
	

	// **************************************************************************
	// Constructor
	// **************************************************************************

	/**
	 * Default constructor
	 */
	public ReplaceFulltextComparisons() {
	}

	// **************************************************************************
	// Getter/Setter
	// **************************************************************************

	/**
	 * @return the metaDataResolver
	 */
	public FulltextMetaDataResolver getMetaDataResolver(PersistenceGmSession session) throws Exception {
		return new FulltextMetaDataResolver(session.getModelAccessory().getCmdResolver());
	}
	
	/**
	 * @param defaultPropertiesProvider the defaultPropertiesProvider to set
	 * @deprecated
	 */
	public void setDefaultPropertiesProvider(
			Function<EntityType<GenericEntity>, List<String>> defaultPropertiesProvider) {
		
	}
	
	public void setAutoDetectFulltextProperties(
			boolean autoDetectFulltextProperties) {
		this.autoDetectFulltextProperties = autoDetectFulltextProperties;
	}
	
	/**
	 * @param queryValueTemplate the queryValueTemplate to set
	 */
	public void setQueryValueTemplate(String queryValueTemplate) {
		this.queryValueTemplate = queryValueTemplate;
	}
	
	public AopAccess getAccess() {
		return access;
	}
	
	
	// **************************************************************************
	// Interceptor Experts
	// **************************************************************************
	
	
	@Override
	public void configurePointCuts(PointCutConfigurationContext context) {
		context.addPointCutBinding( AccessJoinPoint.queryEntities, new QueryAdapter());
		context.addPointCutBinding( AccessJoinPoint.queryProperties, new QueryAdapter());
		context.addPointCutBinding( AccessJoinPoint.query, new QueryAdapter());
	}


	public abstract class AbstractQueryAdapter<Q extends GenericEntity,R extends GenericEntity, F extends GenericEntity, P> implements BeforeInterceptor<Q,R> {

		/**
		 * see biz.i2z.service.ecm.access.impl.aop.interceptor.BesideInterceptor#run(biz.i2z.service.ecm.access.impl.aop.context.BesideContext)
		 */
		@Override
		public void run(BeforeContext<Q,R> context) throws InterceptionException {
			try {
				Q adaptedQuery = preProcessQuery(context.getRequest(), GMF.getTypeReflection(), this, context.getSession());
				context.overrideRequest(adaptedQuery);
			} catch (Exception e) {
				throw new InterceptionException("Error while adapting FulltextComparison in query.",e);
			}
		}
		
		
		public boolean isFulltextComparison (Object object) {
			return (object instanceof FulltextComparison);
		}
		
		public abstract EntityType<GenericEntity> determineQueriedEntityType(Q query, GenericModelTypeReflection typeReflection);
		public abstract String getFulltextCompareValue (F fulltextComparison);
		public abstract P createReplacementComparison(List<FulltextPropertyDescription> fulltextProperties);
		public abstract void createAndAddPropertyComparison(P disjunction, FulltextPropertyDescription property, String comparisonText, Source source);
		
	}
	
	
	/**
	 * Provides functionality to adapt instances of the QueryModel .
	 */
	public class QueryAdapter extends AbstractQueryAdapter<com.braintribe.model.query.Query, com.braintribe.model.query.QueryResult, com.braintribe.model.query.conditions.FulltextComparison, com.braintribe.model.query.conditions.Condition> {

		/**
		 * see biz.i2z.service.ecm.access.impl.aop.ReplaceFulltextComparisons.AbstractQueryAdapter#getFulltextCompareValue(java.lang.Object)
		 */
		@Override
		public String getFulltextCompareValue(com.braintribe.model.query.conditions.FulltextComparison fulltextComparison) {
			return fulltextComparison.getText();
		}
		
		/**
		 * see biz.i2z.service.ecm.access.impl.aop.ReplaceFulltextComparisons.AbstractQueryAdapter#determineQueriedEntityType(java.lang.Object, com.braintribe.model.generic.reflection.GenericModelTypeReflection)
		 */
		@Override
		public EntityType<GenericEntity> determineQueriedEntityType(com.braintribe.model.query.Query query,	GenericModelTypeReflection typeReflection) {
			if (query instanceof com.braintribe.model.query.EntityQuery) {
				return typeReflection.getEntityType(((com.braintribe.model.query.EntityQuery) query).getEntityTypeSignature());
			} else if (query instanceof com.braintribe.model.query.PropertyQuery) {
				return typeReflection.getEntityType(((com.braintribe.model.query.PropertyQuery) query).getEntityReference().getTypeSignature());
			} else if (query instanceof SelectQuery) {
				for (From from : ((SelectQuery) query).getFroms()) {
					return typeReflection.getEntityType(from.getEntityTypeSignature());
				}
			}
			logger.warn("Unsupported query implementation: "+query.getClass());
			return null;	
		}
		
		/**
		 * see biz.i2z.service.ecm.access.impl.aop.ReplaceFulltextComparisons.AbstractQueryAdapter#createAndAddPropertyComparison(java.lang.Object, java.lang.String, java.lang.String)
		 */
		@Override
		public void createAndAddPropertyComparison(com.braintribe.model.query.conditions.Condition condition,FulltextPropertyDescription property, String comparisonText, Source source) {
			
			Operator operator = property.propertyType == SimpleType.TYPE_STRING ? Operator.ilike : Operator.equal;
			String valueString = comparisonText;
			if (property.propertyType == SimpleType.TYPE_STRING) {
				Map<String,Object> vars = new HashMap<String, Object>();
				vars.put("text", comparisonText);
				valueString = StringTools.patternFormat(queryValueTemplate, vars); 
			}
			
			if(condition instanceof AbstractJunction){
				ValueComparison comparison = ValueComparison.T.create();
				PropertyOperand propOperand = PropertyOperand.T.create();
				propOperand.setPropertyName(property.propertyPath);
				propOperand.setSource(source);
				comparison.setLeftOperand(propOperand);
				comparison.setOperator(operator);
				comparison.setRightOperand(valueString);
				
				((AbstractJunction) condition).getOperands().add(comparison);
			}else if(condition instanceof ValueComparison){
				PropertyOperand propOperand = PropertyOperand.T.create();
				propOperand.setPropertyName(property.propertyPath);
				propOperand.setSource(source);
				((ValueComparison) condition).setLeftOperand(propOperand);
				((ValueComparison) condition).setOperator(operator);
				((ValueComparison) condition).setRightOperand(valueString);				
			}
		}
		
		/**
		 * see biz.i2z.service.ecm.access.impl.aop.ReplaceFulltextComparisons.AbstractQueryAdapter#createReplacementComparison()
		 */
		@Override
		public com.braintribe.model.query.conditions.Condition createReplacementComparison(List<FulltextPropertyDescription> fulltextProperties) {
			if(fulltextProperties.size() > 1){
				com.braintribe.model.query.conditions.Disjunction replacement = com.braintribe.model.query.conditions.Disjunction.T.create();
				replacement.setOperands(new ArrayList<com.braintribe.model.query.conditions.Condition>());
				return replacement;
			}else{
				ValueComparison valueComparison = ValueComparison.T.create();
				return valueComparison;
			}
		}
		
	}
	
	// **************************************************************************
	// Helper methods
	// **************************************************************************
	
	
	
	private <Q extends GenericEntity,R extends GenericEntity, F extends GenericEntity,P> Q preProcessQuery (Q query, GenericModelTypeReflection typeReflection, AbstractQueryAdapter<Q, R, F, P> queryInspection, PersistenceGmSession session) throws Exception {

		EntityType<GenericEntity> queriedEntityType = queryInspection.determineQueriedEntityType(query, typeReflection);
		if (queriedEntityType == null) {
			return query;
		}
		
		List<FulltextPropertyDescription> fulltextProperties  = getFulltextProperties(queriedEntityType, session);
		if (fulltextProperties == null || fulltextProperties.isEmpty()) {
			return query;
		}
		
		EntityType<Q> queryEntityType = query.entityType();
		
		Source source = null;
		if (query instanceof SelectQuery) {
			SelectQuery selectQuery = (SelectQuery) query;
			List<From> froms = selectQuery.getFroms();
			if (!froms.isEmpty()) {
				source = froms.get(0);
			}
		}
		
		@SuppressWarnings("unchecked")
		Q processedQuery = (Q) queryEntityType.clone(
				new QueryProcessingCloningContext<Q,R,F,P>(
						fulltextProperties, 
						queryInspection,
						source), 
				query, 
				StrategyOnCriterionMatch.partialize);
		
		return processedQuery;
	}
	
	private List<FulltextPropertyDescription> getFulltextProperties (EntityType<GenericEntity> entityType, PersistenceGmSession session) throws Exception {
		//List<String> fulltextProperties  = getMetaDataResolver(session).getFulltextPropertyNames(entityType);
		FulltextMetaDataResolver resolver = getMetaDataResolver(session);
		List<Property> fulltextProperties  = resolver.getFulltextProperties(entityType);
		if (CollectionTools.isEmpty(fulltextProperties)) {
			if (!autoDetectFulltextProperties || defaultPropertiesProvider == null) return null;
			return this.defaultPropertiesProvider.apply(entityType);
		}
		
		List<FulltextPropertyDescription> adaptedProperties = new ArrayList<FulltextPropertyDescription>();
		for (Property fulltextProperty : fulltextProperties) {
			GenericModelType propertyType = fulltextProperty.getType();
			if (fulltextProperty.isIdentifier()) {
				adaptedProperties.add(new FulltextPropertyDescription(fulltextProperty.getName(), resolver.getIdType(entityType.getTypeSignature())));
			} else if (propertyType instanceof SimpleType || propertyType instanceof EnumType) {
				adaptedProperties.add(new FulltextPropertyDescription(fulltextProperty.getName(), propertyType));
			} else if (propertyType instanceof EntityType) {
				List<FulltextPropertyDescription> subProperties = getFulltextProperties((EntityType<GenericEntity>)propertyType, session);
				if (subProperties != null) {
					for (FulltextPropertyDescription subProperty : subProperties) {
						adaptedProperties.add(new FulltextPropertyDescription(fulltextProperty.getName()+"."+subProperty.propertyPath,subProperty.propertyType));
					}
				}
					
			} else {
				logger.warn("Unsupported fulltext property: "+fulltextProperty.getName()+". Type: "+propertyType+" is not supported.");
			}
		} 
		
		return adaptedProperties;
	}
	
	
	private class FulltextPropertyDescription {
		private String propertyPath;
		private GenericModelType propertyType;
		
		public FulltextPropertyDescription(String propertyPath, GenericModelType propertyType) {
			this.propertyPath = propertyPath;
			this.propertyType = propertyType;
		}
	}
	

	public static class QueryProcessingCloningContext<Q extends GenericEntity,R extends GenericEntity, F extends GenericEntity,P> extends StandardCloningContext {
		
		private List<FulltextPropertyDescription> fulltextProperties = new ArrayList<FulltextPropertyDescription>();
		private final AbstractQueryAdapter<Q,R,F,P> queryInspection;
		private Source fulltextPropertiesSource = null;
		
		public QueryProcessingCloningContext(List<FulltextPropertyDescription> fulltextProperties, AbstractQueryAdapter<Q,R,F,P> queryInspection, Source fulltextPropertiesSource) {
			this.fulltextProperties = fulltextProperties;
			this.queryInspection = queryInspection;
			this.fulltextPropertiesSource = fulltextPropertiesSource;
		}
		
		@Override
		public <T> T getAssociated(GenericEntity entity) {
			if (entity == this.fulltextPropertiesSource || entity instanceof Source) {
				return (T) entity;
			}
			return super.getAssociated(entity);
		}
		
		/**
		 * @see com.braintribe.model.generic.reflection.StandardCloningContext#postProcessCloneValue(com.braintribe.model.generic.reflection.GenericModelType, java.lang.Object)
		 */
		@Override
		public Object postProcessCloneValue(GenericModelType propertyType, Object clonedValue) {
			if (queryInspection.isFulltextComparison(clonedValue)) {
				
				F fulltextComparison = (F)clonedValue;
				String fulltextCompareValue = queryInspection.getFulltextCompareValue(fulltextComparison);
				
				/*
				Source source = null;
				if (fulltextComparison instanceof FulltextComparison) {
					FulltextComparison ftComparison = (FulltextComparison) fulltextComparison;
					source = ftComparison.getSource();
				}*/
				
				if (isAllFulltextExpression(fulltextCompareValue)) {
					return super.postProcessCloneValue(propertyType, clonedValue);
				}
				
				P replacement = queryInspection.createReplacementComparison(fulltextProperties);
				
				for (FulltextPropertyDescription fulltextProperty : fulltextProperties) {

					try {
						
						queryInspection.createAndAddPropertyComparison(replacement, fulltextProperty, fulltextCompareValue, this.fulltextPropertiesSource);
						
					} catch (Exception e) {
						throw new IllegalArgumentException("Could not create PropertyComparison replacement for FulltextComparison.",e);
					}

				}
				
				return replacement;
				
			} else {
				return super.postProcessCloneValue(propertyType, clonedValue);	
			}
			
		}
	}

	/**
	 * Checks if the query given text is a all expression.
	 */
	public static boolean isAllFulltextExpression(Object text) {
		if (text == null || text.toString().trim().length() == 0) {
			return true;
		}
		return isWildarcdExpression(text);
	}

	public static boolean isWildarcdExpression(Object text) {
		return text.equals("*") || text.equals("%");
	}

	@Configurable
	public void setMaxIncludedDefaultProperties(int maxIncludedDefaultProperties) {
		this.maxIncludedDefaultProperties = maxIncludedDefaultProperties;
	}

}
