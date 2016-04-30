package org.hl7;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.hoh.api.IReceivable;
import ca.uhn.hl7v2.hoh.hapi.client.HohClientSimple;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;

@Path("/")
public class MLLPtoHTTP {
	private int port = 2575;
	private URI theUrl;
	private HohClientSimple httpClient;
	private SimpleServer pipesServer;

	public static final Logger logger = LoggerFactory.getLogger(MLLPtoHTTP.class);

	@Inject
	public MLLPtoHTTP(@Context ServletContext context) throws MalformedURLException {

		try {
			port = Integer.parseInt(context.getInitParameter("M2H-ER7-port"));
		} catch (Exception e) {
			logger.warn("Using port 2575 as default", e);
			port = 2575;
		}

		try {
			theUrl = URI.create(context.getInitParameter("M2H-HTTP-URL"));
		} catch (Exception e) {
			logger.warn("Using address http://localhost:8080/http as default", e);
			theUrl = URI.create("http://localhost:8080/http");
		}
		pipesServer = new SimpleServer(port);
		pipesServer.registerApplication(new ReceivingAllApplication());
		pipesServer.start();
		httpClient = new HohClientSimple(theUrl.toURL());
	}
	
	public void tearDown()
	{
		pipesServer.stop();
		httpClient.close();
	}

	@GET
	@Path("info")
	public String getInfo() {
		return "Hosting MLLP on " + port + " and sending it to " + theUrl.toString();
	}

	class ReceivingAllApplication implements ReceivingApplication {

		public Message processMessage(Message theMessage, Map<String, Object> theMetadata)
				throws ReceivingApplicationException, HL7Exception {
			try {
				IReceivable<Message> response = httpClient.sendAndReceiveMessage(theMessage);
				return response.getMessage();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public boolean canProcess(Message theMessage) {
			return true;
		}
	}
}
