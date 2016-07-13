package org.hl7;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.hl7.RoutingConfiguration.MLLPtoHTTPConfiguration;
import org.hl7.applications.AllReceivingApplication;
import org.hl7.applications.ISender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.hoh.api.DecodeException;
import ca.uhn.hl7v2.hoh.api.EncodeException;
import ca.uhn.hl7v2.hoh.hapi.client.HohClientSimple;
import ca.uhn.hl7v2.hoh.util.HTTPUtils;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;

@Path("/")
public class MLLPtoHTTPServlet {

	private HohClientSimple httpClient;
	private SimpleServer pipesServer;
	private MLLPtoHTTPConfiguration configuration;

	public static final Logger logger = LoggerFactory.getLogger(MLLPtoHTTPServlet.class);

	@Inject
	public MLLPtoHTTPServlet(MLLPtoHTTPConfiguration configuration) throws MalformedURLException {
		this.configuration = configuration;

		httpClient = new HohClientSimple(configuration.getTheURI().toURL());
		httpClient.setResponseTimeout(configuration.getTimeout());
		pipesServer = new SimpleServer(configuration.getPort());
		pipesServer.registerApplication(new AllReceivingApplication(new ISender() {

			@Override
			public Message send(Message theMessage, Map<String, Object> theMetadata)
					throws EncodingNotSupportedException, HL7Exception, DecodeException, IOException, EncodeException {
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
	public void getInfo(@Context HttpServletRequest theReq, @Context HttpServletResponse theResp) throws IOException {

		theResp.setStatus(400);
		theResp.setContentType("text/html");

		String message = "Hosting MLLP service on " + theReq.getRemoteHost() + ":" + configuration.getPort()
				+ " and sending it over HTTP to " + configuration.getTheURI().toString();
		HTTPUtils.write400BadRequest(theResp.getOutputStream(), message, false);

	}
}
