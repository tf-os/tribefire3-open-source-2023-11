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
package tribefire.cortex.manipulation.conversion.code;

import static com.braintribe.utils.SysPrint.spOut;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;

import tribefire.cortex.manipulation.conversion.beans.BeanRegistry;
import tribefire.cortex.manipulation.conversion.beans.BeansFinder_Managed;
import tribefire.cortex.manipulation.conversion.beans.BeansFinder_Root;
import tribefire.cortex.manipulation.conversion.beans.EntityBean;
import tribefire.cortex.manipulation.conversion.beans.ExistingBean;
import tribefire.cortex.manipulation.conversion.beans.NewBean;
import tribefire.cortex.sourcewriter.JavaSourceClass;
import tribefire.cortex.sourcewriter.JavaSourceWriter;

/**
 * @author peter.gazdik
 */
public class InitializerCodeWriter {

	public static InitializerSources writeInitializer(CodeWriterParams params) {
		return new InitializerCodeWriter(params).write();
	}

	private final CodeWriterParams params;

	private final List<AtomicManipulation> manipulations;

	private final JavaSourceClass spaceJsc;
	private final JavaSourceClass contractJsc;
	private final JavaSourceClass lookupContractJsc;
	private final JavaSourceWriter spaceWriter;
	private final JavaSourceWriter lookupWriter;

	private final InitializerWritingContext ctx;
	private final BeanRegistry beanRegistry;
	private final JscPool jscPool;

	private final InitializerSources sources = new InitializerSources();

	private Set<NewBean> rootBeans;

	public InitializerCodeWriter(CodeWriterParams params) {
		this.params = params;
		this.manipulations = normalizeAndInline(params.manipulation);

		this.spaceJsc = JavaSourceClass.build(params.initializerPackage + ".space", params.spacePrefix + "InitializerSpace").please();
		this.contractJsc = JavaSourceClass.build(params.initializerPackage + ".contract", params.spacePrefix + "InitializerContract")
				.isInterface(true).please();
		this.lookupContractJsc = JavaSourceClass.build(params.initializerPackage + ".contract", params.spacePrefix + "LookupContract")
				.isInterface(true).please();
		this.spaceWriter = new JavaSourceWriter(spaceJsc);
		this.lookupWriter = new JavaSourceWriter(lookupContractJsc);

		this.ctx = new InitializerWritingContext(spaceWriter, lookupWriter, params.jscPool);
		this.beanRegistry = new BeanRegistry(ctx);
		this.jscPool = ctx.jscPool;
	}

	private static List<AtomicManipulation> normalizeAndInline(Manipulation manipulation) {
		return Normalizer.normalizeGlobal(manipulation).inline();
	}

	// ##############################################
	// ## . . . . . . . . Writing . . . . . . . . .##
	// ##############################################

	private InitializerSources write() {
		processManipulations();
		validateIndices();

		findRootBeans();

		writeSourceCode();

		return sources;
	}

	// ##############################################
	// ## . . . . . . . Processing . . . . . . . . ##
	// ##############################################

	private void processManipulations() {
		for (AtomicManipulation m : manipulations)
			process(m);
	}

	private void process(AtomicManipulation m) {
		switch (m.manipulationType()) {
			case ABSENTING:
			case ACQUIRE:
			case COMPOUND:
			case MANIFESTATION:
			case VOID:
				throw new IllegalArgumentException("Unexpected manipulation: " + m);

			case INSTANTIATION:
				processInstantiation((InstantiationManipulation) m);
				return;
			case CHANGE_VALUE:
				processChangeValue((ChangeValueManipulation) m);
				return;

			case ADD:
			case REMOVE:
			case CLEAR_COLLECTION:
				throw new UnsupportedOperationException("Do we really have these? M: " + m);

			case DELETE:
				throw new UnsupportedOperationException("Delete is not supported yet? M: " + m);

			default:
				return;
		}
	}

	private void processInstantiation(InstantiationManipulation m) {
		PreliminaryEntityReference ref = (PreliminaryEntityReference) m.getEntity();
		beanRegistry.createNewBeanFor(ref);
	}

	private void processChangeValue(ChangeValueManipulation m) {
		Object newValue = m.getNewValue();

		EntityProperty ep = (EntityProperty) m.getOwner();
		EntityReference ref = ep.getReference();
		String propertyName = ep.getPropertyName();

		// No point to write partition
		if (GenericEntity.partition.equals(propertyName))
			return;

		if (GenericEntity.globalId.equals(propertyName))
			processGlobalIdChange(ref, (String) newValue);
		else
			processNonIdChange(ref, propertyName, newValue);
	}

	private void processGlobalIdChange(EntityReference ref, String newGid) {
		// This can happen when entity was created by cloning, there is an unnecessary globalId=null assignment
		if (newGid == null)
			throw new IllegalStateException("This case should have been handled by the normalizer.");

		if (ref.referenceType() == EntityReferenceType.global) {
			String oldGid = (String) ref.getRefId();
			throw new IllegalArgumentException("Cannot assign globalId to an entity that already has one. Old: " + oldGid + ", new: " + newGid);
		}

		beanRegistry.onGidAssigned(ref, newGid);
	}

	private EntityBean<?> ownerBean;

	private void processNonIdChange(EntityReference ref, String propertyName, Object newValue) {
		ownerBean = beanRegistry.acquireBean(ref);
		Object beanValue = resolveScalarOrCollection(newValue);
		ownerBean.onChange(propertyName, beanValue);
	}

	// ##############################################
	// ## . . . . . Value Resolution . . . . . . . ##
	// ##############################################

	private Object resolveScalarOrCollection(Object value) {
		if (value instanceof Collection)
			return resolveCollection((Collection<?>) value, (value instanceof Set) ? newLinkedSet() : newList());

		if (value instanceof Map)
			return resolveMap((Map<?, ?>) value);

		return resolveScalar(value);
	}

	private Object resolveCollection(Collection<?> oldValues, Collection<Object> result) {
		for (Object oldValue : oldValues)
			result.add(resolveScalar(oldValue));

		return result;
	}

	private Map<Object, Object> resolveMap(Map<?, ?> localMap) {
		Map<Object, Object> result = newLinkedMap();

		for (Entry<?, ?> entry : localMap.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();

			result.put(resolveScalar(key), resolveScalar(value));
		}

		return result;
	}

	private Object resolveScalar(Object value) {
		if (value instanceof EntityReference)
			return resolveEntity((EntityReference) value);
		else if (value instanceof EnumReference)
			return resolveEnum((EnumReference) value);
		else
			return value;
	}

	private EntityBean<?> resolveEntity(EntityReference ref) {
		EntityBean<?> result = beanRegistry.acquireBean(ref);
		ownerBean.addDependencyOn(result);

		return result;
	}

	private Enum<?> resolveEnum(EnumReference ref) {
		EnumType enumType = GMF.getTypeReflection().findEnumType(ref.getTypeSignature());
		return enumType.getEnumValue(ref.getConstant());
	}

	// ##############################################
	// ## . . . . . . . Validation . . . . . . . . ##
	// ##############################################

	private void validateIndices() {
		checkEveryInstantaitionHasGlobalId();
	}

	private void checkEveryInstantaitionHasGlobalId() {
		for (EntityBean<?> bean : beanRegistry.newBeans)
			if (bean.globalId == null)
				throw new IllegalArgumentException("Bean has no globalId: " + bean);
	}

	// ##############################################
	// ## . . . . . Bean Preparation . . . . . . . ##
	// ##############################################

	/** For example for workbench access we get rid of everything other than Folders and WorkbenchPerspectives and their dependencies. */
	private void findRootBeans() {
		rootBeans = BeansFinder_Root.findRootBeans(beanRegistry);

		if (params.allowedRootTypeFilter != null)
			filterRootTypes();

		BeansFinder_Managed.markManagedBeans(rootBeans, beanRegistry);
	}

	private void filterRootTypes() {
		boolean removedSomething = rootBeans.removeIf(bean -> !params.allowedRootTypeFilter.test(bean));
		if (!removedSomething)
			return;

		Set<EntityBean<?>> skippedBeans = newSet(beanRegistry.newBeans);
		skippedBeans.removeAll(beanClosure(rootBeans));
		skippedBeans.removeAll(beanClosure(beanRegistry.existingBeans));

		beanRegistry.existingBeans.removeAll(skippedBeans);
		beanRegistry.newBeans.removeAll(skippedBeans);
	}

	private Set<EntityBean<?>> beanClosure(Collection<? extends EntityBean<?>> beans) {
		Set<EntityBean<?>> result = newSet();
		addAllToClosure(beans, result);
		return result;
	}

	private void addAllToClosure(Collection<? extends EntityBean<?>> beans, Set<EntityBean<?>> result) {
		for (EntityBean<?> bean : beans)
			if (result.add(bean))
				addAllToClosure(bean.deps, result);
	}

	// ##############################################
	// ## . . . . . . Code Generating . . . . . . .##
	// ##############################################

	private void writeSourceCode() {
		prepareSourceWriters();

		printInitializeMethod();
		printBeans();

		sources.initializerSpace = spaceWriter.write();
		sources.lookupContract = lookupWriter.write();
	}

	private void prepareSourceWriters() {
		lookupWriter.requireImport(ctx.jscPool.globalIdAnnoJsc);
		lookupWriter.requireImport(ctx.jscPool.instanceLookupAnnoJsc);
		lookupWriter.addClassAnnotation("@InstanceLookup(lookupOnly = true)");
		lookupWriter.addExtends(ctx.jscPool.wireSpaceJsc);

		spaceWriter.requireImport(ctx.jscPool.managedAnnoJsc);
		spaceWriter.requireImport(ctx.jscPool.importAnnoJsc);
		spaceWriter.requireImport(lookupWriter.sourceClass);

		spaceWriter.addClassAnnotation("@Managed");
		spaceWriter.addExtends(ctx.jscPool.abstractInitializerSpaceJsc);
		spaceWriter.addImplements(contractJsc);
		spaceWriter.addField(contractImportCode(lookupWriter.sourceClass));
	}

	private String contractImportCode(JavaSourceClass contractClass) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t@Import\n");
		sb.append("\tprivate ");
		sb.append(contractClass.simpleName);
		sb.append(" ");
		sb.append(ctx.lookupContractInstanceName);
		sb.append(";\n\n");

		return sb.toString();
	}

	private void printInitializeMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t@Override\n");
		sb.append("\tpublic void initialize() {\n");

		for (NewBean bean : rootBeans)
			sb.append("\t\t" + bean.beanName + "();\n");

		if (hasExistingBeansManipulations()) {
			sb.append("\n");
			for (ExistingBean bean : beanRegistry.existingBeans)
				bean.writeYourInitialization(sb);
		}

		sb.append("\t}\n\n");

		spaceWriter.addMethod(sb.toString());
	}

	private boolean hasExistingBeansManipulations() {
		return beanRegistry.existingBeans.stream() //
				.filter(eb -> !eb.changedValues.isEmpty()) //
				.findAny() //
				.isPresent();
	}

	private void printBeans() {
		spOut("Writing: " + params.spacePrefix);

		spOut("\tWriting " + beanRegistry.newBeans.size() + " new beans");
		for (NewBean bean : beanRegistry.newBeans)
			bean.writeYourDeclaration();

		writeInitializerHelpers();

		spOut("\tWriting " + beanRegistry.existingBeans.size() + " existing beans");
		for (ExistingBean bean : beanRegistry.existingBeans)
			bean.writeYourDeclaration();
	}

	private void writeInitializerHelpers() {
		if (ctx.newDateFunctionUsed)
			writeNewDateFunction();
	}

	private void writeNewDateFunction() {
		String dateName = ctx.typeNameInWriter(jscPool.dateJsc, spaceWriter);
		String calendarName = ctx.typeNameInWriter(jscPool.calendarJsc, spaceWriter);
		String timeZoneName = ctx.typeNameInWriter(jscPool.timeZoneJsc, spaceWriter);

		StringBuilder sb = new StringBuilder();
		sb.append("\tprivate " + dateName + " newGmtDate(int year, int month, int day, int hours, int minutes, int seconds, int millis) {\n");
		sb.append("\t\t" + calendarName + " calendar = " + calendarName + ".getInstance();\n");
		sb.append("\t\tcalendar.clear();\n");
		sb.append("\t\tcalendar.setTimeZone(" + timeZoneName + ".getTimeZone(\"GMT\"));\n");
		sb.append("\t\tcalendar.set(" + calendarName + ".YEAR, year);\n");
		sb.append("\t\tcalendar.set(" + calendarName + ".MONTH, month);\n");
		sb.append("\t\tcalendar.set(" + calendarName + ".DAY_OF_MONTH, day);\n");
		sb.append("\t\tcalendar.set(" + calendarName + ".HOUR_OF_DAY, hours);\n");
		sb.append("\t\tcalendar.set(" + calendarName + ".MINUTE, minutes);\n");
		sb.append("\t\tcalendar.set(" + calendarName + ".SECOND, seconds);\n");
		sb.append("\t\tcalendar.set(" + calendarName + ".MILLISECOND, millis);\n");
		sb.append("\t\treturn calendar.getTime();\n");
		sb.append("\t}\n\n");

		ctx.spaceWriter.addMethod(sb.toString());
	}

}
