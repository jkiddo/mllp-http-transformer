package org.hl7;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.hl7.applications.AllReceivingApplication;
import org.hl7.applications.ISender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.hoh.api.DecodeException;
import ca.uhn.hl7v2.hoh.api.EncodeException;
import ca.uhn.hl7v2.hoh.hapi.client.HohClientSimple;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;

@Path("/")
public class MLLPtoHTTP {
	
	private int port = 2575;
	private int timeout = 5000;
	private URI theUrl;
	private HohClientSimple httpClient;
	private SimpleServer pipesServer;

	public static final Logger logger = LoggerFactory.getLogger(MLLPtoHTTP.class);

	@Inject
	public MLLPtoHTTP(@Context ServletContext context) throws MalformedURLException {

		try {
			port = Integer.parseInt(context.getInitParameter("M2H-ER7-port"));
		} catch (Exception e) {
			port = 2575;
			logger.warn("Using port " + port + " as default as port could not be read from servlet context: " + e.getMessage(),
					e);
		}
		
		try {
			timeout = Integer.parseInt(context.getInitParameter("M2H-ER7-timeout"));
		} catch (Exception e) {
			timeout = 5000;
			logger.warn("Using " + timeout + "ms as timeout default as it could not be read from servlet context: " + e.getMessage(),
					e);
		}

		try {
			theUrl = URI.create(context.getInitParameter("M2H-HTTP-URL"));
		} catch (Exception e) {
			logger.warn(
					"Using address http://localhost:8080/http as default as address could not be read from servlet context: "
							+ e.getMessage(),
					e);
			theUrl = URI.create("http://localhost:8080/http");
		}

		httpClient = new HohClientSimple(theUrl.toURL());
		httpClient.setResponseTimeout(timeout);
		pipesServer = new SimpleServer(port);
		pipesServer.registerApplication(new AllReceivingApplication(new ISender() {
			
			@Override
			public Message send(Message theMessage, Map<String, Object> theMetadata) throws EncodingNotSupportedException, HL7Exception, DecodeException, IOException, EncodeException {
				return httpClient.sendAndReceiveMessage(theMessage).getMessage();
			}
		}));
		pipesServer.start();
	}

	public void tearDown() {
		pipesServer.stop();
		httpClient.close();
	}

	@GET
	@Path("info")
	public String getInfo() {
		return "Hosting MLLP on " + port + " and sending it to " + theUrl.toString();
	}
}
