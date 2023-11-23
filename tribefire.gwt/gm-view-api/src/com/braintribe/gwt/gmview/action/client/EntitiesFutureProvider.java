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
package com.braintribe.gwt.gmview.action.client;

import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.meta.data.display.DefaultSort;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.processing.async.api.AsyncCallback;


/**
 * Provider used for loading existing entities.
 * @author michel.docouto
 *
 */
public class EntitiesFutureProvider implements Function<ParserArgument, Future<EntitiesProviderResult>> {
	
	private PersistenceGmSession gmSession;
	
	/**
	 * Configures the required session used for querying.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@Override
	public Future<EntitiesProviderResult> apply(final ParserArgument parserArgument) throws RuntimeException {
		final Future<EntitiesProviderResult> future = new Future<>();
		IsAssignableTo entityTypeCondition = getFirstEntityTypeCondition(parserArgument.getTypeCondition());
		if (entityTypeCondition == null) {
			future.onSuccess(null);
			return future;
		}
		
		GenericModelType type = GMF.getTypeReflection().getType(entityTypeCondition.getTypeSignature());
		if (!type.isEntity()) {
			future.onSuccess(null);
			return future;
		}
		
		String text = parserArgument.getValue();
		if (text == null)
			text = "";
		
		EntityType<?> entityType = (EntityType<?>) type;
		// @formatter:off
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder.from(entityType);
		if (parserArgument.isSimplifiedAssignment()) {
			entityQueryBuilder = entityQueryBuilder
					.where()
						.fullText(EntityQueryBuilder.DEFAULT_SOURCE, text)
					.paging(parserArgument.getLimit(), parserArgument.getOffset());
		} else {
			entityQueryBuilder = entityQueryBuilder
									.where()
										.disjunction()
											.fullText(EntityQueryBuilder.DEFAULT_SOURCE, text)
											.entitySignature(EntityQueryBuilder.DEFAULT_SOURCE).ilike("*" + text + "*")															
										.close()
									.paging(parserArgument.getLimit(), parserArgument.getOffset());
		}
		// @formatter:on
		
		PersistenceGmSession theSession = parserArgument.getGmSession() != null ? parserArgument.getGmSession() : gmSession;
		DefaultSort defaultSort = theSession.getModelAccessory().getMetaData().entityType(entityType).meta(DefaultSort.T).exclusive();
		if (defaultSort == null) {
			entityQueryBuilder = entityQueryBuilder.orderBy(GenericEntity.id);
		} else {
			OrderingDirection orderingDirection;
			switch (defaultSort.getDirection()) {
			case descending:
				orderingDirection = OrderingDirection.descending;
				break;
			default:
				orderingDirection = OrderingDirection.ascending;
			}
			
			entityQueryBuilder = entityQueryBuilder.orderBy(defaultSort.getProperty().getName(), orderingDirection);
		}
		
		theSession.query().entities(entityQueryBuilder.done()).result(AsyncCallback.of( //
				entityQueryResultConvenience -> {
					boolean setFuture = true;
					if (entityQueryResultConvenience != null) {
						try {
							EntityQueryResult result = entityQueryResultConvenience.result();
							if (result != null) {
								setFuture = false;
								future.onSuccess(new EntitiesProviderResult(result.getEntities(), parserArgument.getOffset(), result.getHasMore()));
							}
						} catch (GmSessionException e) {
							future.onFailure(e);
						}
					}

					if (setFuture)
						future.onSuccess(null);
				}, future::onFailure));
		
		return future;
	}
	
	//TODO: check the possibility to perform multiple queries with a more complex condition
	private IsAssignableTo getFirstEntityTypeCondition(TypeCondition typeCondition) {
		if (typeCondition instanceof IsAssignableTo)
			return (IsAssignableTo) typeCondition;
		
		if (typeCondition instanceof TypeConditionDisjunction) {
			TypeConditionDisjunction disjunction = (TypeConditionDisjunction) typeCondition;
			if (disjunction.getOperands() != null) {
				for (TypeCondition operandTypeCondition : disjunction.getOperands()) {
					IsAssignableTo entityTypeCondition = getFirstEntityTypeCondition(operandTypeCondition);
					if (entityTypeCondition != null)
						return entityTypeCondition;
				}
			}
		}
		
		return null;
	}

}
