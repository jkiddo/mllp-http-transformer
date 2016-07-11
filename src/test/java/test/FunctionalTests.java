package test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.hl7.ContextListener;
import org.hl7.applications.AllReceivingApplication;
import org.hl7.applications.ISender;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.google.inject.servlet.GuiceFilter;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.hoh.api.DecodeException;
import ca.uhn.hl7v2.hoh.api.EncodeException;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.message.ACK;
import ca.uhn.hl7v2.model.v251.message.ADT_A01;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;

@RunWith(RandomBlockJUnit4ClassRunner.class)
public class FunctionalTests {

	private static SimpleServer mllpServer;
	private static Server httpServer;
	private static Connection mllpClient;

	private static Message lastReceived;
	private ADT_A01 message;
	private static DefaultHapiContext context;

	@BeforeClass
	public static void beforeClass() throws Exception {
		httpServer = new Server(8080);
		final ServletContextHandler sch = new ServletContextHandler(httpServer, "/");
		sch.addEventListener(new ContextListener());
		sch.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

		mllpServer = new SimpleServer(2576);
		httpServer.start();
		mllpServer.start();
		context = new DefaultHapiContext();
		mllpClient = context.getConnectionHub().attachLazily("localhost", 2575, false);
		mllpServer.registerApplication(new AllReceivingApplication(new ISender() {

			@Override
			public Message send(Message theMessage, Map<String, Object> theMetadata)
					throws EncodingNotSupportedException, HL7Exception, DecodeException, IOException, EncodeException,
					LLPException {
				lastReceived = theMessage;
				return theMessage.generateACK();
			}
		}));
	}

	@AfterClass
	public static void afterClass() throws Exception {
		mllpServer.stop();
		httpServer.stop();
		mllpClient.close();
		context.close();
	}

	@Before()
	public void before() throws Exception {

		if (!mllpServer.isRunning())
			mllpServer.start();
		if (httpServer.isStopped())
			httpServer.start();
		if (!mllpClient.isOpen())
			mllpClient.activate();

		message = new ADT_A01();
		message.initQuickstart("ADT", "A01", UUID.randomUUID().toString());
	}

	@Test
	public void verifyReceiver() throws HL7Exception, IOException, LLPException {
		mllpClient.getInitiator().sendAndReceive(message);
		assertThat(message.encode(), equalTo(lastReceived.encode()));
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void mllpServerDown() throws HL7Exception, IOException, LLPException, InterruptedException {
		mllpServer.stop();
		Thread.sleep(1000);
		ACK returned = (ACK) mllpClient.getInitiator().sendAndReceive(message);

		assertThat(message.getMSH().getMessageControlID().getValue(),
				equalTo(returned.getMSA().getMessageControlID().getValue()));
		assertThat(returned.getERR().getHL7ErrorCode().getCwe9_OriginalText().getValue(), containsString("Exception"));
	}

	@Test
	public void httpServerDown() throws Exception {
		httpServer.stop();
		Thread.sleep(1000);

		thrown.expect(HL7Exception.class);

		mllpClient.getInitiator().sendAndReceive(message);
	}

	@Test
	public void testThroughput() throws Exception {

		for (int i = 0; i < 1000; i++) {
			mllpClient.getInitiator().sendAndReceive(message);
		}
	}
}
