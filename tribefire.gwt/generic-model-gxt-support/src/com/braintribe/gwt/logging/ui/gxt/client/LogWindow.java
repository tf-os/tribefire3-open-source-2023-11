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
package com.braintribe.gwt.logging.ui.gxt.client;

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Formatter;
import com.braintribe.gwt.logging.client.LogEvent;
import com.braintribe.gwt.logging.client.LogEventBuffer;
import com.braintribe.gwt.logging.client.LogEventBuffer.LogEventBufferListener;
import com.braintribe.gwt.logging.client.LogListener;
import com.braintribe.gwt.logging.client.LogManager;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.StandardStringFormatter;
import com.braintribe.gwt.logging.client.resources.LoggingResources;
import com.braintribe.gwt.utils.client.RootKeyNavExpert.RootKeyNavListener;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * This class is a convenience GUI component that displays instances of {@link LogEvent} in a {@link TextArea} that uses
 * plain text formatting.
 * 
 * If the {@link LogWindow} is registered as {@link LogListener} at the {@link LogManager} it will display a configured
 * amount of {@link LogEvent} instances it receives that way.
 * 
 * A {@link LogEventBuffer} should be set to this {@link LogWindow} to get plain text view of this buffer.
 * 
 * @author Dirk
 *
 */
public class LogWindow extends TabbedDialog implements LogListener, RootKeyNavListener {
	
	private ExtendedTextArea textArea;
	private LogEventBuffer logEventBuffer;
	private Formatter<String> formatter = new StandardStringFormatter();
	private TextButton clearLogsButton;
	//private TextButton refreshButton;
	private TextButton profilingActivationButton;
	private boolean performRefresh = true;
	private LogEventBufferListener logEventListener;
	//private Supplier<? extends WorkbenchActionHandler<JsUxComponentOpenerAction>> supplierHandler;
	//private WorkbenchActionHandler<JsUxComponentOpenerAction> handler;
	//private boolean test = false;
	
	public LogWindow() {
		setSize("500px", "400px");
		setModal(false);
		setHeaderVisible(true);
		setMaximizable(false);
		setClosable(false);
		getHeader().setHeight(20);
		
		textArea = new ExtendedTextArea();
		textArea.setReadOnly(true);
		textArea.setBorders(false);
		textArea.getInputEl().getStyle().setBorderStyle(BorderStyle.NONE);
		textArea.getElement().getStyle().setFontSize(10, Unit.PX);
		textArea.getElement().getStyle().setProperty("fontFamily", "monospace");
				
		tabPanel.add(textArea, LocalizedText.INSTANCE.log());
		
		tabPanel.addSelectionHandler(event -> {
			boolean textAreaSelected = event.getSelectedItem() == textArea;
			clearLogsButton.setVisible(textAreaSelected);
			//refreshButton.setVisible(textAreaSelected);
			profilingActivationButton.setVisible(textAreaSelected);
			
			if (textAreaSelected && performRefresh) {
				performRefresh = false;
				refresh();
			}
		});
	}
	
	/*public void setSupplierHandler(Supplier<? extends WorkbenchActionHandler<JsUxComponentOpenerAction>> supplierHandler) {
		this.supplierHandler = supplierHandler;
	}*/
	
	@Override
	public void onRootKeyPress(NativeEvent evt) {
		if ((evt.getCtrlKey() || (GXT.isMac() && evt.getMetaKey())) && evt.getKeyCode() == KeyCodes.KEY_L && evt.getShiftKey()) {
			evt.stopPropagation();
			evt.preventDefault();
			//showTest();
			show();
		}
	}

	private TextButton prepareClearLogsButton() {
		clearLogsButton = new TextButton(LocalizedText.INSTANCE.clear());
		clearLogsButton.setIconAlign(IconAlign.TOP);
		clearLogsButton.setScale(ButtonScale.LARGE);
		clearLogsButton.setIcon(LoggingResources.INSTANCE.clear());
		clearLogsButton.addSelectHandler(event -> {
			textArea.clear();
			logEventBuffer.getEvents().clear();
		});
		return clearLogsButton;
	}
	
	/*private TextButton prepareRefreshButton() {
		refreshButton = new TextButton(LocalizedText.INSTANCE.update());
		refreshButton.setIconAlign(IconAlign.TOP);
		refreshButton.setScale(ButtonScale.LARGE);
		refreshButton.setIcon(LoggingResources.INSTANCE.refresh());
		refreshButton.addSelectHandler(event -> refresh());
		return refreshButton;
	}*/
	
	private TextButton prepareProfilingActivationButton() {
		profilingActivationButton = new TextButton(
				Profiling.isProfilingEnabled() ? LocalizedText.INSTANCE.disableProfiling() : LocalizedText.INSTANCE.enableProfiling());
		profilingActivationButton.setIconAlign(IconAlign.TOP);
		profilingActivationButton.setScale(ButtonScale.LARGE);
		profilingActivationButton.setIcon(LoggingResources.INSTANCE.profiling());
		profilingActivationButton.addSelectHandler(event -> {
			Profiling.setProfilingEnabled(!Profiling.isProfilingEnabled());
			profilingActivationButton.setText(
					Profiling.isProfilingEnabled() ? LocalizedText.INSTANCE.disableProfiling() : LocalizedText.INSTANCE.enableProfiling());
		});
		return profilingActivationButton;
	}
	
	/**
	 * 
	 * @param logEventBuffer this buffer is used when getting {@link LogEvent} instances
	 * for the plain text display
	 */
	@Configurable @Required
	public void setLogEventBuffer(LogEventBuffer logEventBuffer) {
		this.logEventBuffer = logEventBuffer;
	}
	
	/**
	 * @param formatter this formatter is used to format {@link LogEvent} instances
	 * for the plain text view.
	 */
	@Configurable
	public void setFormatter(Formatter<String> formatter) {
		this.formatter = formatter;
	}
	
	/**
	 * refreshes the plain text view with the current data from
	 * the configured {@link LogEventBuffer}
	 * @see #setLogEventBuffer(LogEventBuffer)
	 */
	public void refresh() {
		StringBuffer text = new StringBuffer();
		for (LogEvent logEvent: logEventBuffer.getEvents()) {
			if (text.length() > 0) text.append('\n');
			text.append(formatter.format(logEvent));
		}
		textArea.setValue(text.toString());
	}
	
	@Override
	public void onLogEvent(LogEvent event) {
		//NOP
	}

	@Override
	protected ToolBar prepareToolBar() {
		ToolBar toolBar = super.prepareToolBar();
		toolBar.setEnableOverflow(false);
		//toolBar.insert(prepareRefreshButton(), 1);
		toolBar.insert(prepareProfilingActivationButton(), 1);
		toolBar.insert(prepareClearLogsButton(), 2);
		return toolBar;
	}
	
	@Override
	public void show() {
		super.show();
		refresh();
		logEventBuffer.setListener(getLogEventListener());
	}
	
	@Override
	public void hide() {
		super.hide();
		logEventBuffer.setListener(null);
	}
	
	public LogEventBufferListener getLogEventListener() {
		if (logEventListener != null)
			return logEventListener;
		
		logEventListener = this::refresh;
		
		return logEventListener;
	}
	
	/*private void showTest() {
		Reason reason = Reason.T.create();
		reason.setText("This is a test");
		
		if (test) {
			List<Reason> reasons = new ArrayList<>();
			Reason reason1 = Reason.T.create();
			reason1.setText("This is a test 1");
			
			Reason reason2 = Reason.T.create();
			reason2.setText("This is a test 2");
			
			reasons.add(reason1);
			reasons.add(reason2);
			reason.setReasons(reasons);
		}
		
		test = !test;
			
		
		ReasonException e = new ReasonException(reason);
		GxtReasonErrorDialog.show("This is a test", e, true);
	}
	
	private void showTest2() {
		WorkbenchActionContext<JsUxComponentOpenerAction> testContext = new WorkbenchActionContext<JsUxComponentOpenerAction>() {
			
			@Override
			public JsUxComponentOpenerAction getWorkbenchAction() {
				JsUxComponentOpenerAction testAction = JsUxComponentOpenerAction.T.create();
				testAction.setOpenAsModal(true);
				return testAction;
			}
			
			@Override
			public Object getPanel() {
				return null;
			}
			
			@Override
			public List<ModelPath> getModelPaths() {
				return null;
			}
			
			@Override
			public GmSession getGmSession() {
				return null;
			}
			
			@Override
			public Folder getFolder() {
				return null;
			}
		};
		
		supplierHandler.get().perform(testContext);
	}*/
	
}
