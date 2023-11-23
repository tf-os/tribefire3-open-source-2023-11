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
package com.braintribe.gwt.customizationui.client.startup;

import java.util.function.Supplier;

import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.async.client.RuntimeConfiguration;
import com.braintribe.gwt.async.client.WorkingAndPausingTimeMeasure;
import com.braintribe.gwt.customizationui.client.packaging.ShowPackagingInfoAction;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.IocBeanLifeCycleExpert;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.ExtendedErrorUI;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.logging.ui.gxt.client.GxtErrorDialog;
import com.braintribe.gwt.logging.ui.gxt.client.GxtReasonErrorDialog;
import com.braintribe.model.packaging.Packaging;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonScopeManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.Event;

public class CustomizationStartup {
	protected static Logger logger = new Logger(CustomizationStartup.class);
	
	static {
		SingletonScopeManager.getInstance().setBeanLifeCycleExpert(new IocBeanLifeCycleExpert());
		PrototypeBeanProvider.setDefaultBeanLifeCycleExpert(new IocBeanLifeCycleExpert());

		ErrorDialog.setErrorUI(new ExtendedErrorUI() {
			@Override
			public void show(String message, Throwable t) {
				if (t instanceof ReasonException)
					GxtReasonErrorDialog.show(message, t);
				else
					GxtErrorDialog.show(message, t);
			}
			
			@Override
			public void show(String message) {
				GxtErrorDialog.showMessage(message);
			}
			
			@Override
			public void showDetails(String message, Throwable t) {
				if (t instanceof ReasonException)
					GxtReasonErrorDialog.show(message, t, true);
				else
					GxtErrorDialog.show(message, t, true);
			}
			
			@Override
			public void showDetails(String message, String details) {
				GxtErrorDialog.show(message, details);
			}
		});
		
		GWT.setUncaughtExceptionHandler(new ExceptionHandler());
		
		WorkingAndPausingTimeMeasure.getInstance();
		
		Event.addNativePreviewHandler(e -> {
			if (e.getTypeInt() != Event.getTypeInt(KeyDownEvent.getType().getName()))
				return;
			
			NativeEvent nativeEvent = e.getNativeEvent();
			if (nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE)
				nativeEvent.preventDefault();
			else if (nativeEvent.getKeyCode() == KeyCodes.KEY_BACKSPACE) {
				if (nativeEvent.getEventTarget() != null) {
					Element as = Element.as(nativeEvent.getEventTarget());
					boolean contentEditable = as.getPropertyBoolean("isContentEditable");
					if (as.getTagName().toLowerCase().equals("input") || as.getTagName().toLowerCase().equals("textarea")) {
						boolean readOnly = as.getPropertyBoolean("readOnly");
						boolean disabled = as.getPropertyBoolean("disabled");
						if (readOnly || disabled)
							nativeEvent.preventDefault();
					} else if (!contentEditable)
						nativeEvent.preventDefault();
	            }
			}
		});
	}
	
	/**
	 * Calling this will simply initialize all static configurations.
	 */
	public static void start(Supplier<? extends Supplier<Future<Packaging>>> packagingInfoProviderProvider) {
		GxtErrorDialog.setPackagingInfoProvider(new ShowPackagingInfoAction(packagingInfoProviderProvider.get()));
	}
	
	protected static void startApplication(Supplier<?> customizationProvider) {
		final Object customization = customizationProvider.get();
		
		if (customization instanceof Runnable)
			((Runnable)customization).run();
	}

	public void start(Supplier<? extends StartupConfig>... startupConfigProviders) {
		for (Supplier<? extends StartupConfig> startupConfigProvider: startupConfigProviders) {
			final StartupConfig startupConfig = startupConfigProvider.get();
			
			if (!startupConfig.canRun())
				return;
			
			Supplier<? extends Loader<?>> preparingLoaderProvider = startupConfig.getPreparingLoaderProvider();
			
			if (preparingLoaderProvider == null)
				startApplication(startupConfig.getCustomizationProvider());
			else {
				Loader<Object> loader = (Loader<Object>)preparingLoaderProvider.get();
				
				loader.load(AsyncCallbacks.of( //
						result -> startApplication(startupConfig.getCustomizationProvider()), //
						e -> ErrorDialog.show("Error during bootstrap loading", e)));
			}
			
			break;
		}
	}
	
	public void startCustomization(final Supplier<?> customizationProvider) {
		startCustomization(customizationProvider, true);
	}
	
	public void startCustomization(final Supplier<?> customizationProvider, boolean loadRuntimeConfig) {
		if (!loadRuntimeConfig) {
			startApplication(customizationProvider);
			return;
		}
		
		RuntimeConfiguration.getInstance().loadConfiguration() //
				.andThen(result -> startApplication(customizationProvider)) //
				.onError(e -> ErrorDialog.show("Error While Starting", e));
	}
	
	private static class ExceptionHandler implements UncaughtExceptionHandler {
		@Override
		public void onUncaughtException(Throwable e) {
			GWT.log("A runtime error occurred", e);
			ErrorDialog.show(LocalizedText.INSTANCE.errorRuntime(), e);
		}
	}
	
}
