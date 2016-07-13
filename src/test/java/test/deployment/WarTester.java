package test.deployment;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class WarTester {

	public static void main(String[] args) throws Exception {
		final Server server = new Server(8080);
		server.setHandler(new WebAppContext("target/mllp-http-transformer-1.0.0.war", "/"));
		server.start();
	}

}
