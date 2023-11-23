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
package com.braintribe.model.processing.deployment.access;

import static com.braintribe.model.processing.deployment.access.utils.EntityTools.getValueForSimpleType;
import static com.braintribe.model.processing.deployment.access.utils.EntityTools.isIntegerType;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * 
 */
public class SecurityTest extends AbstractAccessTest {

	private static String typeSignature;
	private static String uniqueProperty;

	private static final String uniqueValue = "uniqueValue3";

	public static void main(String[] args) throws Exception {
		loadArguments(args);

		listAllEntities();

		Object id1 = createNewEntity(uniqueValue);
		listAllEntities();

		// this should not work, due to
		Object id2 = createNewEntity(uniqueValue);
		listAllEntities();

		deleteEntity(id1);
		deleteEntity(id2);
		listAllEntities();
	}

	private static void loadArguments(String[] args) {
		if (args.length < 3) {
			throw new RuntimeException(
					"3 arguments needed - 'acceessId', 'typeSignature', 'propertyName' - id of access to be used and signature of type which will be queried and manipulated.");
		}

		accessId = args[0];
		typeSignature = args[1];
		uniqueProperty = args[2];

		System.out.println("###########################");
		System.out.println("CONFIG:");
		System.out.println("accessId -> " + accessId);
		System.out.println("typeSignature -> " + typeSignature);
		System.out.println("uniqueProperty -> " + uniqueProperty);
		System.out.println("###########################");
	}

	private static Object createNewEntity(String propValue) throws Exception {
		System.out.println("\nCreating new " + typeSignature);

		PersistenceGmSession gmSession = createNewSession();
		EntityType<? extends GenericEntity> et = GMF.getTypeReflection().getType(typeSignature);

		GenericEntity ge = gmSession.create(et);
		for (Property p: et.getProperties()) {
			if (p.isIdentifier()) {
				if (isIntegerType(p.getType())) {
					Object value = getValueForSimpleType((SimpleType) p.getType());
					p.set(ge, value);
					System.out.println(p.getName() + " -> " + value + "[id]");
				} else {
					throw new RuntimeException("This test doesn't support non-numberic 'id's.");
				}

				continue;
			}

			GenericModelType pt = p.getType();
			if (p.getName().equals(uniqueProperty) && pt instanceof SimpleType) {
				System.out.println(p.getName() + " -> " + propValue);
				p.set(ge, propValue);

			} else {
				System.out.println(p.getName() + " -> null [" + pt.getTypeName() + "]");
			}
		}
		System.out.println("Committing: " + ge);
		gmSession.commit();

		System.out.println("--------------> Entity saved with id: " + et.getIdProperty().get(ge));

		return et.getIdProperty().get(ge);
	}

	protected static void deleteEntity(Object id) throws Exception {
		newEntityService().deleteEntity(typeSignature, id);
	}

	private static void listAllEntities() throws Exception {
		listAllEntities(typeSignature);
	}

}
