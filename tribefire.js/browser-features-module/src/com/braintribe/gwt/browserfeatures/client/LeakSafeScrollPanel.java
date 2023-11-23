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
package com.braintribe.gwt.browserfeatures.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class LeakSafeScrollPanel extends ScrollPanel {
	private boolean leakToolEnsured;
	
	public LeakSafeScrollPanel() {
		super();
	}

	public LeakSafeScrollPanel(Widget child) {
		super(child);
	}
	
	/**
	 * overriding this method is the only way to initialize the elements with the LeakToolImpl before
	 * the com.google.gwt.user.client.ui.ScrollImpl.ScrollImplTrident.initialize() is called.
	 * There is no other reason.
	 */
	@Override
	public void setAlwaysShowScrollBars(boolean alwaysShow) {
		ensureLeakTool();
		super.setAlwaysShowScrollBars(alwaysShow);
	}

	private void ensureLeakTool() {
		if (!leakToolEnsured) {
			LeakToolImpl leakToolImpl = LeakToolImpl.getInstance();
			leakToolImpl.initialize(getContainerElement());
			leakToolImpl.initialize(getElement());
			leakToolEnsured = true;
		}
	}
	
	/**
	 * This method must be called to break cycles. Afterwards the LeakSafeScrollPanel is no longer usable because the Workaround has this weakness. 
	 */
	public void cleanup() {
		LeakToolImpl leakToolImpl = LeakToolImpl.getInstance();
		leakToolImpl.cleanup(getContainerElement());
		leakToolImpl.cleanup(getElement());
	}
	
	public static class LeakToolImpl {
		private static LeakToolImpl instance = GWT.create(LeakToolImpl.class);
		
		public static LeakToolImpl getInstance() {
			return instance;
		}
		
		/**
		 * @param element - the Element
		 */
		public void initialize(Element element) {
			// do nothing cause real browser have no leak with ScrollPane
		}
		
		/**
		 * @param element - the Element
		 */
		public void cleanup(Element element) {
			// do nothing cause real browser have no leak with ScrollPane
		}
	}
	
}
