/*
 *    Copyright 2013 Weald Technology Trading Limited
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package test.com.wealdtech.hawk.jersey.guice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import test.com.wealdtech.hawk.jersey.HawkExampleUserAuthenticationFilter;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.wealdtech.hawk.jersey.HawkUnauthorizedFilter;
import com.wealdtech.jersey.filters.BodyPrefetchFilter;
import com.wealdtech.jersey.filters.ServerHeadersFilter;

/**
 * A Guice module to configure a Jetty server with servlets
 */
public class HawkServletModule extends ServletModule
{
  private String packages;

  /**
   * Create a Jersey servlet module with a list of packages to
   * check for resources and the like.
   * @param packages a list of packages
   */
  public HawkServletModule(final String... packages)
  {
    super();
    setPackages(packages);
  }

  @Override
  protected void configureServlets()
  {
    final Map<String, String> params = new HashMap<String, String>();
    params.put(PackagesResourceConfig.PROPERTY_PACKAGES, this.packages);

    // Add the authentication filter to requests and the unauthorized filter to responses
    final String requestFilters = joinClassNames(BodyPrefetchFilter.class, HawkExampleUserAuthenticationFilter.class);
    final String responseFilters = joinClassNames(HawkUnauthorizedFilter.class, ServerHeadersFilter.class);

    params.put(PackagesResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, requestFilters);
    params.put(PackagesResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, responseFilters);

    serve("/*").with(GuiceContainer.class, params);
  }

  /**
   * Set the list of packages that we will search for providers and the like
   * @param additionalPackages The packages above and beyond our standard list
   */
  private void setPackages(final String... additionalPackages)
  {
    String[] packagesList = ObjectArrays.concat("com.wealdtech.jersey", additionalPackages);
    packagesList = ObjectArrays.concat("com.yammer.metrics", packagesList);
    this.packages = Joiner.on(',').skipNulls().join(packagesList);
  }

  /**
   * Convert a varargs of clases in to a comma-separated string of class names
   */
  private String joinClassNames(final Class<?>... clazz)
  {
    Function<Class<?>, String> classToName = new Function<Class<?>, String>()
    {
      @Override
      public String apply(Class<?> klazz)
      {
        return klazz.getName();
      }
    };
    List<String> names = Lists.transform(Lists.newArrayList(clazz), classToName);

    return Joiner.on(',').skipNulls().join(names);
  }
}
