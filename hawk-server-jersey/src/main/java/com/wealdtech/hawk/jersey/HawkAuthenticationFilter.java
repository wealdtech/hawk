package com.wealdtech.hawk.jersey;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class HawkAuthenticationFilter implements ContainerRequestFilter {

	@Override
	public ContainerRequest filter(final ContainerRequest request)
	{
		System.err.println("Authentication *cough*");
		return request;
	}
}
