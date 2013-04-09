/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
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

/* $$Id$$ */
package org.lamsfoundation.lams.tool.qa.web;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.lamsfoundation.lams.learning.web.bean.ActivityPositionDTO;
import org.lamsfoundation.lams.learning.web.util.LearningWebUtil;
import org.lamsfoundation.lams.notebook.model.NotebookEntry;
import org.lamsfoundation.lams.notebook.service.CoreNotebookConstants;
import org.lamsfoundation.lams.tool.ToolAccessMode;
import org.lamsfoundation.lams.tool.qa.QaAppConstants;
import org.lamsfoundation.lams.tool.qa.QaContent;
import org.lamsfoundation.lams.tool.qa.QaQueUsr;
import org.lamsfoundation.lams.tool.qa.QaQueContent;
import org.lamsfoundation.lams.tool.qa.QaSession;
import org.lamsfoundation.lams.tool.qa.dto.GeneralLearnerFlowDTO;
import org.lamsfoundation.lams.tool.qa.dto.QaQuestionDTO;
import org.lamsfoundation.lams.tool.qa.service.IQaService;
import org.lamsfoundation.lams.tool.qa.service.QaServiceProxy;
import org.lamsfoundation.lams.tool.qa.util.QaApplicationException;
import org.lamsfoundation.lams.tool.qa.util.QaComparator;
import org.lamsfoundation.lams.tool.qa.util.QaUtils;
import org.lamsfoundation.lams.tool.qa.web.form.QaLearningForm;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.util.DateUtil;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;

/**
 * 
 * @author Ozgur Demirtas
 * 
 * <lams base path>/<tool's learner url>&userId=<learners user
 * id>&toolSessionId=123&mode=teacher
 * 
 * Since the toolSessionId is passed, we will derive toolContentId from the
 * toolSessionId
 * 
 * This class is used to load the default content and initialize the
 * presentation Map for Learner mode
 * 
 * createToolSession will not be called once the tool is deployed.
 * 
 * It is important that ALL the session attributes created in this action gets
 * removed by: QaUtils.cleanupSession(request)
 * 
 */

/**
 * Tool Session:
 * 
 * A tool session is the concept by which which the tool and the LAMS core
 * manage a set of learners interacting with the tool. The tool session id
 * (toolSessionId) is generated by the LAMS core and given to the tool. A tool
 * session represents the use of a tool for a particulate activity for a group
 * of learners. So if an activity is ungrouped, then one tool session exist for
 * for a tool activity in a learning design.
 * 
 * More details on the tool session id are covered under monitoring. When
 * thinking about the tool content id and the tool session id, it might be
 * helpful to think about the tool content id relating to the definition of an
 * activity, whereas the tool session id relates to the runtime participation in
 * the activity.
 * 
 */

/**
 * 
 * Learner URL: The learner url display the screen(s) that the learner uses to
 * participate in the activity. When the learner accessed this user, it will
 * have a tool access mode ToolAccessMode.LEARNER.
 * 
 * It is the responsibility of the tool to record the progress of the user. If
 * the tool is a multistage tool, for example asking a series of questions, the
 * tool must keep track of what the learner has already done. If the user logs
 * out and comes back to the tool later, then the tool should resume from where
 * the learner stopped. When the user is completed with tool, then the tool
 * notifies the progress engine by calling
 * org.lamsfoundation.lams.learning.service.completeToolSession(Long
 * toolSessionId, User learner).
 * 
 * If the tool's content DefineLater flag is set to true, then the learner
 * should see a "Please wait for the teacher to define this part...." style
 * message. If the tool's content RunOffline flag is set to true, then the
 * learner should see a "This activity is not being done on the computer. Please
 * see your instructor for details."
 *  ?? Would it be better to define a run offline message in the tool? We have
 * instructions for the teacher but not the learner. ?? If the tool has a
 * LockOnFinish flag, then the tool should lock learner's entries once they have
 * completed the activity. If they return to the activity (e.g. via the progress
 * bar) then the entries should be read only.
 */

public class QaLearningStarterAction extends Action implements QaAppConstants {
    static Logger logger = Logger.getLogger(QaLearningStarterAction.class.getName());
    
    private static IQaService qaService;

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws IOException, ServletException, QaApplicationException {

	QaUtils.cleanUpSessionAbsolute(request);
	if (qaService == null) {
	    qaService = QaServiceProxy.getQaService(getServlet().getServletContext());
	}
	
	QaLearningForm qaLearningForm = (QaLearningForm) form;
	/*validate learning mode parameters*/
	validateParameters(request, mapping, qaLearningForm);
	String mode = qaLearningForm.getMode();
	String toolSessionID = qaLearningForm.getToolSessionID();

	/*
	 * By now, the passed tool session id MUST exist in the db by calling:
	 * public void createToolSession(Long toolSessionId, Long toolContentId) by the core.
	 *  
	 * make sure this session exists in tool's session table by now.
	 */

	if (!QaUtils.existsSession(new Long(toolSessionID).longValue(), qaService)) {
	    QaUtils.cleanUpSessionAbsolute(request);
	    logger.error("error: The tool expects mcSession.");
	    return (mapping.findForward(ERROR_LIST_LEARNER));
	}

	QaSession qaSession = qaService.getSessionById(new Long(toolSessionID).longValue());
	QaContent qaContent = qaSession.getQaContent();
	if (qaContent == null) {
	    QaUtils.cleanUpSessionAbsolute(request);
	    logger.error("error: The tool expects qaContent.");
	    return (mapping.findForward(ERROR_LIST_LEARNER));
	}
	
	QaQueUsr qaUser = null;
	if ((mode != null) && mode.equals(ToolAccessMode.TEACHER.toString())) {
	    // monitoring mode - user is specified in URL
	    // assessmentUser may be null if the user was force completed.
	    qaUser = getSpecifiedUser(toolSessionID,
		    WebUtil.readIntParam(request, AttributeNames.PARAM_USER_ID, false));
	} else {
	    qaUser = getCurrentUser(toolSessionID);
	}
	Long userId = qaUser.getQueUsrId();
	qaLearningForm.setUserID(qaUser.getQueUsrId().toString());

	/* holds the question contents for a given tool session and relevant content */
	Map mapQuestionStrings = new TreeMap(new QaComparator());
	Map<String, QaQuestionDTO> mapQuestions = new TreeMap<String,QaQuestionDTO>();

	String httpSessionID = qaLearningForm.getHttpSessionID();
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(httpSessionID);
	if (sessionMap == null) {
	    sessionMap = new SessionMap();
	    Map mapSequentialAnswers = new HashMap();
	    sessionMap.put(MAP_SEQUENTIAL_ANSWERS_KEY, mapSequentialAnswers);
	    request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
	    qaLearningForm.setHttpSessionID(sessionMap.getSessionID());
	}

	GeneralLearnerFlowDTO generalLearnerFlowDTO = LearningUtil.buildGeneralLearnerFlowDTO(qaContent);
	generalLearnerFlowDTO.setUserUid(qaUser.getQueUsrId().toString());
	generalLearnerFlowDTO.setHttpSessionID(sessionMap.getSessionID());
	generalLearnerFlowDTO.setToolSessionID(toolSessionID);
	generalLearnerFlowDTO.setToolContentID(qaContent.getQaContentId().toString());
	generalLearnerFlowDTO.setReportTitleLearner(qaContent.getReportTitle());

	generalLearnerFlowDTO.setReflection(new Boolean(qaContent.isReflect()).toString());
	generalLearnerFlowDTO.setReflectionSubject(qaContent.getReflectionSubject());

	NotebookEntry notebookEntry = qaService.getEntry(new Long(toolSessionID), CoreNotebookConstants.NOTEBOOK_TOOL,
		MY_SIGNATURE, userId.intValue());
	if (notebookEntry != null) {
	    String notebookEntryPresentable = QaUtils.replaceNewLines(notebookEntry.getEntry());
	    qaLearningForm.setEntryText(notebookEntryPresentable);
	    generalLearnerFlowDTO.setNotebookEntry(notebookEntryPresentable);
	}

	/*
	 * Is the tool activity been checked as Define Later in the property inspector?
	 */
	if (qaContent.isDefineLater()) {
	    QaUtils.cleanUpSessionAbsolute(request);
	    return (mapping.findForward(DEFINE_LATER));
	}

	ActivityPositionDTO activityPosition = LearningWebUtil.putActivityPositionInRequestByToolSessionId(new Long(
		toolSessionID), request, getServlet().getServletContext());
	sessionMap.put(AttributeNames.ATTR_ACTIVITY_POSITION, activityPosition);

	/*
	 * fetch question content from content
	 */
	Iterator contentIterator = qaContent.getQaQueContents().iterator();
	while (contentIterator.hasNext()) {
	    QaQueContent qaQuestion = (QaQueContent) contentIterator.next();
	    if (qaQuestion != null) {
		int displayOrder = qaQuestion.getDisplayOrder();

		if (displayOrder != 0) {
		    /*
		     *  add the question to the questions Map in the displayOrder
		     */
		    QaQuestionDTO questionDTO = new QaQuestionDTO(qaQuestion);
		    mapQuestions.put(questionDTO.getDisplayOrder(), questionDTO);

		    mapQuestionStrings.put(new Integer(displayOrder).toString(), qaQuestion.getQuestion());
		    
		}
	    }
	}
	generalLearnerFlowDTO.setMapQuestions(mapQuestionStrings);
	generalLearnerFlowDTO.setMapQuestionContentLearner(mapQuestions);
	generalLearnerFlowDTO.setTotalQuestionCount(new Integer(mapQuestions.size()));
	qaLearningForm.setTotalQuestionCount(new Integer(mapQuestions.size()).toString());

	String feedBackType = "";
	if (qaContent.isQuestionsSequenced()) {
	    feedBackType = FEEDBACK_TYPE_SEQUENTIAL;
	} else {
	    feedBackType = FEEDBACK_TYPE_COMBINED;
	}
	String userFeedback = feedBackType + generalLearnerFlowDTO.getTotalQuestionCount() + QUESTIONS;
	generalLearnerFlowDTO.setUserFeedback(userFeedback);

	generalLearnerFlowDTO.setRemainingQuestionCount(generalLearnerFlowDTO.getTotalQuestionCount().toString());
	generalLearnerFlowDTO.setInitialScreen(new Boolean(true).toString());

	request.setAttribute(GENERAL_LEARNER_FLOW_DTO, generalLearnerFlowDTO);
	/* Is the request for a preview by the author?
	Preview The tool must be able to show the specified content as if it was running in a lesson. 
	It will be the learner url with tool access mode set to ToolAccessMode.AUTHOR 
	3 modes are:
		author
		teacher
		learner
	*/
	/*handling PREVIEW mode*/

	if ((qaSession.getQaQueUsers() != null) && (qaSession.getQaQueUsers().size() > 1)) {
	    //there are multiple user responses
	    generalLearnerFlowDTO.setExistMultipleUserResponses(new Boolean(true).toString());
	}

	/* by now, we know that the mode is either teacher or learner
	 * check if the mode is teacher and request is for Learner Progress
	 */
	if (mode.equals("teacher")) {
	    //start generating learner progress report for toolSessionID

	    /* the report should have the all entries for the users in this tool session,
	     * and display under the "my answers" section the answers for the user id in the url */
	    Long learnerProgressUserId = WebUtil.readLongParam(request, AttributeNames.PARAM_USER_ID, false);
	    QaMonitoringAction qaMonitoringAction = new QaMonitoringAction();
	    generalLearnerFlowDTO.setRequestLearningReport(new Boolean(true).toString());
	    generalLearnerFlowDTO.setRequestLearningReportProgress(new Boolean(true).toString());
	    generalLearnerFlowDTO.setTeacherViewOnly(new Boolean(true).toString());

	    qaMonitoringAction.refreshSummaryData(request, qaContent, qaService, qaContent.isUsernameVisible(), true,
		    toolSessionID, userId.toString(), generalLearnerFlowDTO, false, toolSessionID);

	    return (mapping.findForward(INDIVIDUAL_LEARNER_REPORT));
	}

	// find out if the content is set to run offline or online. If it is set to run offline , the learners are
	// informed about that.
	if (qaContent.isRunOffline()) {
	    QaUtils.cleanUpSessionAbsolute(request);
	    return (mapping.findForward(RUN_OFFLINE));
	}

	//check if there is submission deadline
	Date submissionDeadline = qaContent.getSubmissionDeadline();
	if (submissionDeadline != null) {
		//store submission deadline to sessionMap
		sessionMap.put(QaAppConstants.ATTR_SUBMISSION_DEADLINE, submissionDeadline);
		
		HttpSession ss = SessionManager.getSession();
		UserDTO learnerDto = (UserDTO) ss.getAttribute(AttributeNames.USER);
		TimeZone learnerTimeZone = learnerDto.getTimeZone();
		Date tzSubmissionDeadline = DateUtil.convertToTimeZoneFromDefault(learnerTimeZone, submissionDeadline);
		Date currentLearnerDate = DateUtil.convertToTimeZoneFromDefault(learnerTimeZone, new Date());
		
		//calculate whether submission deadline has passed, and if so forward to "runOffline"
		if (currentLearnerDate.after(tzSubmissionDeadline)) {
			return mapping.findForward("runOffline");
		}
	}
	
	/*
	 * Verify that userId does not already exist in the db.
	 * If it does exist and the passed tool session id exists in the db, that means the user already responded to the content and 
	 * his answers must be displayed  read-only
	 * 
	 * if the user's tool session id AND user id exists in the tool tables go to learner's report.
	 */
	/* if the 'All Responses' has been clicked no more user entry is accepted, and isResponseFinalized() returns true*/
	Long currentToolSessionID = new Long(qaLearningForm.getToolSessionID());

	//if Response is Finalized
	if (qaUser.isResponseFinalized()) {
	    QaSession checkSession = qaUser.getQaSession();

	    if (checkSession != null) {
		Long checkQaSessionId = checkSession.getQaSessionId();

		if (checkQaSessionId.toString().equals(currentToolSessionID.toString())) {

		    // the learner is in the same session and has already responsed to this content

		    generalLearnerFlowDTO.setLockWhenFinished(new Boolean(qaContent.isLockWhenFinished()).toString());

		    QaMonitoringAction qaMonitoringAction = new QaMonitoringAction();
		    /*
		     * the report should have all the users' entries OR the report should have only the current
		     * session's entries
		     */
		    generalLearnerFlowDTO.setRequestLearningReport(new Boolean(true).toString());

		    boolean isUserNamesVisible = qaContent.isUsernameVisible();
		    qaMonitoringAction.refreshSummaryData(request, qaContent, qaService, isUserNamesVisible, true,
			    currentToolSessionID.toString(), userId.toString(), generalLearnerFlowDTO, false, toolSessionID);

		    if (qaUser.isLearnerFinished()) {
			generalLearnerFlowDTO.setRequestLearningReportViewOnly(new Boolean(true).toString());
			return (mapping.findForward(REVISITED_LEARNER_REP));
		    } else {
			generalLearnerFlowDTO.setRequestLearningReportViewOnly(new Boolean(false).toString());
			return (mapping.findForward(INDIVIDUAL_LEARNER_REPORT));
		    }
		}
	    }
	}
	
	//**---- showing AnswersContent.jsp ----**
	LearningUtil.populateAnswers(sessionMap, qaContent, qaUser, mapQuestions, generalLearnerFlowDTO, qaService);

	return (mapping.findForward(LOAD_LEARNER));
    }

    /**
     * validates the learning mode parameters
     * 
     * @param request
     * @param mapping
     * @return ActionForward
     */
    protected void validateParameters(HttpServletRequest request, ActionMapping mapping,
	    QaLearningForm qaLearningForm) {
	/*
	 * process incoming tool session id and later derive toolContentId from it. 
	 */
	String strToolSessionId = request.getParameter(AttributeNames.PARAM_TOOL_SESSION_ID);
	long toolSessionId = 0;
	if ((strToolSessionId == null) || (strToolSessionId.length() == 0)) {
	    ActionMessages errors = new ActionMessages();
	    errors.add(Globals.ERROR_KEY, new ActionMessage("error.toolSessionId.required"));
	    logger.error("error.toolSessionId.required");
	    saveErrors(request, errors);
	    return;
	} else {
	    try {
		toolSessionId = new Long(strToolSessionId).longValue();
		qaLearningForm.setToolSessionID(new Long(toolSessionId).toString());
	    } catch (NumberFormatException e) {
		logger.error("add error.sessionId.numberFormatException to ActionMessages.");
		return;
	    }
	}

	/*mode can be learner, teacher or author */
	String mode = request.getParameter(MODE);
	if ((mode == null) || (mode.length() == 0)) {
	    logger.error("Mode is empty");
	    return;
	}
	if ((!mode.equals("learner")) && (!mode.equals("teacher")) && (!mode.equals("author"))) {
	    logger.error("Wrong mode");
	    return;
	}
	qaLearningForm.setMode(mode);
    }
    
    private QaQueUsr getCurrentUser(String toolSessionId) {

	// get back login user DTO 
	HttpSession ss = SessionManager.getSession();
	UserDTO toolUser = (UserDTO) ss.getAttribute(AttributeNames.USER);
	Long userId = new Long(toolUser.getUserID().longValue());	
	
	QaQueUsr qaUser = qaService.getUserByIdAndSession(userId, new Long(toolSessionId));
	if (qaUser == null) {
	    qaUser = qaService.createUser(new Long(toolSessionId));
	}

	return qaUser;
    }

    private QaQueUsr getSpecifiedUser(String toolSessionId, Integer userId) {
	QaQueUsr qaUser = qaService.getUserByIdAndSession(new Long(userId.intValue()), new Long(toolSessionId));
	if (qaUser == null) {
	    logger.error("Unable to find specified user for Q&A activity. Screens are likely to fail. SessionId="
		    + new Long(toolSessionId) + " UserId=" + userId);
	}
	return qaUser;
    }
}
