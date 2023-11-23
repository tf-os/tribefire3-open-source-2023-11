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
package com.braintribe.model.access.smart;

import static com.braintribe.model.access.smart.ConfigurationTools.conversionExperts;
import static com.braintribe.model.access.smart.ConfigurationTools.functionExperts;
import static com.braintribe.model.processing.smart.query.planner.SmartQueryPlanPrinter.printSafe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.AbstractAccess;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.access.smart.query.SmartPersistenceSession;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.conversion.ScriptedConversion;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.support.QueryAdaptingTools;
import com.braintribe.model.processing.query.support.QueryResultBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.EagerLoaderSupportingAccess;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlanner;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.StaticModelExpert;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;
import com.braintribe.model.processing.traversing.engine.impl.clone.legacy.StandardGmtCompatibleCloningContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.smart.processing.eval.context.BasicSmartQueryEvaluationContext;
import com.braintribe.model.query.smart.processing.eval.context.conversion.ScriptedConversionExpert;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;

import tribefire.extension.scripting.api.ScriptingEngineResolver;

/**
 * The SmartAccess
 * 
 * <h2>Implementation details</h2>
 * 
 * <h3>Unmapped properties</h3>
 * 
 * If an unmapped property is selected, <tt>null</tt> is returned on the corresponding tuple position. If such property
 * is referenced anywhere else other than in the select clause of the query, exception is thrown.
 * 
 * The implementation is a bit more complicated for entity/collection properties, as we have to deal with sources which
 * will have no {@link SourceNode}s. Such sources are detecting when initializing the plan structure (see
 * {@link QueryPlanStructure#acquireSourceNodeLeniently(Source)} a stored in the context. This context is then checked
 * when analyzing select clause for properties to mark and at the end converting it's elements to query-plan values. In
 * other cases, we simply detect missing node and an exception is thrown.
 * 
 * @author peter.gazdik
 * @author dirk.scheffler
 */
public class SmartAccess extends AbstractAccess implements EagerLoaderSupportingAccess {

	private Map<com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess> accessMapping;
	private com.braintribe.model.accessdeployment.smart.SmartAccess smartDenotation;
	private GmMetaModel metaModel;
	private CmdResolver cmdResolver;
	private ModelOracle modelOracle;
	private StaticModelExpert staticModelExpert;
	
	private SmartQueryPlanner queryPlanner;
	
	private final Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> functionExperts = functionExperts(null);
	private final Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> conversionExperts = conversionExperts(null);

	// ################################################
	// ## . . . . . . . Implementation . . . . . . . ##
	// ################################################

	@Required
	public void setAccessMapping(Map<com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess> accessMapping) {
		this.accessMapping = new HashMap<>(accessMapping);

		addSmartDenotationMapping();
	}

	/**
	 * SmartAccess needs also a mapping on itself, because at least one use-case (smart property whose type is a
	 * non-mapped smart entity) is resolved by querying the access itself. So some default mapping meta-data are
	 * generated where the access is the denotation type of this SmartAccess and a mapping from that denotation to this
	 * implementation must be in place.
	 */
	@Required
	public void setSmartDenotation(com.braintribe.model.accessdeployment.smart.SmartAccess smartDenotation) {
		this.smartDenotation = smartDenotation;
		addSmartDenotationMapping();
	}

	private void addSmartDenotationMapping() {
		if (smartDenotation != null && accessMapping != null && !accessMapping.containsKey(smartDenotation))
			accessMapping.put(smartDenotation, this);
	}

	@Required
	public void setMetaModel(GmMetaModel metaModel) {
		this.metaModel = metaModel;
	}

	@Configurable
	public void setScriptingEngineResolver(ScriptingEngineResolver scriptingEngineResolver) {
		ScriptedConversionExpert scriptedConversionExpert = new ScriptedConversionExpert();
		scriptedConversionExpert.setScriptingEngineResolver(scriptingEngineResolver);

		conversionExperts.put(ScriptedConversion.T, scriptedConversionExpert);
	}
	
	/** {@inheritDoc} */
	@Override
	public GmMetaModel getMetaModel() {
		return metaModel;
	}

	/** {@inheritDoc} */
	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		try {
			return query(query, new SmartPersistenceSession(this, newModelExpert()), false);

		} catch (Exception e) {
			throw new RuntimeException("Error while evaluating query: " + printSafe(query), e);
		}
	}

	@Override
	public SelectQueryResult query(SelectQuery query, PersistenceGmSession session) throws ModelAccessException {
		return query(query, session, true);
	}

	private SelectQueryResult query(SelectQuery query, PersistenceGmSession session, boolean returnDirectly) throws ModelAccessException {
		SelectQueryResult result = queryHelper(query, session);

		if (returnDirectly)
			return result;

		List<Object> results = cloneSelectQueryResult(result.getResults(), query, absenceInfoResolvingTraversingContext());
		result.setResults(results);

		return result;
	}

	public static StandardCloningContext absenceInfoResolvingTraversingContext() {
		StandardCloningContext cc = new StandardGmtCompatibleCloningContext();
		cc.setAbsenceResolvable(true);
		// this is safe, cloning query results uses a matcher, and a matcher cannot even access property value
		// saying false here prevents unnecessary property eager-loading before we even find out (via matcher) we need the value
		cc.setPropertyValueUsedForMatching(false);

		return cc;
	}
	
	private SelectQueryResult queryHelper(SelectQuery query, PersistenceGmSession session) {
		SmartLogging.selectQuery(query);

		ModelExpert modelExpert = ((SmartPersistenceSession)session).getModelExpert();
		SmartQueryPlan queryPlan = acquireQueryPlanner().buildQueryPlan(query, modelExpert);
		SmartLogging.queryPlan(queryPlan);

		QueryEvaluationContext context = new BasicSmartQueryEvaluationContext( //
				queryPlan, session, accessMapping, functionExperts, null, conversionExperts);
		EvalTupleSet tuples = context.resolveTupleSet(queryPlan.getTupleSet());

		SelectQueryResult result = QueryResultBuilder.buildQueryResult(tuples, context.resultComponentsCount());
		SmartLogging.queryResult(result);

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public EntityQueryResult queryEntities(EntityQuery entityQuery) throws ModelAccessException {
		return QueryAdaptingTools.queryEntities(entityQuery, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyQueryResult queryProperty(PropertyQuery propertyQuery) throws ModelAccessException {
		SmartLogging.propertyQuery(propertyQuery);

		if (!propertyIsMapped(propertyQuery.getEntityReference().getTypeSignature(), propertyQuery.getPropertyName()))
			return QueryResultBuilder.buildPropertyQueryResult(null, false);
		else
			return QueryAdaptingTools.queryProperties(propertyQuery, this);
	}

	private boolean propertyIsMapped(String ts, String pn) {
		return acquireCmdResolver().getMetaData().entityTypeSignature(ts).property(pn).meta(PropertyAssignment.T).exclusive() != null;
	}

	/** {@inheritDoc} */
	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		return new SmartManipulationProcessor(this, smartDenotation, accessMapping, acquireCmdResolver(), acquireStaticModelExpert(),
				conversionExperts).process(manipulationRequest.getManipulation());
	}

	/** {@inheritDoc} */
	@Override
	public ReferencesResponse getReferences(ReferencesRequest referencesRequest) throws ModelAccessException {
		throw new UnsupportedOperationException("Method 'SmartAccess.getReferences' is not implemented yet!");
	}

	private SmartQueryPlanner acquireQueryPlanner() {
		if (queryPlanner == null) {
			queryPlanner = new SmartQueryPlanner();
			queryPlanner.setFunctionExperts(functionExperts);
			queryPlanner.setConversionExperts(conversionExperts);
		}

		return queryPlanner;
	}

	private ModelExpert newModelExpert() {
		ensureModelTools();
		return new ModelExpert(cmdResolver, staticModelExpert, smartDenotation, accessMapping);
	}
	
	private StaticModelExpert acquireStaticModelExpert() {
		ensureModelTools();
		return staticModelExpert;
	}

	private CmdResolver acquireCmdResolver() {
		ensureModelTools();
		return cmdResolver;
	}

	private void ensureModelTools() {
		if (staticModelExpert == null) // this is an indicator tools are initialized, because it's assigned last
			initModelToolsSync();
	}

	private synchronized void initModelToolsSync() {
		if (staticModelExpert != null)
			return;

		modelOracle = new BasicModelOracle(metaModel);
		cmdResolver = new CmdResolverImpl(modelOracle);
		staticModelExpert = new StaticModelExpert(modelOracle);
	}

	@Override
	public Set<String> getPartitions() throws ModelAccessException {
		if (partitions == null)
			partitions = Collections.unmodifiableSet(partitionsFromDelegates());

		return partitions;
	}

	private Set<String> partitionsFromDelegates() {
		return accessMapping.values().stream() //
				.filter(delegate -> delegate != this) //
				.map(IncrementalAccess::getPartitions) //
				.flatMap(Set::stream) //
				.collect(Collectors.toSet());
	}

}
