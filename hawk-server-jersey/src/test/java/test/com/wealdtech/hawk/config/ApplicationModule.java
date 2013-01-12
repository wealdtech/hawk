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

package test.com.wealdtech.hawk.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.wealdtech.DataError;
import com.wealdtech.configuration.ConfigurationSource;
import com.wealdtech.http.JettyServerConfiguration;

public class ApplicationModule extends AbstractModule
{
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationModule.class);

  @Override
  protected void configure()
  {
    try
    {
      // Bind configuration information
      final JettyServerConfiguration configuration = new ConfigurationSource<JettyServerConfiguration>().getConfiguration("config-test.json", JettyServerConfiguration.class);
      bind(JettyServerConfiguration.class).toInstance(configuration);
    }
    catch (DataError de)
    {
      LOGGER.error("Failed to initialize properties: {}", de.getLocalizedMessage(), de);
    }
  }
}
