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
package com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.theme.base.client.tree.TreeBaseAppearance;
import com.sencha.gxt.theme.blue.client.tree.BlueTreeAppearance.BlueTreeResources;

public class TreeWithoutJointAppearance extends TreeBaseAppearance {
	
	public interface TreeWithoutJointStyle extends TreeBaseStyle {
		String treeWithoutJoint();
    }
	
	public interface TreeWithoutJointResources extends BlueTreeResources {
		@Override
		@Source({"com/sencha/gxt/theme/base/client/tree/Tree.gss", "com/sencha/gxt/theme/blue/client/tree/TreeDefault.gss", "TreeWithoutJoint.gss"})
		TreeWithoutJointStyle style();
	}
	
	public TreeWithoutJointAppearance() {
		super((TreeWithoutJointResources) GWT.create(TreeWithoutJointResources.class));
	}
	
	@Override
	public void render(SafeHtmlBuilder sb) {
		sb.appendHtmlConstant("<div class=" + getStyle().tree() + " style=\"position: relative;\">"
				+ "<table cellpadding=0 cellspacing=0 width=100%><tr><td class='gxtReset'></td></tr></table>" + "</div>");
	}
	
	private native TreeBaseStyle getStyle() /*-{
		return this.@com.sencha.gxt.theme.base.client.tree.TreeBaseAppearance::style;
	}-*/;

}
