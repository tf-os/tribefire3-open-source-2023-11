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
package com.braintribe.devrock.api.ui.viewers.reason;

import java.util.Collections;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.braintribe.gm.model.reason.Reason;

/**
 * content provider for the tree viewer in the page
 * 
 * @author pit
 *
 */
public class ContentProvider implements ITreeContentProvider {
	
	private Reason reason;
	
	public void setupFrom(Reason reason) {
		this.reason = reason;	
	}
	

	@Override
	public Object[] getChildren(Object obj) {
		Reason r = (Reason) obj;
		return r.getReasons().toArray();
	}

	@Override
	public Object[] getElements(Object obj) {		
		return Collections.singletonList( reason).toArray();
		//return getChildren(obj);
		
	}

	private Reason findParent( Reason current, Reason suspect) {
		if (current == suspect)
			return null;
		if (current.getReasons().contains(suspect))
			return current;
		for (Reason child : current.getReasons()) {
			Reason p = findParent(child, suspect);
			if (p == null)
				return p;
		}
		return null;
	}
	
	@Override
	public Object getParent(Object obj) {
		Reason r = (Reason) obj;
		return findParent( reason, r);
	}

	@Override
	public boolean hasChildren(Object obj) {
		Reason r = (Reason) obj;
		return r.getReasons().size() > 0;
	}
	

}
