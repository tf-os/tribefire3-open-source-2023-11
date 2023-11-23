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

public class JseStatementSequence extends JseNode {
	
	protected JseScriptNode anchor = new JseScriptNode();
	protected JseScriptNode last = anchor;
	
	public void append(JseNode node) {
		JseScriptNode listNode = new JseScriptNode();
		listNode.node = node;
		last.next = listNode;
		last = listNode;
	}
	
	@Override
	public void write(CountingWriter writer) throws MarshallException, IOException {
		JseScriptNode listNode = anchor.next;

		int lastCount = writer.getCount();
		
		while (listNode != null) {
			int d = writer.getCount() - lastCount;
			if (d > 160) {
				writer.writeBreakingPoint();
				lastCount = writer.getCount();
			}
			else if (d == 0) {
				writer.writeBreakingPoint();
			}
			
			listNode.node.write(writer);
			listNode = listNode.next;
		}
	}
}
