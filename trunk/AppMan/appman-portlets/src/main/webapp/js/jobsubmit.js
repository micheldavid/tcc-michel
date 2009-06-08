//if (window.appmanPortlets == null) window.appmanPortlets = {};

function checkAppmanJobSubmitCheckAllJobIds() {
	var chk = document.getElementById("appmanJobSubmitCheckAllJobIds");
	var val = chk.checked;
	var els = chk.form.jobId;
	if (!els.length) els = [els];

	for (var i = 0; i < els.length; i++) {
		els[i].checked = val;
	}
}

function appmanJobSubmitSubmitSubmit() {
	var frm = document.getElementById("appmanJobSubmitSubmitForm");
	var arq = frm.file.value;
	var arqParts = arq.split(/\\\//);
	var arqName = arqParts[arqParts.length - 1];
	if (arqName.toLowerCase().substring(arqName.lastIndexOf(".") + 1) != "dag") {
		alert("A extensão do arquivo a ser enviado deve ser \"dag\".");
		return;
	}
	frm.submit();
}

function appmanJobSubmitDagReply() {
	alert("Tarefa enviada com sucesso.");
}

function appmanJobSubmitViewRemoveCheckedJobs() {
	var frm = document.getElementById("appman-delete-job-form");
	var chks = frm.getElementsByTagName("input");
	var hasChecked = false;
	for (var i = 0; i < chks.length; i++) {
		var el = chks[i];
		if (el.name == "jobId" && el.checked) {
			hasChecked = true;
			if (el.getAttribute("running") == "Y") {
				if (!confirm("Algumas das tarefas selecionadas estão em execução.\nTens certeza que deseja excluir?")) {
					return;
				}
				break;
			}
		}
	}
	if (!hasChecked) {
		alert("Não há itens marcados.");
		return;
	}
	executeAction("DeleteJobs", frm);
}

function executeActionJobSubmit(action) {
	executeAction(action, document.getElementById("appman-jobsubmit-action-form"));
}
