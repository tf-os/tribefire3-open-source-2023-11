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
package com.braintribe.model.processing.query.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.processing.query.api.shortening.SignatureExpert;
import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.query.api.stringifier.experts.resolver.SelectionAliasResolver;
import com.braintribe.model.processing.query.selection.experts.AbstractAliasResolver;
import com.braintribe.model.processing.query.selection.experts.PathAliasResolver;
import com.braintribe.model.processing.query.selection.experts.SimpleAliasResolver;
import com.braintribe.model.processing.query.shortening.Qualified;
import com.braintribe.model.processing.query.shortening.Simplified;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;

public class BasicQuerySelectionResolver {
	
	private SignatureExpert shortening = new Simplified();
	private SelectionAliasResolver aliasResolver = null;
	
	private BasicQuerySelectionResolver() {
	}
	
	private void setAliasResolver(SelectionAliasResolver aliasResolver) {
		this.aliasResolver = aliasResolver;
	}
	private void setShortening(SignatureExpert shortening) {
		this.shortening = shortening;
	}
	
	public static BasicQuerySelectionResolver create() {
		return new BasicQuerySelectionResolver();
	}

	public SelectionAliasBuilder aliasMode() {
		return new SelectionAliasBuilder(this);
	}

	public ShorteningBuilder shorteningMode() {
		return new ShorteningBuilder(this);
	}
	
	
	public List<QuerySelection> resolve (SelectQuery query) {
		
		BasicQueryStringifier stringifier =	
				BasicQueryStringifier.create()
										.shorteningMode()
											.custom(shortening);
										
		
		SelectionAliasResolver aliasResolver = ensureAliasResolver();
		
		List<QuerySelection> selections = new ArrayList<>();
		List<Object> operands = query.getSelections();
		if (operands.isEmpty()) {
			// No explicit selections defined. Calculating from froms
			for (From from : query.getFroms())
				selections.addAll(getSelections(from, aliasResolver));
		} else {
			Map<String, String> calculatedPropertyNames = new HashMap<>();
			for (Object operand : operands) {
				StringBuilder alias = new StringBuilder();
				if (!(operand instanceof PropertyOperand))
					alias.append(stringifier.stringify(operand));
				else {
					PropertyOperand propertyOperand = (PropertyOperand) operand;
					String propertyName = propertyOperand.getPropertyName();
					Source source = propertyOperand.getSource();
					
					alias.append(aliasResolver.getAliasForSource(source));
					
					if (propertyName != null) {
						String calculatedPropertyName = aliasResolver.getPropertyNameForSource(source, propertyName);
						alias.append('.');
						alias.append(calculatedPropertyName);
						
						if (!calculatedPropertyName.equals(propertyName))
							calculatedPropertyNames.put(alias.toString(), calculatedPropertyName);
					}
				}
				
				selections.add(new SelectionImpl(alias.toString(), operand));
			}
			
			if (!calculatedPropertyNames.isEmpty())
				handlePrefix(calculatedPropertyNames, selections);
			
		}
		
		return selections;
	}
	
	private void handlePrefix(Map<String, String> calculatedPropertyNames, List<QuerySelection> selections) {
		for (QuerySelection querySelection : selections) {
			if (!(querySelection instanceof SelectionImpl))
				continue;
			
			SelectionImpl selectionImpl = (SelectionImpl) querySelection;
			String calculatedPropertyName = calculatedPropertyNames.remove(selectionImpl.getAlias());
			if (calculatedPropertyName == null)
				continue;
			
			if (!calculatedPropertyNames.containsValue(calculatedPropertyName)) {
				//Name is not duplicated. In this case, we use the calculated property name as alias
				selectionImpl.alias = calculatedPropertyName;
			}
		}
		
	}

	private List<QuerySelection> getSelections(Source source, SelectionAliasResolver aliasResolver) {
		List<QuerySelection> selections = new ArrayList<QuerySelection>();
		String alias = aliasResolver.getAliasForSource(source);
		selections.add(new SelectionImpl(alias, source));
		for (Join join : source.getJoins()) {
			selections.addAll(getSelections(join, aliasResolver));
		}
		return selections;
	}
	
	private SelectionAliasResolver ensureAliasResolver() {
		if (aliasResolver == null) {
			this.aliasResolver = new SimpleAliasResolver();
		}
		if (this.aliasResolver instanceof AbstractAliasResolver) {
			AbstractAliasResolver basicResolver = (AbstractAliasResolver) aliasResolver;
			if (basicResolver.getShortening() == null) {
				basicResolver.setShortening(this.shortening);
			}
		}
		return this.aliasResolver;
	}

	public class SelectionImpl implements QuerySelection {
		private final Object operand;
		private String alias;
		
		public SelectionImpl(String alias, Object operand) {
			this.operand = operand;
			this.alias = alias;
		}
		
		@Override
		public Object getOperand() {
			return operand;
		}
		
		@Override
		public String getAlias() {
			return alias;
		}
	}
	
	public class SelectionAliasBuilder {
		
		private BasicQuerySelectionResolver selectionResolver = null;
		
		public SelectionAliasBuilder(BasicQuerySelectionResolver selectionResolver) {
			this.selectionResolver = selectionResolver;
		}
		
		public BasicQuerySelectionResolver simple() {
			selectionResolver.setAliasResolver(new SimpleAliasResolver());
			return selectionResolver;
		}

		public BasicQuerySelectionResolver path() {
			selectionResolver.setAliasResolver(new PathAliasResolver());
			return selectionResolver;
		}

		public BasicQuerySelectionResolver custom(SelectionAliasResolver aliasResolver) {
			selectionResolver.setAliasResolver(aliasResolver);
			return selectionResolver;
		}
		
	}
	
	public class ShorteningBuilder {
		
		private BasicQuerySelectionResolver selectionResolver = null;
		
		public ShorteningBuilder(BasicQuerySelectionResolver stringifier) {
			this.selectionResolver = stringifier;
		}
		
		public BasicQuerySelectionResolver simplified() {
			selectionResolver.setShortening(new Simplified());
			return selectionResolver;
		}

		/**
		 * @param session
		 *            persistence session
		 */
		public BasicQuerySelectionResolver smart(PersistenceGmSession session) {
			selectionResolver.setShortening(new Simplified());
			return selectionResolver;
		}

		public BasicQuerySelectionResolver qualified() {
			selectionResolver.setShortening(new Qualified());
			return selectionResolver;
		}

		public BasicQuerySelectionResolver custom(SignatureExpert shortening) {
			selectionResolver.setShortening(shortening);
			return selectionResolver;
		}

		
	}
	
	
}
