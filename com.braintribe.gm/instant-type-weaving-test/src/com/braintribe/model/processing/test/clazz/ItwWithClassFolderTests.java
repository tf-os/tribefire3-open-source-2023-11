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
package com.braintribe.model.processing.test.clazz;

import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.entityType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.property;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.stringType;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.io.File;
import java.util.Arrays;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesis;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesisException;
import com.braintribe.model.processing.itw.synthesis.java.clazz.FolderClassLoader;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.test.clazz.model.Animal;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * Use this test as follows. Delete the content of the "working directory" and run the main method three times. What
 * happens is following:
 * 
 * <ol>
 * <li>The entity is woven normally and class file will be created.</li>
 * <li>The entity is loaded from the file, but a new version is created and the file if overwritten (a new property is
 * added).</li>
 * <li>The entity is loaded with the new property, so it will also test that one. The file is overwritten again, but the
 * bytecode is not changed. For this last run, the test also outputs info about testing this new property.</li>
 * </ol>
 */
public class ItwWithClassFolderTests extends ImportantItwTestSuperType {

	private static final File CLASS_FOLDER = new File("working");
	private static final String DOG_SIGNATURE = Animal.class.getName().replace("Animal", "Dog");

	public static void main(String[] args) throws Exception {
		run();
	}

	private static void run() throws Exception {
		FolderClassLoader fcl = new FolderClassLoader();
		fcl.setClassFolder(CLASS_FOLDER);
		fcl.ensureClasses();

		Class<?> dogClass = tryToLoadDogClass();

		if (dogClass == null) {
			System.out.println("Creating Dog class cause it does not exists yet.");

			runItw(false);
			doTest(false);

		} else if (!containsExtraProperty(dogClass)) {
			System.out.println("Dog class exists, so we add new property for the next run.");

			runItw(true); /* true: create the extra property, will be stored for the next run; not accessible in this
							 * run */
			doTest(false);

		} else {
			System.out.println("Dog class with extra property is ready.");

			doTest(true); /* true: we want to run using the extra property */
		}
	}

	private static Class<?> tryToLoadDogClass() {
		try {
			return Class.forName(DOG_SIGNATURE);

		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	// ####################################
	// ## . . . . INITIALIZATION . . . . ##
	// ####################################

	private static boolean containsExtraProperty(Class<?> dogClass) {
		try {
			return dogClass.getDeclaredMethod("getBreed") != null;

		} catch (NoSuchMethodException e) {
			return false;

		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static void runItw(boolean includeExtraProperty) {
		GenericModelTypeSynthesis gmts = GenericModelTypeSynthesis.newInstance();
		gmts.setClassOutputFolder(CLASS_FOLDER);

		try {
			if (includeExtraProperty) {
				GenericModelTypeSynthesis.newInstance().ensureModelTypes(provideModel(false));
			}
			gmts.ensureModelTypes(provideModel(includeExtraProperty));

		} catch (GenericModelTypeSynthesisException e) {
			throw new RuntimeException("ITW FAILED", e);
		}
	}

	private static ProtoGmMetaModel provideModel(boolean includeExtraProperty) {
		GmMetaModel metaModel = new NewMetaModelGeneration().buildMetaModel("test:ItwWithClassFolderModel", asList(Animal.T));

		BasicModelOracle modelOracle = new BasicModelOracle(metaModel);

		GmEntityType animalTyp = modelOracle.findGmType(Animal.T);

		GmEntityType dogType = entityType(DOG_SIGNATURE, Arrays.asList(animalTyp));

		if (includeExtraProperty) {
			GmSimpleType stringType = stringType();

			GmProperty property = property(dogType, "breed", stringType);
			dogType.setProperties(Arrays.asList(property));
		}

		metaModel.getTypes().add(dogType);

		return metaModel;
	}

	// ####################################
	// ## . . . . TEST ACTUAL CLASS . . .##
	// ####################################

	private static void doTest(boolean testExtraProperty) throws Exception {
		EntityType<Animal> entityType = GMF.getTypeReflection().getType(DOG_SIGNATURE);
		Animal a = entityType.createPlain();

		BtAssertions.assertThat(a.getClass()).isNotEqualTo(Animal.class).isAssignableTo(Animal.class);

		a.setName("animal");
		BtAssertions.assertThat(a.getName()).isEqualTo("animal");

		if (testExtraProperty) {
			System.out.println("Testing the extra property.");

			final String germanShepherd = "German Shepherd";

			a.getClass().getDeclaredMethod("setBreed", String.class).invoke(a, germanShepherd);
			String breed = (String) a.getClass().getDeclaredMethod("getBreed").invoke(a);

			BtAssertions.assertThat(breed).isEqualTo(germanShepherd);
		}

	}

}
