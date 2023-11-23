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
package tribefire.cortex.manipulation.conversion.beans;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.utils.StringTools;

import tribefire.cortex.manipulation.conversion.code.InitializerWritingContext;
import tribefire.cortex.manipulation.conversion.code.JscPool;
import tribefire.cortex.sourcewriter.JavaSourceClass;
import tribefire.cortex.sourcewriter.JavaSourceWriter;

public abstract class EntityBean<R extends EntityReference> {

	protected final InitializerWritingContext ctx;
	protected final JscPool jscPool;

	public final R ref;
	public final JavaSourceClass jsc;
	public final String beanName;

	public final Map<String, Object> changedValues = newTreeMap();

	public String globalId;

	protected StringBuilder sb;

	public final Set<EntityBean<?>> deps = newLinkedSet();

	public EntityBean(BeanRegistry beanRegistry, R ref, String globalId) {
		this.ctx = beanRegistry.ctx;
		this.jscPool = beanRegistry.ctx.jscPool;
		this.ref = ref;
		this.globalId = globalId;
		this.jsc = jscPool.acquireJsc(ref);
		this.beanName = beanRegistry.resolveBeanName(ref);
	}

	public void addDependencyOn(EntityBean<?> bean) {
		deps.add(bean);
	}

	public void onChange(String propertyName, Object beanValue) {
		changedValues.put(propertyName, beanValue);
	}

	/** Returns either the simple name or the fully-qualified name (if import failed) of this bean in given */
	protected String typeNameIn(JavaSourceWriter writer) {
		return ctx.typeNameInWriter(jsc, writer);
	}

	protected String typeNameInWriter(JavaSourceClass jsc, JavaSourceWriter writer) {
		return ctx.typeNameInWriter(jsc, writer);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " for ref: " + ref;
	}

	public abstract void writeYourDeclaration();

	// Helpers

	protected void writePropertyChanges() {
		for (Entry<String, Object> e : changedValues.entrySet())
			writePropertyChange(e.getKey(), e.getValue());
	}

	private void writePropertyChange(String name, Object value) {
		// No reason to write nulls
		if (isDefaultValue(value))
			return;

		sb.append("\t\t");
		sb.append(beanInstanceForSetterInvocation());
		sb.append(".set");
		sb.append(StringTools.capitalize(name));
		sb.append("(");
		writeValue(value);
		sb.append(");\n");
	}

	/** Returns the instance on which setters are called. Either "bean" (for new beans) or "myLookupContract" (for existing beans). */
	protected abstract String beanInstanceForSetterInvocation();

	private final Boolean DEFAULT_BOOLEAN = Boolean.FALSE;
	private final Integer DEFAULT_INT = 0;
	private final Long DEFAULT_LONG = 0l;
	private final Float DEFAULT_FLOAT = 0f;
	private final Double DEFAULT_DOUBLE = 0d;

	private boolean isDefaultValue(Object value) {
		return value == null || //
				emptyList().equals(value) || //
				emptySet().equals(value) || //
				emptyMap().equals(value) || //
				DEFAULT_BOOLEAN.equals(value) || //
				DEFAULT_INT.equals(value) || //
				DEFAULT_LONG.equals(value) || //
				DEFAULT_FLOAT.equals(value) || //
				DEFAULT_DOUBLE.equals(value) //
		;
	}

	private void writeValue(Object value) {
		if (value instanceof Collection)
			writeCollection((Collection<?>) value);
		else if (value instanceof Map)
			writeMap((Map<?, ?>) value);
		else
			writeScalar(value);
	}

	private void writeMap(Map<?, ?> map) {
		String maps = typeNameInWriter(jscPool.mapsJsc, ctx.spaceWriter);
		sb.append(maps);
		sb.append(".map(");

		Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<?, ?> e = it.next();
			sb.append(maps);
			sb.append(".entry(");
			writeScalar(e.getKey());
			sb.append(", ");
			writeScalar(e.getValue());
			sb.append(")");

			if (it.hasNext())
				sb.append(", ");
		}

		sb.append(")");

	}

	private void writeCollection(Collection<?> collection) {
		if (collection instanceof List) {
			sb.append(typeNameInWriter(jscPool.listsJsc, ctx.spaceWriter));
			sb.append(".list(");
		} else {
			sb.append(typeNameInWriter(jscPool.setsJsc, ctx.spaceWriter));
			sb.append(".set(");
		}

		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
			writeScalar(it.next());

			if (it.hasNext())
				sb.append(", ");
		}

		sb.append(")");
	}

	private void writeScalar(Object value) {
		if (value == null)
			sb.append("null");
		else if (value instanceof NewBean)
			writeNewBean((NewBean) value);
		else if (value instanceof ExistingBean)
			writeExistingBean((ExistingBean) value);
		else if (value instanceof Enum)
			writeEnum((Enum<?>) value);
		else if (value instanceof Boolean)
			writeBoolean((Boolean) value);
		else if (value instanceof String)
			writeString((String) value);
		else if (value instanceof Integer)
			writeInteger((Integer) value);
		else if (value instanceof Long)
			writeLong((Long) value);
		else if (value instanceof Float)
			writeFloat((Float) value);
		else if (value instanceof Double)
			writeDouble((Double) value);
		else if (value instanceof Date)
			writeDate((Date) value);
		else if (value instanceof BigDecimal)
			writeBigDecimal((BigDecimal) value);
		else
			throw new UnsupportedOperationException("Cannot write [" + value + "] of type: " + value.getClass().getName());
	}

	private void writeNewBean(NewBean bean) {
		sb.append(bean.beanName);
		sb.append("()");
	}

	private void writeExistingBean(ExistingBean value) {
		throw new UnsupportedOperationException("Method 'BeanRegistry.NewBean.writeExistingBean' is not implemented yet! Value: " + value);
	}

	private void writeEnum(Enum<?> value) {
		JavaSourceClass jsc = jscPool.acquireJsc(value);
		String typeName = typeNameInWriter(jsc, ctx.spaceWriter);

		sb.append(typeName);
		sb.append(".");
		sb.append(value.name());
	}

	private void writeBoolean(Boolean value) {
		sb.append("Boolean.");
		sb.append(value.toString().toUpperCase());
	}

	private void writeString(String value) {
		sb.append("\"");
		sb.append(escapeString(value));
		sb.append("\"");
	}

	private String escapeString(String s) {
		return s.replace("\\", "\\\\") //
				.replace("\n", "\\n") //
				.replace("\t", "\\t") //
				.replace("\"", "\\\"");
	}

	private void writeInteger(Integer value) {
		sb.append(value);
	}

	private void writeLong(Long value) {
		sb.append(value);
		sb.append("l");
	}

	private void writeFloat(Float value) {
		sb.append(value);
		sb.append("f");
	}

	private void writeDouble(Double value) {
		sb.append(value);
		sb.append("d");
	}

	private void writeDate(Date value) {
		ctx.newDateFunctionUsed = true;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(value);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));

		sb.append("newGmtDate(");
		sb.append(calendar.get(Calendar.YEAR));
		sb.append(", ");
		sb.append(calendar.get(Calendar.MONTH));
		sb.append(", ");
		sb.append(calendar.get(Calendar.DAY_OF_MONTH));
		sb.append(", ");
		sb.append(calendar.get(Calendar.HOUR_OF_DAY));
		sb.append(", ");
		sb.append(calendar.get(Calendar.MINUTE));
		sb.append(", ");
		sb.append(calendar.get(Calendar.SECOND));
		sb.append(", ");
		sb.append(calendar.get(Calendar.MILLISECOND));

		sb.append(')');
	}

	private void writeBigDecimal(BigDecimal value) {
		sb.append("new java.math.BigDecimal(\"");
		sb.append(value);
		sb.append("\")");
	}

}