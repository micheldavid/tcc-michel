package appman.portlets.jobsubmit;

import java.sql.SQLException;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;

import appman.portlets.AppManConfig;
import appman.portlets.AppManDBHelper;
import appman.portlets.AppManHelper;
import appman.portlets.VelocityTooledPortlet;

public class JobSubmitAction extends VelocityTooledPortlet {
	
	private static final Log log = LogFactory.getLog(JobSubmitAction.class);

    public void doShowHelp(ActionRequest request, ActionResponse response, Context context) {
    	doShowHelp((PortletRequest) request, (PortletResponse) response, context);
    }

    public void doShowHelp(PortletRequest request, PortletResponse response, Context context) {

    	setTemplate(request, "jobsubmit-help.vm");
		context.put("config", AppManConfig.get());
	}

    public void buildViewContext(RenderRequest request, RenderResponse response, Context context) {
    	doShowView(request, response, context);
	}

    public void doShowView(ActionRequest request, ActionResponse response, Context context) {
    	doShowView((PortletRequest) request, (PortletResponse) response, context);
    }

    public void doShowView(PortletRequest request, PortletResponse response, Context context) {
    	try {
			context.put("jobs", AppManDBHelper.searchJobs());
		} catch (SQLException e) {
			log.error(e, e);
		}
    	setTemplate(request, "jobsubmit-view.vm");
	}

	public void doDeleteJobs(ActionRequest request, ActionResponse response, Context context) {
		String[] ids = request.getParameterValues("jobId");

		for (String id : ids) {
			try {
				AppManHelper.deleteJob(Integer.parseInt(id));
			} catch (NumberFormatException ex) {
			} catch (Exception e) {
				log.error("erro excluindo tarefa " + id, e);
				context.put("error", e.getMessage());
				break;
			}
		}
		doShowView(request, response, context);
	}

    public void doNewJob(ActionRequest request, ActionResponse response, Context context) {
    	context.put("userName", getPortalUsername(request));

    	setTemplate(request, "jobsubmit-submit.vm");
	}

    private String getPortalUsername(PortletRequest request) {
		Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
		String username = (userInfo != null) ? (String) userInfo.get("user.name") : null;
		if (username == null || "null".equals(username)) {
			username = request.getRemoteUser();
		}
		return username;
	}

}
