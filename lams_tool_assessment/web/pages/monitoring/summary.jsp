<%@ include file="/common/taglibs.jsp"%>

<c:set var="sessionMap" value="${sessionScope[sessionMapID]}"/>
<c:set var="summaryList" value="${sessionMap.summaryList}"/>
<c:set var="assessment" value="${sessionMap.assessment}"/>

<script type="text/javascript">
	<!--	
	$(document).ready(function(){
		<c:forEach var="summary" items="${summaryList}" varStatus="status">
		
			jQuery("#list${summary.sessionId}").jqGrid({
				datatype: "local",
				height: 'auto',
				width: 630,
				shrinkToFit: false,
				
			   	colNames:['#',
						'userId',
						'sessionId',
						'<fmt:message key="label.monitoring.summary.user.name" />',
			   	        <c:forEach var="question" items="${assessment.questions}">
			   	     		"${question.title}", 
			   	        </c:forEach>
					    '<fmt:message key="label.monitoring.summary.total" />'],
					    
			   	colModel:[
			   		{name:'id',index:'id', width:20, sorttype:"int"},
			   		{name:'userId',index:'userId', width:0},
			   		{name:'sessionId',index:'sessionId', width:0},
			   		{name:'userName',index:'userName', width:100},
		   	        <c:forEach var="question" items="${assessment.questions}">
		   	     		{name:'${question.uid}', index:'${question.uid}', width:60, align:"right", sorttype:"float"},
	   	        	</c:forEach>			   				
			   		{name:'total',index:'total', width:50,align:"right",sorttype:"float"}		
			   	],
			   	
			   	imgpath:  pathToImageFolder + "jqGrid.basic.theme", 
			   	multiselect: false,
			   	caption: "${summary.sessionName}",
			   	ondblClickRow: function(rowid) {
			   		var userId = jQuery("#list${summary.sessionId}").getCell(rowid, 'userId');
			   		var sessionId = jQuery("#list${summary.sessionId}").getCell(rowid, 'sessionId');
					var userSummaryUrl = '<c:url value="/monitoring/userSummary.do?sessionMapID=${sessionMapID}"/>';
					var newUserSummaryHref = userSummaryUrl + "&userID=" + userId + "&sessionId=" + sessionId + "&KeepThis=true&TB_iframe=true&height=540&width=650&modal=true";
					$("#userSummaryHref").attr("href", newUserSummaryHref);	
					$("#userSummaryHref").click(); 		
			  	},
			  	onSelectRow: function(rowid) { 
			  	    if(rowid == null) { 
			  	    	rowid=0; 
			  	    } 
			   		var userId = jQuery("#list${summary.sessionId}").getCell(rowid, 'userId');
			   		var sessionId = jQuery("#list${summary.sessionId}").getCell(rowid, 'sessionId');
					var userMasterDetailUrl = '<c:url value="/monitoring/userMasterDetail.do"/>';
					//userMasterDetailUrl += "?userID=" + userId + "&sessionId=" + sessionId;
		  	        jQuery("#userSummary${summary.sessionId}").clearGridData().setGridParam({gridstate: "visible",caption:"ss"}).trigger("reloadGrid");
		  	   //   jQuery("#userSummary${summary.sessionId}").setGridParam({caption:"ss"});
		  	     // jQuery("#userSummary${summary.sessionId}").trigger("reloadGrid");
		  	        $("#masterDetailArea").load(
		  	        	userMasterDetailUrl,
		  	        	{
		  	        		userID: userId,
		  	        		sessionId: sessionId
		  	       		}
		  	       	);
				  	        
	  	  		} 
			}).hideCol("userId").hideCol("sessionId");
			
   	        <c:forEach var="assessmentResult" items="${summary.assessmentResults}" varStatus="i">
   	     		jQuery("#list${summary.sessionId}").addRowData(${i.index + 1}, {
   	   	     		id:"${i.index + 1}",
   	   	     		userId:"${assessmentResult.user.userId}",
   	   	     		sessionId:"${assessmentResult.user.session.sessionId}",
   	   	     		userName:"${assessmentResult.user.lastName}, ${assessmentResult.user.firstName}",
		   	   	  	<c:choose>
		   	   			<c:when test="${not empty assessmentResult.questionResults}">
				   	        <c:forEach var="questionResult" items="${assessmentResult.questionResults}">
			   	    			${questionResult.assessmentQuestion.uid}:"<fmt:formatNumber value='${questionResult.mark}' maxFractionDigits='3'/>",
	   	        			</c:forEach>		
		   	   			</c:when>
		   	   			<c:otherwise>
				   	        <c:forEach var="question" items="${assessment.questions}">
				   	     		${question.uid}:"-",
		   	        		</c:forEach>		   	   			
		   	   			</c:otherwise>
		   	   		</c:choose>	
   	   	     		
   	   	     		total:"<fmt:formatNumber value='${assessmentResult.grade}' maxFractionDigits='3'/>"
   	   	   	    });
	        </c:forEach>		

			jQuery("#userSummary${summary.sessionId}").jqGrid({
				datatype: "local",
				
				hiddengrid: true,
				height: 90,
				width: 530,
				shrinkToFit: false,
				
			   	colNames:['#',
						'questionResultUid',
  						'Question',
  						'<fmt:message key="label.monitoring.user.summary.response" />',
  						'<fmt:message key="label.monitoring.user.summary.grade" />'],
					    
			   	colModel:[
	  			   		{name:'id', index:'id', width:25, sorttype:"int"},
	  			   		{name:'questionResultUid', index:'questionResultUid', width:0},
	  			   		{name:'title', index:'title', width:200},
	  			   		{name:'response', index:'response', width:200, sortable:false},
	  			   		{name:'grade', index:'grade', width:80, sorttype:"float", editable:true, editoptions: {size:4, maxlength: 4} }
			   	],
			   	
			   	imgpath:  pathToImageFolder + "jqGrid.basic.theme", 
			   	multiselect: false,
			   	caption: "User summary",
				cellurl: '<c:url value="/monitoring/saveUserGrade.do?sessionMapID=${sessionMapID}"/>',
  				cellEdit: true,
  				afterSaveCell : function (rowid,name,val,iRow,iCol){
  					if (isNaN(val)) {
  						jQuery("#userSummary${summary.sessionId}").restoreCell(iRow,iCol); 
  					}
				},	  		
  				beforeSubmitCell : function (rowid,name,val,iRow,iCol){
  					if (isNaN(val)) {
  						return {nan:true};
  					} else {
  						var questionResultUid = jQuery("#userSummary${summary.sessionId}").getCell(rowid, 'questionResultUid');
  						return {questionResultUid:questionResultUid};		  				  		
  				  	}
  				}
			}).hideCol("questionResultUid");	
			
		</c:forEach>

		$("#questionUid").change(function() {
			var questionUid = $("#questionUid").val();
			var questionSummaryUrl = '<c:url value="/monitoring/questionSummary.do?sessionMapID=${sessionMapID}"/>';
			var questionSummaryHref = questionSummaryUrl + "&questionUid=" + questionUid + "&KeepThis=true&TB_iframe=true&height=400&width=650&modal=true";
			$("#questionSummaryHref").attr("href", questionSummaryHref);	
			$("#questionSummaryHref").click(); 		 
	    }); 
	});
	
	function refreshThickbox(){   
		tb_init('a.thickbox, area.thickbox, input.thickbox');//pass where to apply thickbox
		imgLoader = new Image();// preload image
		imgLoader.src = tb_pathToImage;
	};

	function resizeIframe() {
		if (document.getElementById('TB_iframeContent') != null) {
		    var height = top.window.innerHeight;
		    if ( height == undefined || height == 0 ) {
		    	// IE doesn't use window.innerHeight.
		    	height = document.documentElement.clientHeight;
		    	// alert("using clientHeight");
		    }
			// alert("doc height "+height);
		    height -= document.getElementById('TB_iframeContent').offsetTop + 60;
		    document.getElementById('TB_iframeContent').style.height = height +"px";
	
			TB_HEIGHT = height + 28;
			tb_position();
		}
	};
	window.onresize = resizeIframe;
	-->		
</script>


<%@ include file="parts/advanceoptions.jsp"%>

<c:choose>
	<c:when test="${empty summaryList}">
		<div align="center">
			<b> <fmt:message key="message.monitoring.summary.no.session" /> </b>
		</div>	
	</c:when>
	<c:otherwise>
	
		<div id="masterDetailArea">
			<%@ include file="parts/masterDetailLoadUp.jsp"%>
		</div>
	
		<a onclick="" href="return false;" class="thickbox" id="userSummaryHref" style="display: none;"></a>	
	
		<!-- Dropdown menu for choosing a question type -->
		<div style="padding-left: 30px; margin-bottom: 50px; margin-top: 15px;">	
			<div style="margin-bottom: 5px; font-size: small;">
				<fmt:message key="label.monitoring.summary.results.question" />
			</div>

			<select id="questionUid" style="float: left">
				<option selected="selected" value="-1"><fmt:message key="label.monitoring.summary.choose" /></option>
    			<c:forEach var="question" items="${assessment.questions}">
					<option value="${question.uid}">${question.title}</option>
			   	</c:forEach>
			</select>
			
			<a onclick="" href="return false;" class="thickbox" id="questionSummaryHref" style="display: none;"></a>
		</div>
		
		<c:forEach var="summary" items="${summaryList}" varStatus="status">
			<div style="padding-left: 30px; padding-bottom: 30px;">
				<div style="padding-bottom: 5px; font-size: small;">
					<B><fmt:message key="monitoring.label.group" /></B> ${summary.sessionName}
				</div>
				
				<table id="list${summary.sessionId}" class="scroll" cellpadding="0" cellspacing="0"></table>
				<div style="margin-top: 10px;">
					<table id="userSummary${summary.sessionId}" class="scroll" cellpadding="0" cellspacing="0"></table>
				</div>
			</div>	
		</c:forEach>	
	
	</c:otherwise>
</c:choose>
