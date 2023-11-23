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

import static com.braintribe.utils.SysPrint.spOut;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.generic.commons.PartitionIgnoringEntRefHashingComparator;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.GlobalEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.utils.StringTools;

import tribefire.cortex.manipulation.conversion.code.InitializerWritingContext;

/**
 * @author peter.gazdik
 */
public class BeanRegistry {

	public final InitializerWritingContext ctx;

	public final List<NewBean> newBeans = newList();
	public final List<ExistingBean> existingBeans = newList();
	public final Map<EntityReference, EntityBean<?>> refToBean = CodingMap.create(PartitionIgnoringEntRefHashingComparator.INSTANCE);

	private final Map<String, Integer> simpleNameToCount = newMap();

	public BeanRegistry(InitializerWritingContext context) {
		this.ctx = context;
	}

	public String resolveBeanName(EntityReference ref) {
		String typeSignature = ref.getTypeSignature();
		String simpleName = StringTools.findSuffix(typeSignature, ".");

		Integer count = simpleNameToCount.get(simpleName);
		Integer newCount = count == null ? 1 : count + 1;

		simpleNameToCount.put(simpleName, newCount);
		return StringTools.uncapitalize(simpleName) + "_" + newCount;
	}

	public EntityBean<?> acquireBean(EntityReference ref) {
		EntityBean<?> result = refToBean.get(ref);
		if (result != null)
			return result;
		else
			return createBeanIfGlobal(ref);
	}

	private EntityBean<?> createBeanIfGlobal(EntityReference ref) {
		if (ref.referenceType() != EntityReferenceType.global)
			throw new IllegalArgumentException("Unexpected reference, global was expected, not: " + ref);

		spOut("Existing Bean: " + ref);
		return onExistingBean(new ExistingBean(this, (GlobalEntityReference) ref));
	}

	public void createNewBeanFor(PreliminaryEntityReference ref) {
		onNewBean(new NewBean(this, ref));
	}

	private NewBean onNewBean(NewBean bean) {
		newBeans.add(bean);
		return onBean(bean);
	}

	private ExistingBean onExistingBean(ExistingBean bean) {
		existingBeans.add(bean);
		return onBean(bean);
	}

	private <B extends EntityBean<?>> B onBean(B bean) {
		refToBean.put(bean.ref, bean);
		return bean;
	}

	public void onGidAssigned(EntityReference ref, String newGid) {
		EntityBean<?> bean = requireBean(ref, () -> "Trying to asssing globalId to " + newGid);
		bean.globalId = newGid;

		GlobalEntityReference newRef = GlobalEntityReference.T.create();
		newRef.setTypeSignature(ref.getTypeSignature());
		newRef.setRefId(newGid);

		if (refToBean.put(newRef, bean) != null)
			throw new IllegalArgumentException(
					"An attempt is made to assign globalId, but there already is a different instance with said id: " + newGid);
	}

	public EntityBean<?> requireBean(EntityReference ref, Supplier<String> contextSupplier) {
		EntityBean<?> bean = refToBean.get(ref);
		if (bean == null)
			throw new IllegalArgumentException("Entity not found for reference '" + ref + "'. " + contextSupplier.get());
		return bean;
	}

}
