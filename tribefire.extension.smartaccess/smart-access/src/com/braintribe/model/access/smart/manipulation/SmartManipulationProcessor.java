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
package com.braintribe.model.access.smart.manipulation;

import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.add;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.changeValue;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.clear;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.delete;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.instantiationManipulation;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.owner;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.remove;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.SmartAccess;
import com.braintribe.model.access.smart.manipulation.adapt.smart2delegate.CompositeIkpaHandler;
import com.braintribe.model.access.smart.manipulation.adapt.smart2delegate.CompositeKpaHandler;
import com.braintribe.model.access.smart.manipulation.adapt.smart2delegate.IkpaHandler;
import com.braintribe.model.access.smart.manipulation.adapt.smart2delegate.LpaEntityHandler;
import com.braintribe.model.access.smart.manipulation.adapt.smart2delegate.LpaHandler;
import com.braintribe.model.access.smart.manipulation.adapt.smart2delegate.OlpaHandler;
import com.braintribe.model.access.smart.manipulation.adapt.smart2delegate.Smart2DelegateHandler;
import com.braintribe.model.access.smart.manipulation.adapt.smart2delegate.StandardHandler;
import com.braintribe.model.access.smart.manipulation.tools.AccessResolver;
import com.braintribe.model.access.smart.manipulation.tools.PropertyValueResolver;
import com.braintribe.model.access.smart.manipulation.tools.ReferenceManager;
import com.braintribe.model.access.smart.manipulation.tools.SmartManipulationValidator;
import com.braintribe.model.access.smart.manipulation.tools.ValueConverter;
import com.braintribe.model.access.smart.manipulation.tools.ValueConverter.DelegateRefToEmResolver;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.StaticModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchy;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EmUseCase;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;
import com.braintribe.model.record.ListRecord;

/**
 * TODO DOCUMENTATION
 * 
 * NOTE regarding unfinished business:
 * 
 * When we are editing {@link KeyPropertyAssignment} (KPA) properties as well as relevant keyProperties in one transaction (
 * {@link ManipulationRequest}), it is a little messy now. If you do a change-value of KPA property first, and then change the corresponding key
 * property, the generated delegate manipulation corresponding to the key property assignment would use the key property value. Also, if you change
 * the key-property value, then change KPA property, then change the key-property value again, the result is actually dependent on whether we
 * normalize the manipulation, or not. If we do, the first key-property change is thrown away, and the oldest key-property value is used. Without
 * normalizer, we would use the first changed value, which is not valid at all.
 * 
 * First, we should always have normalizer, just like any other manipulation processing tools. Second, we should use final values of key properties,
 * as they will be stored after this transaction is completed (that is most easily understandable for the user).
 * 
 * @author peter.gazdik
 * @author dirk.scheffler
 */
public class SmartManipulationProcessor {

	protected final SmartAccess smartAccess;
	protected final Map<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping;

	protected final ModelExpert modelExpert;
	protected final AccessResolver accessResolver;
	protected final ReferenceManager referenceManager;
	protected final PropertyValueResolver propertyValueResolver;
	protected final ValueConverter valueConverter;
	protected final SmartManipulationValidator manipulationValidator;

	private final SmartManipulationContextVariables $ = new SmartManipulationContextVariables();

	// TODO optimize not to instantiate all of them on startup, but lazily
	private final StandardHandler standardPmh;
	private final CompositeKpaHandler compositeKpaPmh;
	private final IkpaHandler ikpaPmh;
	private final CompositeIkpaHandler compositeIkpaPmh;
	private final LpaHandler lpaPmh;
	private final LpaEntityHandler lpaEntityPmh;
	private final OlpaHandler olpaPmh;

	public static final EmUseCase USE_CASE = null;

	private static final Logger log = Logger.getLogger(SmartManipulationProcessor.class);

	public SmartManipulationProcessor(SmartAccess smartAccess, com.braintribe.model.accessdeployment.smart.SmartAccess smartDenotation,
			Map<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping, CmdResolver cmdResolver,
			StaticModelExpert staticModelExpert, Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> experts) {

		this.smartAccess = smartAccess;
		this.accessMapping = accessMapping;

		this.modelExpert = new ModelExpert(cmdResolver, staticModelExpert, smartDenotation, accessMapping);

		this.accessResolver = new AccessResolver(modelExpert);
		this.referenceManager = new ReferenceManager(this, modelExpert, accessResolver);
		this.propertyValueResolver = new PropertyValueResolver(this, smartAccess, modelExpert, accessResolver);
		this.valueConverter = new ValueConverter(this, experts);
		this.manipulationValidator = new SmartManipulationValidator(this);

		this.standardPmh = new StandardHandler(this);
		this.compositeKpaPmh = new CompositeKpaHandler(this, this.standardPmh);
		this.ikpaPmh = new IkpaHandler(this);
		this.compositeIkpaPmh = new CompositeIkpaHandler(this.ikpaPmh);
		this.lpaPmh = new LpaHandler(this);
		this.lpaEntityPmh = new LpaEntityHandler(this);
		this.olpaPmh = new OlpaHandler(this);
	}

	public ModelExpert modelExpert() {
		return modelExpert;
	}

	public AccessResolver accessResolver() {
		return accessResolver;
	}

	public ReferenceManager referenceManager() {
		return referenceManager;
	}

	public PropertyValueResolver propertyValueResolver() {
		return propertyValueResolver;
	}

	public ValueConverter valueConverter() {
		return valueConverter;
	}

	public SmartManipulationContextVariables context() {
		return $;
	}

	public ManipulationResponse process(Manipulation smartManipulation) {
		initializeAccessResolver(smartManipulation);
		convertToDelegateManipulation(smartManipulation);

		applyDelegateManipulations();
		return processInducedManipulations();
	}

	private void initializeAccessResolver(Manipulation smartManipulation) {
		accessResolver.initialize(smartManipulation);
	}

	// ##########################################################
	// ## . . Convert Smart Manipulations to Delegate Ones . . ##
	// ##########################################################

	private void convertToDelegateManipulation(Manipulation smartManipulation) throws ModelAccessException {
		if (smartManipulation instanceof PropertyManipulation) {
			convertToDelegate((PropertyManipulation) smartManipulation);
			return;
		}

		switch (smartManipulation.manipulationType()) {
			case COMPOUND:
				convertToDelegate((CompoundManipulation) smartManipulation);
				break;
			case INSTANTIATION:
				convertToDelegate((InstantiationManipulation) smartManipulation);
				break;
			case DELETE:
				convertToDelegate((DeleteManipulation) smartManipulation);
				break;
			default:
				throw new UnsupportedOperationException("manipulation of type " + smartManipulation.manipulationType() + " is unsupported");
		}
	}

	private void convertToDelegate(PropertyManipulation smartPropertyManipulation) throws ModelAccessException {
		Smart2DelegateHandler<?> currentPmh = loadOwnerForSmart(smartPropertyManipulation);

		convertPropertyManipulationToDelegate(smartPropertyManipulation, currentPmh);
	}

	private void convertPropertyManipulationToDelegate(PropertyManipulation smartManipulation, Smart2DelegateHandler<?> handler)
			throws ModelAccessException {
		switch (smartManipulation.manipulationType()) {
			case CHANGE_VALUE:
				handler.convertToDelegate((ChangeValueManipulation) smartManipulation);
				break;

			case ADD:
				handler.convertToDelegate((AddManipulation) smartManipulation);
				break;
			case REMOVE:
				handler.convertToDelegate((RemoveManipulation) smartManipulation);
				break;
			case CLEAR_COLLECTION:
				handler.convertToDelegate((ClearCollectionManipulation) smartManipulation);
				break;

			default:
				throw new UnsupportedOperationException("manipulation of type " + smartManipulation.manipulationType() + " is unsupported");
		}
	}

	private void convertToDelegate(CompoundManipulation manipulation) throws ModelAccessException {
		for (Manipulation m : manipulation.getCompoundManipulationList())
			convertToDelegateManipulation(m);
	}

	private void convertToDelegate(InstantiationManipulation manipulation) {
		loadSmartType((EntityReference) manipulation.getEntity());

		List<Manipulation> manipulations = acquireList($.delegateManipulations, $.currentEntityMapping.getAccess());
		manipulations.add(instantiationManipulation($.currentDelegateReference));

		if ($.currentEntityMapping.isPolymorphicAssignment())
			setDiscriminatorValues(manipulations);
	}

	private void setDiscriminatorValues(List<Manipulation> manipulations) {
		DiscriminatedHierarchy dh = $.currentEntityMapping.getDiscriminatedHierarchy();
		Object discriminator = dh.getDiscriminatorForSmartSignature($.currentSmartType.getTypeSignature());

		if (dh.isSingleDiscriminatorProperty()) {
			dh.getSingleDiscriminatorProperty();
			manipulations.add(changeValueManipulation(dh.getSingleDiscriminatorProperty(), discriminator));

		} else {
			List<GmProperty> discProps = dh.getCompositeDiscriminatorProperties();
			List<Object> discValues = ((ListRecord) discriminator).getValues();
			int count = discProps.size();
			for (int i = 0; i < count; i++) {
				GmProperty discProperty = discProps.get(i);
				Object discValue = discValues.get(i);

				manipulations.add(changeValueManipulation(discProperty, discValue));
			}
		}
	}

	private Manipulation changeValueManipulation(GmProperty property, Object value) {
		return changeValue(owner($.currentDelegateReference, property), value);
	}

	private void convertToDelegate(DeleteManipulation manipulation) {
		PersistentEntityReference persistRef = (PersistentEntityReference) manipulation.getEntity();
		loadSmartType(persistRef);
		DeleteManipulation deleteManipulation = delete((PersistentEntityReference) $.currentDelegateReference, manipulation.getDeleteMode());

		acquireList($.delegateManipulations, $.currentEntityMapping.getAccess()).add(deleteManipulation);
	}

	// #
	// # Helpers
	// #

	private Smart2DelegateHandler<?> loadOwnerForSmart(PropertyManipulation smartManipulation) {
		$.currentSmartOwner = (EntityProperty) smartManipulation.getOwner();
		loadSmartType($.currentSmartOwner.getReference());

		loadPropertyInfoForSmart();

		PropertyAssignment pa = resolvePropertyAssignment($.currentSmartType, $.currentAccess, $.currentSmartOwner.getPropertyName());

		manipulationValidator.validate(smartManipulation, pa);

		if (pa instanceof InverseKeyPropertyAssignment) {
			return prepareHandler(ikpaPmh, (InverseKeyPropertyAssignment) pa);
		}

		if (pa instanceof CompositeKeyPropertyAssignment) {
			return prepareHandler(compositeKpaPmh, (CompositeKeyPropertyAssignment) pa);
		}

		if (pa instanceof CompositeInverseKeyPropertyAssignment) {
			return prepareHandler(compositeIkpaPmh, (CompositeInverseKeyPropertyAssignment) pa);
		}

		if (pa instanceof OrderedLinkPropertyAssignment) {
			return prepareHandler(olpaPmh, (OrderedLinkPropertyAssignment) pa);
		}

		if (pa instanceof LinkPropertyAssignment) {
			GmProperty property = modelExpert.getGmProperty($.currentSmartType, $.currentSmartOwner.getPropertyName());

			if (property.getType().isGmEntity()) {
				return prepareHandler(lpaEntityPmh, (LinkPropertyAssignment) pa);

			} else {
				return prepareHandler(lpaPmh, (LinkPropertyAssignment) pa);
			}
		}

		/* NOTE that KeyPropertyAssignment is handled as standard one with special value conversion - ResolveDelegateValueConversion (see
		 * getConverstion method below) */
		return prepareHandler(standardPmh, pa);
	}

	private static <P extends PropertyAssignment, H extends Smart2DelegateHandler<P>> H prepareHandler(H handler, P pa) {
		handler.loadAssignment(pa);
		return handler;
	}

	private PropertyAssignment resolvePropertyAssignment(GmEntityType smartType, IncrementalAccess access, String smartProperty) {
		PropertyAssignment result = modelExpert.resolvePropertyAssignmentIfPossible(smartType, access, smartProperty, USE_CASE);

		if (result == null)
			throw new SmartAccessException("No mapping found for property '" + smartProperty + "' of entity: " + smartType.getTypeSignature());

		return result;
	}

	private void loadSmartType(EntityReference smartReference) {
		GmEntityType smartType = modelExpert.resolveSmartEntityType(smartReference.getTypeSignature());
		IncrementalAccess access = accessResolver.resolveAccess(smartReference);

		if ($.currentSmartType != smartType || $.currentAccess != access) {
			$.currentSmartType = smartType;
			$.currentAccess = access;
			$.currentEntityMapping = modelExpert.resolveEntityMapping($.currentSmartType, $.currentAccess, USE_CASE);
		}

		$.currentSmartReference = smartReference;
		$.currentDelegateReference = referenceManager.acquireDelegateReference($.currentSmartReference, $.currentEntityMapping);
	}

	private void loadPropertyInfoForSmart() {
		$.currentSmartGmProperty = modelExpert.getGmProperty($.currentSmartType, $.currentSmartOwner.getPropertyName());
		$.currentSmartReferencedEntityType = getEntityTypeIfPossible($.currentSmartGmProperty.getType());
		$.currentSmartPropertyReferencesUnmappedType = $.currentSmartReferencedEntityType != null
				&& (modelExpert.resolveEntityMappingsIfPossible($.currentSmartReferencedEntityType, USE_CASE) == null);
	}

	private GmEntityType getEntityTypeIfPossible(GmType gmType) {
		switch (gmType.typeKind()) {
			case ENTITY:
				return (GmEntityType) gmType;
			case LIST:
			case SET:
				return getEntityTypeIfPossible(((GmLinearCollectionType) gmType).getElementType());
			case MAP:
				return getEntityTypeIfPossible(((GmMapType) gmType).getValueType());
			default:
				return null;
		}
	}

	public <T> T conv2Del(T value, SmartConversion conversion) {
		return conv2Del(value, conversion, false);
	}

	public <T> T conv2Del(T value, SmartConversion conversion, boolean convertMapKeys) {
		return (T) valueConverter.convertToDelegate(value, conversion, convertMapKeys);
	}

	// ##########################################################
	// ## . . . . . . Apply Delegate Manipulations . . . . . . ##
	// ##########################################################

	private void applyDelegateManipulations() throws ModelAccessException {
		for (Entry<IncrementalAccess, List<Manipulation>> manipulationEntry : $.delegateManipulations.entrySet()) {
			try {
				delegateManipulation(manipulationEntry.getKey(), manipulationEntry.getValue());

			} catch (Exception e) {
				handleDelegateManipulationException(manipulationEntry.getKey(), e);
			}
		}
	}

	private void handleDelegateManipulationException(IncrementalAccess access, Exception e) throws ModelAccessException {
		Map<IncrementalAccess, String> rollbackErrors = rollback();

		String additionalMessage = null;

		if (rollbackErrors.isEmpty()) {
			additionalMessage = "Manipulations for all other accesses were either skipped or rolled-back.";
		} else {
			additionalMessage = "Rollback not performed correctly. Following problems occured: " + rollbackErrors;
		}

		throw new ModelAccessException("Exception was thrown while applied delegate manipulations for access '" + access + "'. " + additionalMessage,
				e);
	}

	private Map<IncrementalAccess, String> rollback() {
		Map<IncrementalAccess, String> exceptions = newMap();

		for (Entry<IncrementalAccess, ManipulationResponse> entry : $.delegateResponses.entrySet()) {
			IncrementalAccess access = entry.getKey();
			Manipulation rollbackManipulation = entry.getValue().getRollbackManipulation();

			if (rollbackManipulation == null) {
				exceptions.put(access, "No rollback manipulation sent as a response from access: " + access.getExternalId());
				continue;
			}

			try {
				applyManipulation(getAccessImpl(access), rollbackManipulation);

			} catch (Exception e) {
				exceptions.put(access, e.toString());
			}
		}

		return exceptions;
	}

	private void delegateManipulation(IncrementalAccess access, List<Manipulation> manipulations) throws ModelAccessException {
		CompoundManipulation cm = ManipulationBuilder.compound(manipulations);

		ManipulationResponse response = applyManipulation(getAccessImpl(access), cm);
		$.delegateResponses.put(access, response);
	}

	private static ManipulationResponse applyManipulation(com.braintribe.model.access.IncrementalAccess access, Manipulation manipulation)
			throws ModelAccessException {

		return access.applyManipulation(wrapRequest(manipulation));
	}

	private static ManipulationRequest wrapRequest(Manipulation manipulation) {
		ManipulationRequest manipulationRequest = ManipulationRequest.T.create();
		manipulationRequest.setManipulation(manipulation);
		return manipulationRequest;
	}

	private static ManipulationResponse wrapResponse(Manipulation manipulation) {
		ManipulationResponse manipulationResponse = ManipulationResponse.T.create();
		manipulationResponse.setInducedManipulation(manipulation);
		return manipulationResponse;
	}

	// ##########################################################
	// ## . . . . . Processing Induced Manipulations . . . . . ##
	// ##########################################################

	private ManipulationResponse processInducedManipulations() {
		for (Entry<IncrementalAccess, ManipulationResponse> entry : $.delegateResponses.entrySet()) {
			Manipulation delegateInducedManipulation = entry.getValue().getInducedManipulation();

			if (delegateInducedManipulation != null) {
				IncrementalAccess access = entry.getKey();
				convertToSmart(delegateInducedManipulation, access);
			}
		}

		switch ($.smartInducedManipulations.size()) {
			case 0:
				return wrapResponse(null);
			case 1:
				return wrapResponse($.smartInducedManipulations.get(0));
			default:
				return wrapResponse(ManipulationBuilder.compound($.smartInducedManipulations));
		}
	}

	private void convertToSmart(Manipulation manipulation, IncrementalAccess access) {
		if (manipulation instanceof PropertyManipulation) {
			convertToSmart((PropertyManipulation) manipulation, access);
			return;
		}

		switch (manipulation.manipulationType()) {
			case COMPOUND:
				convertToSmart((CompoundManipulation) manipulation, access);
				break;

			case DELETE:
				convertToSmart((DeleteManipulation) manipulation, access);
				break;

			case INSTANTIATION:
				// this is not expected, but even if it happens, we simply ignore that
				break;

			default:
				throw new UnsupportedOperationException("manipulation of type " + manipulation.manipulationType() + " is unsupported");
		}
	}

	private void convertToSmart(CompoundManipulation manipulation, IncrementalAccess access) {
		List<Manipulation> originalList = manipulation.getCompoundManipulationList();

		for (Manipulation m : originalList)
			convertToSmart(m, access);
	}

	private void convertToSmart(DeleteManipulation manipulation, IncrementalAccess access) {
		PersistentEntityReference delegateReference = (PersistentEntityReference) manipulation.getEntity();

		$.currentEntityMapping = findEntityMapping(access, delegateReference);
		if ($.currentEntityMapping == null)
			return;

		PersistentEntityReference smartReference = referenceManager.acquireSmartReference(delegateReference, $.currentEntityMapping);

		DeleteManipulation dm = delete(smartReference, manipulation.getDeleteMode());
		$.smartInducedManipulations.add(dm);
	}

	private void convertToSmart(PropertyManipulation manipulation, IncrementalAccess access) {
		try {
			tryConvertToSmart(manipulation, access);
		} catch (Exception e) {
			Owner o = manipulation.getOwner();
			log.error(logPrefix() + "Error while processing induced manipulations from access: " + access.getExternalId() + ". Problematic property: "
					+ o.ownerEntityType() + "." + o.getPropertyName(), e);
		}
	}

	private void tryConvertToSmart(PropertyManipulation manipulation, IncrementalAccess access) {
		if (!loadOwnerForDelegate(manipulation, access)) {
			/* This branch means the delegate property is not mapped. So we only have to consider it in case it is an id property, to update our
			 * reference information. */
			if (manipulation.manipulationType() == ManipulationType.CHANGE_VALUE)
				handlePossibleUnmappedIdChange((ChangeValueManipulation) manipulation, access);

			return;
		}

		switch (manipulation.manipulationType()) {
			case ADD:
				convertToSmart((AddManipulation) manipulation);
				break;

			case REMOVE:
				convertToSmart((RemoveManipulation) manipulation);
				break;

			case CLEAR_COLLECTION:
				convertToSmart((ClearCollectionManipulation) manipulation);
				break;

			case CHANGE_VALUE:
				convertToSmart((ChangeValueManipulation) manipulation);
				break;

			default:
				throw new UnsupportedOperationException("manipulation of type " + manipulation.manipulationType() + " is unsupported");
		}
	}

	private void handlePossibleUnmappedIdChange(ChangeValueManipulation cvm, IncrementalAccess access) {
		EntityReference dgReference = $.currentDelegateOwner.getReference();
		GmEntityType dgEntityType = modelExpert.resolveEntityType(dgReference.getTypeSignature(), access);
		GmProperty dgProperty = modelExpert.getGmProperty(dgEntityType, $.currentDelegateOwner.getPropertyName());

		referenceManager.notifyChangeValueForUnmapped(dgReference, dgProperty, cvm.getNewValue());
	}

	/**
	 * @return true iff given property is mapped and we have thus initialized the property <tt>$.currentSmartOwner</tt>.
	 */
	private boolean loadOwnerForDelegate(PropertyManipulation delegateManipulation, IncrementalAccess access) {
		$.currentDelegateOwner = (EntityProperty) delegateManipulation.getOwner();

		$.currentEntityMapping = findEntityMapping(access, $.currentDelegateOwner.getReference());
		if ($.currentEntityMapping == null)
			return false;

		$.currentSmartType = $.currentEntityMapping.getSmartEntityType();
		$.currentAccess = $.currentEntityMapping.getAccess();

		GmProperty smartProperty = modelExpert.findSmartProperty($.currentSmartType, $.currentAccess, $.currentDelegateOwner.getPropertyName(),
				USE_CASE);
		if (smartProperty == null)
			return false;

		String smartPropertyName = smartProperty.getName();
		$.currentSmartReference = referenceManager.acquireSmartReference($.currentDelegateOwner.getReference(), $.currentEntityMapping);
		$.currentEpm = modelExpert.resolveEntityPropertyMapping($.currentSmartType, $.currentAccess, smartPropertyName);
		$.currentSmartOwner = owner($.currentSmartReference, smartPropertyName);

		return true;
	}

	private void convertToSmart(AddManipulation manipulation) {
		boolean convertKeys = $.currentEpm.getDelegatePropertyType() instanceof GmSetType;
		Map<Object, Object> ita = conv2Smart(manipulation.getItemsToAdd(), convertKeys);
		AddManipulation am = add($.currentSmartOwner, ita);
		$.smartInducedManipulations.add(am);
	}

	private void convertToSmart(RemoveManipulation manipulation) {
		boolean convertKeys = $.currentEpm.getDelegatePropertyType() instanceof GmSetType;
		Map<Object, Object> itr = conv2Smart(manipulation.getItemsToRemove(), convertKeys);
		RemoveManipulation am = remove($.currentSmartOwner, itr);
		$.smartInducedManipulations.add(am);
	}

	/**
	 * @param manipulation
	 *            is not used (we already got all the information loaded)
	 */
	private void convertToSmart(ClearCollectionManipulation manipulation) {
		$.smartInducedManipulations.add(clear($.currentSmartOwner));
	}

	private void convertToSmart(ChangeValueManipulation manipulation) {
		boolean convertKeys = $.currentEpm.getDelegatePropertyType() instanceof GmSetType;
		Object newSmartValue = conv2Smart(manipulation.getNewValue(), convertKeys);
		ChangeValueManipulation cvm = changeValue($.currentSmartOwner, newSmartValue);
		$.smartInducedManipulations.add(cvm);
	}

	private EntityMapping findEntityMapping(IncrementalAccess access, EntityReference delegateReference) {
		String delegateSignature = delegateReference.getTypeSignature();
		return modelExpert.resolveEntityMappingForDelegateTypeIfPossible(delegateSignature, access);
	}

	private <T> T conv2Smart(T value, boolean convertMapKeys) {
		return conv2Smart(value, $.currentEpm.getConversion(), this::delegateRefToEm, convertMapKeys);
	}

	public <T> T conv2Smart(T value, SmartConversion conversion, DelegateRefToEmResolver emResolver, boolean convertMapKeys) {
		return (T) valueConverter.convertToSmart(value, conversion, emResolver, convertMapKeys);
	}

	private EntityMapping delegateRefToEm(EntityReference ref) {
		EntityMapping em = modelExpert.resolveEntityMappingForDelegateTypeIfPossible(ref.getTypeSignature(), $.currentAccess);
		if (em == null)
			throw new RuntimeException("Don't know how to handle induced manipulation, smart type cannot be resolved for delegate reference: " + ref);

		return em;
	}

	// ##########################################################
	// ## . . . . . . . . Context Methods . . . . . . . . . . .##
	// ##########################################################

	/**
	 * For special use-case when we reference an unmapped type (whose sub-types are mapped) (which can only be done via key property (like
	 * {@link KeyPropertyAssignment}, {@link LinkPropertyAssignment})), our configured key property is actually be a smart property. This use-case can
	 * be recognized by the fact, that current smart property has no {@link EntityMapping}. In such case, we take the type of our actual value (which
	 * must be a type that is mapped) and find the delegate property corresponding to given (smart) property.
	 */
	public String findDelegatePropertyForKeyPropertyOfCurrentSmartType(String keyProperty, EntityReference actualKeyPropertyValueRef) {
		GmProperty gmProperty = modelExpert.getGmProperty($.currentSmartType, $.currentSmartOwner.getPropertyName());
		IncrementalAccess access = accessResolver.resolveAccess(actualKeyPropertyValueRef);

		/* This is the type of our property, which might be an unmapped entity - the keyProperty is taken with respect to this type */
		GmEntityType smartPropertyType = (GmEntityType) resolvePropertyEntityType(gmProperty.getType());
		GmEntityType actualPropertyValueType = modelExpert.resolveSmartEntityType(actualKeyPropertyValueRef.getTypeSignature());

		return modelExpert.findDelegatePropertyForKeyProperty(smartPropertyType, access, keyProperty, actualPropertyValueType);
	}

	private GmType resolvePropertyEntityType(GmType type) {
		if (type instanceof GmEntityType) {
			return type;

		} else if (type instanceof GmLinearCollectionType) {
			return ((GmLinearCollectionType) type).getElementType();

		} else if (type instanceof GmMapType) {
			return ((GmMapType) type).getValueType();
		}

		throw new IllegalArgumentException("Unexpected type, only entity or a collection of entities was expected. Type: " + type);
	}

	public com.braintribe.model.access.IncrementalAccess getAccessImpl(IncrementalAccess access) {
		return accessMapping.get(access);
	}

	private String logPrefix() {
		return "[" + smartAccess.getAccessId() + "] ";
	}

}
