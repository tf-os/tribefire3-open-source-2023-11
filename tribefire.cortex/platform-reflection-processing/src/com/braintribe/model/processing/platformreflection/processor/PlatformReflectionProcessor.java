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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.management.MBeanServer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.marshaller.api.request.ExecuteAndMarshallResponse;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.distributed.api.DcsaIterable;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.deploymentapi.check.request.RunCheckBundles;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.logs.request.GetLogs;
import com.braintribe.model.logs.request.Logs;
import com.braintribe.model.packaging.Packaging;
import com.braintribe.model.platform.setup.api.data.SetupDescriptor;
import com.braintribe.model.platformreflection.AccessDataFolder;
import com.braintribe.model.platformreflection.ConfigurationFolder;
import com.braintribe.model.platformreflection.DiagnosticPackage;
import com.braintribe.model.platformreflection.DiagnosticPackages;
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
import com.braintribe.model.platformreflection.os.Process;
import com.braintribe.model.platformreflection.process.Processes;
import com.braintribe.model.platformreflection.process.ProcessesJson;
import com.braintribe.model.platformreflection.request.CollectDiagnosticPackages;
import com.braintribe.model.platformreflection.request.CollectHealthz;
import com.braintribe.model.platformreflection.request.GetAccessDataFolder;
import com.braintribe.model.platformreflection.request.GetConfigurationFolder;
import com.braintribe.model.platformreflection.request.GetDeployablesInfo;
import com.braintribe.model.platformreflection.request.GetDiagnosticPackage;
import com.braintribe.model.platformreflection.request.GetHeapDump;
import com.braintribe.model.platformreflection.request.GetHostInformation;
import com.braintribe.model.platformreflection.request.GetHotThreads;
import com.braintribe.model.platformreflection.request.GetLicenseInformation;
import com.braintribe.model.platformreflection.request.GetModulesFolder;
import com.braintribe.model.platformreflection.request.GetPackagingInformation;
import com.braintribe.model.platformreflection.request.GetProcesses;
import com.braintribe.model.platformreflection.request.GetProcessesJson;
import com.braintribe.model.platformreflection.request.GetRepositoryViewResolution;
import com.braintribe.model.platformreflection.request.GetSetupDescriptor;
import com.braintribe.model.platformreflection.request.GetSharedStorage;
import com.braintribe.model.platformreflection.request.GetSystemInformation;
import com.braintribe.model.platformreflection.request.GetThreadDump;
import com.braintribe.model.platformreflection.request.GetTribefireInformation;
import com.braintribe.model.platformreflection.request.PlatformReflectionRequest;
import com.braintribe.model.platformreflection.request.PlatformReflectionResponse;
import com.braintribe.model.platformreflection.request.ReflectPlatform;
import com.braintribe.model.platformreflection.request.ReflectPlatformJson;
import com.braintribe.model.platformreflection.tf.ClasspathContainer;
import com.braintribe.model.platformreflection.tf.DeployablesInfo;
import com.braintribe.model.platformreflection.tf.License;
import com.braintribe.model.platformsetup.PlatformSetup;
import com.braintribe.model.platformsetup.api.request.GetAssets;
import com.braintribe.model.platformsetup.api.response.AssetCollection;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.platformreflection.host.HostDetector;
import com.braintribe.model.processing.platformreflection.host.HostInformationProvider;
import com.braintribe.model.processing.platformreflection.host.StandardHostDetector;
import com.braintribe.model.processing.platformreflection.host.tomcat.TomcatHostInformationProvider;
import com.braintribe.model.processing.platformreflection.java.HotThreadsProvider;
import com.braintribe.model.processing.platformreflection.os.ProcessesProvider;
import com.braintribe.model.processing.platformreflection.os.StandardSystemInformationProvider;
import com.braintribe.model.processing.platformreflection.os.SystemInformationProvider;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.CollectAccessDataFolderAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.CollectConfigurationFolderAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.CollectDeployablesInformationAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.CollectHealthzAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.CollectModulesFolderAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.CollectPackagingInformationAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.CollectSetupDescriptorAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.CollectSharedStorageAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.HeapDumpAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.HotThreadsAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.PlatformReflectionJsonAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.ProcessesJsonAsyncCallback;
import com.braintribe.model.processing.platformreflection.processor.ReflectionResponseAsyncCallback.ThreadDumpAsyncCallback;
import com.braintribe.model.processing.platformreflection.tf.TribefireInformationProvider;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.service.common.topology.InstanceIdComparator;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.StillProcessing;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.provider.Holder;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.PipeBackedZippingInputStreamProvider;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.system.SystemTools;
import com.braintribe.utils.system.exec.CommandExecution;
import com.braintribe.utils.system.exec.RunCommandContext;
import com.braintribe.utils.system.exec.RunCommandRequest;

public class PlatformReflectionProcessor extends AbstractDispatchingServiceProcessor<PlatformReflectionRequest, Object>
		implements InitializationAware {

	private static Logger logger = Logger.getLogger(PlatformReflectionProcessor.class);

	public static final DateTimeFormatter fileDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withLocale(Locale.US);

	private static final String FILENAME_SETUP_INFO_YAML = "setup-descriptor.yaml";
	private static final String FILENAME_REPOSITORY_VIEW_RESOLUTION_YAML = "repository-view-resolution.yaml";

	protected GmSerializationOptions serializationOptions = GmSerializationOptions.defaultOptions.derive().setOutputPrettiness(OutputPrettiness.high)
			.useDirectPropertyAccess(true).writeEmptyProperties(true).build();

	private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
	private static volatile Object hotspotMBean;

	private SystemInformationProvider systemInformationProvider = null;
	private Map<String, HostInformationProvider> hostInformationProviderMap = new HashMap<>();
	private TribefireInformationProvider tribefireInformationProvider = null;
	private HostInformationProvider hostInformationProvider = null;

	private HostDetector hostDetector = new StandardHostDetector();

	private CommandExecution commandExecution;
	private Supplier<String> userNameProvider;

	private Supplier<Packaging> packagingProvider = null;

	protected InstanceId localInstanceId;

	private Evaluator<ServiceRequest> evaluator;
	private Marshaller jsonMarshaller;
	private Marshaller yamlMarshaller;

	private Supplier<PlatformSetup> platformSetupSupplier;

	private String zipPassword = "operating";

	private File confFolder = null;
	private File databaseFolder;
	private File modulesFolder;

	private Supplier<DcsaSharedStorage> sharedStorageSupplier;

	private Supplier<PersistenceGmSession> setupAccessSessionProvider;
	private PersistenceGmSessionFactory sessionFactory;

	private File setupInfoPath;

	private StreamPipeFactory streamPipeFactory;

	private List<ClasspathContainer> cachedClasspathContainers = null;

	@Override
	protected void configureDispatching(DispatchConfiguration<PlatformReflectionRequest, Object> dispatching) {
		dispatching.register(GetPackagingInformation.T, this::getPackagingInformation);
		dispatching.register(ReflectPlatform.T, this::reflectPlatform);
		dispatching.register(ReflectPlatformJson.T, this::reflectPlatformJson);
		dispatching.register(GetProcessesJson.T, this::getProcessesJson);
		dispatching.register(GetSystemInformation.T, this::getSystemInformation);
		dispatching.register(GetHostInformation.T, this::getHostInformation);
		dispatching.register(GetTribefireInformation.T, this::getTribefireInformation);
		dispatching.register(GetHotThreads.T, this::getHotThreads);
		dispatching.register(GetProcesses.T, this::getProcesses);
		dispatching.register(CollectHealthz.T, this::collectHealthz);
		dispatching.register(GetDiagnosticPackage.T, this::getDiagnosticPackage);
		dispatching.register(GetHeapDump.T, this::getHeapDump);
		dispatching.register(GetConfigurationFolder.T, this::getConfigurationFolder);
		dispatching.register(GetAccessDataFolder.T, this::getAccessDataFolder);
		dispatching.register(GetSharedStorage.T, this::getSharedStorage);
		dispatching.register(GetThreadDump.T, this::getThreadDump);
		dispatching.register(GetLicenseInformation.T, this::getLicenseInformation);
		dispatching.register(GetSetupDescriptor.T, this::getSetupDescriptor);
		dispatching.register(GetRepositoryViewResolution.T, this::getRepositoryViewResolution);
		dispatching.register(GetModulesFolder.T, this::getModulesFolder);
		dispatching.register(GetDeployablesInfo.T, this::getGetDeployablesInfo);
		dispatching.register(CollectDiagnosticPackages.T, this::collectDiagnosticPackage);
	}

	@Override
	public void postConstruct() {
		initHotspotMBean();
	}

	protected PackagingInformation getPackagingInformation(ServiceRequestContext context, GetPackagingInformation request) {
		PackagingInformation pi = PackagingInformation.T.create();
		if (packagingProvider != null) {
			pi.setPackaging(packagingProvider.get());
		}

		logger.debug(() -> "Starting to get setup assets.");

		try {
			PlatformSetup platformSetup = platformSetupSupplier.get();
			if (platformSetup != null) {

				PlatformAsset setupAsset = platformSetup.getSetupAsset();
				pi.setPlatformAsset(setupAsset);

			} else {
				logger.info("Could not find the PlatformSetup.");
			}

		} finally {
			logger.debug("Done with getting setup assets of this tribefire instance.");
		}

		pi.getClasspathContainers().addAll(getClasspathContainers());

		return pi;
	}

	private List<ClasspathContainer> getClasspathContainers() {
		if (cachedClasspathContainers != null) {
			return cachedClasspathContainers;
		}

		List<ClasspathContainer> newList = new ArrayList<>();
		if (modulesFolder != null) {
			logger.debug(() -> "Searching for modules in " + modulesFolder.getAbsolutePath());

			File[] moduleSubfolders = modulesFolder.listFiles(f -> f.isDirectory());
			for (File sub : moduleSubfolders) {
				File cpFile = new File(sub, "classpath");
				if (cpFile.exists()) {
					List<String> lines = FileTools.readLines(cpFile, "UTF-8");
					List<String> nameList = lines.stream().map(l -> FileTools.getName(l)).collect(Collectors.toList());
					Collections.sort(nameList);

					ClasspathContainer cpc = ClasspathContainer.T.create();
					cpc.setContainerName(sub.getName());
					cpc.setClasspathEntries(nameList);

					newList.add(cpc);
				}
			}
		}

		final String catalinaHome = System.getProperty("catalina.home");
		logger.debug(() -> "Identified catalina.home: " + catalinaHome);
		File tomcatHome = new File(catalinaHome);
		if (tomcatHome.exists() && tomcatHome.isDirectory()) {
			File lib = new File(tomcatHome, "lib");
			if (lib.exists()) {
				File[] files = lib.listFiles(f -> f.isFile());
				if (files != null && files.length > 0) {

					ClasspathContainer cpc = ClasspathContainer.T.create();
					cpc.setContainerName("lib");
					List<String> names = new ArrayList<>(files.length);
					Arrays.stream(files).forEach(f -> names.add(f.getName()));
					Collections.sort(names);
					cpc.setClasspathEntries(names);

					newList.add(cpc);
				}
			} else {
				logger.debug(() -> "Could not find lib folder " + lib.getAbsolutePath());
			}
		} else {
			logger.debug(() -> "Could not find catalina.home " + tomcatHome.getAbsolutePath());
		}
		cachedClasspathContainers = newList;
		return newList;
	}

	@SuppressWarnings("unused")
	protected PlatformReflection reflectPlatform(ServiceRequestContext context, ReflectPlatform request) {

		logger.debug("Processing a ReflectPlatform request.");

		PlatformReflection pr = PlatformReflection.T.create();

		CountDownLatch countdown = new CountDownLatch(3);

		GetSystemInformation gsi = GetSystemInformation.T.create();
		this.<SystemInfo> multicastRequestAsyncWithConsumer(gsi, context, si -> {
			pr.setSystem(si);
		}, countdown);

		GetHostInformation ghi = GetHostInformation.T.create();
		this.<HostInfo> multicastRequestAsyncWithConsumer(ghi, context, si -> {
			pr.setHost(si);
		}, countdown);

		GetTribefireInformation gti = GetTribefireInformation.T.create();
		this.<TribefireInfo> multicastRequestAsyncWithConsumer(gti, context, si -> {
			pr.setTribefire(si);
		}, countdown);

		try {
			countdown.await(20L, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			logger.info("reflectPlatform: Got interrupted while waiting for multicast responses.");
		}

		return pr;
	}

	protected void extractFromServiceResult(ServiceResult serviceResult, Consumer<ResponseEnvelope> envelopeConsumer,
			Consumer<Failure> failureConsumer, Consumer<StillProcessing> spConsumer) {
		if (serviceResult == null) {
			return;
		}
		if (serviceResult instanceof ResponseEnvelope) {
			ResponseEnvelope envelope = (ResponseEnvelope) serviceResult;
			envelopeConsumer.accept(envelope);
		} else if (serviceResult instanceof Failure) {
			Failure fail = (Failure) serviceResult;
			failureConsumer.accept(fail);
		} else if (serviceResult instanceof StillProcessing) {
			StillProcessing sp = (StillProcessing) serviceResult;
			spConsumer.accept(sp);
		} else {
			logger.debug("Unsupported ServiceResult type: " + serviceResult);
		}
	}

	protected void extractFromServiceResult(ServiceResult serviceResult, Consumer<ResponseEnvelope> consumer) {
		extractFromServiceResult(serviceResult, consumer, failure -> {
			Throwable throwable = FailureCodec.INSTANCE.decode(failure);
			logger.debug("Received a failure: " + failure.getDetails(), throwable);
		}, stillProcessing -> {
			logger.debug("Instead of receiving a response, we received " + stillProcessing);
		});
	}

	@SuppressWarnings("unused")
	protected PlatformReflectionJson reflectPlatformJson(ServiceRequestContext context, ReflectPlatformJson request) {

		logger.debug("Processing a ReflectPlatformJson request.");
		try {
			ReflectPlatform rp = ReflectPlatform.T.create();
			PlatformReflection platformReflection = unicastRequestSync(rp, context);

			if (platformReflection != null) {
				PlatformReflectionJson pr = PlatformReflectionJson.T.create();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				jsonMarshaller.marshall(baos, platformReflection, serializationOptions);
				String json = baos.toString("UTF-8");

				pr.setPlatformReflectionJson(json);
				return pr;
			} else {
				logger.debug("Did not get a response for the ReflectPlatform request.");
			}

			return null;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not UTF-8 encode the JSON structure.", e);
		} finally {
			logger.debug("Done with processing a ReflectPlatformJson request.");
		}
	}

	@SuppressWarnings("unused")
	protected ProcessesJson getProcessesJson(ServiceRequestContext context, GetProcessesJson request) {

		logger.debug("Processing a GetProcessesJson request.");
		try {
			GetProcesses gp = GetProcesses.T.create();
			Processes getProcesses = unicastRequestSync(gp, context);

			if (getProcesses != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				jsonMarshaller.marshall(baos, getProcesses, serializationOptions);
				String json = baos.toString("UTF-8");

				ProcessesJson pj = ProcessesJson.T.create();
				pj.setProcessesJson(json);

				return pj;
			} else {
				logger.debug("Did not get a response for the GetProcesses request.");
			}
			return null;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not UTF-8 encode the JSON structure.", e);
		} finally {
			logger.debug("Done with processing a GetProcessesJson request.");
		}
	}

	@SuppressWarnings("unused")
	protected SystemInfo getSystemInformation(ServiceRequestContext context, GetSystemInformation request) {

		logger.debug("Processing a GetSystemInformation request.");

		SystemInformationProvider sysInfoProvider = this.getSystemInformationProvider();
		if (sysInfoProvider != null) {
			SystemInfo system = sysInfoProvider.get();

			logger.debug(() -> "Done with processing a GetSystemInformation request.");

			return system;
		}

		logger.debug("Could not process a GetSystemInformation request as no SystemInformationProvider is set.");

		return null;
	}
	@SuppressWarnings("unused")
	protected HostInfo getHostInformation(ServiceRequestContext context, GetHostInformation request) {

		logger.debug("Processing a GetHostInformation request.");

		HostInformationProvider hostInfoProvider = this.getHostInformationProvider();
		if (hostInfoProvider != null) {
			HostInfo host = hostInfoProvider.provide();

			logger.debug("Done with processing a GetHostInformation request.");

			return host;
		}

		logger.debug("Could not process a GetHostInformation request as no HostInformationProvider is set.");

		return null;
	}
	@SuppressWarnings("unused")
	protected TribefireInfo getTribefireInformation(ServiceRequestContext context, GetTribefireInformation request) {

		logger.debug(() -> "Processing a GetTribefireInformation request.");

		TribefireInformationProvider tfProvider = this.getTribefireInformationProvider();
		if (tfProvider != null) {
			TribefireInfo tfInfo = tfProvider.provide();

			logger.debug("Done with processing a GetTribefireInformation request.");

			return tfInfo;
		}

		logger.debug("Could not process a GetTribefireInformation request as no TribefireInformationProvider is set.");

		return null;
	}

	@SuppressWarnings("unused")
	protected HotThreads getHotThreads(ServiceRequestContext context, GetHotThreads request) {

		logger.debug("Processing a GetHotThreads request.");

		HotThreads hts;
		try {
			hts = HotThreadsProvider.detectWithDefaults(request.getInterval(), request.getThreads(), request.getIgnoreIdleThreads(),
					request.getSampleType(), request.getThreadElementsSnapshotCount(), request.getThreadElementsSnapshotDelayInMs());
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while trying to get hot threads.");
		}

		logger.debug("Done with processing a GetHotThreads request.");

		return hts;

	}

	@SuppressWarnings("unused")
	protected Processes getProcesses(ServiceRequestContext context, GetProcesses request) {

		logger.debug("Processing a GetProcesses request.");
		try {
			oshi.SystemInfo si = new oshi.SystemInfo();

			List<Process> processList = ProcessesProvider.getProcesses(si, false);

			processList.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

			Processes processes = Processes.T.create();
			processes.setProcesses(processList);

			return processes;
		} catch (Throwable t) {
			logger.error("Error while trying to collect process information.", t);
		} finally {
			logger.debug("Done with processing a GetProcesses request.");
		}
		return null;
	}

	private static void initHotspotMBean() {
		if (hotspotMBean == null) {
			synchronized (PlatformReflectionProcessor.class) {
				if (hotspotMBean == null) {
					hotspotMBean = getHotspotMBean();
				}
			}
		}
	}
	private static Object getHotspotMBean() {
		try {
			Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			Object bean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, clazz);
			return bean;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
	}

	@SuppressWarnings("unused")
	protected Healthz collectHealthz(ServiceRequestContext context, CollectHealthz request) {

		logger.debug("Processing a CollectHealthz request.");

		CountDownLatch countDown = new CountDownLatch(1);

		RunCheckBundles run = RunCheckBundles.T.create();
		run.setCoverage(null);
		run.setIsPlatformRelevant(null);

		ExecuteAndMarshallResponse marshallingRequest = ExecuteAndMarshallResponse.T.create();
		marshallingRequest.setMimeType("text/html;spec=check-bundles-response");
		marshallingRequest.setServiceRequest(run);

		Holder<String> resultHolder = new Holder<>();

		final Instant start = NanoClock.INSTANCE.instant();

		marshallingRequest.eval(evaluator).get(new AsyncCallback<Resource>() {
			@Override
			public void onSuccess(Resource response) {
				try (InputStream in = response.openStream()) {
					String json = IOTools.slurp(in, "UTF-8");
					resultHolder.accept(json);
				} catch (Exception e) {
					logger.warn(() -> "Error while trying to read response from wrapped RunCheckBundles service request.", e);
				} finally {
					countDown.countDown();
					logger.debug(() -> "Got a successful response for the local RunCheckBundles request after: "
							+ StringTools.prettyPrintDuration(start, true, null));
				}
			}
			@Override
			public void onFailure(Throwable t) {
				try {
					logger.info(() -> "Got an error while trying to collect healthz information", t);
				} finally {
					countDown.countDown();
					logger.debug(() -> "Got a failure response for local RunCheckBundles request after: "
							+ StringTools.prettyPrintDuration(start, true, null));
				}
			}

		});

		try {
			if (!countDown.await(2, TimeUnit.MINUTES)) {
				logger.info(() -> "Could not get a response for the wrapped RunCheckBundles service request within 2 min.");
			}
		} catch (InterruptedException ie) {
			logger.info("Got interrupted while waiting for results from the CheckRequest multicast.");
		}

		Healthz result = Healthz.T.create();
		result.setCheckBundlesResponseAsHtml(resultHolder.get());

		return result;

	}

	protected DiagnosticPackages collectDiagnosticPackage(ServiceRequestContext context, CollectDiagnosticPackages request) {
		logger.debug("collectDiagnosticPackage: Processing a CollectDiagnosticPackages request.");

		DiagnosticPackages result = DiagnosticPackages.T.create();
		try {

			GetDiagnosticPackage gp = GetDiagnosticPackage.T.create();
			gp.setIncludeHeapDump(request.getIncludeHeapDump());
			gp.setIncludeLogs(request.getIncludeLogs());
			gp.setExcludeSharedStorageBinaries(request.getExcludeSharedStorageBinaries());
			gp.setPasswordProtection(false);

			Long waitTimeoutInMs = request.getWaitTimeoutInMs();
			if (waitTimeoutInMs == null || waitTimeoutInMs <= 0) {
				waitTimeoutInMs = Numbers.MILLISECONDS_PER_MINUTE * 30l;
			}
			float allWaitTimeoutFloat = waitTimeoutInMs.floatValue();
			float individualWaitTimeoutFloat = allWaitTimeoutFloat * 0.75f;
			long individualWaitTimeout = Math.max((long) individualWaitTimeoutFloat, 0l);
			gp.setWaitTimeoutInMs(individualWaitTimeout);

			logger.debug(
					"collectDiagnosticPackage: Issuing a multicast GetDiagnosticPackage request now. Timeout will be: " + waitTimeoutInMs + " ms");

			Map<InstanceId, DiagnosticPackage> resultMap = multicastRequestSync(gp, context, waitTimeoutInMs);

			if (resultMap != null && !resultMap.isEmpty()) {

				logger.debug(() -> "collectDiagnosticPackage: Received a result map with " + resultMap.size() + " entries.");

				String now = DateTools.encode(new Date(), fileDateTimeFormatter);
				String filename = "tribefire-diagnostic-package-" + now + ".zip";

				List<Resource> collectedResources = new ArrayList<>();
				for (Map.Entry<InstanceId, DiagnosticPackage> entry : resultMap.entrySet()) {
					InstanceId responder = entry.getKey();
					logger.debug(() -> "collectDiagnosticPackage: Processing response from " + responder.stringify());

					DiagnosticPackage dp = entry.getValue();
					Resource dpResource = dp.getDiagnosticPackage();
					collectedResources.add(dpResource);
				}

				Resource callResource = null;
				try {
					callResource = Resource.createTransient(
							new ResourceBasedEncryptedZippingInputStreamProvider(streamPipeFactory, filename, collectedResources, zipPassword));
				} catch (IOException ioe) {
					throw new RuntimeException("Could not package diagnostic package.", ioe);
				}
				callResource.setName(filename);
				callResource.setMimeType("application/zip");
				callResource.setCreated(new Date());

				try {
					String owner = userNameProvider.get();
					callResource.setCreator(owner);
				} catch (RuntimeException e) {
					logger.debug("Could not get the current user name.", e);
				}
				result.setDiagnosticPackages(callResource);

			} else {
				logger.debug("collectDiagnosticPackage: Did not get a single response for the GetDiagnosticPackage request.");
			}

		} finally {
			logger.debug("collectDiagnosticPackage: Done with processing a CollectDiagnosticPackages request.");
		}
		return result;

	}

	protected DiagnosticPackage getDiagnosticPackage(ServiceRequestContext context, GetDiagnosticPackage request) {

		StopWatch stopWatch = new StopWatch();

		logger.debug(() -> "getDiagnosticPackage: Processing a GetDiagnosticPackage request.");

		DiagnosticPackageContext dpContext = new DiagnosticPackageContext();

		boolean includeLogs = request.getIncludeLogs() != null ? request.getIncludeLogs() : false;
		boolean includeHeapDump = request.getIncludeHeapDump() != null ? request.getIncludeHeapDump() : false;

		int count = 13;
		if (includeLogs)
			count++;
		if (includeHeapDump)
			count++;

		CountDownLatch countdown = new CountDownLatch(count);

		GetThreadDump gtd = GetThreadDump.T.create();
		multicastRequestAsync(gtd, context, new ThreadDumpAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetThreadDump request.");

		ReflectPlatformJson rpj = ReflectPlatformJson.T.create();
		multicastRequestAsync(rpj, context, new PlatformReflectionJsonAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous PlatformReflectionJson request.");

		GetHotThreads ght = GetHotThreads.T.create();
		ght.setThreads(10);
		multicastRequestAsync(ght, context, new HotThreadsAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous HotThreads request.");

		GetProcessesJson gpj = GetProcessesJson.T.create();
		multicastRequestAsync(gpj, context, new ProcessesJsonAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous CollectHealthz request.");

		CollectHealthz ch = CollectHealthz.T.create();
		multicastRequestAsync(ch, context, new CollectHealthzAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetProcessesJson request.");

		GetPackagingInformation gpi = GetPackagingInformation.T.create();
		multicastRequestAsync(gpi, context, new CollectPackagingInformationAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetPackagingInformation request.");

		GetDeployablesInfo gdi = GetDeployablesInfo.T.create();
		multicastRequestAsync(gdi, context, new CollectDeployablesInformationAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetPackagingInformation request.");

		GetSetupDescriptor gsd = GetSetupDescriptor.T.create();
		ExecuteAndMarshallResponse marshallingRequest = ExecuteAndMarshallResponse.T.create();
		marshallingRequest.setMimeType("text/yaml");
		marshallingRequest.setServiceRequest(gsd);
		multicastRequestAsync(marshallingRequest, context, new CollectSetupDescriptorAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetSetupDescriptor request.");

		GetConfigurationFolder gcf = GetConfigurationFolder.T.create();
		EvalContext<? extends ConfigurationFolder> configurationFolderEval = gcf.eval(context);
		configurationFolderEval.get(new CollectConfigurationFolderAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetConfigurationFolder request.");

		GetModulesFolder gmf = GetModulesFolder.T.create();
		EvalContext<? extends ModulesFolder> modulesFolderEval = gmf.eval(context);
		modulesFolderEval.get(new CollectModulesFolderAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetConfigurationFolder request.");

		GetAccessDataFolder gad = GetAccessDataFolder.T.create();
		EvalContext<? extends AccessDataFolder> accessDataFolderEval = gad.eval(context);
		accessDataFolderEval.get(new CollectAccessDataFolderAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetSharedStorage request.");

		triggerGetAssets(dpContext, countdown);

		if (includeHeapDump) {
			GetHeapDump ghd = GetHeapDump.T.create();
			EvalContext<? extends HeapDump> heapDumpEval = ghd.eval(context);
			heapDumpEval.get(new HeapDumpAsyncCallback(dpContext, countdown));

			logger.debug("getDiagnosticPackage: Issued an asynchronous GetHeapDump request.");
		}

		if (includeLogs) {
			GetLogs gl = GetLogs.T.create();
			gl.setFilename("*");
			EvalContext<? extends Logs> logsEval = gl.eval(context);
			logsEval.get(new LogsAsyncCallback(dpContext, countdown));

			logger.debug("getDiagnosticPackage: Issued an asynchronous GetLogs request.");
		}

		GetSharedStorage gss = GetSharedStorage.T.create();
		gss.setExcludeBinaries(request.getExcludeSharedStorageBinaries());
		EvalContext<? extends SharedStorage> sharedStorageEval = gss.eval(context);
		sharedStorageEval.get(new CollectSharedStorageAsyncCallback(dpContext, countdown));

		logger.debug("getDiagnosticPackage: Issued an asynchronous GetSharedStorage request.");

		stopWatch.intermediate("Triggered internal requests");

		final int totalWaitCount = count;
		logger.debug(() -> "getDiagnosticPackage: Waiting for " + totalWaitCount + " asynchronous responses.");

		Long waitTimeoutInMs = request.getWaitTimeoutInMs();
		try {
			if (waitTimeoutInMs == null || waitTimeoutInMs < 0l) {
				countdown.await();
			} else {
				if (!countdown.await(waitTimeoutInMs, TimeUnit.MILLISECONDS)) {
					String msg = "Diagnostic package may be incomplete as the timeout of " + waitTimeoutInMs + " was exceeded.";
					logger.debug(() -> msg);
					dpContext.errors.add(msg);
				}
			}
		} catch (InterruptedException e) {
			logger.info("getDiagnosticPackage: Got interrupted while waiting for the platform reflection results.");
		}

		stopWatch.intermediate("Got " + totalWaitCount + " result");

		logger.debug(() -> "getDiagnosticPackage: Done waiting for " + totalWaitCount + " asynchronous responses.");

		DiagnosticPackage dp = DiagnosticPackage.T.create();
		dp.setInstanceId(localInstanceId);

		String now = DateTools.encode(new Date(), fileDateTimeFormatter);
		String nodeId = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_NODE_ID);
		if (nodeId == null || nodeId.trim().length() == 0) {
			nodeId = "tribefire";
		} else {
			nodeId = FileTools.normalizeFilename(nodeId, '_');
		}

		Map<String, File> files = new LinkedHashMap<>();

		if (dpContext.healthz != null) {
			String htmlString = dpContext.healthz.getCheckBundlesResponseAsHtml();
			if (htmlString != null) {
				File processesJsonFile = writeToTempFile("healthz-" + now, htmlString);
				files.put("healthz-" + now + ".html", processesJsonFile);
			}
		}
		if (dpContext.packagingInformation != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			jsonMarshaller.marshall(baos, dpContext.packagingInformation,
					GmSerializationOptions.defaultOptions.derive().setOutputPrettiness(OutputPrettiness.high).build());
			String packagingInformationString = null;
			try {
				packagingInformationString = new String(baos.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("Could not encode packaging information", e);
			}
			File processesJsonFile = writeToTempFile("packagingInformation-" + now, packagingInformationString);
			files.put("packagingInformation-" + now + ".json", processesJsonFile);
		}
		if (dpContext.setupDescriptorResource != null) {
			File setupDescriptorYamlFile = writeToTempFile("setupDescriptor-" + now, dpContext.setupDescriptorResource);
			files.put("setupDescriptor-" + now + ".yaml", setupDescriptorYamlFile);
		}
		if (dpContext.deployablesInfo != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			jsonMarshaller.marshall(baos, dpContext.deployablesInfo,
					GmSerializationOptions.defaultOptions.derive().setOutputPrettiness(OutputPrettiness.high).build());
			String deployablesInfoString = null;
			try {
				deployablesInfoString = new String(baos.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("Could not encode deployables information", e);
			}
			File deployablesJsonFile = writeToTempFile("deployables-" + now, deployablesInfoString);
			files.put("deployables-" + now + ".json", deployablesJsonFile);
		}

		File reflectionJsonFile = writeToTempFile("platformReflectionJson-" + now, dpContext.platformReflectionJson);
		File processesJsonFile = writeToTempFile("processesJson-" + now, dpContext.processesJson);
		File threadDumpFile = writeToTempFile("threaddump-" + now, dpContext.threadDump);
		File hotThreadsFile = writeToTempFile("hotThreads-" + now, dpContext.hotThreads);
		File setupAssetsFile = writeToTempFile("setupAssets-" + now, dpContext.setupAssetsAsJson);
		files.put("platformReflectionJson-" + now + ".json", reflectionJsonFile);
		files.put("processesJson-" + now + ".json", processesJsonFile);
		files.put("threaddump-" + now + ".txt", threadDumpFile);
		files.put("hotThreads-" + now + ".txt", hotThreadsFile);
		files.put("setupAssets-" + now + ".json", setupAssetsFile);

		if (includeLogs) {
			if (dpContext.logs != null) {
				files.put(dpContext.logsFilename, dpContext.logs);
			}
		}
		if (includeHeapDump) {
			if (dpContext.heapDump != null) {
				files.put(dpContext.heapDumpFilename, dpContext.heapDump);
			}
		}
		if (dpContext.configurationFolderAsZip != null) {
			files.put(dpContext.configurationFolderAsZipFilename, dpContext.configurationFolderAsZip);
		}
		if (dpContext.modulesFolderAsZip != null) {
			files.put(dpContext.modulesFolderAsZipFilename, dpContext.modulesFolderAsZip);
		}
		if (dpContext.sharedStorageAsZip != null) {
			files.put(dpContext.sharedStorageAsZipFilename, dpContext.sharedStorageAsZip);
		}
		if (dpContext.accessDataFolderAsZip != null) {
			files.put(dpContext.accessDataFolderAsZipFilename, dpContext.accessDataFolderAsZip);
		}

		if (!dpContext.errors.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			dpContext.errors.forEach(e -> sb.append(e).append("\n"));
			File errorFile = writeToTempFile("reflectionErrors-" + now, sb.toString());
			files.put("reflectionErrors-" + now + ".txt", errorFile);
		}

		stopWatch.intermediate("File Prep");

		String filename = "tribefire-diagnostic-package-" + nodeId + "-" + now + ".zip";

		logger.debug(() -> "getDiagnosticPackage: Embedding the diagnostic package directly in the response.");
		Resource callResource = null;
		try {
			String pwd = request.getPasswordProtection() ? zipPassword : null;
			callResource = Resource.createTransient(new EncryptedZippingInputStreamProvider(streamPipeFactory,
					"tribefire-diagnostic-package-" + nodeId + "-" + now, files, true, pwd));
		} catch (IOException ioe) {
			throw new RuntimeException("Could not package diagnostic package.", ioe);
		}
		callResource.setName(filename);
		callResource.setMimeType("application/zip");
		callResource.setCreated(new Date());

		try {
			String owner = userNameProvider.get();
			callResource.setCreator(owner);
		} catch (RuntimeException e) {
			logger.debug("Could not get the current user name.", e);
		}
		dp.setDiagnosticPackage(callResource);

		logger.debug("getDiagnosticPackage: Done with processing a GetDiagnosticPackage request: " + stopWatch);

		return dp;
	}

	private void triggerGetAssets(DiagnosticPackageContext dpContext, CountDownLatch countdown) {
		GetAssets getAssets = GetAssets.T.create();
		getAssets.setEffective(Boolean.TRUE);
		getAssets.setSetupAssets(true);
		PersistenceGmSession setupSession = setupAccessSessionProvider.get();
		EvalContext<AssetCollection> assetCollectionEval = getAssets.eval(setupSession);
		assetCollectionEval.get(new AsyncCallback<AssetCollection>() {
			@Override
			public void onSuccess(AssetCollection future) {
				try {
					List<PlatformAsset> assets = future.getAssets();
					String json = null;
					if (jsonMarshaller instanceof HasStringCodec) {
						json = ((HasStringCodec) jsonMarshaller).getStringCodec().encode(assets,
								GmSerializationOptions.defaultOptions.derive().setOutputPrettiness(OutputPrettiness.high).build());
					} else {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						jsonMarshaller.marshall(baos, assets,
								GmSerializationOptions.defaultOptions.derive().setOutputPrettiness(OutputPrettiness.high).build());
						json = baos.toString("UTF-8");
					}
					dpContext.setupAssetsAsJson = json;
				} catch (Exception e) {
					logger.info("Could not marshall assets.", e);
				} finally {
					countdown.countDown();
				}
			}
			@Override
			public void onFailure(Throwable t) {
				try {
					logger.error("Error while waiting for a AssetCollection", t);
				} finally {
					countdown.countDown();
				}
			}
		});
		logger.debug("Issued an asynchronous GetAssets request.");

	}

	protected File writeToTempFile(String name, Resource resource) {
		try (InputStream in = resource.openStream()) {
			String content = IOTools.slurp(in, "UTF-8");
			return writeToTempFile(name, content);
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Could not open stream of resource " + resource);
		}
	}
	protected File writeToTempFile(String name, String content) {
		File tempFile = null;
		try {
			if (content == null) {
				content = "empty";
			}
			tempFile = File.createTempFile(name, ".txt");
			tempFile.delete();
			FileTools.deleteFileWhenOrphaned(tempFile);
			FileTools.writeStringToFile(tempFile, content, "UTF-8");
			return tempFile;
		} catch (Exception e) {
			logger.error("Could not write content " + content + " to file " + tempFile, e);
			return null;
		}
	}

	@SuppressWarnings("unused")
	protected HeapDump getHeapDump(ServiceRequestContext context, GetHeapDump request) {

		logger.debug("Processing a GetHeapDump request.");

		try {
			HeapDump hd = HeapDump.T.create();

			String now = DateTools.encode(new Date(), fileDateTimeFormatter);
			File headDumpFile = File.createTempFile("heapdump-" + now + "-", ".hprof");
			headDumpFile.delete();
			FileTools.deleteFileWhenOrphaned(headDumpFile);

			Boolean liveObjectOnly = request.getLiveObjectOnly();
			if (liveObjectOnly == null) {
				liveObjectOnly = true;
			}
			try {
				Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
				Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
				m.invoke(hotspotMBean, headDumpFile.getAbsolutePath(), liveObjectOnly.booleanValue());
			} catch (Exception exp) {
				throw new Exception("Error while using the hotspot bean to create a heap dump.", exp);
			}

			Map<String, File> files = new HashMap<>();
			files.put("heapdump-" + now + ".hprof", headDumpFile);
			Resource callResource = Resource
					.createTransient(new PipeBackedZippingInputStreamProvider(streamPipeFactory, "heapdump-" + now, files, true));
			callResource.setName("heapdump-" + now + ".zip");
			callResource.setMimeType("application/zip");
			callResource.setCreated(new Date());
			try {
				callResource.setCreator(this.userNameProvider.get());
			} catch (RuntimeException e) {
				logger.debug("Could not get the current user name.", e);
			}
			hd.setHeapDump(callResource);

			return hd;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not acquire a heapdump.");
		} finally {
			logger.debug("Done with processing a GetHeapDump request.");
		}
	}

	@SuppressWarnings("unused")
	protected DeployablesInfo getGetDeployablesInfo(ServiceRequestContext context, GetDeployablesInfo request) {
		DeployablesInfo result = tribefireInformationProvider.getGetDeployablesInfo();
		return result;
	}

	@SuppressWarnings("unused")
	protected ModulesFolder getModulesFolder(ServiceRequestContext context, GetModulesFolder request) {

		logger.debug("Processing a GetModulesFolder request.");

		try {
			ModulesFolder hd = ModulesFolder.T.create();

			if (modulesFolder != null) {

				String now = DateTools.encode(new Date(), fileDateTimeFormatter);
				String filename = "modules-" + now + ".zip";

				List<File> list = FileTools.findRecursively(modulesFolder, f -> {
					if (f.isDirectory()) {
						String name = f.getName().toLowerCase();
						if (name.equals("resources") || name.equals("lib")) {
							return false;
						}
					}
					return true;
				});

				String basePath = modulesFolder.getAbsolutePath();
				logger.debug(() -> "Base modules folder path: " + basePath);
				int basePathLength = basePath.length();
				if (!basePath.endsWith("/") && !basePath.endsWith("\\")) {
					basePathLength++;
				}
				if (logger.isDebugEnabled())
					logger.debug("Base modules folder path length (with ending '/'): " + basePathLength);

				Map<String, File> map = new LinkedHashMap<>();
				for (File f : list) {
					String fullPath = f.getAbsolutePath();
					logger.debug(() -> "Evaluating full path: " + fullPath);
					if (fullPath.length() > basePathLength) {
						String relPath = fullPath.substring(basePathLength);
						logger.debug(() -> "Including relative path: " + fullPath);
						map.put(relPath, f);
					}
				}

				if (!map.isEmpty()) {
					Resource callResource = Resource
							.createTransient(new PipeBackedZippingInputStreamProvider(streamPipeFactory, filename, map, false));

					callResource.setName(filename);
					callResource.setMimeType("application/zip");
					callResource.setCreated(new Date());
					try {
						callResource.setCreator(this.userNameProvider.get());
					} catch (RuntimeException e) {
						logger.debug("Could not get the current user name.", e);
					}

					hd.setModulesFolderAsZip(callResource);
				}
			}

			return hd;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not collect the files in the modules folder: " + modulesFolder);
		} finally {
			logger.debug("Done with processing a GetModulesFolder request.");
		}

	}

	@SuppressWarnings("unused")
	protected ConfigurationFolder getConfigurationFolder(ServiceRequestContext context, GetConfigurationFolder request) {

		logger.debug("Processing a GetConfigurationFolder request.");

		try {
			ConfigurationFolder hd = ConfigurationFolder.T.create();

			if (confFolder != null) {

				String now = DateTools.encode(new Date(), fileDateTimeFormatter);
				String filename = "conf-" + now + ".zip";

				List<File> list = FileTools.listFilesRecursively(confFolder);

				String basePath = confFolder.getAbsolutePath();
				logger.debug(() -> "Base configuration folder path: " + basePath);
				int basePathLength = basePath.length();
				if (!basePath.endsWith("/") && !basePath.endsWith("\\")) {
					basePathLength++;
				}
				if (logger.isDebugEnabled())
					logger.debug("Base configuration folder path length (with ending '/'): " + basePathLength);
				Map<String, File> map = new LinkedHashMap<>();
				for (File f : list) {
					String fullPath = f.getAbsolutePath();
					logger.debug(() -> "Evaluating full path: " + fullPath);
					if (fullPath.length() > basePathLength) {
						String relPath = fullPath.substring(basePathLength);
						logger.debug(() -> "Including relative path: " + fullPath);
						map.put(relPath, f);
					}
				}

				if (!map.isEmpty()) {
					Resource callResource = Resource
							.createTransient(new PipeBackedZippingInputStreamProvider(streamPipeFactory, filename, map, false));

					callResource.setName(filename);
					callResource.setMimeType("application/zip");
					callResource.setCreated(new Date());
					try {
						callResource.setCreator(this.userNameProvider.get());
					} catch (RuntimeException e) {
						logger.debug("Could not get the current user name.", e);
					}

					hd.setConfigurationFolderAsZip(callResource);
				}
			}

			return hd;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not collect the files in the configuration folder: " + confFolder);
		} finally {
			logger.debug("Done with processing a GetConfigurationFolder request.");
		}
	}

	@SuppressWarnings("unused")
	protected AccessDataFolder getAccessDataFolder(ServiceRequestContext context, GetAccessDataFolder request) {

		logger.debug("Processing a GetAccessDataFolder request.");

		try {
			AccessDataFolder hd = AccessDataFolder.T.create();

			if (databaseFolder != null) {

				String now = DateTools.encode(new Date(), fileDateTimeFormatter);
				String accessId = request.getAccessId();
				if (StringTools.isBlank(accessId)) {
					accessId = TribefireConstants.ACCESS_CORTEX;
				}

				String filename = "storage-data-" + FileTools.normalizeFilename(accessId, '_') + "-" + now + ".zip";

				File accessFolder = new File(databaseFolder, accessId);
				File dataFolder = new File(accessFolder, "data");

				if (dataFolder.exists()) {

					List<File> list = FileTools.listFilesRecursively(dataFolder);

					String basePath = dataFolder.getAbsolutePath();
					int basePathLength = basePath.length();
					if (!basePath.endsWith("/") && !basePath.endsWith("\\")) {
						basePathLength++;
					}
					Map<String, File> map = new LinkedHashMap<>();
					for (File f : list) {
						String fullPath = f.getAbsolutePath();
						if (fullPath.length() > basePathLength) {
							String relPath = fullPath.substring(basePathLength);
							map.put(relPath, f);
						}
					}

					if (!map.isEmpty()) {
						Resource callResource = Resource
								.createTransient(new PipeBackedZippingInputStreamProvider(streamPipeFactory, filename, map, false));

						callResource.setName(filename);
						callResource.setMimeType("application/zip");
						callResource.setCreated(new Date());
						try {
							callResource.setCreator(this.userNameProvider.get());
						} catch (RuntimeException e) {
							logger.debug("Could not get the current user name.", e);
						}

						hd.setAccessDataFolderAsZip(callResource);
					}
				}
			}

			return hd;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not collect the files in the configuration folder.");
		} finally {
			logger.debug("Done with processing a GetConfigurationFolder request.");
		}
	}

	@SuppressWarnings("unused")
	protected SharedStorage getSharedStorage(ServiceRequestContext context, GetSharedStorage request) {

		logger.debug("Processing a GetSharedStorage request with excludeBinaries=" + request.getExcludeBinaries());

		StopWatch stopWatch = new StopWatch();

		try {
			SharedStorage hd = SharedStorage.T.create();
			DcsaSharedStorage sharedStorage = sharedStorageSupplier.get();

			if (sharedStorage != null) {

				String now = DateTools.encode(new Date(), fileDateTimeFormatter);
				String accessId = NullSafe.get(request.getAccessId(), TribefireConstants.ACCESS_CORTEX);
				boolean excludeBinaries = request.getExcludeBinaries();

				String filename = "shared-storage-" + FileTools.normalizeFilename(accessId, '_') + "-" + now + ".zip";

				Map<String, File> map = new LinkedHashMap<>();

				DcsaIterable iterable = sharedStorage.readOperations(accessId, null);
				if (iterable != null) {

					Map<String, LazyResourceReference> lazyLoadedResources = new HashMap<>();
					Iterator<CsaOperation> it = iterable.iterator();
					int index = 1;
					while (it.hasNext()) {
						CsaOperation csaOp = it.next();
						if (excludeBinaries && csaOp instanceof CsaStoreResource)
							continue;

						String indexString = StringTools.extendStringInFront("" + index, '0', 5);
						String name = indexString + "-" + csaOp.operationType() + ".json";

						File outputFile = File.createTempFile(name, ".tmp");
						outputFile.delete();

						try (OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
							jsonMarshaller.marshall(os, csaOp,
									GmSerializationOptions.defaultOptions.derive().setOutputPrettiness(OutputPrettiness.high).build());
							map.put(name, outputFile);
						} catch (Exception e) {
							logger.warn(() -> "Error while trying to marshall CsaOperation " + csaOp + " to " + outputFile.getAbsolutePath(), e);
						}

						if (csaOp instanceof CsaResourceBasedOperation) {
							CsaResourceBasedOperation rbo = (CsaResourceBasedOperation) csaOp;
							Resource payload = rbo.getPayload();

							if (payload == null) {
								if (csaOp instanceof CsaStoreResource) {
									CsaStoreResource csr = (CsaStoreResource) csaOp;
									String relativePath = csr.getResourceRelativePath();
									lazyLoadedResources.put(relativePath, new LazyResourceReference(indexString, rbo));
								}
							}

							if (payload != null) {
								writeResource(map, indexString, rbo, payload);
							}
						}
						index++;
					}

					stopWatch.intermediate("Loaded " + (index - 1) + " Ops");

					if (!lazyLoadedResources.isEmpty() && !excludeBinaries) {

						logger.debug(() -> "Loading " + lazyLoadedResources.size() + " resources.");
						Map<String, Resource> resources = sharedStorage.readResource(accessId, lazyLoadedResources.keySet());
						for (Map.Entry<String, Resource> entry : resources.entrySet()) {
							String relativePath = entry.getKey();
							LazyResourceReference reference = lazyLoadedResources.get(relativePath);
							Resource resource = entry.getValue();
							if (reference != null && resource != null) {
								writeResource(map, reference.indexString, reference.rbo, resource);
							}
						}

						stopWatch.intermediate("Loaded " + lazyLoadedResources.size() + " resources");

					}

				}

				if (!map.isEmpty()) {
					Resource callResource = Resource
							.createTransient(new PipeBackedZippingInputStreamProvider(streamPipeFactory, filename, map, true));

					callResource.setName(filename);
					callResource.setMimeType("application/zip");
					callResource.setCreated(new Date());
					try {
						callResource.setCreator(this.userNameProvider.get());
					} catch (RuntimeException e) {
						logger.debug("Could not get the current user name.", e);
					}

					hd.setSharedStorageAsZip(callResource);

					stopWatch.intermediate("Created Zip Resource provider");
				}

			}

			return hd;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not acquire the DCSA operations from the storage.");
		} finally {
			logger.debug("Done with processing a GetSharedStorage request: " + stopWatch);
		}
	}

	private void writeResource(Map<String, File> map, String indexString, CsaResourceBasedOperation rbo, Resource payload) {
		String payloadName = payload.getName();
		if (StringTools.isBlank(payloadName)) {
			payloadName = "payload.bin";
		}

		String resourceFilename = indexString + "-" + rbo.operationType() + "-" + FileTools.normalizeFilename(payloadName, '_');

		final File outputResourceFile;
		try {
			outputResourceFile = File.createTempFile(resourceFilename, ".tmp");
			outputResourceFile.delete();
		} catch (IOException ioe) {
			logger.warn(() -> "Error while trying to create a temporary file.", ioe);
			return;
		}

		try (InputStream in = payload.openStream(); OutputStream os = new BufferedOutputStream(new FileOutputStream(outputResourceFile))) {
			IOTools.pump(in, os);
			map.put(resourceFilename, outputResourceFile);
		} catch (Exception e) {
			logger.warn(() -> "Error while trying to stream the payload of CsaResourceBasedOperation " + rbo + " to "
					+ outputResourceFile.getAbsolutePath(), e);
		}
	}

	private class LazyResourceReference {
		String indexString;
		CsaResourceBasedOperation rbo;

		public LazyResourceReference(String indexString, CsaResourceBasedOperation rbo) {
			this.indexString = indexString;
			this.rbo = rbo;
		}
	}

	@SuppressWarnings("unused")
	protected License getLicenseInformation(ServiceRequestContext context, GetLicenseInformation request) {
		return tribefireInformationProvider.getLicenseInformation();
	}

	@SuppressWarnings("unused")
	protected ThreadDump getThreadDump(ServiceRequestContext context, GetThreadDump request) {

		ThreadDump td = ThreadDump.T.create();

		// First, we try to get a native thread dump. If that does not work out (e.g., no JDK is installed), we use a
		// minimal Java-internal dump

		String dump = getThreadDumpNative();
		if (dump == null || dump.trim().length() == 0) {
			dump = getThreadDumpJava();
		}
		td.setThreadDump(dump);
		return td;
	}

	protected String getThreadDumpNative() {
		logger.debug("Trying to create a native thread dump.");
		try {
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
			String localProcessName = runtimeMXBean.getName();
			int idx = localProcessName.indexOf('@');
			int pid = -1;
			if (idx != -1) {
				try {
					pid = Integer.parseInt(localProcessName.substring(0, idx));
				} catch (Exception e) {
					logger.debug(() -> "Could not get the local PID from the process name " + localProcessName);
					return null;
				}
			}
			if (pid == -1) {
				logger.debug("The process name " + localProcessName + " does not provide the PID.");
				return null;
			}
			String javaHome = System.getProperty("java.home");
			File javaHomeDir = new File(javaHome);

			String threadDump = null;
			File jcmdExecutable = findJCmd(javaHomeDir);
			if (jcmdExecutable != null) {
				threadDump = getThreadDumpNative(jcmdExecutable, pid, "" + pid, "Thread.print");
			}
			if (threadDump == null) {
				File jstackExecutable = findJStack(javaHomeDir);
				threadDump = getThreadDumpNative(jstackExecutable, pid, "-l", "" + pid);
			}
			return threadDump;

		} catch (Exception e) {
			logger.debug(() -> "Could not get the native thread dump.", e);
		}
		return null;
	}

	protected String getThreadDumpNative(File executable, int pid, String... arguments) {
		if (executable == null) {
			return null;
		}
		logger.debug(() -> "Using executable " + executable.getAbsolutePath() + " to get a thread dump from process " + pid);

		try {
			List<String> execParts = new ArrayList<>(arguments.length + 1);
			execParts.add(executable.getAbsolutePath());
			CollectionTools.addElementsToCollection(execParts, arguments);

			RunCommandRequest request = new RunCommandRequest(execParts.toArray(new String[0]), 5000L);
			RunCommandContext context = commandExecution.runCommand(request);
			int errorCode = context.getErrorCode();
			if (errorCode == 0) {

				logger.debug("Creating a native thread dump succeeded.");

				String output = context.getOutput();
				if (StringTools.isBlank(output)) {
					return null;
				}
				return output;
			} else {
				logger.debug(() -> "Executing " + executable.getAbsolutePath() + " with PID " + pid + " resulted in: " + context.toString());
			}
		} catch (Exception e) {
			logger.debug(() -> "Could not get the native thread dump using " + executable.getAbsolutePath(), e);
		}
		return null;
	}

	protected File findJStack(File javaHomeDir) {
		return findJavaBinExecutable(javaHomeDir, "jstack");
	}

	protected File findJCmd(File javaHomeDir) {
		return findJavaBinExecutable(javaHomeDir, "jcmd");
	}

	protected File findJavaBinExecutable(File javaHomeDir, String execName) {
		if (!javaHomeDir.exists()) {
			return null;
		}
		String executable = SystemTools.isWindows() ? execName + ".exe" : execName;

		List<File> toInspectList = new ArrayList<>();
		toInspectList.add(javaHomeDir.getParentFile());
		while (!toInspectList.isEmpty()) {
			File dir = toInspectList.remove(0);
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files) {
					if (f.isDirectory()) {
						toInspectList.add(f);
					} else {
						String name = f.getName().toLowerCase();
						if (name.equals(executable)) {
							logger.debug(() -> "Found " + execName + " at: " + f.getAbsolutePath());
							return f;
						}
					}
				}
			}
		}

		logger.debug(() -> "Could not find " + execName);

		return null;
	}

	protected String getThreadDumpJava() {

		logger.debug("Trying to create a thread dump within the JVM.");

		try {
			final StringBuilder dump = new StringBuilder();
			final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);

			Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
			Map<Long, Map.Entry<Thread, StackTraceElement[]>> threadMap = new HashMap<>();
			for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
				threadMap.put(entry.getKey().getId(), entry);
			}

			dump.append(DateTools.encode(new Date(), DateTools.ISO8601_DATE_FORMAT));
			dump.append('\n');
			dump.append("Full thread dump");

			RuntimeMXBean rmx = ManagementFactory.getRuntimeMXBean();
			String vmName = rmx.getVmName();
			if (vmName != null) {
				dump.append(' ');
				dump.append(vmName);
				String vmVersion = rmx.getVmVersion();
				if (vmVersion != null) {
					dump.append(" (");
					dump.append(vmVersion);
					dump.append(')');
				}
			}
			dump.append("\n\n");

			for (ThreadInfo threadInfo : threadInfos) {

				String threadName = threadInfo.getThreadName();
				Map.Entry<Thread, StackTraceElement[]> entry = threadMap.get(threadInfo.getThreadId());
				if (entry != null) {

					Thread thread = entry.getKey();

					dump.append(String.format("\"%s\" %sprio=%d tid=%d nid=1 %s\n   java.lang.Thread.State: %s", threadName,
							(thread.isDaemon() ? "daemon " : ""), thread.getPriority(), thread.getId(),
							Thread.State.WAITING.equals(thread.getState()) ? "in Object.wait()" : thread.getState().name().toLowerCase(),
							(thread.getState().equals(Thread.State.WAITING) ? "WAITING (on object monitor)" : thread.getState())));

					final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
					for (final StackTraceElement stackTraceElement : stackTraceElements) {
						dump.append("\n\tat ");
						dump.append(stackTraceElement);
					}

					dump.append("\n\n");

				}
			}

			logger.debug("Successfully created a thread dump within the JVM.");

			return dump.toString();
		} catch (Exception e) {
			logger.error("Could not create a thread dump.", e);
			return null;
		}
	}

	protected <T extends PlatformReflectionResponse> void multicastRequestAsyncWithConsumer(ServiceRequest request, ServiceRequestContext context,
			Consumer<T> onSuccess, final CountDownLatch countdown) {

		MulticastRequest mcR = MulticastRequest.T.create();
		mcR.setSessionId(context.getRequestorSessionId());
		mcR.setServiceRequest(request);
		mcR.setAddressee(this.localInstanceId);
		// mcR.setTimeout((long) Numbers.MILLISECONDS_PER_MINUTE);
		mcR.setTimeout((long) Numbers.MILLISECONDS_PER_SECOND * 20);
		EvalContext<? extends MulticastResponse> eval = mcR.eval(context);
		logger.trace(() -> "Sending " + request + " as an asynchronous multicast request.");

		eval.get(new AsyncCallback<MulticastResponse>() {

			@Override
			public void onSuccess(MulticastResponse multicastResponse) {
				boolean gotResult = false;
				try {
					Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
					if (responses.size() > 1) {
						logger.debug(() -> "Received " + responses.size() + " responses. Expected just one.");
					}
					for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {

						InstanceId instanceId = entry.getKey();

						logger.trace(() -> "Received a response from instance: " + instanceId);

						if (InstanceIdComparator.instance.compare(localInstanceId, instanceId) == 0) {

							logger.trace(() -> "Accepting answer from " + localInstanceId);

							gotResult = true;

							extractFromServiceResult(entry.getValue(), envelope -> {
								T result = (T) envelope.getResult();
								logger.trace(() -> "Trying to forward answer from " + instanceId);
								onSuccess.accept(result);
								logger.trace(() -> "Successfully forwarded answer from " + instanceId);
							}, failure -> {
								Throwable throwable = FailureCodec.INSTANCE.decode(failure);
								logger.debug(() -> "Failure received from " + instanceId, throwable);
							}, stillProcessing -> {
								logger.trace(() -> "Still processing received from " + localInstanceId);
							});

						}
					}
				} finally {
					if (gotResult) {
						countdown.countDown();
					}
				}
			}

			@Override
			public void onFailure(Throwable t) {
				try {
					logger.warn(() -> "Received a failure for request " + request, t);
				} finally {
					countdown.countDown();
				}

			}

		});
	}

	protected <T extends GenericEntity> void multicastRequestAsync(ServiceRequest request, ServiceRequestContext context,
			final AsyncCallback<T> callback) {

		MulticastRequest mcR = MulticastRequest.T.create();
		mcR.setSessionId(context.getRequestorSessionId());
		mcR.setServiceRequest(request);
		mcR.setAddressee(this.localInstanceId);
		// mcR.setTimeout((long) Numbers.MILLISECONDS_PER_MINUTE);
		mcR.setTimeout((long) Numbers.MILLISECONDS_PER_SECOND * 20);
		EvalContext<? extends MulticastResponse> eval = mcR.eval(context);
		logger.trace(() -> "Sending " + request + " as an asynchronous multicast request.");

		eval.get(new AsyncCallback<MulticastResponse>() {

			@Override
			public void onSuccess(MulticastResponse multicastResponse) {
				// Just expecting a single response as we are sending the request to just this instance
				Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
				if (responses.size() > 1) {
					logger.debug(() -> "Received " + responses.size() + " responses. Expected just one.");
				}
				boolean receivedResponse = false;
				for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {

					InstanceId instanceId = entry.getKey();

					logger.trace(() -> "Received a response from instance: " + instanceId);

					String nodeId = instanceId.getNodeId();
					String appId = instanceId.getApplicationId();

					if (nodeId != null && nodeId.equals(localInstanceId.getNodeId()) && appId != null
							&& appId.equals(localInstanceId.getApplicationId())) {

						logger.trace(() -> "Accepting answer from " + localInstanceId);
						receivedResponse = true;

						extractFromServiceResult(entry.getValue(), envelope -> {
							T result = (T) envelope.getResult();
							logger.trace(() -> "Trying to forward answer from " + localInstanceId);
							callback.onSuccess(result);
							logger.trace(() -> "Successfully forwarded answer from " + localInstanceId);
						}, failure -> {
							logger.trace(() -> "Failure received from " + localInstanceId);
							Throwable throwable = FailureCodec.INSTANCE.decode(failure);
							callback.onFailure(throwable);
						}, stillProcessing -> {
							logger.debug(() -> "Still processing received from " + localInstanceId);
						});

					}
				}

				if (!receivedResponse) {
					logger.debug(() -> "Did not receive an answer from " + localInstanceId);

					callback.onFailure(new Exception("No response received from the intended recipient."));
				}
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t);
			}

		});

	}

	protected <T extends GenericEntity> T unicastRequestSync(ServiceRequest request, ServiceRequestContext context) {

		MulticastRequest mcR = MulticastRequest.T.create();
		mcR.setSessionId(context.getRequestorSessionId());
		mcR.setServiceRequest(request);
		mcR.setAddressee(this.localInstanceId);
		// mcR.setTimeout((long) Numbers.MILLISECONDS_PER_MINUTE);
		mcR.setTimeout((long) Numbers.MILLISECONDS_PER_SECOND * 60);
		EvalContext<? extends MulticastResponse> eval = mcR.eval(context);
		logger.trace(() -> "Sending " + request + " as a synchronous multicast request.");

		MulticastResponse multicastResponse = eval.get();

		// Just expecting a single response as we are sending the request to just this instance
		Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
		if (responses.size() > 1) {
			logger.debug(() -> "Received " + responses.size() + " responses. Expected just one.");
		}
		for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {

			InstanceId instanceId = entry.getKey();

			logger.trace(() -> "Received a response from instance: " + instanceId);

			String nodeId = instanceId.getNodeId();
			String appId = instanceId.getApplicationId();

			if (nodeId != null && nodeId.equals(localInstanceId.getNodeId()) && appId != null && appId.equals(localInstanceId.getApplicationId())) {

				logger.trace(() -> "Accepting answer from " + localInstanceId);

				Object[] result = new Object[1];
				extractFromServiceResult(entry.getValue(), envelope -> {
					result[0] = envelope.getResult();
				});

				return (T) result[0];
			}
		}
		logger.debug(() -> "Did not receive an answer from " + localInstanceId);

		return null;
	}

	protected <T extends GenericEntity> Map<InstanceId, T> multicastRequestSync(ServiceRequest request, ServiceRequestContext context,
			Long waitTimeoutInMs) {

		MulticastRequest mcR = MulticastRequest.T.create();
		mcR.setSessionId(context.getRequestorSessionId());
		mcR.setServiceRequest(request);
		mcR.setTimeout(waitTimeoutInMs);
		EvalContext<? extends MulticastResponse> eval = mcR.eval(context);
		logger.trace(() -> "Sending " + request + " as a synchronous multicast request.");

		Map<InstanceId, T> responseMap = new ConcurrentHashMap<>();
		MulticastResponse multicastResponse = eval.get();

		Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();

		for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {

			InstanceId instanceId = entry.getKey();

			logger.trace(() -> "Received a response from instance: " + instanceId);

			ServiceResult result = entry.getValue();
			if (result instanceof Failure) {
				Throwable throwable = FailureCodec.INSTANCE.decode(result.asFailure());
				logger.error("Received failure from " + instanceId, throwable);
			} else if (result instanceof ResponseEnvelope) {

				ResponseEnvelope envelope = (ResponseEnvelope) result;
				T resultPayload = (T) envelope.getResult();
				responseMap.put(instanceId, resultPayload);

			} else {
				logger.error("Unsupported response type: " + result);
			}

		}
		return responseMap;

	}

	/** Get Setup Descriptor request implementation */
	private SetupDescriptor getSetupDescriptor(ServiceRequestContext requestContext, GetSetupDescriptor request) {
		return readYamlFromSetupInfoFolder(FILENAME_SETUP_INFO_YAML, SetupDescriptor.T);
	}

	/** Get RepositoryViewResolution request implementation */
	private RepositoryViewResolution getRepositoryViewResolution(ServiceRequestContext requestContext, GetRepositoryViewResolution request) {
		return readYamlFromSetupInfoFolder(FILENAME_REPOSITORY_VIEW_RESOLUTION_YAML, RepositoryViewResolution.T);
	}

	private <T extends GenericEntity> T readYamlFromSetupInfoFolder(String fileName, EntityType<T> entityType) {
		File yaml = new File(setupInfoPath, fileName);

		if (!yaml.exists())
			return null;

		try (InputStream inputStream = FileTools.newInputStream(yaml)) {
			return (T) yamlMarshaller.unmarshall(inputStream, //
					GmDeserializationOptions.defaultOptions.derive().setInferredRootType(entityType).build());

		} catch (IOException e) {
			throw new UncheckedIOException("Error while reading " + fileName, e);
		}
	}

	public SystemInformationProvider getSystemInformationProvider() {
		if (systemInformationProvider == null) {
			systemInformationProvider = new StandardSystemInformationProvider();
		}
		return systemInformationProvider;
	}
	@Configurable
	public void setSystemInformationProvider(SystemInformationProvider systemInformationProvider) {
		this.systemInformationProvider = systemInformationProvider;
	}

	public HostInformationProvider getHostInformationProvider() {
		if (hostInformationProvider == null) {

			String hostIdentification = hostDetector != null ? hostDetector.hostIdentification() : null;
			logger.debug(() -> "Identified host: " + hostIdentification);
			if (hostIdentification != null) {
				for (String key : this.hostInformationProviderMap.keySet()) {
					try {
						if (key.equalsIgnoreCase(hostIdentification) || hostIdentification.matches(key)) {
							hostInformationProvider = this.hostInformationProviderMap.get(key);
							logger.debug(() -> "Selected host information provider: " + hostInformationProvider);
							break;
						}
					} catch (PatternSyntaxException pse) {
						logger.trace(() -> "Could not use key " + key + " as regex.", pse);
					}
				}
			}

			if (hostInformationProvider == null) {
				logger.info(() -> "Could not detect the current host. Using the default host information provider.");
				hostInformationProvider = new TomcatHostInformationProvider();
			}
		}
		return hostInformationProvider;
	}
	@Configurable
	public void setHostInformationProviderMap(Map<String, HostInformationProvider> hostInformationProviderMap) {
		this.hostInformationProviderMap = hostInformationProviderMap;
	}
	@Configurable
	public void setHostInformationProvider(HostInformationProvider hostInformationProvider) {
		this.hostInformationProvider = hostInformationProvider;
	}

	public TribefireInformationProvider getTribefireInformationProvider() {
		return tribefireInformationProvider;
	}
	@Configurable
	@Required
	public void setTribefireInformationProvider(TribefireInformationProvider tribefireInformationProvider) {
		this.tribefireInformationProvider = tribefireInformationProvider;
	}

	@Configurable
	public void setHostDetector(HostDetector hostDetector) {
		this.hostDetector = hostDetector;
	}

	@Configurable
	@Required
	public void setCommandExecution(CommandExecution commandExecution) {
		this.commandExecution = commandExecution;
	}
	@Configurable
	@Required
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}
	@Configurable
	@Required
	public void setPackagingProvider(Supplier<Packaging> packagingProvider) {
		this.packagingProvider = packagingProvider;
	}
	@Configurable
	@Required
	public void setLocalInstanceId(InstanceId localInstanceId) {
		this.localInstanceId = localInstanceId;
	}
	@Configurable
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}
	@Configurable
	@Required
	public void setJsonMarshaller(Marshaller jsonMarshaller) {
		this.jsonMarshaller = jsonMarshaller;
	}
	@Required
	@Configurable
	public void setPlatformSetupSupplier(Supplier<PlatformSetup> platformSetupSupplier) {
		this.platformSetupSupplier = platformSetupSupplier;
	}
	@Required
	public void setSharedStorageSupplier(Supplier<DcsaSharedStorage> sharedStorageSupplier) {
		this.sharedStorageSupplier = sharedStorageSupplier;
	}
	@Configurable
	public void setZipPassword(String zipPassword) {
		if (!StringTools.isBlank(zipPassword)) {
			this.zipPassword = zipPassword;
		}
	}
	@Configurable
	public void setConfFolder(File confFolder) {
		this.confFolder = confFolder;
	}
	@Configurable
	public void setModulesFolder(File modulesFolder) {
		this.modulesFolder = modulesFolder;
	}
	@Configurable
	public void setDatabaseFolder(File databaseFolder) {
		this.databaseFolder = databaseFolder;
	}
	@Required
	@Configurable
	public void setSetupAccessSessionProvider(Supplier<PersistenceGmSession> setupAccessSessionProvider) {
		this.setupAccessSessionProvider = setupAccessSessionProvider;
	}
	@Required
	@Configurable
	public void setSetupInfoPath(File setupInfoPath) {
		this.setupInfoPath = setupInfoPath;
	}
	@Configurable
	@Required
	public void setYamlMarshaller(Marshaller yamlMarshaller) {
		this.yamlMarshaller = yamlMarshaller;
	}
	@Configurable
	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}
	@Configurable
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
