package appman.portlets.model;

import java.util.Collection;

public class DirectoryView {

	private DirectoryView parent;
	private String name;
	private Collection<DirectoryView> directories;
	private Collection<String> files;

	public DirectoryView(DirectoryView parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public DirectoryView getParent() {
		return parent;
	}
	public void setParent(DirectoryView parent) {
		this.parent = parent;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Collection<DirectoryView> getDirectories() {
		return directories;
	}
	public void setDirectories(Collection<DirectoryView> directories) {
		this.directories = directories;
	}
	public Collection<String> getFiles() {
		return files;
	}
	public void setFiles(Collection<String> files) {
		this.files = files;
	}
	@Override
	public String toString() {
		return name + ":" + files.toString() + "\n" + directories.toString();
	}
}
