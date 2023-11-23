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
package com.braintribe.codec.marshaller.jse.tree;

import java.io.IOException;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.jse.CountingWriter;

public class JseScript extends JseNode {
	
	protected JseScriptNode anchor = new JseScriptNode();
	protected JseScriptNode last = anchor;
	
	public void append(JseNode node) {
		JseScriptNode listNode = new JseScriptNode();
		listNode.node = node;
		last.next = listNode;
		last = listNode;
	}
	
	public void append(JseNode node, boolean breakPotential) {
		JseScriptNode listNode = new JseScriptNode();
		listNode.breakPotential = breakPotential;
		listNode.node = node;
		last.next = listNode;
		last = listNode;
	}

	@Override
	public void write(CountingWriter writer) throws MarshallException, IOException {
		JseScriptNode listNode = anchor.next;

		while (listNode != null) {
			if (listNode.breakPotential) {
				writer.writeBreakingPoint();
			}
			else {
				writer.write('\n');
			}
			listNode.node.write(writer);
			listNode = listNode.next;
		}
	}

}
