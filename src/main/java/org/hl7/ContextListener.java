package org.hl7;

import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;

import org.ebaysf.web.cors.CORSFilter;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class ContextListener extends GuiceServletContextListener {

	private Injector injector;

	public ContextListener() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	@Override
	protected Injector getInjector() {
		injector = Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(MLLPtoHTTP.class).asEagerSingleton();
			}
		}, new JerseyServletModule() {

			@Override
			protected void configureServlets() {
				final Map<String, String> params = ImmutableMap.<String, String> builder()
						.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
								GZIPContentEncodingFilter.class.getName())
						.build();
				bind(CORSFilter.class).in(Singleton.class);
				serve("/mllp/*").with(GuiceContainer.class, params);
				serve("/http*").with(HTTPtoMLLP.class);
				filter("/*").through(CORSFilter.class);
			}
		});
		return injector;
	}

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		injector.getInstance(MLLPtoHTTP.class).tearDown();
		super.contextDestroyed(servletContextEvent);
	}
}
