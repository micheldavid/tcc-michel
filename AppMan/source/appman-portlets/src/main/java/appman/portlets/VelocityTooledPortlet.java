package appman.portlets;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.ToolManager;
import org.ogce.portlets.VelocityPortlet;

public class VelocityTooledPortlet extends VelocityPortlet {

	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		PortletContext portletContext = getPortletContext();

		String param = config.getInitParameter("org.apache.velocity.toolbox");
		// checks if org.apache.velocity.toolbox exists
		if (param != null) {
			ToolManager tools = new ToolManager(false, false);
			tools.configure(portletContext.getRealPath(param));
			portletContext.setAttribute("appman.portlets.toolmanager", tools);
		}
	}

	@Override
	protected VelocityContext getContext(PortletRequest request, PortletResponse response) {

		PortletSession session = request.getPortletSession(true);

		if (session.getAttribute("org.ogce.portlets.VelocityPortlet.context") == null) {
			VelocityContext ctx = null;
			ToolManager tools = (ToolManager) getPortletContext().getAttribute("appman.portlets.toolmanager");
			if (tools != null) {
				ctx = new VelocityContext(tools.createContext());
				session.setAttribute("org.ogce.portlets.VelocityPortlet.context", ctx);
			}
		}

		return super.getContext(request, response);
	}
}
