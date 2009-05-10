package appman.portlets.model;

import java.util.ArrayList;

public class FolderView {

	public ArrayList<FolderView> folders;
	public ArrayList<String> files;

	public ArrayList<FolderView> getFolders() {
		return folders;
	}
	public void setFolders(ArrayList<FolderView> folders) {
		this.folders = folders;
	}
	public ArrayList<String> getFiles() {
		return files;
	}
	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}
}
