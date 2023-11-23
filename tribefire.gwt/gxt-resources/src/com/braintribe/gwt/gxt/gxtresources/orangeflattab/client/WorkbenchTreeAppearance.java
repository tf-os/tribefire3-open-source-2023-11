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
package com.braintribe.gwt.gxt.gxtresources.orangeflattab.client;

import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.TreeWithoutJointAppearance.TreeWithoutJointResources;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.TreeWithoutJointAppearance.TreeWithoutJointStyle;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.AbstractImagePrototype.ImagePrototypeElement;
import com.sencha.gxt.theme.base.client.tree.TreeBaseAppearance;
import com.sencha.gxt.widget.core.client.tree.TreeStyle;
import com.sencha.gxt.widget.core.client.tree.Tree.CheckState;
import com.sencha.gxt.widget.core.client.tree.Tree.Joint;
import com.sencha.gxt.widget.core.client.tree.TreeView.TreeViewRenderMode;

public class WorkbenchTreeAppearance extends TreeBaseAppearance {
	private static final String TREE_TEXT_CLASS_NAME = "gmeTreeText";	
	private static final String WORKBENCH_TABLE_CLASS_NAME = "gmeWorkbenchTable";	
	private static final String WORKBENCH_TREE_CLASS_NAME = "gmeWorkbenchTree";	
	
	public interface WorkbenchTreeAppearanceStyle extends TreeWithoutJointStyle {
		String gmeTreeText();
		String gmeWorkbenchTable();
	}

	public interface WorkbenchTreeAppearanceResources extends TreeWithoutJointResources {
		@Override
		@Source({ "WorkbenchTree.gss", "com/sencha/gxt/theme/base/client/tree/Tree.gss", "com/sencha/gxt/theme/blue/client/tree/TreeDefault.gss",
				"com/braintribe/gwt/gxt/gxtresources/gridwithoutlines/client/TreeWithoutJoint.gss" })
		WorkbenchTreeAppearanceStyle style();
	}

	public WorkbenchTreeAppearance() {
		super((WorkbenchTreeAppearanceResources) GWT.create(WorkbenchTreeAppearanceResources.class));
	}
	
	@Override
	public void render(SafeHtmlBuilder sb) {
		TreeBaseStyle style = getStyle();
		if (style instanceof WorkbenchTreeAppearanceStyle)
			sb.appendHtmlConstant("<div class=\"" + getStyle().tree() +" "+ WORKBENCH_TREE_CLASS_NAME + "\" style=\"position: relative;\">"
	                          + "<table class=\""+ ((WorkbenchTreeAppearanceStyle) style).gmeWorkbenchTable() + " " + WORKBENCH_TABLE_CLASS_NAME +"\" cellpadding=0 cellspacing=0 width=100%><tr><td class='gxtReset'></td></tr></table>"
	                          + "</div>");
		else
			sb.appendHtmlConstant("<div class=\"" + getStyle().tree() +" "+ WORKBENCH_TREE_CLASS_NAME + "\" style=\"position: relative;\">"
                    + "<table class=\""+ WORKBENCH_TABLE_CLASS_NAME +"\" cellpadding=0 cellspacing=0 width=100%><tr><td class='gxtReset'></td></tr></table>"
                    + "</div>");
			
    }		
	
	//Overriden to add a new exported style to the tree node and icon
	@Override
	public void renderNode(SafeHtmlBuilder sb, String id, SafeHtml html, TreeStyle ts, ImageResource icon, boolean checkable, CheckState checked,
			Joint joint, int level, TreeViewRenderMode renderMode) {
		TreeBaseStyle style = getStyle();
		if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.BUFFER_WRAP) {
			//This is the first change here. Adding the new exported node style
			sb.appendHtmlConstant("<div id=\"" + SafeHtmlUtils.htmlEscape(id) + "\" class=\"" + style.node() + " gmeTreeNode\">");

			sb.appendHtmlConstant("<div class=\"" + style.element() + "\">");
		}

		if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.BUFFER_BODY) {
			sb.appendHtmlConstant(getIndentMarkup(level));

			TreeResources resources = getResources();
			ImagePrototypeElement jointProtoEl = DOM.createDiv().cast();
			jointProtoEl.addClassName(style.joint());
			switch (joint) {
				case COLLAPSED:
					AbstractImagePrototype.create(ts.getJointCloseIcon() == null ? resources.jointCollapsedIcon() : ts.getJointCloseIcon())
							.applyTo(jointProtoEl);
					break;
				case EXPANDED:
					AbstractImagePrototype.create(ts.getJointOpenIcon() == null ? resources.jointExpandedIcon() : ts.getJointOpenIcon())
							.applyTo(jointProtoEl);
					break;
				default:
					break;
			}
			sb.appendHtmlConstant(jointProtoEl.getString());

			ImagePrototypeElement checkProtoEl = DOM.createDiv().cast();
			checkProtoEl.addClassName(style.check());
			if (checkable) {
				switch (checked) {
					case CHECKED:
						AbstractImagePrototype.create(resources.checked()).applyTo(checkProtoEl);
						break;
					case UNCHECKED:
						AbstractImagePrototype.create(resources.unchecked()).applyTo(checkProtoEl);
						break;
					case PARTIAL:
						AbstractImagePrototype.create(resources.partialChecked()).applyTo(checkProtoEl);
						break;
				}
			}
			sb.appendHtmlConstant(checkProtoEl.getString());

			ImagePrototypeElement iconProtoEl = DOM.createDiv().cast();
			iconProtoEl.addClassName(style.icon());
			iconProtoEl.addClassName("gmeTreeIcon");
			if (icon != null) {
				AbstractImagePrototype.create(icon).applyTo(iconProtoEl);
			}
			sb.appendHtmlConstant(iconProtoEl.getString());

			//This is the second change here. Adding the new exported text style
			if (resources instanceof WorkbenchTreeAppearanceResources)
				sb.appendHtmlConstant("<div class=\"" + style.text() + " "+ ((WorkbenchTreeAppearanceResources) resources).style().gmeTreeText() +" "+ TREE_TEXT_CLASS_NAME + "\">" + html.asString() + "</div>");			
			else	
				sb.appendHtmlConstant("<div class=\"" + style.text() + " "+ TREE_TEXT_CLASS_NAME +"\">" + html.asString() + "</div>");
		}

		if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.BUFFER_WRAP) {
			sb.appendHtmlConstant("</div>");
			sb.appendHtmlConstant("</div>");
		}
	}
	
	private native TreeBaseStyle getStyle() /*-{
		return this.@com.sencha.gxt.theme.base.client.tree.TreeBaseAppearance::style;
	}-*/;
	
	private native TreeResources getResources() /*-{
		return this.@com.sencha.gxt.theme.base.client.tree.TreeBaseAppearance::resources;
	}-*/;

}
