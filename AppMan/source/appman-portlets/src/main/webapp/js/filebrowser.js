if (!window.appmanPortlets) window.appmanPortlets = {};

// appmanPortlets.fileBrowserTrees = {jobid: tree}
function paintLine(line, color) {
	line.style.backgroundColor = color;
}

function initAppmanFileBrowser(context) {
	appmanPortlets.context = context;
	var tbl = document.getElementById("tblAppmanFileBrowser");
	var trs = tbl.getElementsByTagName("tr");
	for (var i = 1; i < trs.length; i++) (function() {
		var tr = trs[i];
		tr.onmouseover = function() {
			paintLine(tr, "grey");
		};
		tr.onmouseout = function() {
			paintLine(tr, "white");
		};
		tr.onclick = function() {
			appmanFileBrowserLoadTree(tr.getAttribute("jobId"));
		};
	})();
}

function appmanFileBrowserAddDir(dir) {
	var arr = dir.split("/");
	var obj = {id:dir,label:arr[arr.length - 2],dirs:[],files:[]};
	appmanPortlets.fileBrowserTreeMap[dir] = obj;
	var job = arr[0];
	arr.splice(arr.length - 2, 1);
	var parent = appmanPortlets.fileBrowserTreeMap[arr.join("/")];
	if (parent) parent.dirs.push(obj);
	else appmanPortlets.fileBrowserTrees[job] = obj;
}

function appmanFileBrowserAddFile(dir, file) {
	appmanPortlets.fileBrowserTreeMap[dir].files.push({id:dir + file, label:file});
}

function appmanFileBrowserSortTrees() {
	for (var key in appmanPortlets.fileBrowserTrees) {
		appmanFileBrowserSortView(appmanPortlets.fileBrowserTrees[key]);
	}
}

function appmanFileBrowserSortViewFn(a, b) {
	return a.label.toLowerCase() < b.label.toLowerCase() ? -1 : 1;
}

function appmanFileBrowserSortView(view) {
	view.dirs.sort(appmanFileBrowserSortViewFn);
	view.files.sort(appmanFileBrowserSortViewFn);
	for (var i = 0; i < view.dirs.length; i++)
		appmanFileBrowserSortView(view.dirs[i]);
}

function appmanFileBrowserLoadTree(jobid) {
	document.getElementById("fldsetAppmanFileBrowserJobs").style.display = "none";
	document.getElementById("fldsetAppmanFileBrowserTree").style.display = "";
	document.getElementById("actAppmanFileBrowserShowJobs").style.display = "";
	document.getElementById("lgndAppmanFileBrowserTree").innerHTML = "Tarefa " + jobid;
	
	var mountTree = function(view, parent) {
		for (var i = 0; i < view.dirs.length; i++)
			mountTree(view.dirs[i], new YAHOO.widget.TextNode(view.dirs[i], parent, false));
		for (var i = 0; i < view.files.length; i++)
			new YAHOO.widget.TextNode(view.files[i], parent, false);
	};
	var tree = new YAHOO.widget.TreeView("divAppmanFileBrowserTreeView");
	mountTree(appmanPortlets.fileBrowserTrees[jobid], tree.getRoot());
	tree.subscribe("labelClick", function(node) {
		var path = node.data.id;
		if (path.lastIndexOf("/") != path.length - 1) {
			appmanFileBrowserDownload(path);
		}
	}); 
	tree.render();
}

function appmanFileBrowserDownload(path) {
	window.open(appmanPortlets.context + "/download?file=" + escape(path));
}

function appmanFileBrowserShowJobs() {
	document.getElementById("fldsetAppmanFileBrowserJobs").style.display = "";
	document.getElementById("fldsetAppmanFileBrowserTree").style.display = "none";
	document.getElementById("actAppmanFileBrowserShowJobs").style.display = "none";
}

// código da árvore
USETEXTLINKS = 1;
STARTALLOPEN = 0;
USEFRAMES = 0;
PRESERVESTATE = 1;
