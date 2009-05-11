package appman.portlets.filebrowser;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

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
import appman.portlets.DirViewHelper;
import appman.portlets.VelocityTooledPortlet;
import appman.portlets.model.AppManJob;
import appman.portlets.model.DirectoryView;

public class FileBrowserAction extends VelocityTooledPortlet {

	private static final Log log = LogFactory.getLog(FileBrowserAction.class);

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
		context.put("root", loadDirectories());
		setTemplate(request, "filebrowser-view.vm");
	}

	public ArrayList<DirectoryView> loadDirectories() {
		String runningJobId = null;
		try {
			ArrayList<AppManJob> jobs = AppManDBHelper.searchRunningJobs();
			if (!jobs.isEmpty() && jobs.size() == 1) runningJobId = String.valueOf(jobs.get(0).getId());
		} catch (SQLException ex) {
			log.error("searching running jobs", ex);
		}

		ArrayList<DirectoryView> root = new ArrayList<DirectoryView>();
		File jobs = new File(AppManConfig.get().getString("appman.portlets.job.dir"));
		for (File f : jobs.listFiles()) {
			if (f.isDirectory()) {
				DirectoryView view = loadDirectory(null, f);
				if (view == null) continue;

				if (f.getName().equals(runningJobId)) {
					view = mergeDirectories(view, loadDirectory(null, new File(AppManConfig.get().getString(
						"appman.portlets.job.dir"))));
				}
				root.add(view);
			}
		}
		return root;
	}

	public static void main(String[] args) {
		DirectoryView dirview = new FileBrowserAction().loadDirectory(null, new File("C:\\NVIDIA"));
		System.out.println(new DirViewHelper().getPath(dirview.getDirectories().iterator().next()));
	}
	
	private DirectoryView loadDirectory(DirectoryView parent, File fld) {
		if (fld.getName().startsWith(".")) return null;

		DirectoryView view = new DirectoryView(parent, fld.getName());
		ArrayList<DirectoryView> dirs = new ArrayList<DirectoryView>();
		ArrayList<String> files = new ArrayList<String>();

		for (File f : fld.listFiles()) {
			if (f.isDirectory()) {
				DirectoryView loaded = loadDirectory(view, f);
				if (loaded != null) dirs.add(loaded);
			} else files.add(f.getName());
		}
		view.setDirectories(dirs);
		view.setFiles(files);
		return view;
	}

	private DirectoryView mergeDirectories(DirectoryView view1, DirectoryView view2) {
		DirectoryView merged = new DirectoryView(view1.getParent(), view1.getName());
		ArrayList<DirectoryView> mergedDirs = new ArrayList<DirectoryView>();
		LinkedHashSet<String> mergedFiles = new LinkedHashSet<String>();
		merged.setDirectories(mergedDirs);
		merged.setFiles(mergedFiles);

		for (DirectoryView dir : view1.getDirectories()) {
			DirectoryView dirv1 = new DirectoryView(view1, dir.getName());
			dirv1.setDirectories(new ArrayList<DirectoryView>());
			dirv1.setFiles(new ArrayList<String>());
			mergedDirs.add(mergeDirectories(dirv1, dir));
		}
		for (DirectoryView dir : view2.getDirectories()) {
			DirectoryView mergedDir = findDirectory(merged, dir.getName(), false);
			if (mergedDir == null) {
				mergedDir = new DirectoryView(view1, dir.getName());
				mergedDir.setDirectories(new ArrayList<DirectoryView>());
				mergedDir.setFiles(new ArrayList<String>());
			}
			mergedDir = mergeDirectories(mergedDir, dir);

			DirectoryView dirv2 = new DirectoryView(view1, dir.getName());
			dirv2.setDirectories(new ArrayList<DirectoryView>());
			dirv2.setFiles(new ArrayList<String>());
			mergedDirs.add(mergeDirectories(dirv2, dir));
		}

		for (String file : view1.getFiles())
			mergedFiles.add(file);
		for (String file : view2.getFiles())
			mergedFiles.add(file);

		return merged;
	}
	
	private DirectoryView findDirectory(DirectoryView view, String name, boolean deep) {
		for (DirectoryView vsearch : view.getDirectories()) {
			if (name.equals(vsearch.getName())) return vsearch;
			if (deep) {
				DirectoryView found = findDirectory(vsearch, name, deep);
				if (found != null) return found;
			}
		}
		return null;
	}
}
