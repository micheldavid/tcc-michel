package appman.portlets;

import appman.portlets.model.DirectoryView;

public class DirViewHelper {

	public String getPath(DirectoryView view) {
		if (view.getParent() == null) return view.getName() + "/";
		return getPath(view.getParent()) + view.getName() + "/";
	}

}
