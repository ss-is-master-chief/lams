<%@ include file="/taglibs.jsp"%>

<script type="text/javascript">
	function warnIfRemoved(){
		// check if "Remove" state was selected
		var state = document.querySelector('select[name="stateId"]'),
			selected = state.options[state.options.selectedIndex].value;
		if (selected == 4) {
			// check if the course or one of its subcourses contain lessons
			if (${not empty courseToDeleteLessons}) {
				// confirm redirect to "delete all lessons" page
				if (confirm('<fmt:message key="msg.delete.organisation.delete.lessons.confirm"/>')) {
					document.location.href = 'organisation.do?method=deleteAllLessonsInit&orgId=${courseToDeleteLessons}';
				}
				return false;
			}
			// confirm removal of empty course
			if (!confirm('<fmt:message key="msg.delete.organisation.confirm"/>')) {
				return false;
			}
		}
		return true;
	}
</script>

<html-el:form action="/orgsave.do" method="post" onsubmit="return warnIfRemoved()">
<html-el:hidden property="orgId" />
<html-el:hidden property="parentId" />
<html-el:hidden property="typeId" />

<p><a href="orgmanage.do?org=1" class="btn btn-default"><fmt:message key="admin.course.manage" /></a>
	<logic:equal name="OrganisationForm" property="typeId" value="3">
		: <a href="orgmanage.do?org=<bean:write name="OrganisationForm" property="parentId" />" class="btn btn-default"><bean:write name="OrganisationForm" property="parentName"/></a>
	</logic:equal>
	
	<logic:notEmpty name="OrganisationForm" property="orgId">
		: <a href="orgmanage.do?org=<bean:write name="OrganisationForm" property="orgId" />" class="btn btn-default"><bean:write name="OrganisationForm" property="name"/></a>
	</logic:notEmpty>
</p>

<h4>
	<logic:notEmpty name="OrganisationForm" property="orgId">
		<fmt:message key="admin.edit"/>&nbsp;<bean:write name="OrganisationForm" property="name"/>&nbsp;
	</logic:notEmpty>
	<logic:empty name="OrganisationForm" property="orgId">
		<logic:equal name="OrganisationForm" property="typeId" value="2">
			<fmt:message key="admin.course.add"/>
		</logic:equal>
		<logic:equal name="OrganisationForm" property="typeId" value="3">
			<fmt:message key="admin.class.add"/>
		</logic:equal>
	</logic:empty>
</h4>

<div align="center"><html-el:errors/></div>

<div id="deleteAllLessonsBox" class="alert alert-info" style="display: none">
	<fmt:message key="label.delete.all.lesson.count" />&nbsp;<span id="lessonCount"></span> / <span id="totalLessonCount"></span>
	<fmt:message key="label.delete.all.lesson.progress" />
</div>


<table class="table table-condensed table-no-border">
	<tr>
		<td width="15%"><fmt:message key="admin.organisation.name"/> *</td>
		<td><html-el:text property="name" size="40" styleClass="form-control" maxlength="240"/></td>
	</tr>
	<tr>
	<td><fmt:message key="admin.organisation.code"/></td>
		<td><html-el:text property="code" size="20" styleClass="form-control" maxlength="20"/></td>
	</tr>
	<tr>
		<td><fmt:message key="admin.organisation.description"/></td>
		<td><html-el:textarea property="description" cols="50" rows="3" styleClass="form-control" styleId="description" /></td>
	</tr>
	<tr>
		<td><fmt:message key="admin.organisation.status"/></td>
		<td>
			<html-el:select property="stateId" styleClass="form-control" >
				<c:forEach items="${status}" var="state">
					<html-el:option value="${state.organisationStateId}"><fmt:message key="organisation.state.${state.description}"/></html-el:option>
				</c:forEach>
			</html-el:select>
		</td>
	</tr>
</table>
	
	<logic:equal name="OrganisationForm" property="typeId" value="2">
		<div class="checkbox">
			<label for="course-admin-can-add-new-users">
			<html-el:checkbox property="courseAdminCanAddNewUsers" styleId="course-admin-can-add-new-users" />
				<fmt:message key="admin.can.add.user"/>
			</label>
		</div>
		<div class="checkbox">
			<label for="course-admin-can-browse-all-users">
			<html-el:checkbox property="courseAdminCanBrowseAllUsers" styleId="course-admin-can-browse-all-users"/>
				<fmt:message key="admin.can.browse.user"/>
			</label>
		</div>
		<div class="checkbox">
			<label for="course-admin-can-change-status-of-course">
			<html-el:checkbox property="courseAdminCanChangeStatusOfCourse" styleId="course-admin-can-change-status-of-course"/>
				<fmt:message key="admin.can.change.status"/>
			</label>
		</div>
		<div class="checkbox">
			<label for="enable-course-notifications">
			<html-el:checkbox property="enableCourseNotifications" styleId="enable-course-notifications"/>
				<fmt:message key="admin.enable.course.notifications"/>
			</label>
		</div>
		<div class="checkbox">
			<label for="enableGradebookForLearners">
			<html-el:checkbox styleId="enableGradebookForLearners" property="enableGradebookForLearners" />
				<fmt:message key="admin.gradebook.learner.enable"/>
			</label>
		</div>
		<div class="checkbox">
			<label for="enable-single-activity-lessons">
			<html-el:checkbox property="enableSingleActivityLessons" styleId="enable-single-activity-lessons"/>
				<fmt:message key="config.authoring.single.activity"/>
			</label>
		</div>
	</logic:equal>
	
		<div class="checkbox">
			<label for="enable-live-edit">
			<html-el:checkbox property="enableLiveEdit" styleId="enable-live-edit"/>
			<!-- to overcome nasty DynaActionForm bug (http://www.coderanch.com/t/46408/Struts/DynaValidatorActionForm-checkboxes) -->
			<input type="hidden" name="enableLiveEdit" value="false">
				<fmt:message key="config.live.edit"/>
			</label>
		</div>
		
		<div class="checkbox">
			<label for="enable-kumalive">
			<html-el:checkbox property="enableKumalive" styleId="enable-kumalive"/>
				<fmt:message key="config.kumalive.enable"/>
			</label>
		</div>
	
	<logic:notEmpty name="OrganisationForm" property="orgId">
		<div class="voffset10">
			<logic:equal name="OrganisationForm" property="typeId" value="2"><fmt:message key="msg.group.organisation_id"/></logic:equal>
			<logic:equal name="OrganisationForm" property="typeId" value="3"><fmt:message key="msg.subgroup.organisation_id"/></logic:equal>
			&nbsp;<bean:write name="OrganisationForm" property="orgId" />.
		</div>
	</logic:notEmpty>
	
	<div class="pull-right">
		<html-el:cancel styleId="cancelButton" styleClass="btn btn-default"><fmt:message key="admin.cancel"/></html-el:cancel>
		<html-el:submit styleId="saveButton" styleClass="btn btn-primary loffset5"><fmt:message key="admin.save"/></html-el:submit>
	</div>

</html-el:form>
