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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

import com.braintribe.logging.Logger;

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
public class LocaleAwarePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements ApplicationContextAware, InitializingBean {

  protected Logger logger = Logger.getLogger(LocaleAwarePropertyPlaceholderConfigurer.class);
  
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(.*)\\$\\{([^}]+)\\}(.*)");
  private static final String XML_FILE_EXTENSION = ".xml";
  
  private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();
  
  private String fileEncoding;
  
  private String i18nPrefix = "i18n.";

  private ApplicationContext applicationContext;  
  
  private Resource location;
  private Resource [] locations;
  
  public LocaleAwarePropertyPlaceholderConfigurer() {}
    
  @Override
  public void afterPropertiesSet() throws Exception {
    
    super.setLocation(this.process(this.location));
      
    List<Resource> resources = new ArrayList<Resource>();
    
    for (Resource location : locations) {
     Resource r = this.process(location);
     if (r != null)
       resources.add(r);
    }
    
    Resource [] rs = resources.toArray(new Resource[resources.size()]);
    
    logger.info(String.format("%s: loaded the following resources ...", LocaleAwarePropertyPlaceholderConfigurer.class.getName()));
    for (Resource r : resources)
      logger.info(String.format("%s: loaded resource %s", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), r.getFilename()));
      
    super.setLocations(rs);
  }
  
  
  @Override
  protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {

    String signature = String.format("%s: resolvePlaceholder(placeholder = %s, systemPropertiesMode = %d, properties = %s)"
      , LocaleAwarePropertyPlaceholderConfigurer.class.getName()
      , placeholder
      , systemPropertiesMode
      , props);
    
    if (logger.isTraceEnabled())
      logger.trace(String.format("%s: resolving ...", signature));
    
    if (placeholder.startsWith(i18nPrefix)) {
      placeholder = placeholder.substring(i18nPrefix.length());
      String resolved = super.resolvePlaceholder(placeholder, props, systemPropertiesMode);
      
      /*
       * if we cannot resolve it using the specified locale we try to implement a fallback
       */
      if (resolved == null) {
        
        if (this.locations != null) {
          Resource [] arr = (Resource [])ArrayUtils.clone(this.locations);
          ArrayUtils.reverse(arr);
                    
          for (Resource r : arr) {
            
            Resource _resolved = this.applicationContext.getResource(r.getFilename());
            if (!exists(_resolved))
              continue;
            
            try {
              Properties ps = this.loadProperties(r);              
              resolved = ps.getProperty(placeholder);
              
              if (resolved != null) {
                if (logger.isTraceEnabled())
                  logger.trace(String.format("%s, resolved placeholder %s from resource %s", signature, placeholder, r.getFilename()));
                
                break;
              }
              
            } catch (IOException e) {
              logger.warn(String.format("%s: error loading properties from resource %s: %s", signature, r.getFilename(), e.getMessage()), e);
            }
          }
        }
      }
      if (logger.isTraceEnabled())
        logger.trace(String.format("%s: resolved i18n placeholder '%s' to '%s'", signature, placeholder, resolved));
      
      return resolved;
    } 
      
    if (logger.isTraceEnabled())
      logger.trace(String.format("%s: delegating to %s", signature, PropertyPlaceholderConfigurer.class.getName()));
    
    String resolved = super.resolvePlaceholder(placeholder, props, systemPropertiesMode);    

    if (logger.isTraceEnabled())
      logger.trace(String.format("%s: resolved placeholder '%s' to '%s'", signature, placeholder, resolved));
    
    return resolved;
  }
  
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  
  /**
   * Configures the I18N prefix to be used. Defaults to "^".
   * @param i18nPrefix
   */
  public void setI18nPrefix(String i18nPrefix) {
    this.i18nPrefix = i18nPrefix;
  }


  /**
   * @see org.springframework.core.io.support.PropertiesLoaderSupport#setLocation(org.springframework.core.io.Resource)
   */
  @Override
  public void setLocation(Resource location) {
    super.setLocation(location);
    
    this.location = location;
  }


  /**
   * @see org.springframework.core.io.support.PropertiesLoaderSupport#setLocations(org.springframework.core.io.Resource[])
   */
  @Override
  public void setLocations(Resource[] locations) {
    super.setLocations(locations);    
    
    this.locations = locations;
  }
  
  private Resource process(Resource resource)  {
        
    if (resource == null)
      return null;
    
    Locale locale = Locale.getDefault();
    
    String filename = null;
    try {
      filename = resource.getURL().toExternalForm();
    } catch (IOException e) {
      return resource;
    }
    
    Matcher m = VARIABLE_PATTERN.matcher(filename);
    if (!m.matches()) {
      
      Resource resolved = this.applicationContext.getResource(resource.getFilename());
      if (exists(resolved)) {
        logger.info(String.format("%s: loading default resource '%s'", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), resource.getFilename()));      
        return resource;
      }
      
      logger.info(String.format("%s: default resource '%s' could not be loaded", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), resolved.getFilename()));
      
      return null;
    }
    
    String pre = m.group(1);
    String variable = m.group(2);
    String post = m.group(3);
    
    if (!variable.equalsIgnoreCase("app-locale"))
      throw new RuntimeException(String.format("%s variable '%s' is not supported in resource '%s'", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), variable, filename));
    
    String localized = null;
    Resource resolved = null;
    
    if ((locale.getVariant() != null) && (locale.getVariant().trim().length() > 0)) {
      localized = String.format("%s_%s_%s_%s%s", pre, locale.getLanguage(), locale.getCountry(), locale.getVariant(), post);
      
      resolved = this.applicationContext.getResource(localized);
      if (exists(resolved)) {
        logger.info(String.format("%s: loading resource '%s'", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), resolved.getFilename()));
        return resolved;     
      }
    }

    if ((locale.getCountry() != null) && (locale.getCountry().trim().length() > 0)) {
      localized = String.format("%s_%s_%s%s", pre, locale.getLanguage(), locale.getCountry(), post);
      
      resolved = this.applicationContext.getResource(localized);
      if (exists(resolved)) {
        logger.info(String.format("%s: loading resource '%s'", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), resolved.getFilename()));
        return resolved;      
      }
    }

    if ((locale.getLanguage() != null) && (locale.getLanguage().trim().length() > 0)) {
      localized = String.format("%s_%s%s", pre, locale.getLanguage(), post);
      
      resolved = this.applicationContext.getResource(localized);
      if (exists(resolved)) {
        logger.info(String.format("%s: loading resource '%s'", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), resolved.getFilename()));
        return resolved;      
      }
    }

    localized = String.format("%s%s", pre, post);
    
    resolved = this.applicationContext.getResource(localized);
    if (exists(resolved)) {
      logger.info(String.format("%s: loading resource '%s'", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), resolved.getFilename()));
      return resolved;      
    }

    logger.info(String.format("%s: resource '%s' could not be loaded", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), resolved.getFilename()));
    
    return null;
  }

  private boolean exists(Resource resource) {
    
    if ((resource == null) || !resource.exists())
      return false;
    
    InputStream is = null;
    String location = null;
    try {
      URL url = resource.getURL();
      
      location = url.toExternalForm();
      logger.debug(String.format("%s: trying to read resource '%s'", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), location));
      
      byte [] buf = new byte[2048];
      int bytes = 0, total = 0;
      
      is = url.openStream();
      while ((bytes = is.read(buf)) > 0)
        total += bytes;
      
      logger.debug(String.format("%s: successfully read resource '%s', %d bytes read totally", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), location, total));
      
      return true;
    } catch (Throwable e) {
      logger.warn(String.format("%s: resource '%s' could not be loaded: %s", LocaleAwarePropertyPlaceholderConfigurer.class.getName(), location, e.getMessage()));
      
      return false;
    } finally {
      if (is != null) {
        try { is.close();} catch (Exception e) {}
      }
    }
  }

  /**
   * Load properties into the given instance.
   * 
   * @param location
   *          the Properties instance to load into
   * @throws java.io.IOException
   *           in case of I/O errors
   * @see #setLocations
   */
  protected Properties loadProperties(Resource location) throws IOException {
    
    Properties props = new Properties();
    
    
    
    if (logger.isInfoEnabled()) 
      logger.info("Loading properties file from " + location);
    
    InputStream is = null;
    try {
      is = location.getInputStream();

      String filename = null;
      try {
        filename = location.getFilename();
      } catch (IllegalStateException ex) {
        // resource is not file-based. See SPR-7552.
      }

      if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
        this.propertiesPersister.loadFromXml(props, is);
      } else {
        if (this.fileEncoding != null) {
          this.propertiesPersister.load(props, new InputStreamReader(is, this.fileEncoding));
        } else {
          this.propertiesPersister.load(props, is);
        }
      }
    } catch (IOException ex) {
      ; // ignore
    } finally {
      if (is != null) {
        is.close();
      }
    }
    
    return props;
  } 
  
}
