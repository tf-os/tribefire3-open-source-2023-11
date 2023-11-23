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
package com.braintribe.model.processing.query.expander;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.query.api.expander.QuerySignatureExpander;
import com.braintribe.model.processing.query.api.expander.QueryTypeSignatureExpanderRuntimeException;
import com.braintribe.model.processing.query.api.shortening.SignatureExpert;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Query;

public class QueryTypeSignatureExpanderImpl implements QuerySignatureExpander {
	private Query query = null;
	private SignatureExpert mode = null;

	/******************** Getter/Setter ********************/

	@Override
	public Query getQuery() {
		return this.query;
	}

	@Override
	public void setQuery(final Query shortenedQuery) {
		this.query = shortenedQuery;
	}

	@Override
	public SignatureExpert getMode() {
		return this.mode;
	}

	@Override
	public void setMode(final SignatureExpert expandMode) {
		this.mode = expandMode;
	}

	@Override
	public Query expandTypeSignature() throws QueryTypeSignatureExpanderRuntimeException {
		if (mode == null) {
			return query;
		}
		query.traverse(new StandardTraversingContext() {
			@SuppressWarnings("unusable-by-js")
			@Override
			public void registerAsVisited(GenericEntity entity, Object associate) {
				super.registerAsVisited(entity, associate);

				if (entity instanceof From) {
					From fromSource = (From) entity;
					String typeSignature = fromSource.getEntityTypeSignature();
					fromSource.setEntityTypeSignature(mode.expand(typeSignature));

				} else if (entity instanceof EnumReference) {
					EnumReference enumReference = (EnumReference) entity;
					String typeSignature = enumReference.getTypeSignature();
					enumReference.setTypeSignature(mode.expand(typeSignature));

				} else if (entity instanceof PersistentEntityReference) {
					PersistentEntityReference entityReference = (PersistentEntityReference) entity;
					String typeSignature = entityReference.getTypeSignature();
					entityReference.setTypeSignature(mode.expand(typeSignature));

				} else if (entity instanceof PreliminaryEntityReference) {
					PreliminaryEntityReference preliminaryReference = (PreliminaryEntityReference) entity;
					String typeSignature = preliminaryReference.getTypeSignature();
					preliminaryReference.setTypeSignature(mode.expand(typeSignature));

				} else if (entity instanceof EntityQuery) {
					EntityQuery entityQuery = (EntityQuery) entity;
					String typeSignature = entityQuery.getEntityTypeSignature();
					entityQuery.setEntityTypeSignature(mode.expand(typeSignature));
				}
			}
		});

		return query;
	}

	/* PGA: I commented this out on 5.April.2017 as I wanted to get rid of a dependency on BasicGmTraversing (which
	 * creates a cycle). Should this really be needed, we need to re-structure the artifacts. If nobody has a problem
	 * with the change, this should be deleted. */

	// @formatter:off
	//	@Override
//	public Query expandTypeSignature() throws QueryTypeSignatureExpanderRuntimeException {
//		try {
//			// Get and check defined SignatureExpert
//			final SignatureExpert expandMode = this.mode;
//			if (expandMode != null) {
//				// Expand query with defined SignatureExpert
//				GMT.traverse().visitor(new GmTraversingVisitor() {
//					@Override
//					public void onElementEnter(final GmTraversingContext context, final TraversingModelPathElement pathElement) throws GmTraversingException {
//						final Object currentValue = pathElement.getValue();
//
//						if (currentValue instanceof From) {
//							final From fromSource = (From) currentValue;
//
//							final String typeSignature = fromSource.getEntityTypeSignature();
//							fromSource.setEntityTypeSignature(expandMode.expand(typeSignature));
//						} else if (currentValue instanceof EnumReference) {
//							final EnumReference enumReference = (EnumReference) currentValue;
//
//							final String typeSignature = enumReference.getTypeSignature();
//							enumReference.setTypeSignature(expandMode.expand(typeSignature));
//						} else if (currentValue instanceof PersistentEntityReference) {
//							final PersistentEntityReference entityReference = (PersistentEntityReference) currentValue;
//
//							final String typeSignature = entityReference.getTypeSignature();
//							entityReference.setTypeSignature(expandMode.expand(typeSignature));
//						} else if (currentValue instanceof PreliminaryEntityReference) {
//							final PreliminaryEntityReference preliminaryReference = (PreliminaryEntityReference) currentValue;
//
//							final String typeSignature = preliminaryReference.getTypeSignature();
//							preliminaryReference.setTypeSignature(expandMode.expand(typeSignature));
//						} else if (currentValue instanceof EntityQuery) {
//							final EntityQuery entityQuery = (EntityQuery) currentValue;
//
//							final String typeSignature = entityQuery.getEntityTypeSignature();
//							entityQuery.setEntityTypeSignature(expandMode.expand(typeSignature));
//						}
//					}
//
//					@Override
//					public void onElementLeave(final GmTraversingContext context, final TraversingModelPathElement pathElement) throws GmTraversingException {
//						// leave empty
//					}
//				}).doFor(this.query);
//			}
//		} catch (final GmTraversingException e) {
//			e.printStackTrace();
//			throw new QueryTypeSignatureExpanderRuntimeException(e);
//		}
//
//		return this.query;
//	}
	// @formatter:on	
}
