package org.hl7;

import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.hoh.hapi.server.HohServlet;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;

@Singleton
public class HTTPtoMLLP extends HohServlet {

	public static final Logger logger = LoggerFactory.getLogger(HTTPtoMLLP.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -5851058131169148425L;
	private int port;
	private String host;
	private Connection connection;

	@Override
	public void init(ServletConfig config) throws ServletException {

		try {
			port = Integer.parseInt(config.getServletContext().getInitParameter("H2M-ER7-port"));
		} catch (Exception e) {
			logger.warn("Using port 2576 as default", e);
			port = 2576;
		}

		host = config.getServletContext().getInitParameter("H2M-ER7-host");
		if (Strings.isNullOrEmpty(host)) {
			logger.warn("Using localhost as default");
			host = "localhost";
		}

		try {
			connection = new DefaultHapiContext().getConnectionHub().attachLazily(host, port, false);
		} catch (HL7Exception e) {
			throw new RuntimeException(e);
		}
		setApplication(new ReceivingAllApplication());
	}

	class ReceivingAllApplication implements ReceivingApplication {

		@Override
		public Message processMessage(Message theMessage, Map<String, Object> theMetadata)
				throws ReceivingApplicationException, HL7Exception {

			try {
				return connection.getInitiator().sendAndReceive(theMessage);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean canProcess(Message theMessage) {
			return true;
		}
	}
}
