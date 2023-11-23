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
package tribefire.app.web;

import javax.servlet.ServletContextEvent;

import com.braintribe.config.configurator.ClasspathConfigurator;
import com.braintribe.config.configurator.ConfiguratorContext;
import com.braintribe.logging.Logger;
import com.braintribe.logging.listener.LoggingRuntimeListener;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.StringTools;
import com.braintribe.web.api.WebApp;
import com.braintribe.web.api.WebAppInfo;
import com.braintribe.web.api.registry.WebRegistry;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.platform.impl.PreLoader;
import tribefire.platform.wire.TribefirePlatformWireModule;
import tribefire.platform.wire.contract.MainTribefireContract;


public class TribefireWebApp extends WebApp {

	private static final Logger logger = Logger.getLogger(TribefireWebApp.class);

	private static WireContext<MainTribefireContract> wireContext;

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {

		WebAppInfo info = info(contextEvent.getServletContext());

		logger.pushContext(info + "#initialize");

		logger.info("Initializing " + info);

		try {
			publishServletContext(contextEvent.getServletContext());

			ensureTmpDir();
			ensureNodeUuid();
			
			// info.getContextPath() is deprecated. tribefire-services should always be addressed as "master"
			TribefireRuntime.registerMbean(TribefireConstants.TRIBEFIRE_SERVICES_APPLICATION_ID, info.getContextPath());

			LoggingRuntimeListener.register();

			TribefireRuntime.enterStartup();

			runConfigurators(info);

			PreLoader.startStaticPreLoading();

			initializeWireContext(info);

			super.contextInitialized(contextEvent);

			logger.info("Initialized " + info);

		} finally {
			TribefireRuntime.leaveStartup();
			logger.popContext();
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_INITIALIZATION_COMPLETED, ""+System.currentTimeMillis());
		}
	}

	private void ensureTmpDir() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if (!StringTools.isBlank(tmpDir)) {
			try {
				FileTools.createDirectory(tmpDir);
			} catch(Exception e) {
				logger.warn("Error while trying to ensure tmp directory: "+tmpDir, e);
			}
		}
	}

	private void ensureNodeUuid() {
		if (System.getProperty(TribefireConstants.TRIBEFIRE_JVM_UUID_KEY) == null)
			System.setProperty(TribefireConstants.TRIBEFIRE_JVM_UUID_KEY, RandomTools.newStandardUuid());
	}

	@Override
	protected WebRegistry provideConfiguration() {
		if (wireContext == null)
			throw new IllegalStateException("Wire context is not initialized");

		// We need to load modules first, only then access the web registry 
		MainTribefireContract platformContract = wireContext.contract();

		platformContract.activate();

		return platformContract.webRegistry();
	}

	private void initializeWireContext(WebAppInfo info) {
		long start = System.currentTimeMillis();

		wireContext = Wire.context(TribefirePlatformWireModule.INSTANCE);

		logger.info("Wire context initialization for "+info+" has finished in "+(System.currentTimeMillis()-start)+" ms");
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {

		WebAppInfo info = info(contextEvent.getServletContext());

		logger.pushContext(info + "#destroy");

		logger.info("Shutting down " + info);

		TribefireRuntime.enterShutdown();

		try {

			try {
				super.contextDestroyed(contextEvent);
			} catch (Exception e) {
				logger.error(super.getClass().getName() + ".contextDestroyed() failed for " + info + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
			}

			shutdownWireContext(info);

		} finally {
			TribefireRuntime.leaveShutdown();
			TribefireRuntime.shutdown();
			TribefireRuntime.unregisterMbean();
			LoggingRuntimeListener.unregister();
			logger.popContext();
		}

		logger.info("Shut down completed for " + info);

	}

	private void shutdownWireContext(WebAppInfo info) {
		if (wireContext != null) {
			try {
				wireContext.shutdown();
			} catch (Exception e) {
				logger.error("Failed to shutdown wire context for "+info+(e.getMessage() != null ? ": "+e.getMessage() : ""), e);
			}
		} else {
			logger.warn(info+" wire context cannot be shut down as it was not initialized.");
		}
	}

	private void runConfigurators(WebAppInfo info) {
		try {
			ConfiguratorContext context = new ConfiguratorContext(info.getContextPath());
			context.setMaster(true);
			new ClasspathConfigurator(context).configure();

		} catch (Throwable e) {
			logger.error("Failed to execute configurators: " + e.getMessage(), e);
		}
	}


}
