package appman.portlets.filebrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.portlets.AppManConfig;
import appman.portlets.AppManDBHelper;
import appman.portlets.model.AppManJob;

public class DownloadFileServlet extends HttpServlet {

	private static final long serialVersionUID = 2310566629897977346L;

	private static final Log log = LogFactory.getLog(DownloadFileServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String runningJobId = null;
		try {
			ArrayList<AppManJob> jobs = AppManDBHelper.searchRunningJobs();
			if (!jobs.isEmpty() && jobs.size() == 1) runningJobId = String.valueOf(jobs.get(0).getId());
		} catch (SQLException ex) {
			log.error("searching running jobs", ex);
		}

		String file = req.getParameter("file");
		if (file.contains("..")) throw new ServletException("falha de segurança");
		boolean isRunningJob = runningJobId == null ? false : file.startsWith(runningJobId);
		File toDownload;
		if (isRunningJob) {
			toDownload = new File(AppManConfig.get().getString("exehda.log.dir"), file.substring(file.indexOf('/')));
		} else {
			toDownload = new File(AppManConfig.get().getString("appman.portlets.job.dir"), file);
		}
		if (toDownload.isDirectory()) throw new ServletException("download de pastas não implementado");
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition", "attachment; filename=" + toDownload.getName());

		if (isRunningJob) {
			// para não atrapalhar o trabalho do appman, le tudo para a memória
			FileInputStream fis = new FileInputStream(toDownload);
			byte[] data = new byte[file.length()];
			fis.read(data);
			fis.close();
			resp.getOutputStream().write(data);
		} else {

			FileInputStream fis = new FileInputStream(toDownload);
			byte[] data = new byte[10240];
			for (int read; (read = fis.read(data)) != -1;) {
				resp.getOutputStream().write(data, 0, read);
			}
			fis.close();
		}

		resp.getOutputStream().close();
	}

}
