package org.hl7;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.RoutingConfiguration.HTTPtoMLLPConfiguration;
import org.hl7.applications.AllReceivingApplication;
import org.hl7.applications.ISender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.hoh.api.DecodeException;
import ca.uhn.hl7v2.hoh.api.EncodeException;
import ca.uhn.hl7v2.hoh.hapi.server.HohServlet;
import ca.uhn.hl7v2.hoh.util.HTTPUtils;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;

@Singleton
public class HTTPtoMLLPServlet extends HohServlet {

	/**
	 * 
	 */
	public static final Logger logger = LoggerFactory.getLogger(HTTPtoMLLPServlet.class);
	private static final long serialVersionUID = -3852050052170456780L;
	private Connection connection;
	private HTTPtoMLLPConfiguration configuration;

	@Inject
	public HTTPtoMLLPServlet(final HTTPtoMLLPConfiguration configuration) {
		this.configuration = configuration;

		try {
			@SuppressWarnings("resource")
			DefaultHapiContext context = new DefaultHapiContext();
			connection = context.getConnectionHub().attachLazily(configuration.getHost(), configuration.getPort(),
					false);
		} catch (HL7Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		setApplication(new AllReceivingApplication(new ISender() {

			@Override
			public Message send(Message theMessage, Map<String, Object> theMetadata)
					throws EncodingNotSupportedException, HL7Exception, DecodeException, IOException, EncodeException,
					LLPException {
				Initiator initiator = connection.getInitiator();
				initiator.setTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS);
				return initiator.sendAndReceive(theMessage);
			}
		}));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {

		theResp.setStatus(400);
		theResp.setContentType("text/html");

		String message = "Hosting HTTP service here and sending it over MLLP to " + configuration.getHost() + ":"
				+ configuration.getPort();
		HTTPUtils.write400BadRequest(theResp.getOutputStream(), message, false);

	}
}
