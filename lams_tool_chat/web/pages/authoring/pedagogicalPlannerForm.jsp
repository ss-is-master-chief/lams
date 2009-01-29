<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
		"http://www.w3.org/TR/html4/loose.dtd">

<%@ include file="/common/taglibs.jsp"%>
<lams:html>
<lams:head>
	<lams:css style="core" />
	
	<script type="text/javascript">
	function prepareFormData(){
		//FCKeditor content is not submitted when sending by jQuery; we need to do this
		var content = FCKeditorAPI.GetInstance('instructions').GetXHTML();
		document.getElementById("instructions").value=content;
	}
	</script>
</lams:head>
<body>
	<%@ include file="/common/messages.jsp"%>
	<h4 class="space-left"><fmt:message key="label.authoring.basic.instructions" /></h4>
	<html:form action="/pedagogicalPlanner.do?dispatch=saveOrUpdatePedagogicalPlannerForm" styleId="pedagogicalPlannerForm" method="post">
		<html:hidden property="toolContentID" />
		<html:hidden property="valid" styleId="valid" />
		<html:hidden property="callID" styleId="callID" />
		<html:hidden property="activityOrderNumber" styleId="activityOrderNumber" />
		
		<c:set var="formBean" value="<%=request.getAttribute(org.apache.struts.taglib.html.Constants.BEAN_KEY)%>" />
		<lams:FCKEditor id="instructions"
			value="${formBean.instructions}"
			contentFolderID="${formBean.contentFolderID}"
			toolbarSet="Custom-Wiki" height="200px">
		</lams:FCKEditor>
	</html:form>
</body>
</lams:html>