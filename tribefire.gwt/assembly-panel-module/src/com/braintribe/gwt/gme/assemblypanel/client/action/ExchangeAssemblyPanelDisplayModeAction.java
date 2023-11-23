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
package com.braintribe.gwt.gme.assemblypanel.client.action;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyPanel;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 * Action used for exchanging the {@link AssemblyPanel} display mode.
 * The modes are:
 * - Simple (Multiplex Dimension + Recursion Dimension);
 * - Detailed (Multiplex Dimension + Recursion Dimension + Detail Dimension)
 * - Flat (Multiplex Dimension + Detail Dimension)
 * @author michel.docouto
 *
 */
public class ExchangeAssemblyPanelDisplayModeAction extends Action {
	public static final String DISPLAY_MODE = "displayMode";
	
	public enum DisplayMode {
		Simple, Detailed, Flat
	}
	
	private AssemblyPanel assemblyPanel;
	private List<Integer> previousWidths;
	
	/**
	 * Configures the required {@link AssemblyPanel} which will be affected by this action.
	 */
	public void configureAssemblyPanel(AssemblyPanel assemblyPanel) {
		this.assemblyPanel = assemblyPanel;
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		DisplayMode displayMode = triggerInfo.get(DISPLAY_MODE);
		assemblyPanel.setDefaultDisplayMode(displayMode);
		
		TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		ColumnModel<AbstractGenericTreeModel> columnModel = treeGrid.getColumnModel();
		switch (displayMode) {
		case Simple:
			treeGrid.getView().setForceFit(false);
			columnModel.setHidden(0, false);
			for (int i = 1; i < columnModel.getColumnCount(); i++)
				columnModel.setHidden(i, true);
			usePreviousWidths();
			break;
		case Detailed:
			treeGrid.getView().setForceFit(false);
			columnModel.setHidden(0, false);
			for (int i = 1; i < columnModel.getColumnCount(); i++)
				columnModel.setHidden(i, false);
			usePreviousWidths();
			break;
		case Flat:
			preparePreviousWidths();
			treeGrid.getView().setForceFit(true);
			columnModel.setHidden(0, true);
			for (int i = 1; i < columnModel.getColumnCount(); i++)
				columnModel.setHidden(i, false);
			break;
		}
	}
	
	public DisplayMode getCurrentDisplayMode() {
		return assemblyPanel.getCurrentDisplayMode();
	}
	
	private void preparePreviousWidths() {
		if (previousWidths == null)
			previousWidths = new ArrayList<Integer>();
		else
			previousWidths.clear();
		
		ColumnModel<AbstractGenericTreeModel> columnModel = assemblyPanel.getTreeGrid().getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++)
			previousWidths.add(columnModel.getColumnWidth(i));
	}
	
	private void usePreviousWidths() {
		if (previousWidths != null) {
			int counter = 0;
			ColumnModel<AbstractGenericTreeModel> columnModel = assemblyPanel.getTreeGrid().getColumnModel();
			for (Integer width : previousWidths)
				columnModel.setColumnWidth(counter++, width);
		}
	}

}
