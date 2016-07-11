package org.hl7.applications;

import java.io.IOException;
import java.util.Map;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.hoh.api.DecodeException;
import ca.uhn.hl7v2.hoh.api.EncodeException;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;

public interface ISender {

	public Message send(Message theMessage, Map<String, Object> theMetadata) throws EncodingNotSupportedException, HL7Exception, DecodeException, IOException, EncodeException, LLPException;
}
