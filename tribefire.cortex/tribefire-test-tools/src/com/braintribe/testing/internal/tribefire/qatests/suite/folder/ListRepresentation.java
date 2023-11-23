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
package com.braintribe.testing.internal.tribefire.qatests.suite.folder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ListRepresentation<E> implements GmInstanceRepresentation<List<E>> {
	private static Logger logger = Logger.getLogger(ListRepresentation.class);
	
	private final List<GmInstanceRepresentation<Object>> content;

	public ListRepresentation() {
		content = new ArrayList<>();
	}

	@Override
	public boolean matches(List<E> actual) {
		if (actual.size() != content.size())
			return false;
			
		Iterator<E> actualIterator = actual.iterator();
		Iterator<GmInstanceRepresentation<Object>> contentIterator = content.iterator();
		
		while (actualIterator.hasNext()) {
			E actualElement = actualIterator.next();
			GmInstanceRepresentation<Object> contentElement = contentIterator.next();
			
			if (!contentElement.matches(actualElement))
				return false;
		}
		
		return true;
	}

	@Override
	public List<E> create(PersistenceGmSession session) {
		return null;
		
	}

}
