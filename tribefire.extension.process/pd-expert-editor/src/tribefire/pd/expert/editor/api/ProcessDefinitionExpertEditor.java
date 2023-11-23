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
package tribefire.pd.expert.editor.api;

import java.util.function.Predicate;

import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.pd.expert.editor.impl.BasicProcessDefinitionExpertEditor;

public interface ProcessDefinitionExpertEditor {
	
	ProcessDefinition definition();
	
	ProcessElementEditor elements();
	
	ProcessElementEditor elements(Predicate<? super ProcessElement> filter);
	
	NodeEditor node(Object state);
	
	NodeEditor nodes();

	NodeEditor nodes(Object... states);
	
	NodeEditor nodes(Predicate<? super Node> filter);
	
	StandardNodeEditor standardNode(Object state);
	
	StandardNodeEditor standardNodes();
	
	StandardNodeEditor standardNodes(Object... states);
	
	StandardNodeEditor standardNodes(Predicate<? super Node> filter);
	
	EdgeEditor edge(String name);
	
	EdgeEditor edge(Object fromState, Object toState);
	
	EdgeEditor edgesFromState(Object fromState);

	EdgeEditor edgesToState(Object toState);
	
	EdgeEditor edges(Predicate<? super Edge> filter);
	
	EdgeEditor edges();
	
	ConditionalEdgeEditor conditionalEdge(String name);
	
	ConditionalEdgeEditor conditionalEdges(Object fromState, Object toState);
	
	ConditionalEdgeEditor conditionalEdgesFromState(Object fromState);
	
	ConditionalEdgeEditor conditionalEdgesToState(Object toState);
	
	ConditionalEdgeEditor conditionalEdges(Predicate<? super ConditionalEdge> filter);
	
	ConditionalEdgeEditor conditionalEdges();
	
	static ProcessDefinitionExpertEditor of(ProcessDefinition processDefinition) {
		return new BasicProcessDefinitionExpertEditor(processDefinition);
	}
}
