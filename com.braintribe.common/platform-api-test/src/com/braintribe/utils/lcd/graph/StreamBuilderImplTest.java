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
package com.braintribe.utils.lcd.graph;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.graph.StreamBuilderImplTest.Node.node;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.utils.lcd.graph.StreamBuilder.StreamBuilder2;
import com.braintribe.utils.lcd.graph.StreamBuilder.TraversalOrder;

/**
 * Tests for {@link StreamBuilderImpl}.
 *
 * @author michael.lafite
 */
public class StreamBuilderImplTest {

	Node root;

	@Test
	public void singleNode() {
		root = node("root");

		List<Node> list = streamRootNode() //
				.please() //
				.collect(Collectors.toList());

		assertThat(list).containsExactly(root);
	}

	@Test
	public void smallTree() {
		Node left = node("left");
		Node right = node("right");

		root = node("root", left, right);

		List<Node> list = streamRootNode() //
				.please() //
				.collect(Collectors.toList());

		assertThat(list).containsExactly(left, right, root);
	}

	@Test
	public void smallTree_PreOrder() {
		Node left = node("left");
		Node right = node("right");

		root = node("root", left, right);

		List<Node> list = streamRootNode() //
				.withOrder(TraversalOrder.preOrder) //
				.please() //
				.collect(Collectors.toList());

		assertThat(list).containsExactly(root, left, right);
	}

	@Test
	public void biggerTree() {
		Node leftLeft = node("leftLeft");
		Node leftRight = node("leftRight");

		Node rightLeft = node("rightLeft");
		Node rightRight = node("rightRight");

		Node left = node("left", leftLeft, leftRight);
		Node right = node("right", rightLeft, rightRight);

		root = node("root", left, right);

		List<Node> list = streamRootNode() //
				.please() //
				.collect(Collectors.toList());

		assertThat(list).containsExactly(leftLeft, leftRight, left, rightLeft, rightRight, right, root);
	}

	@Test
	public void biggerTree_PreOrder() {
		Node leftLeft = node("leftLeft");
		Node leftRight = node("leftRight");

		Node rightLeft = node("rightLeft");
		Node rightRight = node("rightRight");

		Node left = node("left", leftLeft, leftRight);
		Node right = node("right", rightLeft, rightRight);

		root = node("root", left, right);

		List<Node> list = streamRootNode() //
				.withOrder(TraversalOrder.preOrder) //
				.please() //
				.collect(Collectors.toList());

		assertThat(list).containsExactly(root, left, leftLeft, leftRight, right, rightLeft, rightRight);
	}

	// #############################################
	// ## . . . . . . . . Helpers . . . . . . . . ##
	// #############################################

	private StreamBuilder2<Node> streamRootNode() {
		return new StreamBuilderImpl<>(root) //
				.withNeighbors(Node::getNeighbors);
	}

	static class Node {

		public String name;
		public List<Node> neighbors;

		private Node(String name, Node... neighbors) {
			this(name, asList(neighbors));
		}

		private Node(String name, List<Node> neighbors) {
			this.name = name;
			this.neighbors = neighbors;
		}

		public static Node node(String name, Node... neighbors) {
			return new Node(name, neighbors);
		}

		public static Node node(String name, List<Node> neighbors) {
			return new Node(name, neighbors);
		}

		public List<Node> getNeighbors() {
			return neighbors;
		}

		@Override
		public String toString() {
			return "N[" + name + "]";
		}

	}

}
