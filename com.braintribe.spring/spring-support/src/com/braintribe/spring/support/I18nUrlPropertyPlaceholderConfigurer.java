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
package com.braintribe.spring.support;

import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.braintribe.logging.Logger;
import com.braintribe.utils.localization.Utf8ResourceBundle;

/**
 * This PropertyPlaceholderConfigurer (which is an extension to 
 * {@link org.springframework.beans.factory.config.PropertyPlaceholderConfigurer PropertyPlaceholderConfigurer})
 * is used for getting the values for I18N keys (the ones which starts with i18nPrefix). 
 * 
 * The values are gotten from the configured resource file.
 * If the key is not an I18N key, then the normal value is returned.
 * 
 * @author michel.docouto
 *
 */
public class I18nUrlPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

  protected Logger logger = Logger.getLogger(I18nUrlPropertyPlaceholderConfigurer.class);
  
  private String i18nPrefix = "^";
  private URL defaultLocalizationLocation;

  /**
   * Configures the I18N prefix to be used. Defaults to "^".
   * @param i18nPrefix
   */
  public void setI18nPrefix(String i18nPrefix) {
    this.i18nPrefix = i18nPrefix;
  }

  public void setDefaultLocalizationLocation(URL defaultLocalizationLocation) {
    this.defaultLocalizationLocation = defaultLocalizationLocation;
  }

  @Override
  protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {

    String signature = String.format("I18nUrlPropertyPlaceholderConfigurer: resolvePlaceholder(placeholder = %s, systemPropertiesMode = %d, properties = %s)", placeholder, systemPropertiesMode, props);
    if (logger.isTraceEnabled())
      logger.trace(String.format("%s: resolving ...", signature));
    
    if (placeholder.startsWith(i18nPrefix)) {
      placeholder = placeholder.substring(i18nPrefix.length());
      return getLocalizedString(placeholder);
    } 
      
    if (logger.isTraceEnabled())
      logger.trace(String.format("%s: delegating to %s", signature, PropertyPlaceholderConfigurer.class.getName()));
    
    return super.resolvePlaceholder(placeholder, props, systemPropertiesMode);    
  }

  
  
  private String getLocalizedString(String placeholder) {
    
    Locale locale = Locale.getDefault();
    
    ResourceBundle resourceBundle = null;
    try {
      resourceBundle = this.getResourceBundleFor(locale);            
      if (resourceBundle == null) {
        logger.warn(
          String.format("I18nUrlPropertyPlaceholderConfigurer: no more locale fallback available for %s, giving up"
            , defaultLocalizationLocation.getPath()));
        
        return null;        
      }
            
    } catch (Throwable e) {            
      logger.error(String.format("I18nUrlPropertyPlaceholderConfigurer: unable to get resource bundle for resource '%s' for locale '%s': %s", placeholder, locale.toString(), e.getMessage()), e);
      return null;
    }

    try {
      return resourceBundle.getString(placeholder);
    } catch (Exception e) {      
      logger.error(String.format("I18nUrlPropertyPlaceholderConfigurer: unable to get localized resource '%s' for locale '%s': %s", placeholder, locale.toString(), e.getMessage()), e);
    }

    return null;
  }
  
  
  
  private ResourceBundle getResourceBundleFor(Locale locale) {
    
    ResourceBundle resourceBundle = null;
    try {
      if (logger.isTraceEnabled())
        logger.trace(
          String.format("I18nUrlPropertyPlaceholderConfigurer: get resource bundle for locale '%s' from '%s'"
            ,  locale.toString()
            , defaultLocalizationLocation.getPath()));
      resourceBundle = Utf8ResourceBundle.getBundle(defaultLocalizationLocation, locale, this.getClass().getClassLoader());
      
      if (logger.isTraceEnabled())
        logger.trace(
          String.format("I18nUrlPropertyPlaceholderConfigurer: resource bundle is %s"
            , resourceBundle.getClass().getName()));
      
      return resourceBundle;
      
    } catch (Throwable e) {      
      logger.error(
        String.format("I18nUrlPropertyPlaceholderConfigurer: unable to get resource bundle with locale '%s' from '%s': %s"
          , locale.toString()
          , defaultLocalizationLocation.getPath()
          , e.getMessage()), e);
    }
    
    return null;    
  }

}
