/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.lib.fcrepo.camel.routing;

import static org.apache.camel.builder.PredicateBuilder.and;
import static org.apache.camel.builder.PredicateBuilder.or;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.fcrepo.camel.processor.EventProcessor;
import org.slf4j.Logger;

public class Router extends RouteBuilder{
    private static final Logger logger = getLogger(Router.class);

    private static final String JMS_IDENTIFIER = "org.fcrepo.jms.identifier";

    private static final String JMS_USER = "org.fcrepo.jms.user";

    private static final String BATCH_HEADER = "UMDBatchEvent";

    public void configure() throws Exception {

        /*
         * A generic error handler (specific to this RouteBuilder)
         */
        onException(Exception.class)
            .maximumRedeliveries("{{error.maxRedeliveries}}")
            .log("Routing Error: ${routeId}");

        /*
         * route a message to the proper queue, based on whether
         * it is a live event or batch event
         */
        from("broker:{{input.queue.name}}")
            .routeId("UmdFcrepoEventRouter")
            .process(new EventProcessor())
            .log(LoggingLevel.INFO, logger, "Routing event with id: " + headerString(JMS_IDENTIFIER))
            .log(LoggingLevel.DEBUG, logger, "Event user: " + headerString(JMS_USER))
            .choice()
                .when(simple(headerString(JMS_USER) + " == '{{batch.user}}'"))
                    .to("direct:batch.queue")
                .otherwise()
                    .to("direct:live.queue");

        from("direct:batch.queue")
            .log(LoggingLevel.DEBUG, logger, "Routed to batch queue.")
            .choice()
                .when(
                    or(
                        and(
                            simple("{{batch.skip.pcdm.container}} == 'true'"),
                            simple(headerString(JMS_IDENTIFIER) + " == '/pcdm'")
                        ),
                        simple(headerString(JMS_IDENTIFIER) + " in {{batch.skip.paths}}")
                    )
                    )
                    .log(LoggingLevel.DEBUG, logger,
                        "Suppressing '" + headerString(JMS_IDENTIFIER) + "' node event for batch user.")
                    .stop()
                .otherwise()
                    .setHeader("JMSPriority").simple("{{batch.jms.priority}}").end()
                    .setHeader(BATCH_HEADER).simple("true").end()
                    .to("broker:{{batch.queue.name}}");

        from("direct:live.queue")
            .log(LoggingLevel.DEBUG, logger, "Routed to live queue.")
            .to("broker:{{live.queue.name}}");
    }

    private String headerString(String headerName) {
        return "${header." + headerName + "}";
    }

}