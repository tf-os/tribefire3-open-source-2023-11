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
package com.braintribe.model.asset.natures;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Dynamic initializer is a mechanism to bring custom smood initializer types into Collaborative Smood Access (CSA).
 * <p>
 * The actual implementation is bound from a module and is identified by that module's name (i.e. the module name is written into the access'
 * config.json). This implementation is then used for every single asset of this nature ({@link DynamicInitializerInput}), meaning the bound expert is
 * called with a file denoting a folder in the input asset as a parameter.
 * <p>
 * <b>IMPORTANT:</b> This means when setting up a project, there must be an entry in the corresponding config.json for every
 * {@link DynamicInitializerInput}, with the correct name of the module that binds the expert. The name of the module is determined by examining the
 * dependencies of the input asset, as this asset is expected to have exactly one direct module dependency - the module that binds the actual
 * initializer implementation.
 * <p>
 * The input for that implementation is given by the content of the "resources" folder of the input asset (which is copied into the corresponding
 * access' data folder).
 * 
 * <p>
 * <b>Example:</b> In this example we will simply prepare a new initializer type which simply creates a new model in cortex based on a text file
 * containing the name, version and a list of model dependencies.
 * 
 * <pre>
 *  model-creation-configuring-module
 *  	// This module binds an initializer, that expects a single file called model-description.txt in it's input folder.
 *  
 *  	ModelCreationConfiguringModuleSpace {
 *  
 *  		public void bindInitializers(InitializerBindingBuilder bindings) {
 *				bindings.bindDynamicInitializerFactory(this::newModelCreatingInitializer);
 *  		}
 *  
 *			private DataInitializer newRemotesInitializer(File inputFolder) {
 *				return ctx -> new ModelCreatingInitializer(ctx, inputFolder).run();
 *			}
 *
 *  		private class ModelCreatingInitializer {
 *  			...
 *  			public ModelCreatingInitializer(PersistenceInitializationContext context, File inputFolder) {
 *  				this.context = context;
 *  				this.inputFolder = inputFolder;
 *  			}
 *  			public void run() {
 *  				// create a new model based on the model-description.txt inside of inputFolder.
 *  				...
 *  			}
 *  		}
 *  	}
 *  	
 *  my-model-configuration // asset of type DynamicInitializerInput
 *  	resources
 *  		model-dependencies.txt >
 *  			model1,model2,model3
 * 		asset.man >
 * 			$nature = !com.braintribe.model.asset.natures.DynamicInitializerInput()
 * 			.accessIds=['cortex']
 *		pom.xml >
 * 			&lt;dependencies>
 *				&lt;dependency>
 *					&lt;groupId>my.extension&lt;/groupId>
 *					&lt;artifactId>model-creation-configuring-module&lt;/artifactId>
 *					&lt;version>1.0&lt;/version>
 *					&lt;classifier>asset&lt;/classifier>
 *					&lt;type>man&lt;/type>
 *					&lt;?tag asset?>
 *				&lt;/dependency>
 *				...
 *			&lt;/dependencies>
 * </pre>
 * 
 * <p>
 * After Jinni is run for a project which contains "my-model-configuration", the cortex storage will contain the following entries
 * 
 * <pre>
 * data
 * 	my.extension_my-model-configuration#1.0
 * 		model.dependencies.txt
 *	config.json >
 *		{"_type": "com.braintribe.model.csa.DynamicInitializer", "_id": "23",
 *   		"moduleId": my.extension:model-creation-configuring-module"
 *  	},
 * </pre>
 */
public interface DynamicInitializerInput extends ConfigurableStoragePriming {

	EntityType<DynamicInitializerInput> T = EntityTypes.T(DynamicInitializerInput.class);

}
