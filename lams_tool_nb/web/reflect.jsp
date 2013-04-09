<%@ include file="/includes/taglibs.jsp"%>

<script type="text/javascript">
	function disableFinishButton() {
		document.getElementById("finishButton").disabled = true;
	}
	function submitForm(methodName){
		var f = document.getElementById('learnerForm');
		f.submit();
	}

</script>

<div id="content">
<h1>
	<c:out value="${title}" escapeXml="false" />
</h1>

	<html:form action="/learner" method="post" onsubmit="disableFinishButton();" styleId="learnerForm">
	 
		<p><lams:out value="${reflectInstructions}" /></p>				
			 
		<html:textarea cols="60" rows="8" property="reflectionText" value="${reflectEntry}" styleClass="text-area"></html:textarea>
		 
		<div align="right" class="space-bottom-top">
			<html:hidden property="toolSessionID" />
			<html:hidden property="mode" />
			<html:hidden property="method" value="finish"/>
			<html:link href="#nogo" styleClass="button" styleId="finishButton" onclick="submitForm('finish')">
				<span class="nextActivity">								
					<c:choose>
						<c:when test="${activityPosition.last}">
							<fmt:message key="button.submit" />
						</c:when>
						<c:otherwise>
							<fmt:message key="button.finish" />
						</c:otherwise>
					</c:choose>
				</span>
			</html:link>
		</div>
			 
	</html:form>
</div>

