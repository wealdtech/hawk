package com.wealdtech.hawk.guice;

import com.google.inject.AbstractModule;
import com.wealdtech.hawk.HawkServerConfiguration;

/**
 * Make Hawk configuration available to Guice
 */
public class HawkServerConfigurationModule extends AbstractModule
{
  private final HawkServerConfiguration configuration;

  public HawkServerConfigurationModule(final HawkServerConfiguration configuration)
  {
    super();
    this.configuration = configuration;
  }

  @Override
  protected void configure()
  {
    binder().bind(HawkServerConfiguration.class).toInstance(this.configuration);
  }
}
