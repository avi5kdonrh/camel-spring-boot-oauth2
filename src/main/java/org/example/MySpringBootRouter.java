package org.example;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMethods;
import org.apache.camel.spi.DataFormat;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Override
    public void configure() {

        restConfiguration()
        .contextPath("/rest")
        .component("servlet");

        rest()
       .get("/get")
       .route()
       .setBody(constant("{\"Hi\":\"Test\"}"))
       .to("stream:out");

        from("timer:hello?period=10s").routeId("hello")
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/x-www-form-urlencoded"))
                .setHeader("Accept",constant("application/json"))
                .setBody()
                .constant("grant_type=password&client_id=camel&username=admin&password=admin")
                .to("http://localhost:8080/realms/camel/protocol/openid-connect/token")
                .unmarshal().json()
                .setHeader("jwt")
                .jsonpath(".access_token")
                .removeHeaders("CamelHttp*")
                .removeHeaders("Set-Cookie")
                .setBody(constant(null))
                .setHeader("Authorization", simple("Bearer ${header.jwt}"))
                .log("Calling Service ${headers}")
                .to("http://localhost:8082/rest/get");
    }

}
