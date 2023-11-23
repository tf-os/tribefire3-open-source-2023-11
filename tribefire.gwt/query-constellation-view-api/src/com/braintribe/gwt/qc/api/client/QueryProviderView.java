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
package com.braintribe.gwt.qc.api.client;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.gm.storage.api.StorageColumnInfo;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.QueryResult;
import com.google.gwt.user.client.ui.Widget;

public interface QueryProviderView<T extends GenericEntity> {

	Widget getWidget();

	QueryProviderContext getQueryProviderContext();

	void notifyQueryPerformed(QueryResult queryResult, QueryProviderContext queryProviderContext);

	void setEntityContent(T entityContent);

	void addQueryProviderViewListener(QueryProviderViewListener listener);

	void removeQueryProviderViewListener(QueryProviderViewListener listener);

	void configureGmSession(PersistenceGmSession gmSession);

	void focusEditor();

	void setOtherModeQueryProviderView(Supplier<? extends QueryProviderView<T>> otherModelQueryProviderView);

	void modeQueryProviderViewChanged();
	
	/**
	 * Shows the form with variables.
	 */
	default void showForm() {
		//NOP
	}
	
	/**
	 * Hides the form with variables.
	 */
	default void hideForm() {
		//NOP
	}
	
	/**
	 * Returns whether the form is available for the view.
	 */
	default boolean isFormAvailable() {
		return false;
	}
	
	/**
	 * Notification for view changes.
	 * @param displayNode - true for displaying the node.
	 * @param nodeWidth - width of the node.
	 * @param columnsVisible - List of columns that are visible.
	 */
	default void onViewChange(boolean displayNode, Integer nodeWidth, List<StorageColumnInfo> columnsVisible) {
		//NOP
	}
	
	/**
	 * Sets the current query context name.
	 * @param global - true when using a global context. Should be false when a context is given.
	 * @param contextName - name of the context. Should be null when set as global.
	 */
	default void setCurrentContext(boolean global, String contextName) {
		//NOP
	}
	
	/**
	 * Adds a new query context.
	 * @param contextName - name of the context
	 */
	default void addQueryContext(String contextName) {
		//NOP
	}
	
	/**
	 * Removes the query context.
	 */
	default void removeQueryContext() {
		//NOP
	}
	
	default String getCurrentQueryText() {
		return null;
	}
	
	/**
	 * Displays the given count text.
	 * @param countText - the text to be displayed
	 */
	default void setDisplayCountText(String countText) {
		//NOOP
	}
}
