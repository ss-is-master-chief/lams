<%@ include file="/common/taglibs.jsp"%>
<c:set var="formBean" value="<%=request.getAttribute(org.apache.struts.taglib.html.Constants.BEAN_KEY)%>" />

<link rel="stylesheet" href="<html:rewrite page='/includes/css/lytebox.css'/>"  type="text/css">

<script type="text/javascript"> 
	var pathToImageFolder = "<html:rewrite page='/includes/images/'/>"; 
</script>
<script type="text/javascript" src="<html:rewrite page='/includes/javascript/lytebox.js'/>" ></script>
<script lang="javascript">

	function hideMessage(){
		$("#new-image-input-area").hide();
		$("#saveCancelButtons").show();
	}
	
	function editItem(idx,sessionMapID){
		var url = "<c:url value="/authoring/editImage.do?imageIndex="/>" + idx +"&sessionMapID="+sessionMapID;;
		showMessage(url);
	}
	//The panel of imageGallery list panel
	var imageGalleryListTargetDiv = "imageGalleryListArea";
	function deleteItem(idx, sessionMapID) {

		var	deletionConfirmed = confirm("<fmt:message key="warning.msg.authoring.do.you.want.to.delete"></fmt:message>");
		
		if (deletionConfirmed) {
			deleteItemLoading();
			$("#" + imageGalleryListTargetDiv).load(
				"<c:url value="/authoring/removeImage.do"/>",
				{
					imageIndex: idx,
					reqID: (new Date()).getTime(), 
					sessionMapID: sessionMapID
				},
				function() {
					deleteItemComplete();
				}
			);
		}
	}
	
	function deleteItemLoading(){
		showBusy(imageGalleryListTargetDiv);
	}
	function deleteItemComplete(){
		hideBusy(imageGalleryListTargetDiv);
		initLytebox();
	}
	
	function upImage(idx, sessionMapID){
		deleteItemLoading();
		$("#" + imageGalleryListTargetDiv).load(
			"<c:url value="/authoring/upImage.do"/>",
			{
				imageIndex: idx,
				reqID: (new Date()).getTime(), 
				sessionMapID: sessionMapID
			},
			function() {
				deleteItemComplete();
			}
		);
	}
	function downImage(idx, sessionMapID){
		deleteItemLoading();
		$("#" + imageGalleryListTargetDiv).load(
			"<c:url value="/authoring/downImage.do"/>",
			{
				imageIndex: idx,
				reqID: (new Date()).getTime(), 
				sessionMapID: sessionMapID
			},
			function() {
				deleteItemComplete();
			}
		);
	}
</script>

<!-- Basic Tab Content -->
<div class="form-group">
    <label for="imageGallery.title">
    	<fmt:message key="label.authoring.basic.title"/>
    </label>
    <html:text property="imageGallery.title" styleClass="form-control"/>
</div>

<div class="form-group">
    <label for="imageGallery.instructions">
    	<fmt:message key="label.authoring.basic.instruction"/>
    </label>
	<lams:CKEditor id="imageGallery.instructions" value="${formBean.imageGallery.instructions}"
			contentFolderID="${formBean.contentFolderID}">
	</lams:CKEditor>
</div>

<div id="imageGalleryListArea">
	<c:set var="sessionMapID" value="${formBean.sessionMapID}" />
	<%@ include file="/pages/authoring/parts/itemlist.jsp"%>
</div>

<c:url var="showMessageURL" value='/authoring/newImageInit.do'>
	<c:param name="sessionMapID" value="${formBean.sessionMapID}" />
</c:url>
<html:link href="#nogo" styleClass="btn btn-default btn-sm" onclick="javascript:showMessage('${showMessageURL}')">
	<i class="fa fa-plus"></i>&nbsp;<fmt:message key="label.authoring.basic.add.image" />
</html:link>

<div id="new-image-input-area" class="voffset10"></div>
