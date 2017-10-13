# HL7 Protocol transformer
An HTTP/MLLP and MLLP/HTTP transformer (suitable for non-restricted servlet environments and OSGi environments)

This project consists of 2 servlets bundled together making it transparent to consumers and senders whether MLLP or HTTP is being used as a transport format for HL7v2 messages. The servlet spawns threads (controlled by the default thread factory of HAPI - so it is not totally havoc) when consuming and sending so deploying in a strict servlet container is probably not an option. It can however be used as a proxy in front of whatever services you have in your environment that fit your needs - being HTTP or MLLP - or both. Enjoy!

## Default setup is the following:

MLLP-2-HTTP:

- inbound MLLP port: 2575
- outbound URL: http://localhost:8080/http

HTTP-2-MLLP:

- inbound URL: <wherever the servlet is deployed> (in OSGi environments this is controlled by the Web-ContextPath in the pom.xml)
- outbound MLLP port: 2576
- outbound MLLP host: localhost

All values are configurable in the web.xml

'Quick-and-dirty' deploy can be done using 'java -jar jetty-runner.jar mllp-http-transformer.war'


PS. On my sloppy machine, 200 'protocol format transformations' can be conducted pr. sec.

[<img src="https://github.com/jkiddo/mllp-http-transformer/raw/master/HoH_Relay_extended.png">](https://github.com/jkiddo/mllp-http-transformer)
