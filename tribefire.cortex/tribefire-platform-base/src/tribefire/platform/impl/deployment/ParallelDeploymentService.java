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
package tribefire.platform.impl.deployment;

import static com.braintribe.model.generic.typecondition.TypeConditions.and;
import static com.braintribe.model.generic.typecondition.TypeConditions.hasCollectionElement;
import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.collectionType;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.entityType;
import static java.util.Objects.requireNonNull;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeployableComponent;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.DenotationTypeBindings;
import com.braintribe.model.processing.deployment.api.DeployContext;
import com.braintribe.model.processing.deployment.api.DeployedComponent;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.deployment.api.DeploymentContext;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DeploymentScoping;
import com.braintribe.model.processing.deployment.api.DeploymentService;
import com.braintribe.model.processing.deployment.api.MutableDeployRegistry;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.deployment.api.UndeployContext;
import com.braintribe.model.processing.deployment.api.UndeploymentContext;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.extended.EntityMdDescriptor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.thread.api.ThreadContextScoping;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.StopWatch;

import tribefire.platform.impl.deployment.ParallelDeploymentStatistics.PromiseStatistics;

/**
 * A {@link DeploymentService} implementation.
 * 
 * @author dirk.scheffler
 */
public class ParallelDeploymentService implements DeploymentService {

	// constants
	private static final Logger log = Logger.getLogger(ParallelDeploymentService.class);

	// configurable
	private DeploymentScoping deploymentScoping;
	private MutableDeployRegistry deployRegistry;
	private DenotationTypeBindings denotationTypeBindings;
	private DeployedComponentResolver deployedComponentResolver;

	// post initialized
	private final Map<String, DeploymentPromise> promises = new ConcurrentHashMap<>();
	private int standardParallelDeployments = 5;
	private final Object deploymentMonitor = new Object();

	private ThreadContextScoping threadContextScoping;

	@Required
	public void setDeploymentScoping(DeploymentScoping deploymentScoping) {
		this.deploymentScoping = deploymentScoping;
	}

	@Required
	public void setDeployRegistry(MutableDeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

	/* I had to change this to a Supplier, rather than the actual value - DeploymentSpace.parallelDeploymentService() */
	@Required
	public void setDenotationTypeBindings(DenotationTypeBindings denotationTypeBindings) {
		this.denotationTypeBindings = denotationTypeBindings;
	}

	@Required
	public void setDeployedComponentResolver(DeployedComponentResolver deployedComponentResolver) {
		this.deployedComponentResolver = deployedComponentResolver;
	}

	@Configurable
	public void setStandardParallelDeployments(int threadCount) {
		if (threadCount <= 0) {
			log.warn(() -> "Not accepting standard deployment thread count " + threadCount + ". Keeping default: " + standardParallelDeployments);
		} else {
			this.standardParallelDeployments = threadCount;
		}
	}

	@Required
	public void setThreadContextScoping(ThreadContextScoping threadContextScoping) {
		this.threadContextScoping = threadContextScoping;
	}

	@Override
	public void deploy(DeployContext deployContext) throws DeploymentException {
		ParallelDeploymentStatistics stats = new ParallelDeploymentStatistics();
		synchronized (deploymentMonitor) {
			stats.stopWatch.intermediate("Monitor");
			s_deploy(deployContext, stats);
			stats.stopWatch.intermediate("Deployment");
		}
		if (log.isDebugEnabled()) {
			stats.createStatistics();
			log.debug("Deployment of deploy context:\n" + stats);
		}
	}

	class ParallelDeploymentController {

		// TODO this brings dependency to com.braintribe.execution:execution.
		// Thread pool should injected (or rather ThreadPoolFactory that takes params like thread name should be injected)
		private final ExecutorService standardDeploymentThreadPool;
		private ExecutorService eagerDeploymentThreadPool;
		private final DeployContext context;
		private final ParallelDeploymentStatistics stats;

		public ParallelDeploymentController(int deployableAmount, DeployContext context, ParallelDeploymentStatistics stats) {
			this.context = context;
			this.stats = stats;

			int threadCount = Math.min(deployableAmount, standardParallelDeployments);
			standardDeploymentThreadPool = VirtualThreadExecutorBuilder.newPool().concurrency(threadCount).threadNamePrefix("Deployer")
					.description("Deployer").build();
			stats.standardThreadCount = threadCount;

			if (deployableAmount > standardParallelDeployments) {

				eagerDeploymentThreadPool = VirtualThreadExecutorBuilder.newPool().concurrency(Integer.MAX_VALUE).threadNamePrefix("EagerDeployer")
						.description("Eager Deployer").build();
			}
		}

		private class ErrorCatcher implements Runnable {
			private final Runnable delegate;
			private final DeploymentPromise promise;

			public ErrorCatcher(Runnable delegate, DeploymentPromise promise) {
				super();
				this.delegate = delegate;
				this.promise = promise;
			}

			@Override
			public void run() {
				PromiseStatistics promiseStats = promise.getPromiseStatistics();
				promiseStats.executionStarts();
				try {
					delegate.run();
				} catch (Throwable t) {
					promise.onFailure(t);
				} finally {
					promiseStats.executionEnded();
				}
			}
		}

		void enqueue(DeploymentPromise promise) {
			SingleDeployer deployer = new SingleDeployer(context, promise.getDeployable(), promise, QueueStatus.pending);
			ContextClassLoaderTransfer classLoaderTransfer = new ContextClassLoaderTransfer(deployer);
			promise.getPromiseStatistics().enqueuedStandard();
			standardDeploymentThreadPool.execute(new ErrorCatcher(threadContextScoping.bindContext(classLoaderTransfer), promise));
		}

		void ensureEagerDeployment(DeploymentPromise promise) {
			// standard pool is sufficient to handle all deployments without eager deployment deadlocks
			if (eagerDeploymentThreadPool == null)
				return;

			PromiseStatistics promiseStats = promise.getPromiseStatistics();

			promiseStats.eagerMonitorAcquisition();
			// moving deployment to queue that is able to grow indefinitely and therefore resolves eager deployment
			// deadlocks
			Object statusMonitor = promise.getStatusMonitor();
			synchronized (statusMonitor) {
				promiseStats.eagerMonitorAcquired();

				if (promise.getQueueStatus() == QueueStatus.pending) {
					promise.setQueueStatus(QueueStatus.movedToExtraQueue);

					log.debug(() -> "Moving deployable " + promise.getDeployable().getExternalId() + " to extra deployment queue.");

					SingleDeployer deployer = new SingleDeployer(context, promise.getDeployable(), promise, QueueStatus.movedToExtraQueue);
					ContextClassLoaderTransfer classLoaderTransfer = new ContextClassLoaderTransfer(deployer);

					long bindStart = System.nanoTime();
					Runnable bindContext = threadContextScoping.bindContext(classLoaderTransfer);
					long bindStop = System.nanoTime();
					long bindDuration = bindStop - bindStart;
					if (bindDuration > Numbers.NANOSECONDS_PER_SECOND) {
						log.debug(() -> "Scoping the thread context took "
								+ StringTools.prettyPrintDuration(bindDuration / Numbers.NANOSECONDS_PER_SECOND, true, ChronoUnit.MILLIS));
					}

					promiseStats.enqueuedEager();
					eagerDeploymentThreadPool.execute(new ErrorCatcher(bindContext, promise));
				}
			}
		}

		void waitForFinishedDeployment() {

			stats.waitForFinishedDeploymentStart();

			for (DeploymentPromise p : promises.values()) {
				// Forwards the internal asynchronous deployment result to the context notification methods
				try {
					p.get();
					context.succeeded(p.getOriginalDeployable());
				} catch (Throwable t) {
					context.failed(p.getOriginalDeployable(), t);
				}
			}

			stats.endOfWaitForFinishedDeploymentStart();

			promises.clear();
		}
	}

	/**
	 * This class is required as it seems that the {@link ThreadContextScoping} is not able to transfer the current class
	 * loader context.
	 */
	class ContextClassLoaderTransfer implements Runnable {

		private final Runnable delegate;
		private final ClassLoader classLoader;

		public ContextClassLoaderTransfer(Runnable delegate) {
			this.delegate = delegate;
			this.classLoader = Thread.currentThread().getContextClassLoader();
		}

		@Override
		public void run() {
			ClassLoader classLoaderBackup = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(classLoader);

			try {
				delegate.run();

			} finally {
				Thread.currentThread().setContextClassLoader(classLoaderBackup);
			}
		}

	}

	// TODO make this a future to allow canceling
	class SingleDeployer implements Runnable {

		private final DeployContext deployContext;
		private final Deployable deployable;
		private final DeploymentPromise deploymentPromise;
		private final QueueStatus preconditionalState;

		public SingleDeployer(DeployContext deployContext, Deployable deployable, DeploymentPromise deploymentPromise,
				QueueStatus preconditionalState) {
			this.deployContext = deployContext;
			this.deployable = deployable;
			this.deploymentPromise = deploymentPromise;
			this.preconditionalState = preconditionalState;
		}

		@Override
		public void run() {
			Object statusMonitor = deploymentPromise.getStatusMonitor();
			synchronized (statusMonitor) {
				if (deploymentPromise.getQueueStatus() != preconditionalState)
					return;

				deploymentPromise.setQueueStatus(QueueStatus.processing);
			}

			BasicDeploymentContext<?, ?> context = new BasicDeploymentContext<>( //
					deployedComponentResolver, resolveDeploymentSession(), deployable, deployContext.areDeployablesFullyFetched());

			try {
				deploy(context);
				deploymentPromise.onSuccess(null);

			} catch (Exception e) {
				deploymentPromise.onFailure(e);

			} finally {
				context.onAfterDeployment();
			}

			synchronized (statusMonitor) {
				deploymentPromise.setQueueStatus(QueueStatus.done);
			}
		}

		private PersistenceGmSession resolveDeploymentSession() {
			GmSession deployableSession = deployable.session();
			if (deployableSession instanceof PersistenceGmSession)
				return (PersistenceGmSession) deployableSession;
			else
				return deployContext.session().newEquivalentSession();
		}
	}

	private void s_deploy(DeployContext deployContext, ParallelDeploymentStatistics stats) throws DeploymentException {

		final int size = deployContext.deployables().size();
		stats.deployablesCount = size;

		ParallelDeploymentController controller = new ParallelDeploymentController(size, deployContext, stats);
		stats.stopWatch.intermediate("Controller Init");

		for (Deployable deployable : deployContext.deployables()) {
			DeploymentPromise deploymentPromise = new DeploymentPromise(deployable, deployable, controller::ensureEagerDeployment, stats);

			String externalId = deployable.getExternalId();
			if (externalId == null) {
				log.error("No externalId set on deployable of type '" + deployable.entityType().getTypeSignature() + "'. Instance: " + deployable);
				deploymentPromise.onFailure(new NullPointerException("The deployable " + deployable + " does not have an external Id"));
			} else {
				promises.put(externalId, deploymentPromise);
			}
		}
		stats.stopWatch.intermediate("Promises Created");

		// notify deployment started
		stats.deploymentStarts();
		deployContext.deploymentStarted();

		promises.values().stream() //
				.peek(controller::enqueue) //
				.forEach(dp -> deployContext.started(dp.getDeployable()));
		stats.stopWatch.intermediate("Promises Enqueued");

		controller.waitForFinishedDeployment();
		stats.stopWatch.intermediate("Deployment Finish Waiting");
		stats.deploymentFinished();

	}

	@Override
	public void undeploy(UndeployContext undeployContext) throws DeploymentException {
		synchronized (deploymentMonitor) {
			s_undeploy(undeployContext);
		}
	}

	private void s_undeploy(UndeployContext undeployContext) {
		for (Deployable deployable : undeployContext.deployables()) {
			BasicDeploymentContext<?, ?> context = new BasicDeploymentContext<>(deployable);
			try {
				undeploy(context);
				undeployContext.succeeded(deployable);
			} catch (Exception e) {
				undeployContext.failed(deployable, e);
			}
		}
	}

	private void deploy(MutableDeploymentContext<?, ?> context) throws DeploymentException {
		validateContext(context);

		String externalId = context.getDeployable().getExternalId();
		if (!deployRegistry.isDeployed(externalId)) {
			startScope(context);
			try {
				scopedDeploy(context);
				popScope(context);
			} catch (RuntimeException | Error e) {
				endScope(context);
				throw e;
			}
		}
	}

	private void undeploy(MutableDeploymentContext<?, ?> context) throws DeploymentException {
		validateContext(context);

		startScope(context);
		try {
			if (deployRegistry.isDeployed(context.getDeployable().getExternalId())) {
				scopedUndeploy(context);
			}
		} finally {
			endScope(context);
		}
	}

	private void scopedDeploy(MutableDeploymentContext<?, ?> context) throws DeploymentException {
		Deployable deployable = requireNonNull(context.getDeployable(), "deployable");
		String shortDeployableString = shortDeployableString(deployable);

		log.info(() -> "Deploying " + shortDeployableString);
		if (log.isDebugEnabled())
			logDetails(context, deployable, shortDeployableString);

		if (deployable instanceof HardwiredDeployable) {
			log.info(() -> "Ignored local deployment of hardwired deployable: " + shortDeployableString);
			return;
		}

		DeployedUnit deployedUnit = buildLocalUnit(context, shortDeployableString);

		Deployable shallowCopy = makeShallowCopyOf(deployable);
		deployRegistry.register(shallowCopy, deployedUnit);
		log.info(() -> "DEPLOYED  " + shortDeployableString);
	}

	private void logDetails(MutableDeploymentContext<?, ?> context, Deployable deployable, String shortDeployableString) {
		// Profiler shows the cloning takes quite some time; only using it when tracing is active
		if (log.isTraceEnabled()) {
			Deployable printableDeployable = clonePrintable(deployable, context.getSession().getModelAccessory().getMetaData());
			log.debug("Deployable details: " + ((printableDeployable == null) ? shortDeployableString : printableDeployable));
		} else {
			log.debug("Deployable details: " + shortDeployableString);
		}
	}

	// @formatter:off
	private static TraversingCriterion deployableShallowifyingTc = TC.create()
			.conjunction()
				.property()
				.typeCondition(or( //
						and(
								isKind(collectionType),
								hasCollectionElement(isKind(entityType))
						),
						isKind(entityType)
					))
				.negation()
					.pattern()
						.typeCondition(isAssignableTo(Deployable.T))
						.property(Deployable.module)
					.close() // pattern
			.close() // conjunction
		.done();
	// @formatter:on

	private Deployable makeShallowCopyOf(Deployable deployable) {
		CloningContext cc = ConfigurableCloningContext.build().withMatcher(StandardMatcher.create(deployableShallowifyingTc)).done();

		return deployable.clone(cc);
	}

	private void scopedUndeploy(DeploymentContext<?, ?> context) throws DeploymentException {
		Deployable deployable = context.getDeployable();

		DeployedUnit unit = deployRegistry.unregister(deployable);

		unbind(deployable, unit);

		String desc = shortDeployableString(deployable);

		if (unit != null)
			log.info(() -> "Undeployed: " + desc);
		else
			log.debug(() -> "No unit to be undeployed for: " + desc);
	}

	private void unbind(Deployable deployable, DeployedUnit unit) {
		for (DeployedComponent dc : unit.getComponents().values()) {
			ComponentBinder<Deployable, Object> binder = (ComponentBinder<Deployable, Object>) dc.binder();
			if (binder != null)
				binder.unbind(new UndeployContextImpl(deployable, dc));
		}
	}

	static class UndeployContextImpl implements UndeploymentContext<Deployable, Object> {

		private final Deployable deployable;
		private final DeployedComponent deployedComponent;

		public UndeployContextImpl(Deployable deployable, DeployedComponent deployedComponent) {
			this.deployable = deployable;
			this.deployedComponent = deployedComponent;
		}

		@Override
		public Deployable getDeployable() {
			return deployable;
		}

		@Override
		public Object getBoundInstance() {
			return deployedComponent.exposedImplementation();
		}

		@Override
		public Object getSuppliedInstance() {
			return deployedComponent.suppliedImplementation();
		}

	}

	private DeployedUnit buildLocalUnit(MutableDeploymentContext<?, ?> context, String shortDeployableString) throws DeploymentException {
		StopWatch stopWatch = new StopWatch();

		Set<EntityType<? extends Deployable>> componentTypes = resolveComponentTypes(context);
		stopWatch.intermediate("Resolve Component Types");

		Function<MutableDeploymentContext<?, ?>, DeployedUnit> deployedUnitSupplier = denotationTypeBindings
				.resolveDeployedUnitSupplier(context.getDeployable());
		stopWatch.intermediate("Resolve DeployedUnit Supplier");

		DeployedUnit deployedUnit = deployedUnitSupplier.apply(context);

		stopWatch.intermediate("DeployedUnit Creation");

		validateLocalUnitComponents(context.getDeployable(), deployedUnit, componentTypes);

		stopWatch.intermediate("Validation");

		log.debug(() -> "Building local unit " + shortDeployableString + " took: " + stopWatch);
		return deployedUnit;
	}

	private Set<EntityType<? extends Deployable>> resolveComponentTypes(DeploymentContext<?, ?> context) {
		List<EntityMdDescriptor> metadataList = context //
				.getSession() //
				.getModelAccessory() //
				.getMetaData() //
				.entity(context.getDeployable()) //
				.meta(DeployableComponent.T) //
				.listExtended();

		if (metadataList.isEmpty() && log.isWarnEnabled())
			log.warn("No " + DeployableComponent.class.getSimpleName() + " metadata was resolved for "
					+ shortDeployableString(context.getDeployable()));

		return (Set<EntityType<? extends Deployable>>) (Set<?>) metadataList.stream() //
				.map(this::getOwnerTypeSignature) //
				.map(GMF.getTypeReflection()::findEntityType) //
				.filter(t -> t != null && Deployable.T.isAssignableFrom(t)) //
				.collect(Collectors.toSet());
	}

	private String getOwnerTypeSignature(EntityMdDescriptor e) {
		return e.getOwnerTypeInfo().addressedType().getTypeSignature();
	}

	private void startScope(DeploymentContext<?, ?> context) {
		deploymentScoping.push(context);
	}

	private void popScope(DeploymentContext<?, ?> context) {
		try {
			deploymentScoping.pop(context);
		} catch (Exception e) {
			log.error("Failed to pop scope", e);
		}
	}

	private void endScope(DeploymentContext<?, ?> context) {
		popScope(context);

		try {
			deploymentScoping.end(context);
		} catch (Exception e) {
			log.error("Failed to end scope", e);
		}
	}

	private void validateContext(DeploymentContext<?, ?> context) {
		if (context == null)
			throw new IllegalArgumentException(DeploymentContext.class.getName() + " cannot be null");

		if (context.getDeployable() == null)
			throw new IllegalArgumentException(
					"Given " + DeploymentContext.class.getName() + " has no " + Deployable.T.getTypeSignature() + " set: " + context);
	}

	private void validateLocalUnitComponents(Deployable deployable, DeployedUnit deployedUnit,
			Set<EntityType<? extends Deployable>> resolvedComponentTypes) {

		Set<EntityType<? extends Deployable>> currentComponentTypes = deployedUnit.getComponents().keySet();

		Set<String> missingTypes = null;

		if (resolvedComponentTypes.size() == currentComponentTypes.size()) {
			resolvedComponentTypes.removeAll(currentComponentTypes);

			// Current is exactly as resolved. Nothing more to be checked after this.
			if (resolvedComponentTypes.isEmpty())
				return;

			missingTypes = resolvedComponentTypes.stream() //
					.map(GenericModelType::getTypeSignature) //
					.collect(Collectors.toSet());

		} else {
			missingTypes = resolvedComponentTypes.stream() //
					.filter((e) -> !currentComponentTypes.contains(e)) //
					.map(e -> e.getTypeSignature()) //
					.collect(Collectors.toSet());
		}

		if (!missingTypes.isEmpty())
			throw new DeploymentException("Missing binding for type(s): " + String.join(", ", missingTypes) + " while deploying " + desc(deployable));

		Set<String> extraTypes = currentComponentTypes.stream() //
				.filter((e) -> !resolvedComponentTypes.contains(e)) //
				.map(GenericModelType::getTypeSignature) //
				.collect(Collectors.toSet());

		if (!extraTypes.isEmpty())
			log.warn("Deployed unit created for " + desc(deployable) + " contains component types which were not resolved for its deployable type: "
					+ String.join(", ", extraTypes));
	}

	private String desc(Deployable d) {
		return d.shortDescription();
	}

	public void waitForDeployableIfInDeployment(String externalId) {
		DeploymentPromise deploymentPromise = promises.get(externalId);

		if (deploymentPromise == null)
			return;

		deploymentPromise.notifyEagerAccess();
		deploymentPromise.waitFor();
	}

	private String shortDeployableString(Deployable deployable) {
		return deployable.entityType().getShortName() + "[" + deployable.getExternalId() + "]";
	}

	private Deployable clonePrintable(Deployable deployable, ModelMdResolver mdResolver) {
		try {
			return BaseType.INSTANCE.clone(new PasswordFilteringCloningContext(mdResolver), deployable, StrategyOnCriterionMatch.skip);

		} catch (Exception e) {
			log.warn("Unable to clone printable version of deployable: " + shortDeployableString(deployable));
			return null;
		}
	}

	private static class PasswordFilteringCloningContext extends StandardCloningContext {

		private final ModelMdResolver mdResolver;

		public PasswordFilteringCloningContext(ModelMdResolver mdResolver) {
			this.mdResolver = mdResolver;
		}

		@Override
		public Object postProcessCloneValue(GenericModelType propertyType, Object clonedValue) {
			if (!(propertyType instanceof SimpleType))
				return super.postProcessCloneValue(propertyType, clonedValue);

			Stack<BasicCriterion> ts = getTraversingStack();

			BasicCriterion peekCriterion = ts.peek();
			if (peekCriterion.criterionType() != CriterionType.PROPERTY)
				return super.postProcessCloneValue(propertyType, clonedValue);

			Stack<Object> os = getObjectStack();
			GenericEntity entity = (GenericEntity) os.get(os.size() - 2);
			if (entity.getId() == null)
				return super.postProcessCloneValue(propertyType, clonedValue);

			PropertyCriterion pc = (PropertyCriterion) peekCriterion;

			boolean confidential = mdResolver.entity(entity).property(pc.getPropertyName()).is(Confidential.T);

			if (confidential)
				return getValueToReplacePassword(propertyType);

			return super.postProcessCloneValue(propertyType, clonedValue);
		}

		public static Object getValueToReplacePassword(GenericModelType pawwordPropertyType) {
			if (pawwordPropertyType.getTypeCode() == TypeCode.stringType)
				return "*****";
			else
				return null;
		}

	}

}
