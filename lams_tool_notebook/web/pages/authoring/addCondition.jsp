<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
		"http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/common/taglibs.jsp"%>

<lams:html>
	<lams:head>
		<%@ include file="/common/taglibs.jsp"%>

		<c:set var="tool">
			<lams:WebAppURL />
		</c:set>
		<title>
			<fmt:message key="activity.title" />
		</title>
		<link href="${tool}pages/learning/notebook_style.css" rel="stylesheet" type="text/css">
		<lams:headItems />
		<lams:css style="tabbed" />
	</lams:head>
	<body class="tabpart">
		<!-- Basic Info Form-->
		<%@ include file="/common/messages.jsp"%>
		<html:form action="/authoring/saveOrUpdateCondition" method="post" styleId="notebookConditionForm" focus="displayName" >
			<html:hidden property="orderId" />
			<h2 class="no-space-left">
				<fmt:message key="label.authoring.conditions.add.condition" />
			</h2>

			<div class="field-name">
            	<fmt:message key="label.authoring.conditions.condition.name" />
			</div>
			<div class="small-space-bottom">
         		<html:text property="displayName" size="51"/>
			</div>
			<%-- Text search form fields are being included --%>
			<lams:TextSearch wrapInFormTag="false" sessionMapID="${sessionMapID}"  />
		</html:form>

		<lams:ImgButtonWrapper>
			<a href="#" onclick="notebookConditionForm.submit();" class="button-add-item"><fmt:message
					key="button.save" /> </a>
			<a href="#" onclick="window.top.hideConditionMessage();"
				class="button space-left"><fmt:message key="label.cancel" /> </a>
		</lams:ImgButtonWrapper>
	</body>
</lams:html>
