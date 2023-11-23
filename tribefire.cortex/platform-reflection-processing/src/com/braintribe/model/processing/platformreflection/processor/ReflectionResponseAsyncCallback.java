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
package com.braintribe.model.processing.platformreflection.processor;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.platformreflection.AccessDataFolder;
import com.braintribe.model.platformreflection.ConfigurationFolder;
import com.braintribe.model.platformreflection.Healthz;
import com.braintribe.model.platformreflection.HeapDump;
import com.braintribe.model.platformreflection.HostInfo;
import com.braintribe.model.platformreflection.ModulesFolder;
import com.braintribe.model.platformreflection.PackagingInformation;
import com.braintribe.model.platformreflection.PlatformReflection;
import com.braintribe.model.platformreflection.PlatformReflectionJson;
import com.braintribe.model.platformreflection.SharedStorage;
import com.braintribe.model.platformreflection.SystemInfo;
import com.braintribe.model.platformreflection.TribefireInfo;
import com.braintribe.model.platformreflection.hotthreads.HotThreads;
import com.braintribe.model.platformreflection.hotthreads.ThreadDump;
import com.braintribe.model.platformreflection.process.ProcessesJson;
import com.braintribe.model.platformreflection.tf.DeployablesInfo;
import com.braintribe.model.processing.platformreflection.java.PlatformReflectionTools;
import com.braintribe.model.resource.Resource;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.IOTools;

public abstract class ReflectionResponseAsyncCallback<T extends GenericEntity> implements AsyncCallback<T> {

	private static Logger logger = Logger.getLogger(ReflectionResponseAsyncCallback.class);

	protected CountDownLatch countdown;
	protected PlatformReflection reflection;
	protected DiagnosticPackageContext diagnosticPackageContext;

	public ReflectionResponseAsyncCallback(PlatformReflection reflection, CountDownLatch countdown) {
		this.reflection = reflection;
		this.countdown = countdown;
	}

	public ReflectionResponseAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
		this.diagnosticPackageContext = diagnosticPackageContext;
		this.countdown = countdown;
	}

	@Override
	public void onFailure(Throwable t) {
		countdown.countDown();
		logger.error("Error while waiting for a PlatformReflectionResponse", t);
		try {
			String trace = Exceptions.stringify(t);
			String name = getClass().getSimpleName();
			diagnosticPackageContext.errors.add(name + "\n" + trace);
		} catch (Exception e) {
			logger.debug("Could not add the error to the context", e);
		}
	}

	public static class HostInfoAsyncCallback extends ReflectionResponseAsyncCallback<HostInfo> {
		public HostInfoAsyncCallback(PlatformReflection reflection, CountDownLatch countdown) {
			super(reflection, countdown);
		}
		@Override
		public void onSuccess(HostInfo future) {
			logger.debug(() -> "Received a HostInfo response asynchronously.");
			reflection.setHost(future);
			countdown.countDown();
		}
	}

	public static class SystemInfoAsyncCallback extends ReflectionResponseAsyncCallback<SystemInfo> {
		public SystemInfoAsyncCallback(PlatformReflection reflection, CountDownLatch countdown) {
			super(reflection, countdown);
		}
		@Override
		public void onSuccess(SystemInfo future) {
			logger.debug(() -> "Received a SystemInfo response asynchronously.");
			reflection.setSystem(future);
			countdown.countDown();
		}
	}

	public static class TribefireInfoAsyncCallback extends ReflectionResponseAsyncCallback<TribefireInfo> {
		public TribefireInfoAsyncCallback(PlatformReflection reflection, CountDownLatch countdown) {
			super(reflection, countdown);
		}
		@Override
		public void onSuccess(TribefireInfo future) {
			logger.debug(() -> "Received a TribefireInfo response asynchronously.");
			reflection.setTribefire(future);
			countdown.countDown();
		}
	}

	public static class ThreadDumpAsyncCallback extends ReflectionResponseAsyncCallback<ThreadDump> {
		public ThreadDumpAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(ThreadDump future) {
			try {
				logger.debug(() -> "Received a ThreadDump response asynchronously.");
				diagnosticPackageContext.threadDump = future.getThreadDump();
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class PlatformReflectionJsonAsyncCallback extends ReflectionResponseAsyncCallback<PlatformReflectionJson> {
		public PlatformReflectionJsonAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(PlatformReflectionJson future) {
			try {
				logger.debug(() -> "Received a PlatformReflectionJson response asynchronously.");
				diagnosticPackageContext.platformReflectionJson = future.getPlatformReflectionJson();
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class HotThreadsAsyncCallback extends ReflectionResponseAsyncCallback<HotThreads> {
		public HotThreadsAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(HotThreads future) {
			try {
				logger.debug(() -> "Received a HotThreads response asynchronously.");
				diagnosticPackageContext.hotThreads = PlatformReflectionTools.toString(future);
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class CollectHealthzAsyncCallback extends ReflectionResponseAsyncCallback<Healthz> {
		public CollectHealthzAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(Healthz healthz) {
			try {
				logger.debug(() -> "Received a Healthz response asynchronously.");
				diagnosticPackageContext.healthz = healthz;
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class CollectPackagingInformationAsyncCallback extends ReflectionResponseAsyncCallback<PackagingInformation> {
		public CollectPackagingInformationAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(PackagingInformation pi) {
			try {
				logger.debug(() -> "Received a PackagingInformation response asynchronously.");
				diagnosticPackageContext.packagingInformation = pi;
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class CollectDeployablesInformationAsyncCallback extends ReflectionResponseAsyncCallback<DeployablesInfo> {
		public CollectDeployablesInformationAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(DeployablesInfo pi) {
			try {
				logger.debug(() -> "Received a PackagingInformation response asynchronously.");
				diagnosticPackageContext.deployablesInfo = pi;
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class CollectSetupDescriptorAsyncCallback extends ReflectionResponseAsyncCallback<Resource> {
		public CollectSetupDescriptorAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(Resource setupDescriptorResource) {
			try {
				logger.debug(() -> "Received a PackagingInformation response asynchronously.");
				diagnosticPackageContext.setupDescriptorResource = setupDescriptorResource;
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class CollectConfigurationFolderAsyncCallback extends ReflectionResponseAsyncCallback<ConfigurationFolder> {
		public CollectConfigurationFolderAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(ConfigurationFolder cf) {
			try {
				logger.debug(() -> "Received a ConfigurationFolder response asynchronously.");

				Resource resource = cf.getConfigurationFolderAsZip();
				if (resource != null) {
					File tempFile = File.createTempFile(resource.getName(), ".tmp");
					tempFile.delete();
					try (InputStream in = resource.openStream()) {
						IOTools.inputToFile(in, tempFile);
					}
					diagnosticPackageContext.configurationFolderAsZip = tempFile;
					diagnosticPackageContext.configurationFolderAsZipFilename = resource.getName();
				}
			} catch (Exception e) {
				logger.error("Error while trying to include the configuration folder as a ZIP in the diagnostic package.", e);
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class CollectModulesFolderAsyncCallback extends ReflectionResponseAsyncCallback<ModulesFolder> {
		public CollectModulesFolderAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(ModulesFolder mf) {
			try {
				logger.debug(() -> "Received a ModulesFolder response asynchronously.");

				Resource resource = mf.getModulesFolderAsZip();
				if (resource != null) {
					File tempFile = File.createTempFile(resource.getName(), ".tmp");
					tempFile.delete();
					try (InputStream in = resource.openStream()) {
						IOTools.inputToFile(in, tempFile);
					}
					diagnosticPackageContext.modulesFolderAsZip = tempFile;
					diagnosticPackageContext.modulesFolderAsZipFilename = resource.getName();
				}
			} catch (Exception e) {
				logger.error("Error while trying to include the modules folder as a ZIP in the diagnostic package.", e);
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class CollectSharedStorageAsyncCallback extends ReflectionResponseAsyncCallback<SharedStorage> {
		public CollectSharedStorageAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(SharedStorage ss) {
			try {
				logger.debug(() -> "Received a SharedStorage response asynchronously.");

				Resource resource = ss.getSharedStorageAsZip();
				if (resource != null) {
					File tempFile = File.createTempFile(resource.getName(), ".tmp");
					tempFile.delete();
					try (InputStream in = resource.openStream()) {
						IOTools.inputToFile(in, tempFile);
					}
					diagnosticPackageContext.sharedStorageAsZip = tempFile;
					diagnosticPackageContext.sharedStorageAsZipFilename = resource.getName();
				}
			} catch (Exception e) {
				logger.error("Error while trying to include the shared storage ZIP in the diagnostic package.", e);
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class CollectAccessDataFolderAsyncCallback extends ReflectionResponseAsyncCallback<AccessDataFolder> {
		public CollectAccessDataFolderAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(AccessDataFolder adf) {
			try {
				logger.debug(() -> "Received a AccessDataFolder response asynchronously.");

				Resource resource = adf.getAccessDataFolderAsZip();
				if (resource != null) {
					File tempFile = File.createTempFile(resource.getName(), ".tmp");
					tempFile.delete();
					try (InputStream in = resource.openStream()) {
						IOTools.inputToFile(in, tempFile);
					}
					diagnosticPackageContext.accessDataFolderAsZip = tempFile;
					diagnosticPackageContext.accessDataFolderAsZipFilename = resource.getName();
				}
			} catch (Exception e) {
				logger.error("Error while trying to include the accessdata folder ZIP in the diagnostic package.", e);
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class ProcessesJsonAsyncCallback extends ReflectionResponseAsyncCallback<ProcessesJson> {
		public ProcessesJsonAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(ProcessesJson future) {
			try {
				logger.debug(() -> "Received a ProcessesJson response asynchronously.");
				diagnosticPackageContext.processesJson = future.getProcessesJson();
			} finally {
				countdown.countDown();
			}
		}
	}

	public static class HeapDumpAsyncCallback extends ReflectionResponseAsyncCallback<HeapDump> {
		public HeapDumpAsyncCallback(DiagnosticPackageContext diagnosticPackageContext, CountDownLatch countdown) {
			super(diagnosticPackageContext, countdown);
		}
		@Override
		public void onSuccess(HeapDump future) {
			try {
				logger.debug(() -> "Received a HeapDump response asynchronously.");

				Resource resource = future.getHeapDump();
				File tempFile = File.createTempFile(resource.getName(), ".tmp");
				tempFile.delete();
				try (InputStream in = resource.openStream()) {
					IOTools.inputToFile(in, tempFile);
				}
				diagnosticPackageContext.heapDump = tempFile;
				diagnosticPackageContext.heapDumpFilename = resource.getName();
			} catch (Exception e) {
				logger.error("Error while trying to include the heap dump in the diagnostic package.", e);
			} finally {
				countdown.countDown();
			}
		}
	}
}
