package org.hl7;

import java.net.URI;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class RoutingConfiguration {

	public static final Logger logger = LoggerFactory.getLogger(RoutingConfiguration.class);
	private HTTPtoMLLPConfiguration httpToMLLP;
	private MLLPtoHTTPConfiguration mllpToHTTP;

	public RoutingConfiguration(HTTPtoMLLPConfiguration httpToMLLP, MLLPtoHTTPConfiguration mllpToHTTP) {
		this.httpToMLLP = httpToMLLP;
		this.mllpToHTTP = mllpToHTTP;
	}

	public HTTPtoMLLPConfiguration getHttpToMLLP() {
		return httpToMLLP;
	}

	public MLLPtoHTTPConfiguration getMllpToHTTP() {
		return mllpToHTTP;
	}

	public static RoutingConfiguration transform(ServletContext servletContext) {

		HTTPtoMLLPConfiguration httpToMLLP = new HTTPtoMLLPConfiguration(servletContext);
		MLLPtoHTTPConfiguration mllpToHTTP = new MLLPtoHTTPConfiguration(servletContext);
		return new RoutingConfiguration(httpToMLLP, mllpToHTTP);
	}

	public static class HTTPtoMLLPConfiguration {

		private int port = 2576;
		private int timeout = 5000;
		private String host;

		public int getPort() {
			return port;
		}

		public int getTimeout() {
			return timeout;
		}

		public String getHost() {
			return host;
		}

		public HTTPtoMLLPConfiguration(ServletContext servletContext) {
			try {
				port = Integer.parseInt(servletContext.getInitParameter("H2M-ER7-port"));
			} catch (Exception e) {
				logger.warn("Using port " + port + " as default as port could not be read from servlet context");
				logger.debug(e.getMessage(), e);
			}

			try {
				timeout = Integer.parseInt(servletContext.getInitParameter("H2M-ER7-timeout"));
			} catch (Exception e) {
				logger.warn("Using " + timeout + "ms as timeout default as it could not be read from servlet context");
				logger.debug(e.getMessage(), e);
			}

			host = servletContext.getInitParameter("H2M-ER7-host");
			if (Strings.isNullOrEmpty(host)) {
				host = "localhost";
				logger.warn("Using " + host + " as default as hostname could not be read from servlet context");
			}
		}

	}

	public static class MLLPtoHTTPConfiguration {

		public int getPort() {
			return port;
		}

		public int getTimeout() {
			return timeout;
		}

		public URI getTheURI() {
			return theUrl;
		}

		private int port = 2575;
		private int timeout = 5000;
		private URI theUrl = URI.create("http://localhost:8080/http");

		public MLLPtoHTTPConfiguration(ServletContext servletContext) {
			try {
				port = Integer.parseInt(servletContext.getInitParameter("M2H-ER7-port"));
			} catch (Exception e) {
				port = 2575;
				logger.warn("Using port " + port + " as default as port could not be read from servlet context");
				logger.debug(e.getMessage(), e);
			}

			try {
				timeout = Integer.parseInt(servletContext.getInitParameter("M2H-ER7-timeout"));
			} catch (Exception e) {
				timeout = 5000;
				logger.warn("Using " + timeout + "ms as timeout default as it could not be read from servlet context");
				logger.debug(e.getMessage(), e);
			}

			try {
				theUrl = URI.create(servletContext.getInitParameter("M2H-HTTP-URL"));
			} catch (Exception e) {
				logger.warn(
						"Using address " + theUrl.toString()
								+ " as default as address could not be read from servlet context");
				logger.debug(e.getMessage(), e);
			}
		}

	}
}
