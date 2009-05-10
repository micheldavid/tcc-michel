package appman.portlets.jobsubmit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.portlets.AppManHelper;

public class UploadDagFileServlet extends HttpServlet {

	private static final long serialVersionUID = -7611206237654360476L;

	private static final Log log = LogFactory.getLog(UploadDagFileServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String response = "";
		try {
			execute(req);
			response = "parent.appmanJobSubmitDagReply();";
		} catch (Exception ex) {
			log.error(ex, ex);
			response = "alert(\"" + ex.getMessage().replace("\"", "\\\"") + "\");";
		}
		String output = "<html><head></head><body><script type=\"text/javascript\">" + response + "</script></body></html>";
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().write(output);
		resp.getWriter().close();
	}

	private void execute(HttpServletRequest req) throws Exception {
		if (!ServletFileUpload.isMultipartContent(req)) {
			throw new Exception("requisição não é multipart");
		}
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(factory.getSizeThreshold());

		List<FileItem> items = upload.parseRequest(req);
		HashMap<String, Object> reqData = new HashMap<String, Object>(items.size());
		for (FileItem item : items) {
			if (item.isFormField()) {
				reqData.put(item.getFieldName(), item.getString());
			} else {
				reqData.put(item.getFieldName(), item);
			}
		}

		AppManHelper.setupJob((String) reqData.get("userName"), (FileItem) reqData.get("file"));
	}

}
