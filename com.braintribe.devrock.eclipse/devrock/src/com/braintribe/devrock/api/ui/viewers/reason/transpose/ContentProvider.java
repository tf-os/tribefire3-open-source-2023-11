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
package com.braintribe.devrock.api.ui.viewers.reason.transpose;

import java.util.Collections;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.eclipse.model.resolution.nodes.ReasonNode;

/**
 * content provider for the tree viewer in the page
 * 
 * @author pit
 *
 */
public class ContentProvider implements ITreeContentProvider {
	
	private ReasonNode reasonNode;
	
	public void setupFrom(ReasonNode reasonNode) {
		this.reasonNode = reasonNode;	
	}
	

	@Override
	public Object[] getChildren(Object obj) {
		ReasonNode r = (ReasonNode) obj;
		return r.getChildren().toArray();
	}

	@Override
	public Object[] getElements(Object obj) {		
		return Collections.singletonList( reasonNode).toArray();				
	}

	private ReasonNode findParent( ReasonNode current, ReasonNode suspect) {
		if (current == suspect)
			return null;
		if (current.getChildren().contains(suspect))
			return current;
		for (Node child : current.getChildren()) {
			if (child instanceof ReasonNode) {
				ReasonNode p = findParent((ReasonNode) child, suspect);
				if (p == null)
					return p;
				}
		}
		return null;
	}
	
	@Override
	public Object getParent(Object obj) {
		ReasonNode r = (ReasonNode) obj;
		return findParent( reasonNode, r);
	}

	@Override
	public boolean hasChildren(Object obj) {
		ReasonNode r = (ReasonNode) obj;
		return r.getChildren().size() > 0;
	}
	

}
