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
package com.braintribe.model.query.smart.processing.eval.context;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.api.continuation.EvaluationStep;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionAspect;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.eval.context.BasicQueryEvaluationContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.smartquery.eval.api.AssembleEntityContext;
import com.braintribe.model.processing.smartquery.eval.api.RuntimeSmartQueryEvaluationException;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;
import com.braintribe.model.processing.smartquery.eval.api.SmartQueryEvaluationContext;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.smart.processing.eval.context.function.BasicAssembleEntityContext;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueType;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;
import com.braintribe.model.smartqueryplan.functions.AssembleEntity;
import com.braintribe.model.smartqueryplan.value.SmartValue;

public class BasicSmartQueryEvaluationContext extends BasicQueryEvaluationContext implements SmartQueryEvaluationContext {

	private final SmartTupleSetRepository smartTupleSetRepository;
	private final Map<com.braintribe.model.accessdeployment.IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping;
	private final PersistenceGmSession session;
	private final Map<AssembleEntity, AssembleEntityContext> assembleEntityCtxs;
	private final SmartValueResolver smartValueResolver;

	public BasicSmartQueryEvaluationContext(SmartQueryPlan queryPlan, PersistenceGmSession session,
			Map<com.braintribe.model.accessdeployment.IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping,
			Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> queryFunctionExperts,
			Map<Class<? extends QueryFunctionAspect<?>>, Supplier<?>> queryFunctionAspectProviders,
			Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> conversionExperts) {

		super(null, queryPlan, queryFunctionExperts, queryFunctionAspectProviders, new TupleSetDescriptorImpl(queryPlan));

		this.accessMapping = accessMapping;
		this.session = session;
		this.assembleEntityCtxs = newMap();
		this.smartTupleSetRepository = new SmartTupleSetRepository(this);
		this.smartValueResolver = new SmartValueResolver(this, conversionExperts);
	}

	@Override
	public PersistenceGmSession getSession() {
		return session;
	}

	@Override
	public SelectQueryResult runQuery(com.braintribe.model.accessdeployment.IncrementalAccess accessDenotation, SelectQuery query) {
		try {
			com.braintribe.model.access.IncrementalAccess access = accessMapping.get(accessDenotation);
			if (access == null)
				throw new RuntimeQueryEvaluationException("No mapped access found for: " + accessDenotation
						+ ". Make sure this access is configured as a delegate of your SmartAcess and deployed.");

			return access.query(query);

		} catch (Exception e) {
			throw new RuntimeQueryEvaluationException("Error while running query " + query + " on acccess " + accessDenotation + ".", e);
		}
	}

	@Override
	public AssembleEntityContext acquireAssembleEntityContext(AssembleEntity assembleEntityFunction) {
		AssembleEntityContext result = assembleEntityCtxs.get(assembleEntityFunction);

		if (result == null) {
			result = new BasicAssembleEntityContext(assembleEntityFunction);
			assembleEntityCtxs.put(assembleEntityFunction, result);
		}

		return result;
	}

	@Override
	public EvalTupleSet resolveTupleSet(TupleSet tupleSet) {
		return smartTupleSetRepository.newEvalTupleSetFor(tupleSet);
	}

	@Override
	public <T> T resolveValue(Tuple tuple, Value value) {
		if (value.valueType() == ValueType.extension)
			return smartValueResolver.resolve(tuple, (SmartValue) value);
		else
			return super.resolveValue(tuple, value);
	}

	@Override
	public GenericEntity findEntity(String typeSignature, Object id, String partition) {
		try {
			return session.queryCache().entity(typeSignature, id, partition).find();
		} catch (GmSessionException e) {
			throw new RuntimeSmartQueryEvaluationException(e);
		}
	}

	@Override
	protected GenericEntity resolveReference(PersistentEntityReference reference) {
		try {
			return session.queryCache().entity(reference).find();
		} catch (GmSessionException e) {
			throw new RuntimeSmartQueryEvaluationException(e);
		}
	}

	@Override
	public GenericEntity instantiate(String typeSignature) {
		return session.createRaw(GMF.getTypeReflection().getEntityType(typeSignature));
	}

	@Override
	public boolean fulfillsCondition(Tuple tuple, Condition condition) {
		return SmartConditionEvaluator.getInstance().evaluate(tuple, condition, this);
	}

	// #####################################
	// ## . . . Unsupported methods . . . ##
	// #####################################

	@Override
	public Collection<? extends GenericEntity> getPopulation(String typeSignature) {
		throw new UnsupportedOperationException("SmartAccess cannot access entity popoulation directly!");
	}

	@Override
	public void pushStep(EvaluationStep step) {
		throw new UnsupportedOperationException("Continuable iteration is not supported by SmartAccess!");
	}

	@Override
	public EvaluationStep popStep() {
		throw new UnsupportedOperationException("Continuable iteration is not supported by SmartAccess!");
	}

	@Override
	public void pushValue(Value value) {
		throw new UnsupportedOperationException("Continuable iteration is not supported by SmartAccess!");
	}

	@Override
	public <T> T popValue() {
		throw new UnsupportedOperationException("Continuable iteration is not supported by SmartAccess!");
	}

}
