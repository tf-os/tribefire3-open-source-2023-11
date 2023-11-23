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
package com.braintribe.devrock.zed.ui.viewer.extraction;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.braintribe.devrock.zarathud.model.common.Node;

/**
 * @author pit
 *
 */
public class ContentProvider implements ITreeContentProvider {
	
	private List<Node> nodes;

	public void setupFrom( List<Node> nodes) {
		this.nodes = nodes;		
	}

	@Override
	public Object[] getChildren(Object arg0) {
		if (arg0 instanceof Node)  {
			Node node = (Node) arg0;
			return node.getChildren().toArray();
		}		
		return new Object[0];		
	}

	@Override
	public Object[] getElements(Object arg0) {		
		return nodes.toArray();
	}

	@Override
	public Object getParent(Object arg0) {
		return null;
	}

	@Override
	public boolean hasChildren(Object arg0) {
		if (arg0 instanceof Node)  {
			Node node = (Node) arg0;
			return node.getChildren().size() > 0;		
		}

		return false;		
	}
	

}
