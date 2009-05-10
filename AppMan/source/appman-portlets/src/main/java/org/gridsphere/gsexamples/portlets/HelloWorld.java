package org.gridsphere.gsexamples.portlets;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * a simple HelloWorld Portlet
 */
public class HelloWorld extends GenericPortlet {

    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h1>Hello World</h1>");
    }

}