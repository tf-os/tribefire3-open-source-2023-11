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
package com.braintribe.model.processing.session.impl.managed;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.session.api.managed.QueryResultConvenience;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluation;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.template.Template;

public abstract class AbstractQueryResultConvenience<Q extends Query, R extends QueryResult, C extends QueryResultConvenience> implements QueryResultConvenience {
	
	private static Logger log = Logger.getLogger(AbstractDelegatingQueryResultConvenience.class);
	
	private Q query;
	private final Map<String, Object> variables = newMap();
	
	public AbstractQueryResultConvenience(Q query) {
		this.query = query;
	}
	
	public Q getQuery() {
		return query;
	}
	
	@Override
	public <E> E first() throws GmSessionException {
		List<E> list = list();
		
		return list.isEmpty() ? null : list.get(0);
	}
	
	@Override
	public <E> E unique() throws GmSessionException {
		List<E> list = list();
		switch (list.size()) {
			case 0:
				return null;
			case 1:
				return list.get(0);
			default:
				throw new GmSessionException("Unique query returned " + list.size() + " results. Query: " + query.stringify() + ", result: " + list);
		}
	}
	
	@Override
	public <E> List<E> list() throws GmSessionException {
		QueryResult qr = result();
		
		if (qr instanceof SelectQueryResult)
			return (List<E>)((SelectQueryResult) qr).getResults();

		if (qr instanceof EntityQueryResult)
			return (List<E>)((EntityQueryResult) qr).getEntities();

		if (qr instanceof PropertyQueryResult)
			return (List<E>)((PropertyQueryResult) qr).getPropertyValue();
		
		throw new GmSessionException("Unsupported query result: " + qr);
	}
	
	@Override
	public <E> E value() throws GmSessionException {
		QueryResult result = result();

		if (result instanceof SelectQueryResult)
			return (E) ((SelectQueryResult) result).getResults();

		if (result instanceof EntityQueryResult)
			return (E) ((EntityQueryResult) result).getEntities();

		if (result instanceof PropertyQueryResult)
			return (E) ((PropertyQueryResult) result).getPropertyValue();
		
		throw new GmSessionException("Unsupported query result: "+result);
	}
	
	@Override
	public R result() {
		try {
			return resultInternal(resolveQuery());

		} catch (RuntimeException e) {
			throw Exceptions.unchecked(e, "Error while evaluating query: " + query.stringify());
		}
	}
	
	private Q resolveQuery() {
		if (!variables.isEmpty())
			resolveQueryTemplate();

		return query;
	}

	private void resolveQueryTemplate() {
		log.trace(() -> "Evaluating variables of query: " + query);

		TemplateEvaluation templateEvaluation = createQueryTemplateEvaluation();
		
		try {
			query = templateEvaluation.evaluateTemplate(false);

			log.trace(() -> "Successfully evaluated variables of query: " + query.stringify());

		} catch (TemplateEvaluationException e) {
			throw new GmSessionRuntimeException("Error while evaluating query variables.",e);
		}
	}

	private TemplateEvaluation createQueryTemplateEvaluation() {
		Template template = Template.T.create();
		template.setPrototype(query);
		
		TemplateEvaluation result = new TemplateEvaluation();
		result.setTemplate(template);
		result.setValueDescriptorValues(variables);

		return result;
	}

	protected abstract R resultInternal(Q query) throws GmSessionException;
	
	@Override
	public C setVariable(String name, Object value) {
		variables.put(name, value);
		return self();
	}

	
	@Override
	public C setTraversingCriterion(TraversingCriterion traversingCriterion) {
		query.setTraversingCriterion(traversingCriterion);
		return self();
	}

	protected C self() {
		return (C) this;
	}
}
