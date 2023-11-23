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
/**
 * 
 */
package com.braintribe.spring.support.converter;

import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;

/**
 *
 */
public class StringToIconConverter implements Converter<String, Icon>, ApplicationContextAware {

  private ApplicationContext applicationContext;

  public StringToIconConverter() {
  }

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public Icon convert(String source) {
    if ((source == null) || (source.trim().length() == 0))
      return null;

    try {
      Resource resource = applicationContext.getResource(source);

      URL url = resource.getURL();
      ImageIcon icon = new ImageIcon(url);

      return icon;
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("error while building icon url from: %s", source), e);
    }
  }

}
