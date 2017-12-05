package com.be.axeldlv.activemqcamel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JmsRoute extends RouteBuilder {

	static final Logger log = LoggerFactory.getLogger(JmsRoute.class);
	
	@Override
	public void configure() throws Exception {
		from("{{inbound.queue}}")
		.transacted()
		.log(LoggingLevel.INFO, log, "Received Message")
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				log.info("Exchange: {}", exchange);
			}
		})
		.loop()
		.simple("{{outbound.loop.count}}")
		.to("{{outbound.queue}}")
		.log(LoggingLevel.INFO, log, "Message Sent. Iteration: ${property.CamelLoopIndex}")
		.end();
	}

}
