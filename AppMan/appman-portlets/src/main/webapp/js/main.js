function getActionForm() {
	return document.getElementById("appman-action-form");
}

function executeAction(act, frm) {
	if (!frm) frm = getActionForm();

	var el = document.createElement("input");
	el.type = "hidden";
	el.name = "eventSubmit_do" + act;
	el.value = "1";

	frm.appendChild(el);
	frm.submit();
}
