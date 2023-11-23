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
package tribefire.extension.graphux.deployables.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.braintribe.gm.ux.decorator.ArrowDecorator;
import com.braintribe.gm.ux.decorator.ArrowType;
import com.braintribe.gm.ux.decorator.Decorator;
import com.braintribe.gm.ux.decorator.StyleDecorator;
import com.braintribe.gm.ux.decorator.ValueDecorator;
import com.braintribe.gm.ux.graph.Edge;
import com.braintribe.gm.ux.graph.Graph;
import com.braintribe.gm.ux.graph.Node;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

import tribefire.extension.graphux.model.deployment.metadata.GraphUxHidden;
import tribefire.extension.graphux.model.deployment.metadata.GraphUxNodeStyle;
import tribefire.extension.graphux.model.deployment.metadata.GraphUxNonRecursion;
import tribefire.extension.graphux.model.deployment.metadata.GraphUxPropertyAsNode;
import tribefire.extension.graphux.model.deployment.metadata.GraphUxValueAsDecorator;
import tribefire.extension.graphux.model.service.GetEntityAsGraph;
import tribefire.extension.graphux.model.service.GraphUxServiceRequest;

public class GraphUxServiceProcessor implements AccessRequestProcessor<GraphUxServiceRequest, Graph> {
	private static Logger logger = Logger.getLogger(GraphUxServiceProcessor.class);
	
	private HashMap<String, Node> nodeMap = new HashMap<String, Node>();
	private HashSet<Edge> edgeSet = new HashSet<Edge>();
//	private ArrayList<String> postEdgeRemove = new ArrayList<String>();
	private ModelMdResolver mdr;
	
	private CmdResolver cmdResolver;
	
	public void setMdr(ModelMdResolver mdr) {
		this.mdr = mdr;
	}

	public void setCmdResolver(CmdResolver cmdResolver) {
		this.cmdResolver = cmdResolver;
	}
	
	private AccessRequestProcessor<GraphUxServiceRequest, Graph> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(GetEntityAsGraph.T, this::getEntityAsGraph);
	});
	
	@Override
	public Graph process(AccessRequestContext<GraphUxServiceRequest> context) {
		return dispatcher.process(context);
	}
	
	private Graph getEntityAsGraph(AccessRequestContext<GetEntityAsGraph> context) {
		GetEntityAsGraph request = context.getRequest();
		ModelAccessory modelAccesory = context.getSession().getModelAccessory();
		setMdr(modelAccesory.getMetaData());
		setCmdResolver(modelAccesory.getCmdResolver());
		List<GenericEntity> entityList = getEntity(request.getEntityType(), request.getEntityId(), context.getSession());
		
		Graph response = getGraphFromEntity(entityList);
		response.setName(request.getEntityType());
		
		return response;
	}
	
	public List<GenericEntity> getEntity(String entityType, String entityId, PersistenceGmSession session ) {
		Object id = Objects.isNull(entityId) || entityId == "" ? entityId : cmdResolver.getIdType(entityType).instanceFromString(entityId);
		
		EntityQuery q = Objects.isNull(entityId) || entityId == ""
				? EntityQueryBuilder.from(entityType).done() 
				: EntityQueryBuilder.from(entityType).where().property("id").eq(id).done();
				
		return session.query().entities(q).list();
	}

	public Graph getGraphFromEntity(List<GenericEntity> entityList) {
		nodeMap.clear();
		edgeSet.clear();
		
		Graph response = Graph.T.create();
		entityList.stream().forEach(this::mapEntityToNode);
		
		response.getNodes().addAll(nodeMap.values());
		response.getEdges().addAll(edgeSet);
		
		nodeMap.clear();
		edgeSet.clear();
		return response;
	}
	
	private Node mapEntityToNode(GenericEntity entity) {
		
		if (nodeMap.containsKey(getMapKey(entity))) {
			logger.info("Exist: " + entity.toSelectiveInformation());
			return nodeMap.get(getMapKey(entity));
		}

		GraphUxNonRecursion nonRecursive = (GraphUxNonRecursion) getMdData(GraphUxNonRecursion.T.cast(), entity);
		GraphUxNodeStyle nodeStyle = (GraphUxNodeStyle) getMdData(GraphUxNodeStyle.T.cast(), entity);
		
		Node node = createNode(entity);
		
		logger.info("Type: " + entity.entityType());
		logger.info("Properties: " + entity.entityType().getProperties());
		// Add Edges And nodes
		entity.entityType().getProperties().stream().forEach((p) -> {
			GraphUxHidden hidden = (GraphUxHidden) getPropsMdData(GraphUxHidden.T.cast(), entity, p);
			GraphUxPropertyAsNode asNode = (GraphUxPropertyAsNode) getPropsMdData(GraphUxPropertyAsNode.T.cast(), entity, p);
			GraphUxValueAsDecorator asDecorator = (GraphUxValueAsDecorator) getPropsMdData(GraphUxValueAsDecorator.T.cast(), entity, p);
			if (Objects.isNull(hidden)) {
				if (p.getType().isCollection() && Objects.nonNull(p.get(entity))) {
					Node collNode = asNode != null ? addExtraNode(node, p, entity) : node;
					
					Collection<GenericEntity> enityColl = p.get(entity);
					enityColl.stream().forEach(e -> {
						if (asNode == null) {
							node.getEdges().add(getEdge(p.getName(), e, collNode, nonRecursive==null ? true : false));
						} else {
							node.getEdges().add(getEdge(p.getName(), e, collNode, false));
						}
					});
					if (asDecorator != null) {
						collNode.getDecorators().add(addDecorator(p, entity));
					}
				}
				
				if (p.getType().isEntity() && Objects.nonNull(p.get(entity)) ) {
					GenericEntity  e =  p.get(entity);
					node.getEdges().add(getEdge(p.getName(), e, node, nonRecursive==null ? true : false));
				}
				
				if (p.getType().isSimple() && Objects.nonNull(p.get(entity)) && asNode != null) {
					addExtraNode(node, p, entity);
				}
				if (p.getType().isSimple() && Objects.nonNull(p.get(entity)) && asDecorator != null) {
					node.getDecorators().add(addDecorator(p, entity));
				}
			}
			
		});
		if (Objects.nonNull(nodeStyle)) {
			node.getDecorators().add(addStyleDecorator(nodeStyle));
		}
		return node;
	}

	private Decorator addStyleDecorator(GraphUxNodeStyle nodeStyle) {
		StyleDecorator decorator = StyleDecorator.T.create();
		decorator.setName("style");
		decorator.setStyleMap(nodeStyle.getStyleMap());
		return decorator;
	}

	private Node addExtraNode(Node origin, Property p, GenericEntity entity) {
		Node newNode = Node.T.create();
		newNode.setName(origin.getName() + "" + p.getName());
		if (p.getType().isSimple()) newNode.setDescription(p.get(entity).toString());
		if (p.getType().isCollection()) newNode.setDescription("Count: " + ((Collection<GenericEntity>) p.get(entity)).size());
		nodeMap.put(newNode.getName(), newNode);
		Edge edge = Edge.T.create();
		edge.setName(origin.getName()+"-"+ p.getName());
		edge.setOrigin(origin);
		edge.setDestination(newNode);
		edgeSet.add(edge);
		origin.getEdges().add(edge);
		return newNode;
	}
	
	private Decorator addDecorator(Property p, GenericEntity entity) {
		ValueDecorator decorator = ValueDecorator.T.create();
		decorator.setName(p.getName());
		if (p.getType().isSimple()) decorator.setValue(p.get(entity).toString());
		if (p.getType().isCollection()) decorator.setValue("" + ((Collection<GenericEntity>) p.get(entity)).size());
		return decorator;
	}

	private MetaData getPropsMdData(EntityType<MetaData> entityType, GenericEntity entity, Property p) {
		return mdr.lenient(true)
		        .entity(entity)
		        .property(p)
		        .meta(entityType)
		        .exclusive();
	}

	private MetaData getMdData(EntityType<MetaData> entityType, GenericEntity entity) {
		return mdr.lenient(true)
		        .entity(entity)
		        .meta(entityType)
		        .exclusive();
	}

	private Edge getEdge(String name, GenericEntity destination, Node origin, boolean recursive) {
		Edge edge = Edge.T.create();
		edge.setName(name+":"+origin.getName()+"-"+ destination.toSelectiveInformation());
		edge.setOrigin(origin);
		if (recursive == true) {
			edge.setDestination(mapEntityToNode(destination));
		} else {
			edge.setDestination(createNode(destination));
		}
		edge.getDecorators().add(arrowDecorator(name));
		edgeSet.add(edge);
		return edge;
	}

	private Decorator arrowDecorator(String name) {
		ArrowDecorator ad = ArrowDecorator.T.create();
		ad.setName(name);
		ad.setArrowType(ArrowType.Open);
		return ad;
	}

	private Node createNode(GenericEntity entity) {
		Node node = Node.T.create();
		node.setName(entity.toSelectiveInformation());
		node.setDescription(entity.getId().toString());
		node.setPayload(entity);
		nodeMap.put(getMapKey(entity), node);
		return node;
	}

	private String getMapKey(GenericEntity entity) {
		return entity.globalReference().stringify();
		//  Objects.nonNull(entity.getGlobalId()) ? entity.getGlobalId() : entity.getId() + ":" + entity.entityType();
	}
}
