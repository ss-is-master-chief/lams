/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $Id$ */
package org.lamsfoundation.lams.tool.assessment.web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.lamsfoundation.lams.tool.assessment.AssessmentConstants;
import org.lamsfoundation.lams.tool.assessment.dto.JQGridJSONModel;
import org.lamsfoundation.lams.tool.assessment.dto.JQGridRow;
import org.lamsfoundation.lams.tool.assessment.dto.QuestionSummary;
import org.lamsfoundation.lams.tool.assessment.dto.Summary;
import org.lamsfoundation.lams.tool.assessment.dto.UserSummary;
import org.lamsfoundation.lams.tool.assessment.dto.UserSummaryItem;
import org.lamsfoundation.lams.tool.assessment.model.Assessment;
import org.lamsfoundation.lams.tool.assessment.model.AssessmentQuestion;
import org.lamsfoundation.lams.tool.assessment.model.AssessmentQuestionOption;
import org.lamsfoundation.lams.tool.assessment.model.AssessmentQuestionResult;
import org.lamsfoundation.lams.tool.assessment.model.AssessmentResult;
import org.lamsfoundation.lams.tool.assessment.service.IAssessmentService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class MonitoringAction extends Action {
    public static Logger log = Logger.getLogger(MonitoringAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws IOException, ServletException {
	request.setAttribute("initialTabId", WebUtil.readLongParam(request, AttributeNames.PARAM_CURRENT_TAB, true));

	String param = mapping.getParameter();
	if (param.equals("summary")) {
	    return summary(mapping, form, request, response);
	}
	if (param.equals("userMasterDetail")) {
	    return userMasterDetail(mapping, form, request, response);
	}
	if (param.equals("questionSummary")) {
	    return questionSummary(mapping, form, request, response);
	}
	if (param.equals("userSummary")) {
	    return userSummary(mapping, form, request, response);
	}
	if (param.equals("saveUserGrade")) {
	    return saveUserGrade(mapping, form, request, response);
	}

	return mapping.findForward(AssessmentConstants.ERROR);
    }

    private ActionForward summary(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	// initialize Session Map
	SessionMap sessionMap = new SessionMap();
	request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
	request.setAttribute(AssessmentConstants.ATTR_SESSION_MAP_ID, sessionMap.getSessionID());

	Long contentId = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID);
	IAssessmentService service = getAssessmentService();
	List<Summary> summaryList = service.getSummaryList(contentId);

	Assessment assessment = service.getAssessmentByContentId(contentId);
	assessment.toDTO();

	// cache into sessionMap
	sessionMap.put(AssessmentConstants.ATTR_SUMMARY_LIST, summaryList);
	sessionMap.put(AssessmentConstants.PAGE_EDITABLE, assessment.isContentInUse());
	sessionMap.put(AssessmentConstants.ATTR_ASSESSMENT, assessment);
	sessionMap.put(AssessmentConstants.ATTR_TOOL_CONTENT_ID, contentId);
	sessionMap.put(AttributeNames.PARAM_CONTENT_FOLDER_ID, WebUtil.readStrParam(request,
		AttributeNames.PARAM_CONTENT_FOLDER_ID));	
	return mapping.findForward(AssessmentConstants.SUCCESS);
    }
    
    private ActionForward userMasterDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	Long userId = WebUtil.readLongParam(request, AttributeNames.PARAM_USER_ID);
	Long sessionId = WebUtil.readLongParam(request, AssessmentConstants.PARAM_SESSION_ID);
	IAssessmentService service = getAssessmentService();
	AssessmentResult result = service.getUserMasterDetail(sessionId, userId);
	
//	// Construct the json data
//	JQGridJSONModel json = new JQGridJSONModel(); 
//	json.setPage("1"); 
//	int records = (result.getQuestionResults() == null) ? 0 : result.getQuestionResults().size();
//	json.setRecords(records); 
//	json.setTotal("1");
//	
//	List<JQGridRow> rows = new ArrayList<JQGridRow>(); 
//	     
//	for (AssessmentQuestionResult questionResult : result.getQuestionResults()) { 
//	  JQGridRow row = new JQGridRow(); 
//	  row.setId(questionResult.getAssessmentQuestion().getSequenceId()); 
//	  List<String> cells = new ArrayList<String>(); 
//	  cells.add(questionResult.getUid().toString()); 
//	  cells.add(questionResult.getFinishDate().toString()); 
//	  cells.add(questionResult.getAnswerString());
//	  cells.add(questionResult.getMark().toString());
//	  row.setCell(cells); 
//	  rows.add(row); 
//	} 
//	json.setRows(rows);
//
//	JSONSerializer serializer = new JSONSerializer(); 
//	String jsonResult = serializer.exclude("*.class").deepSerialize(json); 
	
	request.setAttribute(AssessmentConstants.ATTR_ASSESSMENT_RESULT, result);

	return mapping.findForward(AssessmentConstants.SUCCESS);
    }

    private ActionForward questionSummary(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	String sessionMapID = request.getParameter(AssessmentConstants.ATTR_SESSION_MAP_ID);
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(sessionMapID);
	request.setAttribute(AssessmentConstants.ATTR_SESSION_MAP_ID, sessionMap.getSessionID());
	
	Long questionUid = WebUtil.readLongParam(request, AssessmentConstants.PARAM_QUESTION_UID);
	if (questionUid.equals(-1)) {
	    return null;
	}
	Long contentId = (Long) sessionMap.get(AssessmentConstants.ATTR_TOOL_CONTENT_ID);
	IAssessmentService service = getAssessmentService();
	QuestionSummary questionSummary = service.getQuestionSummary(contentId, questionUid);
	
	request.setAttribute(AssessmentConstants.ATTR_QUESTION_SUMMARY, questionSummary);
	return mapping.findForward(AssessmentConstants.SUCCESS);
    }

    private ActionForward userSummary(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	String sessionMapID = request.getParameter(AssessmentConstants.ATTR_SESSION_MAP_ID);
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(sessionMapID);
	request.setAttribute(AssessmentConstants.ATTR_SESSION_MAP_ID, sessionMap.getSessionID());
	
	Long userId = WebUtil.readLongParam(request, AttributeNames.PARAM_USER_ID);
	Long sessionId = WebUtil.readLongParam(request, AssessmentConstants.PARAM_SESSION_ID);
	Long contentId = (Long) sessionMap.get(AssessmentConstants.ATTR_TOOL_CONTENT_ID);
	IAssessmentService service = getAssessmentService();
	UserSummary userSummary = service.getUserSummary(contentId, userId, sessionId);
	
	request.setAttribute(AssessmentConstants.ATTR_USER_SUMMARY, userSummary);
	return mapping.findForward(AssessmentConstants.SUCCESS);
    }

    private ActionForward saveUserGrade(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	
	if ((request.getParameter(AssessmentConstants.PARAM_NOT_A_NUMBER) == null) && 
		!StringUtils.isEmpty(request.getParameter(AssessmentConstants.PARAM_QUESTION_RESULT_UID))) {
	    Long questionResultUid = WebUtil.readLongParam(request, AssessmentConstants.PARAM_QUESTION_RESULT_UID);
	    float newGrade = Float.valueOf(request.getParameter(AssessmentConstants.PARAM_GRADE));	    
	    IAssessmentService service = getAssessmentService();
	    service.changeQuestionResultMark(questionResultUid, newGrade);
	}

	return null;
    }

    // *************************************************************************************
    // Private method
    // *************************************************************************************
    private IAssessmentService getAssessmentService() {
	WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet()
		.getServletContext());
	return (IAssessmentService) wac.getBean(AssessmentConstants.ASSESSMENT_SERVICE);
    }

}
