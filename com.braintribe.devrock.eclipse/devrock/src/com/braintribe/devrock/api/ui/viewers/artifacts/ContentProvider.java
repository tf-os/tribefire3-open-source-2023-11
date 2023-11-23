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
package com.braintribe.devrock.api.ui.viewers.artifacts;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;


/**
 * {@link ITreeContentProvider} for the {@link TransposedAnalysisArtifactViewer}
 * @author pit
 *
 */
public class ContentProvider implements ITreeContentProvider {

	private List<Node> nodes;
	
	@Configurable @Required
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	List<Node> getCurrentNodes() {
		return nodes;
	}
			
	@Override
	public Object[] getChildren(Object arg0) {
		Node node = (Node) arg0;
		
		return node.getChildren().toArray();
		
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
		Object[] children = getChildren(arg0);
		return children != null && children.length > 0;
	}
	

}
