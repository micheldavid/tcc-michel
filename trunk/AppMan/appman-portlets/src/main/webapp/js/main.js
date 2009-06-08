function executeAction(act, frm) {
	var el = document.createElement("input");
	el.type = "hidden";
	el.name = "eventSubmit_do" + act;
	el.value = "1";
	frm.appendChild(el);

	frm.submit();
}
