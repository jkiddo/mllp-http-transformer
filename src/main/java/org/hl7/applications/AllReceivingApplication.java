package org.hl7.applications;

import java.util.Map;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;

public class AllReceivingApplication implements ReceivingApplication {

	private ISender routingConnection;

	public AllReceivingApplication(ISender routingConnection) {
		this.routingConnection = routingConnection;
	}

	@Override
	public Message processMessage(Message theMessage, Map<String, Object> theMetadata)
			throws ReceivingApplicationException, HL7Exception {

		try {
			return routingConnection.send(theMessage, theMetadata);
		} catch (Exception e) {
			throw new HL7Exception(e.getMessage(), e);
		}
	}

	@Override
	public boolean canProcess(Message theMessage) {
		return true;
	}
}