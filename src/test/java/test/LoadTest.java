package test;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.hl7.ContextListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.servlet.GuiceFilter;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.message.ADT_A01;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;

public class LoadTest {

	private SimpleServer ss;
	private Server server;
	private Connection c;

	@Before
	public void before() throws Exception {
		server = new Server(8080);
		final ServletContextHandler sch = new ServletContextHandler(server, "/");
		sch.addEventListener(new ContextListener());
		sch.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		sch.addServlet(DefaultServlet.class, "/");
		

		ss = new SimpleServer(2576);
		ss.registerApplication(new ReceivingApplication() {

			@Override
			public Message processMessage(Message theMessage, Map<String, Object> theMetadata)
					throws ReceivingApplicationException, HL7Exception {
				try {
					return theMessage.generateACK();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}

			@Override
			public boolean canProcess(Message theMessage) {
				return true;
			}
		});
		server.start();
		ss.start();
		c = new DefaultHapiContext().getConnectionHub().attachLazily("localhost", 2575, false);
	}

	@After
	public void after() throws Exception {
		ss.stop();
		server.stop();
		c.close();
	}

	@Test
	public void test() throws Exception {

		System.out.println("Start: " + new Date(System.currentTimeMillis()));
		ADT_A01 m = new ADT_A01();
		m.initQuickstart("ADT", "A01", UUID.randomUUID().toString());
		for (int i = 0; i < 1000; i++) {
			Message respn = c.getInitiator().sendAndReceive(m);
		}
		System.out.println("Stop: " + new Date(System.currentTimeMillis()));
	}
}
