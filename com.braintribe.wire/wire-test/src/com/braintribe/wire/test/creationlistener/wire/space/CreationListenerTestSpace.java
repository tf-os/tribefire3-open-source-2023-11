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
package com.braintribe.wire.test.creationlistener.wire.space;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.example.TreeNode;
import com.braintribe.wire.test.creationlistener.wire.contract.CreationListenerTestContract;

@Managed
public class CreationListenerTestSpace implements CreationListenerTestContract {
	@Override
	@Managed
	public TreeNode rootInstance() {
		TreeNode bean = new TreeNode();
		bean.subNodes.add(subInstance1());
		bean.subNodes.add(subInstance2());
		return bean;
	}
	
	@Managed
	private TreeNode subInstance1() {
		TreeNode bean = new TreeNode();
		bean.subNodes.add(sub1SubInstance());
		return bean;
	}
	
	@Managed
	private TreeNode subInstance2() {
		TreeNode bean = new TreeNode();
		return bean;
	}
	
	@Managed
	private TreeNode sub1SubInstance() {
		TreeNode bean = new TreeNode();
		return bean;
	}
	
}
