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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.script.Script;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.constraint.DateClipping;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.processing.core.commons.EntityHashingComparator.Builder;
import com.braintribe.model.processing.core.commons.hashing.EntityHashing;
import com.braintribe.model.processing.core.expert.api.DenotationMap;
import com.braintribe.model.processing.deployment.script.CompiledScript;
import com.braintribe.model.processing.deployment.script.ScriptEngine;
import com.braintribe.model.processing.deployment.script.ScriptEvaluationException;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.query.building.EntityQueries;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.provider.Holder;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;

import tribefire.extension.spreadsheet.model.exchange.api.data.ImportCompletionStatus;
import tribefire.extension.spreadsheet.model.exchange.api.data.ImportReport;
import tribefire.extension.spreadsheet.model.exchange.api.data.PushAddress;
import tribefire.extension.spreadsheet.model.exchange.api.data.TransientImportReport;
import tribefire.extension.spreadsheet.model.exchange.api.request.ImportSpreadsheetRequest;
import tribefire.extension.spreadsheet.model.exchange.api.request.NotifyImportStatusMonitor;
import tribefire.extension.spreadsheet.model.exchange.metadata.AbstractSpreadsheetColumnNameMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnDatePatternMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnDateZoneMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnFormatMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnNameAdapterScriptMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnNameMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnNameScriptMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnNumberFormatMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnRegexMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnTrimming;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetEntityContextLinking;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetEntityFilterScript;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetIdentityProperty;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetReferenceColumnMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetRowNumProperty;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetUnmappedProperty;
import tribefire.extension.spreadsheet.model.reason.ConversionFailed;

public abstract class SpreadsheetImporter<T extends ImportSpreadsheetRequest> {
	private static Logger logger = Logger.getLogger(SpreadsheetImporter.class);
	private static final int MAX_ERROR_IN_REPORT = 1000;

	protected static List<TypeConverter<?, ?>> converters = new ArrayList<TypeConverter<?, ?>>();
	private DenotationMap<Script, ScriptEngine<?>> engines;
	private T request;
	private StreamPipeFactory streamPipeFactory;
	private EntityType<? extends ImportReport> reportType;

	static {
		registerConverter(String.class, BigDecimal.class, BigDecimal::new);
		registerConverter(String.class, Double.class, Double::parseDouble);
		registerConverter(String.class, Float.class, Float::parseFloat);
		registerConverter(String.class, Integer.class, Conversions::stringToInteger);
		registerConverter(String.class, Long.class, Conversions::stringToLong);
		registerConverter(String.class, Boolean.class, Conversions::stringToBoolean);
		registerConverter(Number.class, BigDecimal.class, n -> new BigDecimal(n.doubleValue()));
		registerConverter(Number.class, Double.class, Number::doubleValue);
		registerConverter(Number.class, Float.class, Number::floatValue);
		registerConverter(Number.class, Integer.class, Number::intValue);
		registerConverter(Number.class, Long.class, Number::longValue);
		registerConverter(Number.class, String.class, Number::toString);

		registerFailingConverter(String.class, Date.class,
				Reasons.build(ConversionFailed.T).text("Missing SpreadsheetColumnDatePatternMapping meta-data").toReason());
	}

	private static <S, T> void registerConverter(Class<S> fromClass, Class<T> toClass, Function<S, T> converter) {
		converters.add(new BasicTypeConverter<S, T>(fromClass, toClass, converter));
	}

	private static <S, T> void registerFailingConverter(Class<S> fromClass, Class<T> toClass, Reason reason) {
		converters.add(new FailingTypeConverter<S, T>(fromClass, toClass, reason));
	}

	private Map<Script, CompiledScript> compiledScriptCache = new HashMap<>();
	private int threadCount = 8;

	public SpreadsheetImporter(DenotationMap<Script, ScriptEngine<?>> engines) {
		this.engines = engines;
	}

	@Configurable
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	public ImportReport process(AccessRequestContext<T> context) {
		try {
			T spreadsheetImport = context.getSystemRequest();
			request = spreadsheetImport;

			reportType = request.getTransient() ? TransientImportReport.T : ImportReport.T;

			if (spreadsheetImport.getSheet() == null)
				throw new IllegalArgumentException("Property sheet must not be null on [ " + spreadsheetImport.entityType().getTypeSignature());

			PersistenceGmSession session = context.getSystemSession();
			ModelAccessory modelAccessory = session.getModelAccessory();
			ModelOracle oracle = modelAccessory.getOracle();

			String typeSignature = spreadsheetImport.getTargetType();

			EntityType<GenericEntity> importTargetType = requireEntityType(oracle, typeSignature);

			ModelMdResolver cmdrContextBuilder = modelAccessory.getCmdResolver().getMetaData().useCase(spreadsheetImport.getUseCase())
					.useCase(getImportFormatUseCase());
			Map<String, Property> propertyColumnMapping = buildPropertyColumnMapping(importTargetType, cmdrContextBuilder);

			BiFunction<Property, String, String> textAdapter = buildTextAdapter(importTargetType, cmdrContextBuilder);

			if (propertyColumnMapping == null || propertyColumnMapping.isEmpty()) {
				ImportReport report = reportType.create();
				report.setCompletionStatus(ImportCompletionStatus.failed);
				report.setNotifications(Notifications.build().add().message()
						.confirmWarn("No entity was imported because no column mapping found for type: " + importTargetType.getTypeSignature())
						.close().list());
				return report;
			}

			// determine column name transform
			SpreadsheetColumnNameAdapterScriptMapping nameMapping = cmdrContextBuilder.entityType(importTargetType)
					.meta(SpreadsheetColumnNameAdapterScriptMapping.T).exclusive();

			final Function<String, String> columnNameAdapter = getColumnNameAdapter(nameMapping);

			EnrichingContext enrichingContext = new EnrichingContext(cmdrContextBuilder, importTargetType);

			// Validation
			if (!request.getLenient()) {
				StopWatch stopWatch = new StopWatch();
				SheetEntityStreamingContextImpl<?> validationContext = importEntities(context, null, null, importTargetType, spreadsheetImport,
						columnNameAdapter, propertyColumnMapping, textAdapter, cmdrContextBuilder, enrichingContext);

				Collection<Reason> errors = validationContext.getErrors();

				if (!errors.isEmpty()) {
					logger.debug("Validation of entities for import in " + stopWatch.getElapsedTime() + "ms");
					ImportReport report = reportType.create();
					report.setProcessedRowCount(validationContext.rowCount);
					report.setCompletionStatus(ImportCompletionStatus.failed);
					report.setErrorReport(buildErrorReport(context, importTargetType, errors, validationContext.hasErrorOverflow()));
					report.setNotifications(
							Notifications.build().add().message()
									.confirmError("Import for target type [" + request.getTargetType()
											+ "] was aborted due to validation problems. Please read error report for more information!")
									.close().list());
					return report;
				}

				logger.debug("Validated " + validationContext.getEntityCount() + " entities for import in " + stopWatch.getElapsedTime() + "ms");
			}

			// Actual Import

			StopWatch stopWatch = new StopWatch();

			final SheetEntityStreamingContextImpl<?> importContext;
			final ImportReport report;

			if (spreadsheetImport.getTransient()) {
				TransientImportReport transientReport = TransientImportReport.T.create();
				report = transientReport;
				Map<Integer, GenericEntity> transientEntities = Collections.synchronizedMap(new TreeMap<>());
				importContext = importEntities(context, null, transientEntities::put, importTargetType, spreadsheetImport, columnNameAdapter,
						propertyColumnMapping, textAdapter, cmdrContextBuilder, enrichingContext);
				transientReport.getEntities().addAll(transientEntities.values());
			} else {
				report = ImportReport.T.create();
				importContext = importEntities(context, session, null, importTargetType, spreadsheetImport, columnNameAdapter, propertyColumnMapping,
						textAdapter, cmdrContextBuilder, enrichingContext);
			}

			report.setProcessedRowCount(importContext.rowCount);
			report.setImportedEntityCount(importContext.getEntityCount());

			Collection<Reason> errors = importContext.getErrors();

			if (!errors.isEmpty()) {
				report.setCompletionStatus(ImportCompletionStatus.problematic);
				report.setErrorReport(buildErrorReport(context, importTargetType, errors, importContext.hasErrorOverflow()));
				report.setNotifications(Notifications.build() //
						.add().message()
						.success("Imported " + importContext.getEntityCount() + " entities with problematic data from spreadsheet of type: "
								+ request.getTargetType() + ". Please read import for more information on the problems!") //
						.command().gotoModelPath("Report", false, true).addElement(report).addElement(report, ImportReport.errorReport).close()
						.close().list());
				logger.debug("Imported " + importContext.getEntityCount() + " entities with problematic data into database "
						+ stopWatch.getElapsedTime() + "ms");
			} else {
				report.setCompletionStatus(ImportCompletionStatus.successful);
				report.setNotifications(Notifications.build().add() //
						.message()
						.success("Successfully imported " + importContext.getEntityCount() + " entities from spreadsheet of type: "
								+ request.getTargetType()) //
						.command().gotoModelPath("Report").addElement(report).close().close() //
						.list());
				logger.debug("Imported " + importContext.getEntityCount() + " entities into database " + stopWatch.getElapsedTime() + "ms");
			}

			return report;

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while executing spreadsheet import for type: " + request.getTargetType());
		}
	}

	protected abstract String getImportFormatUseCase();

	private static class ImportEntityQueries extends EntityQueries {
		static EntityQuery queryExistingEntitiesByHash(EntityType<?> type, Property hashProperty, List<GenericEntity> entities) {
			Set<String> hashes = entities.stream().map(e -> (String) hashProperty.get(e)).collect(Collectors.toSet());
			return from(type).where(in(property(hashProperty), hashes));
		}

		static EntityQuery queryExistingEntities(EntityType<?> type, List<Pair<Property, SpreadsheetIdentityProperty>> identificationProperties,
				List<GenericEntity> entities) {
			if (identificationProperties.size() == 1) {
				Pair<Property, SpreadsheetIdentityProperty> idPropertyPair = CollectionTools.getFirstElement(identificationProperties);
				Property idProperty = idPropertyPair.first();
				boolean nullIsIdentifier = idPropertyPair.second().getNullIsIdentifier();
				Set<Object> idValues = new HashSet<>();

				for (GenericEntity entity : entities) {
					Object idValue = idProperty.get(entity);
					if (nullIsIdentifier || idValue != null)
						idValues.add(idValue);
				}

				return from(type).where(in(property(idProperty), idValues));
			} else {
				Disjunction identityConditions = Disjunction.T.create();

				for (GenericEntity entity : entities) {
					Conjunction conjunction = Conjunction.T.create();

					for (Pair<Property, SpreadsheetIdentityProperty> pair : identificationProperties) {
						Property p = pair.first();
						boolean nullIsIdentifier = pair.second().getNullIsIdentifier();

						Object propertyValue = p.get(entity);

						if (nullIsIdentifier || propertyValue != null) {
							conjunction.getOperands().add(eq(property(p), propertyValue));
						} else {
							conjunction = null;
						}

					}

					if (conjunction != null) {
						identityConditions.getOperands().add(conjunction);
					}
				}

				if (identityConditions.getOperands().size() == 0) {
					return null;
				} else {
					return from(type).where(identityConditions);
				}
			}
		}
	}

	private SheetEntityStreamingContextImpl<T> importEntities(AccessRequestContext<T> context, PersistenceGmSession session,
			BiConsumer<Integer, GenericEntity> transientEntityConsumer, EntityType<GenericEntity> importTargetType, T spreadsheetImport,
			Function<String, String> columnNameAdapter, Map<String, Property> propertyColumnMapping, BiFunction<Property, String, String> textAdapter,
			ModelMdResolver cmdrContextBuilder, EnrichingContext enrichingContext) throws Exception {
		SheetEntityStreamingContextImpl<T> streamingContext = new SheetEntityStreamingContextImpl<>(importTargetType, spreadsheetImport,
				columnNameAdapter, propertyColumnMapping, textAdapter, cmdrContextBuilder, context);
		streamingContext.setTransientEntityConsumer(transientEntityConsumer);

		registerDedicatedConverters(streamingContext, cmdrContextBuilder, importTargetType);

		try (EntityStreamer streamer = streamEntitiesFromSpreadsheet(streamingContext)) {

			final SessionImport sessionImport;

			final List<Pair<Property, SpreadsheetIdentityProperty>> identificationProperties;
			final EntityHashingComparator<GenericEntity> identityEqProxyFactory;

			if (session != null) {
				if (enrichingContext.getShareEntities()) {
					Property hashProperty = enrichingContext.getHashProperty();
					sessionImport = new SharableIdentitiesImport(importTargetType, session, hashProperty);
				} else {
					sessionImport = new UpdatableIdentitiesImport(importTargetType, cmdrContextBuilder, propertyColumnMapping);
				}
			} else {
				sessionImport = null;
			}

			try (ParallelProcessing<Integer> parallelProcessing = new ParallelProcessing<>(threadCount)) {
				parallelProcessing.submitForAllThreads(() -> {
					int bulkSize = 100;
					List<Pair<GenericEntity, Integer>> entities = new ArrayList<>(bulkSize);

					boolean stop = false;

					Predicate<GenericEntity> entityFilter = streamingContext.buildEntityFilter();

					while (!stop) {
						final PersistenceGmSession bulkSession;
						final BiConsumer<GenericEntity, Integer> bindingEnricher;

						if (session != null) {
							bulkSession = session.newEquivalentSession();
							bindingEnricher = enrichingContext.buildEnricher(bulkSession);
						} else {
							bulkSession = null;
							if (request.getTransient())
								bindingEnricher = enrichingContext.rowNumberEnricher;
							else
								bindingEnricher = null;
						}

						int entityCount = 0;

						for (int i = 0; i < bulkSize; i++) {
							ParsedEntity parsedEntity = streamer.next(importTargetType::create);

							if (parsedEntity == null) {
								stop = true;
								break;
							}

							if (parsedEntity.error != null) {
								streamingContext.notifyError(parsedEntity.row, parsedEntity.error);
							}

							GenericEntity entity = parsedEntity.entity;

							if (entity == null)
								continue;

							if (!entityFilter.test(entity))
								continue;

							streamingContext.notifyEntity(entity, parsedEntity.row);

							int rowNum = parsedEntity.row + 1;

							enrichingContext.applyHash(entity);
							entities.add(Pair.of(entity, rowNum));

							entityCount++;
						}

						if (bulkSession != null) {
							final ImportCloningContext cloningContext = sessionImport.prepareImportCloningContext(bulkSession, entities);

							List<GenericEntity> sessionEntities = new ArrayList<>(entities.size());

							// cloning each entity into the session and after that enrich a binding entity if required
							for (Pair<GenericEntity, Integer> entityAndRow : entities) {
								int rowNum = entityAndRow.second();
								GenericEntity entity = entityAndRow.first();

								GenericEntity sessionEntity = importTargetType.clone(cloningContext, entity, StrategyOnCriterionMatch.reference);
								sessionEntities.add(sessionEntity);

								if (bindingEnricher != null)
									bindingEnricher.accept(sessionEntity, rowNum);
							}

							bulkSession.commit();

							// with identification properties entities are updatable and therefore duplicates will not happen
							// thus no rollback is needed in case of errors
							if (cloningContext.isRollbackable())
								sessionEntities.stream().map(GenericEntity::getId).forEach(streamingContext::addCreatedEntityId);

						} else {
							for (Pair<GenericEntity, Integer> entityAndRow : entities) {
								int rowNum = entityAndRow.second();
								GenericEntity entity = entityAndRow.first();
								if (bindingEnricher != null)
									bindingEnricher.accept(entity, rowNum);
							}
						}

						entities.clear();

						streamingContext.increaseEntityCount(entityCount);

					}
				});

				LazyInitialized<RuntimeException> lazyException = new LazyInitialized<>(() -> new RuntimeException("Error while importing entities"));

				parallelProcessing.consumeResults(lazyException);

				streamingContext.updateStatus(true);

				if (lazyException.isInitialized()) {
					RuntimeException e = lazyException.get();
					if (session != null)
						rollback(importTargetType, session, streamingContext.createdEntityIds, e);
					throw e;
				}
			}
		}

		return streamingContext;
	}

	private abstract class SessionImport {

		protected abstract ImportCloningContext createBaseCloningContext(PersistenceGmSession session);

		public ImportCloningContext prepareImportCloningContext(PersistenceGmSession session, List<Pair<GenericEntity, Integer>> entities) {

			ImportCloningContext cloningContext = createBaseCloningContext(session);

			if (cloningContext.isQueryingExistingEntitiesRequired()) {

				int maxEntities = cloningContext.maxEntities();

				List<GenericEntity> existingEntityBulk = new ArrayList<>(maxEntities);

				int i = 0;
				for (Pair<GenericEntity, Integer> entityAndRow : entities) {
					GenericEntity entity = entityAndRow.first();
					existingEntityBulk.add(entity);
					i++;
					if (existingEntityBulk.size() == maxEntities || i == entities.size()) {

						cloningContext.queryAndIndexExistingEntities(existingEntityBulk);
						existingEntityBulk.clear();
					}
				}
			}

			return cloningContext;
		}

	}

	private class UpdatableIdentitiesImport extends SessionImport {
		private final List<Pair<Property, SpreadsheetIdentityProperty>> identificationProperties;
		private final EntityHashingComparator<GenericEntity> identityEqProxyFactory;
		private EntityType<GenericEntity> importTargetType;

		public UpdatableIdentitiesImport(EntityType<GenericEntity> importTargetType, ModelMdResolver cmdrContextBuilder,
				Map<String, Property> propertyColumnMapping) {
			this.importTargetType = importTargetType;

			identificationProperties = getIdentificationProperties(importTargetType, propertyColumnMapping, cmdrContextBuilder);

			Builder<GenericEntity> builder = EntityHashingComparator.build(importTargetType);

			for (Pair<Property, SpreadsheetIdentityProperty> entry : identificationProperties) {
				Property p = entry.first();
				builder.addField(p.getName());
			}

			identityEqProxyFactory = builder.done();
		}

		@Override
		protected BaseCloningContext createBaseCloningContext(PersistenceGmSession session) {
			return new UpdateCloningContext(importTargetType, identityEqProxyFactory, session, identificationProperties);
		}

	}

	private class SharableIdentitiesImport extends SessionImport {
		private final EntityHashingComparator<GenericEntity> identityEqProxyFactory;
		private Property hashProperty;
		private PersistenceGmSession session;
		private EntityType<GenericEntity> importTargetType;

		public SharableIdentitiesImport(EntityType<GenericEntity> importTargetType, PersistenceGmSession session, Property hashProperty) {

			this.importTargetType = importTargetType;
			this.session = session;
			this.hashProperty = hashProperty;
			List<Property> identificationProperties = importTargetType.getProperties().stream()
					.filter(p -> !(p == hashProperty || p.isIdentifying() || p.isGlobalId())).collect(Collectors.toList());

			Builder<GenericEntity> builder = EntityHashingComparator.build(importTargetType);

			for (Property p : identificationProperties) {
				builder.addField(p.getName());
			}

			identityEqProxyFactory = builder.done();
		}

		@Override
		protected BaseCloningContext createBaseCloningContext(PersistenceGmSession session) {
			return new SharingCloningContext(importTargetType, identityEqProxyFactory, session, hashProperty);
		}
	}

	interface ImportCloningContext extends CloningContext {
		boolean isRollbackable();
		boolean isQueryingExistingEntitiesRequired();
		int maxEntities();
		void queryAndIndexExistingEntities(List<GenericEntity> existingEntityBulk);
	}

	private static abstract class BaseCloningContext extends StandardCloningContext implements ImportCloningContext {
		protected final PersistenceGmSession session;

		public BaseCloningContext(PersistenceGmSession session) {
			super();
			this.session = session;
		}

		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			return session.createRaw(entityType);
		}

		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned,
				GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
			return !(property.isIdentifying() || property.isGlobalId());
		}
	}

	private static class UpdateCloningContext extends BaseCloningContext {
		private final List<Pair<Property, SpreadsheetIdentityProperty>> identificationProperties;
		private Map<EqProxy<GenericEntity>, GenericEntity> existingEntitiesLookup = new HashMap<>();
		private final EntityHashingComparator<GenericEntity> identityEqProxyFactory;
		private EntityType<GenericEntity> importTargetType;

		public UpdateCloningContext(EntityType<GenericEntity> importTargetType, EntityHashingComparator<GenericEntity> identityEqProxyFactory,
				PersistenceGmSession session, List<Pair<Property, SpreadsheetIdentityProperty>> identificationProperties) {
			super(session);
			this.importTargetType = importTargetType;
			this.identityEqProxyFactory = identityEqProxyFactory;
			this.identificationProperties = identificationProperties;
		}

		@Override
		public boolean isQueryingExistingEntitiesRequired() {
			return !identificationProperties.isEmpty();
		}

		@Override
		public int maxEntities() {
			return Math.max(1, 50 / identificationProperties.size());
		}

		@Override
		public boolean isRollbackable() {
			return identificationProperties.isEmpty();
		}

		@Override
		public void queryAndIndexExistingEntities(List<GenericEntity> existingEntityBulk) {
			if (identificationProperties.isEmpty())
				return;

			EntityQuery query = ImportEntityQueries.queryExistingEntities(importTargetType, identificationProperties, existingEntityBulk);

			if (query != null) {
				List<GenericEntity> existingEntities = session.query().entities(query).list();
				for (GenericEntity existingEntity : existingEntities) {
					existingEntitiesLookup.put(identityEqProxyFactory.eqProxy(existingEntity), existingEntity);
				}
			}
		}

		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			GenericEntity existingEntity = existingEntitiesLookup.get(identityEqProxyFactory.eqProxy(instanceToBeCloned));
			if (existingEntity != null)
				return existingEntity;

			return super.supplyRawClone(entityType, instanceToBeCloned);
		}

		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned,
				GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
			return !(property.isIdentifying() || property.isGlobalId());
		}
	}

	private static class SharingCloningContext extends BaseCloningContext implements ImportCloningContext {
		private MultiMap<String, GenericEntity> entitiesByHash = new HashMultiMap<>();
		private EntityHashingComparator<GenericEntity> identityEqProxyFactory;
		private Property hashProperty;
		private EntityType<GenericEntity> importTargetType;

		public SharingCloningContext(EntityType<GenericEntity> importTargetType, EntityHashingComparator<GenericEntity> identityEqProxyFactory,
				PersistenceGmSession session, Property hashProperty) {
			super(session);
			this.importTargetType = importTargetType;
			this.identityEqProxyFactory = identityEqProxyFactory;
			this.hashProperty = hashProperty;
		}

		@Override
		public boolean isQueryingExistingEntitiesRequired() {
			return hashProperty != null;
		}

		@Override
		public boolean isRollbackable() {
			return false;
		}

		@Override
		public int maxEntities() {
			return 50;
		}

		@Override
		public void queryAndIndexExistingEntities(List<GenericEntity> existingEntityBulk) {
			EntityQuery query = ImportEntityQueries.queryExistingEntitiesByHash(importTargetType, hashProperty, existingEntityBulk);

			List<GenericEntity> existingEntities = session.query().entities(query).list();

			for (GenericEntity existingEntity : existingEntities) {
				entitiesByHash.put(hashProperty.get(existingEntity), existingEntity);
			}
		}

		@Override
		public <T> T getAssociated(GenericEntity entity) {
			T associated = super.getAssociated(entity);

			if (associated != null)
				return associated;

			String hash = hashProperty.get(entity);

			Collection<GenericEntity> candidates = entitiesByHash.getAll(hash);

			if (!candidates.isEmpty()) {
				EqProxy<GenericEntity> e1 = identityEqProxyFactory.eqProxy(entity);
				for (GenericEntity candidate : candidates) {
					EqProxy<GenericEntity> e2 = identityEqProxyFactory.eqProxy(candidate);

					if (e1.equals(e2)) {
						registerAsVisited(entity, candidate);
						return (T) candidate;
					}
				}
			}

			return null;
		}
	}

	private static String formatErrorReportDate(Date now) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy h:mm a z").withLocale(Locale.ENGLISH);
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(now.toInstant(), ZoneOffset.systemDefault());
		String formattedDate = formatter.format(dateTime);
		return formattedDate;
	}

	private Resource buildErrorReport(AccessRequestContext<T> context, EntityType<?> targetType, Collection<Reason> errors, boolean errorOverflow) {
		StreamPipe streamPipe = Optional.ofNullable(streamPipeFactory).orElseGet(StreamPipes::simpleFactory).newPipe("spreadheet-import");
		CountingOutputStream countingOut = null;

		Date now = new Date();

		MessageDigest digest;

		try {
			digest = MessageDigest.getInstance("MD5");

			try (Writer writer = new OutputStreamWriter(
					new DigestOutputStream(countingOut = new CountingOutputStream(streamPipe.openOutputStream()), digest), "UTF-8")) {
				String fileName = context.getOriginalRequest().getSheet().getName();
				if (fileName != null)
					writer.write("Processed file: " + fileName + "\n");

				writer.write("Imported at: " + formatErrorReportDate(now) + "\n");

				String requestorUserName = context.getRequestorUserName();

				if (requestorUserName != null)
					writer.write("Imported by: " + requestorUserName + "\n");

				writer.write("\n");

				if (errorOverflow) {
					writer.write("NOTE! Error report was stopped after too many errors (first " + MAX_ERROR_IN_REPORT + " errors are shown).\n\n");
				}

				for (Reason reason : errors) {
					printReason(writer, reason, 0);
				}

			}

		} catch (Exception e) {
			logger.error("Error while generating error report", e);
			return null;
		}

		Resource report = Resource.createTransient(streamPipe::openInputStream);
		report.setCreated(now);
		report.setCreator(context.getRequestorUserName());
		report.setFileSize(countingOut.getCount());
		report.setMd5(StringTools.toHex(digest.digest()));

		StringBuilder fileNameBuilder = new StringBuilder("import-");
		fileNameBuilder.append(StringTools.camelCaseToDashSeparated(targetType.getShortName()));
		fileNameBuilder.append('-');
		fileNameBuilder.append(formatDateForFilename(now));
		fileNameBuilder.append(".txt");

		report.setName(fileNameBuilder.toString());
		report.setMimeType("text/plain");

		return report;
	}

	private String formatDateForFilename(Date now) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss").withLocale(Locale.getDefault());
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(now.toInstant(), ZoneOffset.systemDefault());
		String formattedDate = formatter.format(dateTime);
		return formattedDate;
	}

	private void printReason(Writer writer, Reason reason, int indent) throws IOException {
		for (int i = 0; i < indent; i++) {
			writer.append("  ");
		}

		if (indent > 0)
			writer.append("- ");

		writer.append(reason.getText());
		writer.append('\n');

		int subIndent = indent + 1;
		for (Reason cause : reason.getReasons()) {
			printReason(writer, cause, subIndent);
		}

		if (indent == 0)
			writer.append('\n');
	}

	private void registerDedicatedConverters(SpreadsheetImporter<T>.SheetEntityStreamingContextImpl<T> streamingContext,
			ModelMdResolver cmdrContextBuilder, EntityType<GenericEntity> importTargetType) {

		EntityMdResolver entityMdResolver = cmdrContextBuilder.entityType(importTargetType);

		for (Property property : importTargetType.getProperties()) {
			PropertyMdResolver propertyMdResolver = entityMdResolver.property(property);
			SpreadsheetColumnFormatMapping mapping = propertyMdResolver.meta(SpreadsheetColumnFormatMapping.T).exclusive();

			if (mapping != null) {
				if (mapping instanceof SpreadsheetColumnDatePatternMapping) {
					if (property.getType().getTypeCode() == TypeCode.dateType) {
						SpreadsheetColumnDatePatternMapping dateMapping = (SpreadsheetColumnDatePatternMapping) mapping;

						ZoneId zoneId = Optional.ofNullable(dateMapping.getDefaultTimeZone()).map(ZoneId::of).orElse(ZoneOffset.UTC);
						Locale locale = Optional.ofNullable(dateMapping.getLocale()).map(Locale::forLanguageTag).orElse(Locale.US);

						DateParser parser = new DateParser(dateMapping.getPattern(), zoneId, locale,
								propertyMdResolver.meta(DateClipping.T).exclusive(), dateMapping.getEmptyStringToNull());

						streamingContext.registerDedicatedConverter(property, String.class, parser);
					}
				} else if (mapping instanceof SpreadsheetColumnNumberFormatMapping) {
					if (property.getType().isNumber()) {
						SpreadsheetColumnNumberFormatMapping numberMapping = (SpreadsheetColumnNumberFormatMapping) mapping;

						Function<String, Number> stringToNumberFunction = Conversions.stringToNumberFunction(numberMapping.getDecimalSeparator(),
								numberMapping.getDigitGroupingSymbol(), (SimpleType) property.getType());

						streamingContext.registerDedicatedConverter(property, String.class, stringToNumberFunction);
					}
				} else if (mapping instanceof SpreadsheetColumnRegexMapping) {
					if (property.getType() == EssentialTypes.TYPE_STRING) {
						SpreadsheetColumnRegexMapping regexMapping = (SpreadsheetColumnRegexMapping) mapping;

						streamingContext.registerDedicatedConverter(property, String.class,
								new RegexMapper(regexMapping.getPattern(), regexMapping.getTemplate()));
					}
				} else if (mapping instanceof SpreadsheetColumnDateZoneMapping) {
					if (property.getType() == EssentialTypes.TYPE_DATE) {
						SpreadsheetColumnDateZoneMapping dateZoneMapping = (SpreadsheetColumnDateZoneMapping) mapping;

						DateZoneMapper dateZoneMapper = new DateZoneMapper(dateZoneMapping.getTimeZone(),
								propertyMdResolver.meta(DateClipping.T).exclusive());
						streamingContext.registerDedicatedConverter(property, Date.class, dateZoneMapper);
					}

				}
			}
		}

	}

	private class EnrichingContext {
		private EntityType<GenericEntity> bindingType;
		private Property linkProperty;
		private EntityHashing<GenericEntity> hashGenerator;
		private Property hashProperty;
		private EntityType<GenericEntity> importTargetType;
		private BiConsumer<GenericEntity, Integer> rowNumberEnricher;
		private boolean shareEntities;

		public EnrichingContext(ModelMdResolver cmdrContextBuilder, EntityType<GenericEntity> importTargetType) {
			this.importTargetType = importTargetType;
			SpreadsheetEntityContextLinking spreadsheetEntityBinding = cmdrContextBuilder.entityType(importTargetType)
					.meta(SpreadsheetEntityContextLinking.T).exclusive();

			if (spreadsheetEntityBinding != null && !request.getTransient()) {
				GmEntityType gmBindingType = spreadsheetEntityBinding.getType();
				GmProperty gmLinkProperty = spreadsheetEntityBinding.getLinkProperty();
				GmProperty gmHashProperty = spreadsheetEntityBinding.getHashProperty();

				if (gmBindingType != null && gmLinkProperty != null) {
					bindingType = gmBindingType.reflectionType();
					linkProperty = bindingType.findProperty(gmLinkProperty.getName());
					hashProperty = gmHashProperty != null ? importTargetType.findProperty(gmHashProperty.getName()) : null;

					rowNumberEnricher = buildRowNumEnricher(cmdrContextBuilder, bindingType);

					if (hashProperty != null) {
						shareEntities = true;
						hashGenerator = EntityHashing.hashGenerator(importTargetType, p -> !(p.isIdentifying() || p.isGlobalId()));
					}
				}

			} else {
				rowNumberEnricher = buildRowNumEnricher(cmdrContextBuilder, importTargetType);
			}
		}

		public void applyHash(GenericEntity entity) {
			if (hashProperty != null) {
				hashProperty.set(entity, hashGenerator.hashEntity(entity));
			}
		}

		public Property getHashProperty() {
			return hashProperty;
		}

		public boolean getShareEntities() {
			return shareEntities;
		}

		private BiConsumer<GenericEntity, Integer> buildRowNumEnricher(ModelMdResolver cmdrContextBuilder, EntityType<GenericEntity> type) {

			EntityMdResolver entityMdResolver = cmdrContextBuilder.entityType(type);

			BiConsumer<GenericEntity, Integer> enricher = (e, r) -> {
				/* noop */ };

			for (Property property : type.getProperties()) {
				if (!entityMdResolver.property(property).is(SpreadsheetRowNumProperty.T))
					continue;

				enricher = enricher.andThen((e, r) -> {
					property.set(e, r);
				});
			}

			return enricher;
		}

		public BiConsumer<GenericEntity, Integer> buildEnricher(PersistenceGmSession session) {
			if (bindingType != null) {

				Consumer<GenericEntity> propertyEnricher = buildEnricher(bindingType, session);

				BiConsumer<GenericEntity, Integer> enricher = (e, r) -> {
					GenericEntity bindingEntity = session.create(bindingType);

					if (linkProperty != null)
						linkProperty.set(bindingEntity, e);

					propertyEnricher.accept(bindingEntity);

					rowNumberEnricher.accept(bindingEntity, r);
				};

				return enricher;
			} else {
				Consumer<GenericEntity> propertyEnricher = buildEnricher(importTargetType, session);
				return rowNumberEnricher.andThen((e, r) -> propertyEnricher.accept(e));
			}
		}

		private Consumer<GenericEntity> buildEnricher(EntityType<GenericEntity> type, PersistenceGmSession session) {
			Consumer<GenericEntity> enricher = e -> {
				/* noop */ };

			for (Map.Entry<String, Object> enrichEntry : request.getEnrichments().entrySet()) {
				String propertyName = enrichEntry.getKey();
				Object value = enrichEntry.getValue();

				if (value == null)
					continue;

				Property property = type.findProperty(propertyName);

				if (property == null)
					continue;

				final Object sessionSafeValue;

				if (property.getType().isEntity()) {
					GenericEntity entity = (GenericEntity) value;
					sessionSafeValue = session.query().entity(entity).findLocalOrBuildShallow();
				} else {
					sessionSafeValue = value;
				}

				Consumer<GenericEntity> propertyEnricher = e -> property.set(e, sessionSafeValue);

				enricher = enricher.andThen(propertyEnricher);
			}

			return enricher;
		}

	}

	protected void rollback(EntityType<?> entityType, PersistenceGmSession session, Set<Object> ids, Exception e) {
		ParallelIterator<Object> it = ParallelIterator.of(ids.iterator());

		StopWatch stopWatch = new StopWatch();

		logger.debug("Deleting " + ids.size() + " entities from database as rollback after an error.");

		try (ParallelProcessing<Void> parallelProcessing = new ParallelProcessing<>(threadCount)) {
			parallelProcessing.submitForAllThreads(() -> {
				boolean stop = false;
				while (!stop) {
					PersistenceGmSession rollbackSession = session.newEquivalentSession();

					for (int i = 0; i < 100; i++) {
						Object id = it.next();

						if (id == null) {
							stop = true;
							break;
						}

						GenericEntity deleteCandidate = rollbackSession.query().entity(entityType, id).findLocalOrBuildShallow();
						rollbackSession.deleteEntity(deleteCandidate, DeleteMode.ignoreReferences);
					}

					rollbackSession.commit();
				}
			});

			parallelProcessing.consumeResults(() -> e);

			logger.debug("Deleted " + ids.size() + " entities from database as rollback after an error in " + stopWatch.getElapsedTime() + "ms.");
		}
	}

	private Function<String, String> getColumnNameAdapter(SpreadsheetColumnNameAdapterScriptMapping nameMapping) throws ScriptEvaluationException {
		if (nameMapping != null) {
			Script script = nameMapping.getScript();
			ScriptEngine<Script> engine = engines.get(script);

			CompiledScript compiledScript = engine.compile(script);

			return columnName -> {
				try {
					return compiledScript.evaluate(Collections.singletonMap("columnName", columnName));
				} catch (ScriptEvaluationException e) {
					throw new RuntimeException(e);
				}
			};
		} else {
			return Function.identity();
		}
	}

	private EntityType<GenericEntity> requireEntityType(ModelOracle oracle, String typeSignature) {
		if (typeSignature.indexOf('.') != -1) {
			// qualified name case
			EntityTypeOracle entityTypeOracle = oracle.findEntityTypeOracle(typeSignature);

			if (entityTypeOracle == null)
				throw new IllegalArgumentException("Type [" + typeSignature + "] not found in model [" + oracle.getGmMetaModel().getName() + "]");

			if (entityTypeOracle.asGmEntityType().getIsAbstract())
				throw new IllegalArgumentException(
						"Cannot instantiate abstract type [" + typeSignature + "] found in model [" + oracle.getGmMetaModel().getName() + "]");

			return entityTypeOracle.asType();
		} else {
			// simple name case
			List<EntityType<GenericEntity>> matchingTypes = oracle.findGmTypeBySimpleName(typeSignature).stream() //
					.filter(GmCustomType::isEntity).map(t -> (EntityType<GenericEntity>) t.reflectionType()).collect(Collectors.toList());

			switch (matchingTypes.size()) {
				case 0:
					throw new IllegalArgumentException("Type [" + typeSignature + "] not found in model [" + oracle.getGmMetaModel().getName() + "]");

				case 1:
					EntityType<GenericEntity> entityType = matchingTypes.get(0);

					if (entityType.isAbstract())
						throw new IllegalArgumentException("Cannot instantiate abstract type [" + typeSignature + "] found in model ["
								+ oracle.getGmMetaModel().getName() + "]");

					return entityType;

				default:
					String signatures = matchingTypes.stream().map(EntityType::getTypeSignature).collect(Collectors.joining(","));
					throw new IllegalArgumentException("Short type name [" + typeSignature + "] has ambigous matches [" + signatures + "] in model ["
							+ oracle.getGmMetaModel().getName() + "]");
			}
		}
	}

	protected List<Pair<Property, SpreadsheetIdentityProperty>> getIdentificationProperties(EntityType<GenericEntity> importTargetType,
			Map<String, Property> propertyColumnMapping, ModelMdResolver cmdrContextBuilder) {
		List<Pair<Property, SpreadsheetIdentityProperty>> identityProperties = new ArrayList<>();
		for (Property property : propertyColumnMapping.values()) {

			SpreadsheetIdentityProperty identityProperty = cmdrContextBuilder.entityType(importTargetType).property(property.getName())
					.meta(SpreadsheetIdentityProperty.T).exclusive();

			if (identityProperty != null) {
				identityProperties.add(Pair.of(property, identityProperty));
			}
		}
		return identityProperties;
	}

	protected Map<String, Property> buildPropertyColumnMapping(EntityType<GenericEntity> importTargetType, ModelMdResolver cmdrContextBuilder) {

		Map<String, Property> propertyColumnMapping = new HashMap<String, Property>();
		for (Property property : importTargetType.getProperties()) {
			if (cmdrContextBuilder.entityType(importTargetType).property(property).is(SpreadsheetUnmappedProperty.T))
				continue;

			String mappingColumnName = getMappingColumnName(importTargetType, cmdrContextBuilder, property);
			propertyColumnMapping.put(mappingColumnName, property);
		}
		return propertyColumnMapping;
	}

	private BiFunction<Property, String, String> buildTextAdapter(EntityType<GenericEntity> importTargetType, ModelMdResolver cmdrContextBuilder) {
		Set<Property> trimmedProperties = new HashSet<>();

		for (Property property : importTargetType.getProperties()) {
			if (cmdrContextBuilder.entityType(importTargetType).property(property).is(SpreadsheetColumnTrimming.T))
				trimmedProperties.add(property);
		}

		if (trimmedProperties.isEmpty())
			return (p, s) -> s;

		return (p, s) -> trimmedProperties.contains(p) ? s.trim() : s;
	}

	protected String getMappingColumnName(EntityType<GenericEntity> importTargetType, ModelMdResolver cmdrContextBuilder, Property property) {
		String propertyName = property.getName();
		// Property column mapping
		String mappingColumnName = property.getName(); // default mapping is propertyName
		AbstractSpreadsheetColumnNameMapping columnMapping = cmdrContextBuilder.entityType(importTargetType).property(propertyName)
				.meta(AbstractSpreadsheetColumnNameMapping.T).exclusive();

		if (columnMapping != null) {
			if (columnMapping instanceof SpreadsheetColumnNameMapping) {
				mappingColumnName = ((SpreadsheetColumnNameMapping) columnMapping).getColumnName();
			} else if (columnMapping instanceof SpreadsheetColumnNameScriptMapping) {
				Script script = ((SpreadsheetColumnNameScriptMapping) columnMapping).getScript();

				CompiledScript compiledScript = compiledScriptCache.computeIfAbsent(script, this::compileScript);

				try {
					mappingColumnName = compiledScript.evaluate(Collections.singletonMap("propertyName", property.getName()));
				} catch (ScriptEvaluationException e) {
					throw new RuntimeException("Error while evaluating SpreadsheetColumnNameScript: " + script, e);
				}
			}
		}
		return mappingColumnName;
	}

	private CompiledScript compileScript(Script script) {
		ScriptEngine<Script> scriptEngine = (ScriptEngine<Script>) engines.get(script);

		try {
			return scriptEngine.compile(script);
		} catch (ScriptEvaluationException e) {
			throw new RuntimeException("Error while compiling SpreadsheetColumnNameScript: " + script, e);
		}
	}

	protected abstract EntityStreamer streamEntitiesFromSpreadsheet(SheetEntityStreamingContext<T> streamContext) throws Exception;

	public class ColumnInfo {
		public int cellIndex;
		public Property property;
		public String columnName;
	}

	public static class ParsedEntity {
		public GenericEntity entity;
		public Reason error;
		public int row;

		public ParsedEntity(int row, GenericEntity entity, Reason error) {
			this(row, entity);
			this.error = error;
		}

		public ParsedEntity(int row, GenericEntity entity) {
			this.row = row;
			this.entity = entity;
		}

	}

	public interface EntityStreamer extends AutoCloseable {
		@Override
		void close();
		ParsedEntity next(Supplier<GenericEntity> factory);
	}

	public class SheetEntityStreamingContextImpl<T extends ImportSpreadsheetRequest> extends SheetEntityStreamingContext<T> {
		private static final int MONTIOR_UPDATE_INTERVAL_IN_MS = 200;
		private EntityType<GenericEntity> importTargetType;
		private T spreadsheetImport;
		private Function<String, String> columnNameAdapter;
		private Map<String, Property> properties;
		ModelMdResolver cmdrContextBuilder;
		private Function<EntityType<?>, GenericEntity> entityFactory;
		private int rowCount = 0;
		private int totalRowCount = -1;
		private int entityCount = 0;
		private long lastStatusUpdate = -1;
		private Object statusMonitor = new Object();
		private Evaluator<ServiceRequest> requestEvaluator;
		private Map<Pair<Class<?>, Property>, TypeConverter<Object, Object>> cachedConverters = new ConcurrentHashMap<>();
		private Map<Pair<Class<?>, Property>, Function<Object, Object>> dedicatedConverters = new HashMap<>();
		private final BiFunction<Property, String, String> textAdapter;
		private Map<Integer, Reason> errors = new TreeMap<>();
		private Holder<Boolean> errorOverflow = new Holder<>(false);
		private Set<Object> createdEntityIds = ConcurrentHashMap.newKeySet();
		private BiConsumer<Integer, GenericEntity> transientEntityConsumer;
		private final Supplier<Predicate<GenericEntity>> filterSupplier;

		public SheetEntityStreamingContextImpl(EntityType<GenericEntity> importTargetType, T spreadsheetImport,
				Function<String, String> columnNameAdapter, Map<String, Property> properties, BiFunction<Property, String, String> textAdapter,
				ModelMdResolver cmdrContextBuilder, Evaluator<ServiceRequest> requestEvaluator) {
			super();
			this.importTargetType = importTargetType;
			this.spreadsheetImport = spreadsheetImport;
			this.columnNameAdapter = columnNameAdapter;
			this.properties = properties;
			this.cmdrContextBuilder = cmdrContextBuilder;
			this.requestEvaluator = requestEvaluator;
			this.textAdapter = textAdapter;

			SpreadsheetEntityFilterScript filterScript = cmdrContextBuilder.entityType(importTargetType).meta(SpreadsheetEntityFilterScript.T)
					.exclusive();

			if (filterScript != null) {
				Script script = filterScript.getScript();
				filterSupplier = () -> buildScriptedFilterSupplier(script);
			} else {
				filterSupplier = () -> e -> true;
			}

		}

		private Predicate<GenericEntity> buildScriptedFilterSupplier(Script script) {
			CompiledScript compiledScript = compileScript(script);
			return e -> runFilterScript(compiledScript, e);
		}

		private CompiledScript compileScript(Script script) {
			ScriptEngine<Script> scriptEngine = engines.get(script);
			CompiledScript compiledScript;
			try {
				compiledScript = scriptEngine.compile(script);
			} catch (ScriptEvaluationException e1) {
				throw new RuntimeException(e1);
			}
			return compiledScript;
		}

		private boolean runFilterScript(CompiledScript compiledScript, GenericEntity e) {
			try {
				return (boolean) compiledScript.evaluate(Collections.singletonMap("entity", e));
			} catch (ScriptEvaluationException e1) {
				throw new RuntimeException(e1);
			}
		}

		public Predicate<GenericEntity> buildEntityFilter() {
			return filterSupplier.get();
		}

		public void setTransientEntityConsumer(BiConsumer<Integer, GenericEntity> transientEntityConsumer) {
			this.transientEntityConsumer = transientEntityConsumer;
		}

		public void notifyEntity(GenericEntity entity, int row) {
			if (transientEntityConsumer != null)
				transientEntityConsumer.accept(row, entity);
		}

		public int getEntityCount() {
			return entityCount;
		}

		public void addCreatedEntityId(Object id) {
			createdEntityIds.add(id);
		}

		public void notifyError(int row, Reason error) {
			synchronized (errors) {
				if (errors.size() < MAX_ERROR_IN_REPORT)
					errors.put(row, error);
				else
					errorOverflow.accept(true);
			}
		}

		public <T> void registerDedicatedConverter(Property property, Class<T> clazz, Function<T, ?> converter) {
			dedicatedConverters.put(Pair.of(clazz, property), (Function<Object, Object>) converter);
		}

		public Collection<Reason> getErrors() {
			return errors.values();
		}

		public boolean hasErrorOverflow() {
			return errorOverflow.get();
		}

		public EntityType<GenericEntity> getImportTargetType() {
			return importTargetType;
		}

		public T getSpreadsheetImport() {
			return spreadsheetImport;
		}

		public Function<String, String> getColumnNameAdapter() {
			return columnNameAdapter;
		}

		public Map<String, Property> getProperties() {
			return properties;
		}

		public ModelMdResolver getCmdrContextBuilder() {
			return cmdrContextBuilder;
		}

		public GenericEntity create() {
			return entityFactory.apply(importTargetType);
		}

		public void updateStatus(boolean forcePush) {
			long t = System.currentTimeMillis();

			boolean sendNotification = forcePush || lastStatusUpdate == -1 ? true : t - lastStatusUpdate > MONTIOR_UPDATE_INTERVAL_IN_MS;

			lastStatusUpdate = t;

			if (sendNotification) {
				PushAddress pushAddress = request.getStatusMonitor();

				if (pushAddress != null) {
					PushRequest pushRequest = PushRequest.T.create();
					pushRequest.setClientIdPattern(pushAddress.getClientIdPattern());
					pushRequest.setSessionIdPattern(pushAddress.getSessionIdPattern());

					NotifyImportStatusMonitor notifyRequest = NotifyImportStatusMonitor.T.create();
					notifyRequest.setEntityCount(entityCount);
					notifyRequest.setTotalRowCount(totalRowCount);
					notifyRequest.setRowCount(rowCount);
					notifyRequest.setServiceId(pushAddress.getServiceId());

					pushRequest.setServiceRequest(notifyRequest);
					pushRequest.eval(requestEvaluator).get(null);
				}
			}
		}

		@Override
		public void notifyRowCount(int rowCount) {
			synchronized (statusMonitor) {
				this.rowCount = rowCount;
				updateStatus(false);
			}
		}

		@Override
		public void notifyTotalRowCount(int rowCount) {
			synchronized (statusMonitor) {
				this.totalRowCount = rowCount;
				updateStatus(false);
			}
		}

		public void increaseEntityCount(int amount) {
			synchronized (statusMonitor) {
				this.entityCount += amount;
				updateStatus(false);
			}
		}

		private TypeConverter<Object, Object> getConverter(Class<?> sourceClass, Property property) {
			return cachedConverters.computeIfAbsent(Pair.of(sourceClass, property), this::buildConverter);
		}

		private TypeConverter<Object, Object> buildConverter(Pair<Class<?>, Property> key) {
			Class<?> sourceClass = key.first();
			Property property = key.second();

			Class<?> targetClass = property.getType().getJavaType();

			Function<Object, Object> dedicatedConverter = dedicatedConverters.get(key);
			if (dedicatedConverter != null)
				return new BasicTypeConverter<Object, Object>((Class<Object>) sourceClass, (Class<Object>) targetClass, dedicatedConverter);

			if (sourceClass == targetClass)
				return new BasicTypeConverter<Object, Object>((Class<Object>) sourceClass, (Class<Object>) targetClass, Function.identity());

			GenericModelType type = property.getType();

			switch (type.getTypeCode()) {
				case enumType:
					EnumType enumType = (EnumType) type;
					return (TypeConverter<Object, Object>) (TypeConverter<?, ?>) new EnumTypeConverter(enumType);
				default:
					for (TypeConverter<?, ?> converter : converters) {
						if (targetClass == converter.getToClass() && converter.getFromClass().isAssignableFrom(sourceClass)) {
							return (TypeConverter<Object, Object>) converter;
						}
					}
			}

			return new FailingTypeConverter<Object, Object>((Class<Object>) sourceClass, (Class<Object>) targetClass,
					Reasons.build(ConversionFailed.T) //
							.text("Conversion from [" + sourceClass + "] to [" + targetClass + "] not supported.").toReason());
		}

		@Override
		public Maybe<Object> convert(Object value, int row, int cell, String column, Property targetProperty) {
			if (value == null) {
				return Maybe.complete(targetProperty.getDefaultRawValue());
			}

			if (value.getClass() == String.class) {
				value = textAdapter.apply(targetProperty, (String) value);
			}

			GenericModelType propertyType = targetProperty.getType();
			Class<?> sourceClass = value.getClass();

			try {
				Maybe<Object> potential = getConverter(sourceClass, targetProperty).convert(value);

				if (potential.isUnsatisfied())
					return Reasons.build(ConversionFailed.T)//
							.text("Could not convert cell value [" + value + "] from column [" + column + "] for target property ["
									+ targetProperty.getName() + "]") //
							.cause(potential.whyUnsatisfied()).toMaybe();

				return potential;
			} catch (RuntimeException e) {
				Class<?> targetClass = propertyType.getJavaType();

				return Reasons.build(ConversionFailed.T)//
						.text("Could not convert cell value [" + value + "] from column [" + column + "] for target property ["
								+ targetProperty.getName() + "]") //
						.cause(InternalError.from(e)).toMaybe();
			}
		}

		protected Object decodeEntityValue(Object value, int row, int cell, String column, Property targetProperty,
				EntityType<GenericEntity> entityType) {
			@SuppressWarnings("unchecked")
			EntityType<GenericEntity> targetEntityType = (EntityType<GenericEntity>) targetProperty.getType();
			GenericEntity targetEntity = targetEntityType.create();

			SpreadsheetReferenceColumnMapping referenceMapping = cmdrContextBuilder.entityType(entityType).property(targetProperty.getName())
					.meta(SpreadsheetReferenceColumnMapping.T).exclusive();

			String columnName = null;
			String propertyName = null;
			if (referenceMapping != null) {
				GmProperty referenceProperty = referenceMapping.getReferenceProperty();
				if (referenceProperty != null) {
					propertyName = referenceProperty.getName();
				}
				// columnName = referenceMapping.getColumnName();
			}
			if (propertyName == null) {
				Map<String, Property> targetColumnPropertyMap = buildPropertyColumnMapping(targetEntityType, cmdrContextBuilder);
				if (columnName == null) {
					columnName = getMappingColumnName(entityType, cmdrContextBuilder, targetProperty);
				}
				Property mappedProperty = targetColumnPropertyMap.get(columnName);
				if (mappedProperty != null) {
					propertyName = mappedProperty.getName();
				}
			}

			if (propertyName != null) {
				Property targetEntityProperty = targetEntityType.getProperty(propertyName);
				targetEntityProperty.set(targetEntity, convert(value, row, cell, column, targetEntityProperty));
				return targetEntity;
			}

			logger.warn("No column mappings found for target entity.");
			return null;
		}
	}

}
