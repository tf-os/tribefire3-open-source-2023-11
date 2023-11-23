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
package tribefire.extension.graphux.graph_ux.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.gm.ux.decorator.StyleDecorator;
import com.braintribe.gm.ux.graph.Graph;
import com.braintribe.gm.ux.graph.Node;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.record.MapRecord;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

import tribefire.extension.graphux.model.deployment.metadata.GraphUxNodeStyle;
import tribefire.extension.graphux.model.deployment.metadata.GraphUxNonRecursion;
import tribefire.extension.graphux.model.deployment.metadata.GraphUxPropertyAsNode;
import tribefire.extension.graphux.model.service.GetEntityAsGraph;

public class GraphUxTest extends GraphUxTestBase {
	private static Logger logger = Logger.getLogger(GraphUxTest.class);
	
	@Test
	public void testGraphUxProcessor() {
		GetEntityAsGraph request = GetEntityAsGraph.T.create();
		request.setDomainId("test.access");
		request.setEntityType("tribefire.extension.simple.model.data.Company");
		
		
		Graph result = (Graph) evaluator.eval(request).get();
		logger.info(result.toString());
		Assertions.assertThat(result.getNodes().size()).isEqualTo(7);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(12);
		
		request.setEntityType("tribefire.extension.simple.model.data.Department");
		
		result = (Graph) evaluator.eval(request).get();
		logger.info(result.toString());
		Assertions.assertThat(result.getNodes().size()).isEqualTo(7);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(12);
		
		request.setEntityType("tribefire.extension.simple.model.data.Person");
		
		result = (Graph) evaluator.eval(request).get();
		logger.info(result.toString());
		Assertions.assertThat(result.getNodes().size()).isEqualTo(4);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(1);
		
	}
	
	@Test
	public void testGraphUxProcessorWithId() {
		
		GetEntityAsGraph request = GetEntityAsGraph.T.create();
		request.setDomainId("test.access");
		request.setEntityType("tribefire.extension.simple.model.data.Department");
		request.setEntityId("1");
		
		Graph result = (Graph) evaluator.eval(request).get();
		Assertions.assertThat(result.getNodes().size()).isEqualTo(7);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(12);
		
		request.setEntityType("tribefire.extension.simple.model.data.Person");
		
		result = (Graph) evaluator.eval(request).get();
		Assertions.assertThat(result.getNodes().size()).isEqualTo(1);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(0);
		
	}
	
	@Test
	public void testGraphUxProcessorWidthMD() {
		
		HashMap<String, MetaData[]> mdData = getMdData(session);
		mdData.keySet().stream().forEach(key -> {
			Arrays.stream(mdData.get(key)).forEach(md ->{
				mdEditor.onEntityType(key).addMetaData(md);
			});
			
		});
		
		
		GetEntityAsGraph request = GetEntityAsGraph.T.create();
		request.setDomainId("test.access");
		request.setEntityType("tribefire.extension.simple.model.data.Company");
		
		
		Graph result = (Graph) evaluator.eval(request).get();
		Assertions.assertThat(result.getNodes().size()).isEqualTo(7);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(7);
		
		request.setEntityType("tribefire.extension.simple.model.data.Department");
		
		result = (Graph) evaluator.eval(request).get();
		Assertions.assertThat(result.getNodes().size()).isEqualTo(5);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(4);
		
		request.setEntityId("1");
		
		result = (Graph) evaluator.eval(request).get();
		Assertions.assertThat(result.getNodes().size()).isEqualTo(3);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(2);
		
		
		mdData.keySet().stream().forEach(key -> {
			Arrays.stream(mdData.get(key)).forEach(md ->{
				mdEditor.onEntityType(key).removeMetaData((m) -> m == md);
			});
			
		});
	}
	
	@Test
	public void testGraphUxProcessorWidthPropertyMD() {
		GraphUxPropertyAsNode mds = session.create(GraphUxPropertyAsNode.T);
		mdEditor.onEntityType("tribefire.extension.simple.model.data.Company").addPropertyMetaData("departments", mds);
		
		mdEditor.onEntityType("tribefire.extension.simple.model.data.Department").addPropertyMetaData("numberOfEmployees", mds);
		
		
		GetEntityAsGraph request = GetEntityAsGraph.T.create();
		request.setDomainId("test.access");
		request.setEntityType("tribefire.extension.simple.model.data.Company");
		
		
		Graph result = (Graph) evaluator.eval(request).get();
		Assertions.assertThat(result.getNodes().size()).isEqualTo(8);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(9);
		
		request.setEntityType("tribefire.extension.simple.model.data.Department");
		
		result = (Graph) evaluator.eval(request).get();
		logger.info(result.toString());
		Assertions.assertThat(result.getNodes().size()).isEqualTo(9);
		Assertions.assertThat(result.getEdges().size()).isEqualTo(12);
		
		mdEditor.onEntityType("tribefire.extension.simple.model.data.Company").removePropertyMetaData("departments", (md) -> md == mds);
		mdEditor.onEntityType("tribefire.extension.simple.model.data.Department").removePropertyMetaData("numberOfEmployees", (md) -> md == mds);
	
	}
	
	@Test
	public void testGraphUxProcessorWidthStyleMetadata() {
		GraphUxNodeStyle mds = session.create(GraphUxNodeStyle.T);
		MapRecord mapStyle = session.create(MapRecord.T);
		mapStyle.put("sharp", "circle");
		mapStyle.put("color", "red");
		mapStyle.put("size", "xl");
		mds.setStyleMap(mapStyle);
		mdEditor.onEntityType("tribefire.extension.simple.model.data.Company").addMetaData(mds);
		
		GetEntityAsGraph request = GetEntityAsGraph.T.create();
		request.setDomainId("test.access");
		request.setEntityType("tribefire.extension.simple.model.data.Company");
		
		
		Graph result = (Graph) evaluator.eval(request).get();
		List<Node> wirhDecorators = result.getNodes().stream().filter((n) -> n.getDecorators().size() > 0).collect(Collectors.toList());
		Assertions.assertThat(wirhDecorators.size()).isEqualTo(1);
		Assertions.assertThat(wirhDecorators.get(0).getName()).isEqualTo("Acme");
		Assertions.assertThat(wirhDecorators.get(0).getDecorators().size()).isEqualTo(1);
		StyleDecorator sd = (StyleDecorator) wirhDecorators.get(0).getDecorators().toArray()[0];
		Assertions.assertThat(sd.getName()).isEqualTo("style");
		Assertions.assertThat(sd.getStyleMap().get("sharp")).isEqualTo(mapStyle.get("sharp"));
		Assertions.assertThat(sd.getStyleMap().get("color")).isEqualTo(mapStyle.get("color"));
		Assertions.assertThat(sd.getStyleMap().get("size")).isEqualTo(mapStyle.get("size"));
		
		mdEditor.onEntityType("tribefire.extension.simple.model.data.Company").removeMetaData((md) -> md == mds);
	}
	
	
	private HashMap<String, MetaData[]> getMdData(PersistenceGmSession cortexSession) {
		HashMap<String, MetaData[]> mdData = new HashMap<String, MetaData[]>();
		
		GraphUxNonRecursion guRMreta = cortexSession.create(GraphUxNonRecursion.T);
		
		MetaData[] companyMd = {guRMreta};
		mdData.put("tribefire.extension.simple.model.data.Company", companyMd);
		
		MetaData[] departrmentMd = {guRMreta};
		mdData.put("tribefire.extension.simple.model.data.Department", departrmentMd);
		
		return mdData;
	}
	
	
}
