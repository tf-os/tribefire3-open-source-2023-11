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
package model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.AbstractProperty;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GmtsEnhancedEntityStub;
import com.braintribe.model.processing.itw.synthesis.gm.JvmProperty;

/**
 * @author peter.gazdik
 */
public class Person_gm extends GmtsEnhancedEntityStub implements Person_aop, Person_weak {

	private Object name;
	private Object count;
	private String transientName;

	/**
	 * @see GmtsEnhancedEntityStub#GmtsEnhancedEntityStub()
	 */
	public Person_gm() {
		super();
	}

	public Person_gm(boolean initialized) {
		super(initialized);
	}

	@Override
	protected void initializePrimitiveFields() {
		this.count = 0L;
	}

	@Override
	public Object readName() {
		return name;
	}

	@Override
	public void writeName(Object value) {
		this.name = value;
	}

	@Override
	public Object readCount() {
		return count;
	}

	@Override
	public void writeCount(Object value) {
		this.count = value;
	}

	@Override
	public String getTransientName() {
		return transientName;
	}

	@Override
	public void setTransientName(String transientName) {
		this.transientName = transientName;
	}

	@Override
	public GenericModelType type() {
		return Person_EntityType.INSTANCE;
	}
}

class Person_Name_Property extends JvmProperty {

	public static final Person_Name_Property INSTANCE = new Person_Name_Property();

	private Person_Name_Property() {
		super(Person_EntityType.INSTANCE, "name", true/* nullable */, false /* confidential */);
	}

	@Override
	public <T> T getDirectUnsafe(GenericEntity entity) {
		return (T) ((Person_weak) entity).readName();
	}

	@Override
	public void setDirectUnsafe(GenericEntity entity, Object value) {
		((Person_weak) entity).writeName(value);
	}

}

class Person_Count_Property extends JvmProperty {
	public static final Person_Count_Property INSTANCE = new Person_Count_Property();

	/**
	 * Note that in JVM ITW we do not provide the type via constructor as it would be difficult to generate the bytecode. It is easier to set it on
	 * the instance via {@link AbstractProperty#setPropertyType(GenericModelType)}
	 */
	private Person_Count_Property() {
		super(Person_EntityType.INSTANCE, "count", true, false);

		// GWT:
		// super(Person_EntityType.INSTANCE, GenericModelTypeReflection.TYPE_STRING, "name", false, true, null);
	}

	@Override
	public <T> T getDirectUnsafe(GenericEntity entity) {
		return (T) ((Person_weak) entity).readCount();
	}

	@Override
	public void setDirectUnsafe(GenericEntity entity, Object value) {
		((Person_weak) entity).writeCount(value);
	}

}

class Person__FATHER extends JvmProperty {

	public static final Person__FATHER INSTANCE = new Person__FATHER();

	private Person__FATHER() {
		super(Person_EntityType.INSTANCE, "father", true /* nullable */, false /* confidential */);
	}

	@Override
	public <T> T getDirectUnsafe(GenericEntity entity) {
		return null;
	}

	@Override
	public void setDirectUnsafe(GenericEntity entity, Object value) {
		// analogous
	}

}
