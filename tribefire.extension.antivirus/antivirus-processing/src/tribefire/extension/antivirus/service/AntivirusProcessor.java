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
package tribefire.extension.antivirus.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.ExtendedThreadPoolExecutor;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.antivirus.connector.api.AbstractAntivirusConnector;
import tribefire.extension.antivirus.connector.api.AntivirusConnector;
import tribefire.extension.antivirus.model.deployment.repository.configuration.ClamAVSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.CloudmersiveSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.ProviderSpecification;
import tribefire.extension.antivirus.model.deployment.repository.configuration.VirusTotalSpecification;
import tribefire.extension.antivirus.model.service.request.AntivirusRequest;
import tribefire.extension.antivirus.model.service.request.ScanForVirus;
import tribefire.extension.antivirus.model.service.result.AbstractAntivirusResult;
import tribefire.extension.antivirus.model.service.result.AntivirusResult;
import tribefire.extension.antivirus.model.service.result.VirusInformation;
import tribefire.extension.antivirus.service.base.ResponseBuilder;
import tribefire.extension.antivirus.service.connector.clamav.ClamAVExpert;
import tribefire.extension.antivirus.service.connector.cloudmersive.CloudmersiveExpert;
import tribefire.extension.antivirus.service.connector.virustotal.VirusTotalExpert;

public class AntivirusProcessor implements AccessRequestProcessor<AntivirusRequest, AntivirusResult> {

	private static final Logger logger = Logger.getLogger(AntivirusProcessor.class);

	private List<ProviderSpecification> providerSpecification;

	private ExtendedThreadPoolExecutor threadPool = null;

	private ReentrantLock threadPoolLock = new ReentrantLock();

	// -----------------------------------------------------------------------
	// DISPATCHING
	// -----------------------------------------------------------------------

	private AccessRequestProcessor<AntivirusRequest, AntivirusResult> delegate = AccessRequestProcessors.dispatcher(dispatching -> {
		dispatching.register(ScanForVirus.T, this::scan);
	});

	@Override
	public AntivirusResult process(AccessRequestContext<AntivirusRequest> context) {
		return delegate.process(context);
	}

	private VirusInformation scan(AccessRequestContext<ScanForVirus> context) {

		long start = System.currentTimeMillis();

		ScanForVirus request = context.getRequest();

		List<Resource> resources = request.getResources();
		List<ProviderSpecification> requestProviderSpecifications = request.getProviderSpecifications();

		if (CommonTools.isEmpty(resources)) {
			throw new IllegalArgumentException("At least one resource needs to be specified");
		}

		List<ProviderSpecification> actualProviderSpecifications = new ArrayList<>();
		if (CommonTools.isEmpty(requestProviderSpecifications)) {
			// use the provider specifications from the module
			actualProviderSpecifications.addAll(this.providerSpecification);
		} else {
			// use provider specifications from the request
			actualProviderSpecifications.addAll(requestProviderSpecifications);
		}

		logger.debug(() -> {
			List<String> inputResources = resources.stream().map(r -> AbstractAntivirusConnector.resourceInformation(r)).collect(Collectors.toList());
			List<String> providerSpecifications = actualProviderSpecifications.stream().map(p -> p.entityType().getShortName())
					.collect(Collectors.toList());
			return "Scanning: '" + inputResources + "' specifications: '" + providerSpecifications + "'";
		});

		if (actualProviderSpecifications.isEmpty()) {
			throw new IllegalArgumentException("At least one provider specification needs to be set. Either per request or on the processor");
		}

		List<AbstractAntivirusResult> results = new ArrayList<>();

		int parallelExecution = request.getParallelExecution();

		if (parallelExecution > 0) {
			
			List<Future<Void>> futures = new ArrayList<>();
			ExecutorService service = getThreadPool(parallelExecution);

			resources.forEach(r -> {
				futures.add(service.submit(() -> {
					for (ProviderSpecification providerSpecification : actualProviderSpecifications) {
						AntivirusConnector<? extends AbstractAntivirusResult> expert;
						if (providerSpecification instanceof ClamAVSpecification) {
							expert = ClamAVExpert.forScanForVirus((ClamAVSpecification) providerSpecification, r);
						} else if (providerSpecification instanceof CloudmersiveSpecification) {
							expert = CloudmersiveExpert.forScanForVirus((CloudmersiveSpecification) providerSpecification, r);
						} else if (providerSpecification instanceof VirusTotalSpecification) {
							expert = VirusTotalExpert.forScanForVirusAPI((VirusTotalSpecification) providerSpecification, r);
						} else {
							throw new IllegalStateException("ProviderSpecification: '" + providerSpecification + "' not supported");
						}
						AbstractAntivirusResult result = expert.scan();
						results.add(result);
					}

					return null;
				}));
			});

			RuntimeException exception = null;
			for (Future<Void> f : futures) {
				try {
					f.get();
				} catch (ExecutionException ee) {
					if (exception == null) {
						exception = Exceptions.unchecked(ee, "Error while scanning resources");
					} else {
						exception.addSuppressed(ee);
					}
				} catch (InterruptedException ie) {
					logger.debug(() -> "Got interrupted while scanning resources");
					throw Exceptions.unchecked(ie);
				}
			}

			if (exception != null) {
				throw exception;
			}
		} else {
			resources.forEach(r -> {
				for (ProviderSpecification providerSpecification : actualProviderSpecifications) {
					AntivirusConnector<? extends AbstractAntivirusResult> expert;
					if (providerSpecification instanceof ClamAVSpecification) {
						expert = ClamAVExpert.forScanForVirus((ClamAVSpecification) providerSpecification, r);
					} else if (providerSpecification instanceof CloudmersiveSpecification) {
						expert = CloudmersiveExpert.forScanForVirus((CloudmersiveSpecification) providerSpecification, r);
					} else if (providerSpecification instanceof VirusTotalSpecification) {
						expert = VirusTotalExpert.forScanForVirusAPI((VirusTotalSpecification) providerSpecification, r);
					} else {
						throw new IllegalStateException("ProviderSpecification: '" + providerSpecification + "' not supported");
					}
					AbstractAntivirusResult result = expert.scan();
					results.add(result);
				}
			});
		}

		long end = System.currentTimeMillis();

		//@formatter:off
		long numberInfectedFiles = results.stream()
				.map(res -> res.getInfected())
				.filter(i -> i == true)
			.count();
		//@formatter:on

		//@formatter:off
		return responseBuilder(VirusInformation.T, request)
				.responseEnricher((r) -> {
					r.setDetails(results);
					r.setNumberInfectedResources(numberInfectedFiles);
					r.setDurationInMs(end - start);
				})
				.build();
		//@formatter:on
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private ExtendedThreadPoolExecutor getThreadPool(int poolSize) {
		if (threadPool != null) {
			return threadPool;
		}
		threadPoolLock.lock();
		try {
			if (threadPool == null) {
				threadPool = new ExtendedThreadPoolExecutor(poolSize, poolSize, 10l, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
				threadPool.allowCoreThreadTimeOut(true);
				threadPool.setThreadNamePrefix("content-migration-");
			}
		} finally {
			threadPoolLock.unlock();
		}
		return threadPool;
	}

	// -----------------------------------------------------------------------
	// FOR NOTIFICATIONS
	// -----------------------------------------------------------------------

	protected <T extends HasNotifications> ResponseBuilder<T> responseBuilder(EntityType<T> responseType, AntivirusRequest request) {

		return new ResponseBuilder<T>() {
			private List<Notification> localNotifications = new ArrayList<>();
			private boolean ignoreCollectedNotifications = false;
			private Consumer<T> enricher;
			private NotificationsBuilder notificationsBuilder = null;
			private List<Notification> notifications = new ArrayList<>();

			@Override
			public ResponseBuilder<T> notifications(Supplier<List<Notification>> notificationsSupplier) {
				notifications = notificationsSupplier.get();
				return this;
			}
			@Override
			public ResponseBuilder<T> notifications(Consumer<NotificationsBuilder> consumer) {
				this.notificationsBuilder = Notifications.build();
				consumer.accept(notificationsBuilder);
				return this;
			}

			@Override
			public ResponseBuilder<T> ignoreCollectedNotifications() {
				this.ignoreCollectedNotifications = true;
				return this;
			}

			@Override
			public ResponseBuilder<T> responseEnricher(Consumer<T> enricher) {
				this.enricher = enricher;
				return this;
			}

			@Override
			public T build() {

				T response = responseType.create();
				if (enricher != null) {
					this.enricher.accept(response);
				}
				if (request.getSendNotifications()) {
					response.setNotifications(localNotifications);
					if (!ignoreCollectedNotifications) {

						if (notificationsBuilder != null) {
							notifications.addAll(notificationsBuilder.list());
						}

						Collections.reverse(notifications);
						response.getNotifications().addAll(notifications);
					}
				}
				return response;
			}
		};

	}

	protected <T extends HasNotifications> T prepareSimpleNotification(EntityType<T> responseEntityType, AntivirusRequest request, Level level,
			String msg) {

		//@formatter:off
		T result = responseBuilder(responseEntityType, request)
				.notifications(builder -> 
					builder	
					.add()
						.message()
							.level(level)
							.message(msg)
						.close()
					.close()
				).build();
		//@formatter:on
		return result;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setProviderSpecifications(List<ProviderSpecification> providerSpecification) {
		this.providerSpecification = providerSpecification;
	}

	public List<ProviderSpecification> getProviderSpecification() {
		return providerSpecification;
	}

}
