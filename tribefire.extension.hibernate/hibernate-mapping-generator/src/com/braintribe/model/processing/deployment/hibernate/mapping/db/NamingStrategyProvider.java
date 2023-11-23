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
package com.braintribe.model.processing.deployment.hibernate.mapping.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.CollectionPropertyDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.EntityDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.render.context.PropertyDescriptor;
import com.braintribe.model.processing.deployment.hibernate.mapping.utils.CamelCaseStringShortener;
import com.braintribe.model.processing.deployment.hibernate.mapping.utils.Suffixer;

public class NamingStrategyProvider {
	
	private final HbmXmlGenerationContext context;
	private final Collection<EntityDescriptor> entityDescriptors;
	private final NamingLimitations namingLimitations;
	
	private final Set<String> reservedTableNames;
	private final Map<String, Set<String>> reservedColumnNames;

	public NamingStrategyProvider(HbmXmlGenerationContext context, 
			                      Collection<EntityDescriptor> entityDescriptors) { 
		this.context = context;
		this.entityDescriptors = entityDescriptors;
		this.namingLimitations = NamingLimitations.create(context);
		
		reservedTableNames = new HashSet<String>();
		reservedColumnNames = new HashMap<String, Set<String>>();
	}
	
	public Collection<EntityDescriptor> apply() { 
		registerNonOverwritableNames();
		provideDataBaseNames();
		return entityDescriptors;
	}
	
	/**
	 * Registers previously set database names which shouldn't be overwritten
	 */
	private void registerNonOverwritableNames() { 
		
		//registers tb names initially given 
		for (EntityDescriptor entityDescriptor : entityDescriptors) {
			
			if (entityDescriptor.getTableName() != null) {
				registerTableName(entityDescriptor.getTableName());
				
				for (PropertyDescriptor propertyDescriptor : entityDescriptor.getProperties())
					registerProvidedNames(propertyDescriptor);
			}
		}
	}
	
	private void registerProvidedNames(PropertyDescriptor propertyDescriptor) {
		
		if (propertyDescriptor.getEntityDescriptor().getTableName() != null && propertyDescriptor.getColumnName() != null) {
			registerColumnName(propertyDescriptor.getEntityDescriptor().getTableName(), propertyDescriptor.getColumnName());
		}
		
		if (propertyDescriptor instanceof CollectionPropertyDescriptor) {
			registerProvidedNames((CollectionPropertyDescriptor)propertyDescriptor);
		}
	}

	private void registerProvidedNames(CollectionPropertyDescriptor propertyDescriptor) {

		String collectionTable = propertyDescriptor.getMany2ManyTable();
		
		if (collectionTable != null) {
			registerTableName(collectionTable);
			
			if (propertyDescriptor.getKeyColumn() != null)
				registerColumnName(collectionTable, propertyDescriptor.getKeyColumn());
			
			if (propertyDescriptor.getElementColumn() != null)
				registerColumnName(collectionTable, propertyDescriptor.getElementColumn());

			if (propertyDescriptor.getIsList() && propertyDescriptor.getIndexColumn() != null)
				registerColumnName(collectionTable, propertyDescriptor.getIndexColumn());
				
			if (propertyDescriptor.getIsMap() && propertyDescriptor.getMapKeyColumn() != null)
				registerColumnName(collectionTable, propertyDescriptor.getMapKeyColumn());
		}
		
	}

	/**
	 * Fixes table/column names, based on the given HbmXmlGenerationConfig options
	 */
	private void provideDataBaseNames() {
		for (EntityDescriptor entityDescriptor : entityDescriptors) {
			//if (!entityDescriptor.getDbNamesFromMappingMetadata() && !entityDescriptor.getDbNamesFromGeneratorHistory()) {
				provideDataBaseNames(entityDescriptor);
			//}
			for (PropertyDescriptor propertyDescriptor : entityDescriptor.getProperties()) {
				//if (propertyDescriptor.getDbNamesFromMappingMetadata() || propertyDescriptor.getDbNamesFromGeneratorHistory()) continue;
				provideDataBaseNames(propertyDescriptor);
			}
		}
	}
	
	private void provideDataBaseNames(EntityDescriptor entityDescriptor) { 
		
		//table names are set only if not previsouly set
		if (entityDescriptor.getTableName() == null) {
			entityDescriptor.setTableName(generateTableName(entityDescriptor.getTableNameBase()));
			registerTableName(entityDescriptor.getTableName());
		}

		//discriminator column names are set only if not previsouly set
		if (entityDescriptor.getDiscriminatorColumnName() == null) {
			entityDescriptor.setDiscriminatorColumnName(generateColumnName(entityDescriptor.getTableName(), entityDescriptor.getDiscriminatorName()));
			registerColumnName(entityDescriptor.getTableName(), entityDescriptor.getDiscriminatorColumnName());
		}
		
	}

	private void provideDataBaseNames(PropertyDescriptor propertyDescriptor) { 
		
		if (propertyDescriptor.getColumnName() == null) {
			String tableName = propertyDescriptor.getEntityDescriptor().getTableName();
			propertyDescriptor.setColumnName(generateColumnName(tableName, propertyDescriptor.getName()));
			registerColumnName(tableName, propertyDescriptor.getColumnName());
		}
		
		if (propertyDescriptor instanceof CollectionPropertyDescriptor) {
			provideDataBaseNames((CollectionPropertyDescriptor)propertyDescriptor);
		}
	}
	
	private void provideDataBaseNames(CollectionPropertyDescriptor propertyDescriptor) {
		
		//many2ManyTable is only overwritten if null, as this information could have been provided to the CollectionPropertyDescriptor during its creation with a PropertyHint
		String many2ManyTable = propertyDescriptor.getMany2ManyTable();
		if (many2ManyTable == null) {
			many2ManyTable = generateTableName(propertyDescriptor.getCollectionName());
			propertyDescriptor.setMany2ManyTable(many2ManyTable);
		}

		registerTableName(many2ManyTable);
		
		if (propertyDescriptor.getKeyColumn() == null) {
			propertyDescriptor.setKeyColumn(generateColumnName(many2ManyTable, propertyDescriptor.getCollectionKeyName()));
			registerColumnName(many2ManyTable, propertyDescriptor.getKeyColumn());
		}
		
		if (propertyDescriptor.getElementColumn() == null) {
			propertyDescriptor.setElementColumn(generateColumnName(many2ManyTable, propertyDescriptor.getCollectionElementName()));
			registerColumnName(many2ManyTable, propertyDescriptor.getElementColumn());
		}

		if (propertyDescriptor.getIsList() && propertyDescriptor.getIndexColumn() == null) {
			propertyDescriptor.setIndexColumn(generateColumnName(many2ManyTable, propertyDescriptor.getCollectionIndexName()));
			registerColumnName(many2ManyTable, propertyDescriptor.getIndexColumn());
		}
		
		if (propertyDescriptor.getIsMap() && propertyDescriptor.getMapKeyColumn() == null) {
			propertyDescriptor.setMapKeyColumn(generateColumnName(many2ManyTable, propertyDescriptor.getCollectionMapKeyName()));
			registerColumnName(many2ManyTable, propertyDescriptor.getMapKeyColumn());
		}
	}
	
	private String generateTableName(String coreTableName) {
		coreTableName = removeIllegalTableNameChar(coreTableName);
		if (nonPrefixedTableNameExceedsMaxLength(coreTableName)) {
			coreTableName = shortenTableName(coreTableName);
		}
		return applyConfiguredCase(uniqueTableName(applyTablePrefix(coreTableName)));
	}
	
	
	private String generateColumnName(String tableName, String coreColumnName) {
		coreColumnName = removeIllegalColumnNameChar(coreColumnName);
		if (columnNameExceedsMaxLength(coreColumnName)) {
			coreColumnName = shortenColumnName(coreColumnName);
		}
		return applyConfiguredCase(uniqueColumnName(tableName, coreColumnName));
	}
	
    private void registerTableName(String tableName) {
    	reservedTableNames.add(tableName.toUpperCase());
	}
    
    private void registerColumnName(String tableName, String columnName) {
    	if (!reservedColumnNames.containsKey(tableName.toUpperCase()))
    		reservedColumnNames.put(tableName.toUpperCase(), new HashSet<String>());
    	reservedColumnNames.get(tableName.toUpperCase()).add(columnName.toUpperCase());
	}
    
    private boolean isValidTableName(String tableName) {
    	return isTableNameAvailable(tableName) && !namingLimitations.isReservedWord(tableName);
    }
    
    private boolean isTableNameAvailable(String tableName) {
    	return !reservedTableNames.contains(tableName.toUpperCase());
    }
    
    private boolean isValidColumnName(String tableName, String columnName) {
    	return isColumnNameAvailable(tableName, columnName) && !namingLimitations.isReservedWord(columnName);
    }
    
    private boolean isColumnNameAvailable(String tableName, String columnName) {
    	return !(reservedColumnNames.containsKey(tableName.toUpperCase()) && 
   			 reservedColumnNames.get(tableName.toUpperCase()).contains(columnName.toUpperCase()));
    }
    
    private static boolean exceedsMaxLength(String string, Integer maxLength) { 
    	return (string != null && string.length() > maxLength);
    }
    
    private static String shorten(String camelCaseString, Integer maxLength) {
    	return new CamelCaseStringShortener(camelCaseString, maxLength).shorten();
    }

    private String shortenTableName(String camelCaseString) {
    	return shorten(camelCaseString, namingLimitations.getTableNameNonPrefixedMaxLength());
    }

    private String shortenColumnName(String camelCaseString) {
    	return shorten(camelCaseString, namingLimitations.getColumnNameMaxLength());
    }
    
    private boolean tableNameExceedsMaxLength(String tableName) {
    	return exceedsMaxLength(tableName, namingLimitations.getTableNameMaxLength());
    }
    
    private boolean nonPrefixedTableNameExceedsMaxLength(String camelCaseString) {
    	return exceedsMaxLength(camelCaseString, namingLimitations.getTableNameNonPrefixedMaxLength());
    }
    
    private boolean columnNameExceedsMaxLength(String columnName) {
    	return exceedsMaxLength(columnName, namingLimitations.getColumnNameMaxLength());
    }
    
    private boolean isTablePrefixConfigured() { 
    	return !(context.tablePrefix == null || context.tablePrefix.isEmpty());
    }
    
    private String applyTablePrefix(String coreTableName) { 
    	if (!isTablePrefixConfigured()) return coreTableName;
    	return context.tablePrefix+coreTableName;
    }

    /**
     * TODO: provide support for UPPER, lower, CamelCase and lowerCamelCase
     * @param name Text to have its case changed
     * @return Case changed text
     */
    private String applyConfiguredCase(String name) { 
    	if (context.allUppercase) 
    		return name.toUpperCase(); 
    	return name;
    }
    
    /**
     * Removes illegal character from the column name, as specified by the given NamingLimitations
     * @param string String to have illegal characters removed from
     * @return The input String without the illegal characters
     */
    private String removeIllegalColumnNameChar(String string) { 
    	
    	//trailing chars
    	if (namingLimitations.getColumnNameIllegalLeadingCharsPattern() != null)
    		string = string.replaceAll(namingLimitations.getColumnNameIllegalLeadingCharsPattern(), "");
    	
    	return string;
    }
    
    /**
     * Removes illegal character from the table name, as specified by the given NamingLimitations
     * @param string String to have illegal characters removed from
     * @return The input String without the illegal characters
     */
    private String removeIllegalTableNameChar(String string) { 
    	
    	//trailing chars
    	if (namingLimitations.getTableNameIllegalLeadingCharsPattern() != null)
    		string = string.replaceAll(namingLimitations.getTableNameIllegalLeadingCharsPattern(), "");
    	
    	return string;
    }

    private String uniqueTableName(String tableName) {
    	int attempt = 0;
    	String originalTableName = tableName;
    	while (!isValidTableName(tableName)) {
    		if (attempt == 0 && tableNameExceedsMaxLength(tableName)) {
    			String tableNameOneCharShorter = tableName.substring(0, tableName.length()-1);
    			if (isValidTableName(tableNameOneCharShorter)) return tableNameOneCharShorter;
    		}
    		tableName = Suffixer.suffixIt(originalTableName, attempt++, namingLimitations.getTableNameMaxLength());
    	}
    	return tableName;
    }

    private String uniqueColumnName(String tableName, String columnName) {
    	int attempt = 0;
    	String originalColumnName = columnName;
    	while (!isValidColumnName(tableName, columnName)) {
    		columnName = Suffixer.suffixIt(originalColumnName, attempt++, namingLimitations.getColumnNameMaxLength());
    	}
    	return columnName;
    }
    


}
