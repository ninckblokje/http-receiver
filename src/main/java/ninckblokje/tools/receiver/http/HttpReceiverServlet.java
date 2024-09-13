/*
 * Copyright (c) 2024, ninckblokje
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ninckblokje.tools.receiver.http;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;

@WebServlet("/")
@RequestScoped
public class HttpReceiverServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpReceiverServlet.class);

    @Inject
    @ConfigProperty(name = "httpReceiverLogAuthorizationHeader", defaultValue = "false")
    private boolean logAuthorizationHeader;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("======================================================================");
        LOGGER.info("Received request from: {}, on: {}:{}", req.getRemoteAddr(), req.getMethod(), req.getRequestURI());
        LOGGER.info("SSL enabled: {}, TLS version: {}, cipher: {}", req.isSecure(), req.getAttribute("jakarta.servlet.request.secure_protocol"), req.getAttribute("jakarta.servlet.request.cipher_suite"));

        LOGGER.info("- With headers:");
        req.getHeaderNames().asIterator()
                .forEachRemaining(header -> LOGGER.info("\t{}: {}", header, getHeaderValue(req, header)));

        LOGGER.info("- With body:");
        req.getReader().lines()
                .map("\t%s"::formatted)
                .forEach(LOGGER::info);

        super.service(req, resp);

        resp.setStatus(OK.getStatusCode());
        resp.setContentType(APPLICATION_JSON);

        try (var jsonb = JsonbBuilder.create()) {
            var requestDetails = buildRequestDetails(req);
            resp.getWriter().print(jsonb.toJson(requestDetails));
        } catch (Exception ex) {
            LOGGER.error("Exception while using Jsonb", ex);
            throw new ServletException(ex);
        }
    }

    RequestDetails buildRequestDetails(HttpServletRequest req) {
        var headers = new HashMap<String, String>();
        req.getHeaderNames().asIterator()
                .forEachRemaining(header -> headers.put(header, getHeaderValue(req, header)));

        return new RequestDetails(
                req.getRemoteAddr(),
                req.getRequestURI(),
                req.getMethod(),
                req.isSecure(),
                "%s".formatted(req.getAttribute("jakarta.servlet.request.secure_protocol")),
                "%s".formatted(req.getAttribute("jakarta.servlet.request.cipher_suite")),
                headers);
    }

    String getHeaderValue(HttpServletRequest req, String header) {
        return (logAuthorizationHeader || !"Authorization".equalsIgnoreCase(header)) ? req.getHeader(header) : "***";
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {}

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {}

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {}
}
