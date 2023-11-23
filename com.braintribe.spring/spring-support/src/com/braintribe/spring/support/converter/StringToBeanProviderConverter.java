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
package com.braintribe.spring.support.converter;

import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.convert.converter.Converter;

import com.braintribe.spring.support.BeanFactoryProvider;

public class StringToBeanProviderConverter implements BeanFactoryAware, Converter<String, Supplier<?>> {
  private BeanFactory beanFactory;

  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  public Supplier<?> convert(String beanId) {
    if (beanFactory.containsBean(beanId)) {
      BeanFactoryProvider<Object> provider = new BeanFactoryProvider<Object>();
      provider.setBeanFactory(beanFactory);
      provider.setBeanName(beanId);
      return provider;
    } else
      throw new IllegalArgumentException("no bean with id " + beanId + " found");
  }

  public static String getBuildVersion() {
    return "$Build_Version$ $Id: StringToBeanProviderConverter.java 102880 2018-01-18 11:36:53Z roman.kurmanowytsch $";
  }
}
