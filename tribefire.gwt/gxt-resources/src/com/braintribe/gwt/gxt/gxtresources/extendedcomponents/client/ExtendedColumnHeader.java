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
package com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.sencha.gxt.core.client.dom.DomHelper;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GroupingView;
import com.sencha.gxt.widget.core.client.grid.HeaderGroupConfig;

/**
 * Extends the {@link ColumnHeader} by exposing a new css to be used in the header.
 * 
 * @author michel.docouto
 */
public class ExtendedColumnHeader<M> extends ColumnHeader<M> {
	
	private boolean refreshDisabled;

	public ExtendedColumnHeader(Grid<M> container, ColumnModel<M> cm) {
		super(container, cm);
	}
	
	@Override
	protected void onAttach() {
		//The refresh should not be done when we attach a second time
		if (this.isOrWasAttached())
			refreshDisabled = true;
		super.onAttach();
		
		refreshDisabled = false;
	}

	@Override
	public void refresh() {
		if (refreshDisabled)
			return;
		
		groups.clear();
		heads.clear();

		columnToHead = new int[cm.getColumnCount()];
		for (int i = 0, mark = 0; i < columnToHead.length; i++)
			columnToHead[i] = cm.isHidden(i) ? -1 : mark++;

		int cnt = table.getRowCount();
		for (int i = 0; i < cnt; i++)
			table.removeRow(0);

		table.setWidth(cm.getTotalWidth() + "px");
		// Defer header size check until heads are created

		Element body = table.getElement().<XElement> cast().selectNode("tbody");
		
		TableSectionElement tbody = getTbody();

		table.getElement().insertBefore(tbody, body);
		tbody.<XElement> cast().removeChildren();
		DomHelper.insertHtml("afterBegin", tbody, renderHiddenHeaders(getColumnWidths()));

		List<HeaderGroupConfig> configs = cm.getHeaderGroups();

		FlexCellFormatter cf = table.getFlexCellFormatter();
		RowFormatter rf = table.getRowFormatter();

		rows = 0;
		for (HeaderGroupConfig config : configs)
			rows = Math.max(rows, config.getRow() + 1);
		rows++;
		
		ColumnHeaderStyles styles = getAppearance().styles();

		for (int i = 0; i < rows; i++)
			rf.setStyleName(i, styles.headRow());

		int cols = cm.getColumnCount();

		//This is the only change in this class
		String cellClass = styles.header() + " " + styles.head() + " gmeExtendedColumnHead";

		if (rows > 1) {
			Map<Integer, Integer> map = new HashMap<>();
			for (int i = 0; i < rows - 1; i++) {
				for (HeaderGroupConfig config : cm.getHeaderGroups()) {
					int col = config.getColumn();
					int row = config.getRow();
					Integer start = map.get(row);

					if (start == null || col < start)
						map.put(row, col);
				}
			}
		}

		for (HeaderGroupConfig config : cm.getHeaderGroups()) {
			int col = config.getColumn();
			int row = config.getRow();
			int rs = config.getRowspan();
			int cs = config.getColspan();

			Group group = createNewGroup(config);

			boolean hide = true;
			if (rows > 1) {
				for (int i = col; i < (col + cs); i++) {
					if (!cm.isHidden(i)) {
						hide = false;
						break;
					}
				}
			}
			if (hide)
				continue;

			table.setWidget(row, col, group);

			cf.setStyleName(row, col, cellClass);

			HorizontalAlignmentConstant align = config.getHorizontalAlignment();
			cf.setHorizontalAlignment(row, col, align);

			int ncs = cs;
			if (cs > 1) {
				for (int i = col; i < (col + cs); i++) {
					if (cm.isHidden(i))
						ncs -= 1;
				}
			}

			cf.setRowSpan(row, col, rs);
			cf.setColSpan(row, col, ncs);
		}

		for (int i = 0; i < cols; i++) {
			if (cm.isHidden(i))
				continue;
			Head h = createNewHead(cm.getColumn(i));
			int rowspan = 1;
			if (rows > 1) {
				for (int j = rows - 2; j >= 0; j--) {
					if (!hasGroup(cm,j, i))
						rowspan += 1;
				}
			}

			int row;
			if (rowspan > 1)
				row = (rows - 1) - (rowspan - 1);
			else
				row = rows - 1;

			setRow(h, row);

			if (rowspan > 1) {
				table.setWidget(row, i, h);
				table.getFlexCellFormatter().setRowSpan(row, i, rowspan);
			} else
				table.setWidget(row, i, h);
			ColumnConfig<M, ?> cc = cm.getColumn(i);
			String s = cc.getCellClassName() == null ? "" : " " + cc.getCellClassName();
			cf.setStyleName(row, i, cellClass + s);
			cf.getElement(row, i).setPropertyInt("gridColumnIndex", i);

			HorizontalAlignmentConstant align = cm.getColumnHorizontalAlignment(i);

			// override the header alignment
			if (cm.getColumnHorizontalHeaderAlignment(i) != null)
				align = cm.getColumnHorizontalHeaderAlignment(i);

			if (align != null) {
				table.getCellFormatter().setHorizontalAlignment(row, i, align);
				if (align == HasHorizontalAlignment.ALIGN_RIGHT) {
					table.getCellFormatter().getElement(row, i).getFirstChildElement().getStyle().setPropertyPx("paddingRight",
							getRightAlignOffset());
				}
			}
		}

		if (container != null) {
			Grid<M> grid = container;
			if (grid.getLoader() != null && grid.getLoader().isRemoteSort()) {
				List<? extends SortInfo> sortInfos = grid.getLoader().getSortInfo();
				boolean grouping = grid.getView() instanceof GroupingView;
				if (sortInfos.size() > (grouping ? 1 : 0)) {
					SortInfo sortInfo = sortInfos.get((grouping ? 1 : 0));
					String sortField = sortInfo.getSortField();
					if (sortField != null && !"".equals(sortField)) {
						ColumnConfig<M, ?> column = cm.findColumnConfig(sortField);
						if (column != null) {
							int index = cm.indexOf(column);
							if (index != -1) {
								updateSortIcon(index, sortInfo.getSortDir());
							}
						}
					}
				}
			} else {
				StoreSortInfo<M> sortInfo = grid.getView().getSortState();
				if (sortInfo != null && sortInfo.getValueProvider() != null) {
					ColumnConfig<M, ?> column = grid.getColumnModel().findColumnConfig(sortInfo.getPath());
					if (column != null) {
						updateSortIcon(grid.getColumnModel().indexOf(column), sortInfo.getDirection());
					}
				}
			}
		}

		cleanCells();

		adjustColumnWidths(getColumnWidths());
	}
	
	private native TableSectionElement getTbody() /*-{
		return this.@com.sencha.gxt.widget.core.client.grid.ColumnHeader::tbody;
	}-*/;
	
	private native boolean hasGroup(ColumnModel<M> columnModel, int row, int column) /*-{
		return columnModel.@com.sencha.gxt.widget.core.client.grid.ColumnModel::hasGroup(II)(row, column);
	}-*/;
	
	private native void setRow(Head head, int rowToSet) /*-{
		head.@com.sencha.gxt.widget.core.client.grid.ColumnHeader.Head::row = rowToSet;
	}-*/;

}
