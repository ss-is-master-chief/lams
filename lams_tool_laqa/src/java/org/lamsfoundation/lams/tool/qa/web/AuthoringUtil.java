/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ***********************************************************************/

package org.lamsfoundation.lams.tool.qa.web;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.tool.qa.QaAppConstants;
import org.lamsfoundation.lams.tool.qa.QaComparator;
import org.lamsfoundation.lams.tool.qa.QaContent;
import org.lamsfoundation.lams.tool.qa.QaQueContent;
import org.lamsfoundation.lams.tool.qa.QaStringComparator;
import org.lamsfoundation.lams.tool.qa.service.IQaService;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;

/**
 * 
 * Keeps all operations needed for Authoring mode. 
 * @author Ozgur Demirtas
 *
 */
public class AuthoringUtil implements QaAppConstants {
	static Logger logger = Logger.getLogger(AuthoringUtil.class.getName());
    
	
    /**
     * reconstructQuestionContentMapForAdd(TreeMap mapQuestionContent, HttpServletRequest request)
     * return void
     * adds a new entry to the Map
     */
    protected void reconstructQuestionContentMapForAdd(Map mapQuestionContent, HttpServletRequest request)
    {
    	logger.debug("pre-add Map content: " + mapQuestionContent);
    	logger.debug("pre-add Map size: " + mapQuestionContent.size());
    	
    	repopulateMap(mapQuestionContent, request);
    	
    	mapQuestionContent.put(new Long(mapQuestionContent.size()+1).toString(), "");
    	request.getSession().setAttribute("mapQuestionContent", mapQuestionContent);
	     
    	logger.debug("post-add Map is: " + mapQuestionContent);    	
	   	logger.debug("post-add count " + mapQuestionContent.size());
    }


    /**
     * reconstructQuestionContentMapForRemove(TreeMap mapQuestionContent, HttpServletRequest request)
     * return void
     * deletes the requested entry from the Map
     */
    protected void reconstructQuestionContentMapForRemove(Map mapQuestionContent, HttpServletRequest request, QaAuthoringForm qaAuthoringForm)
    {
    	 	String questionIndex =qaAuthoringForm.getQuestionIndex();
    	 	logger.debug("pre-delete map content:  " + mapQuestionContent);
    	 	logger.debug("questionIndex: " + questionIndex);
    	 	
    	 	long longQuestionIndex= new Long(questionIndex).longValue();
    	 	logger.debug("pre-delete count: " + mapQuestionContent.size());
    	 	
        	repopulateMap(mapQuestionContent, request);
        	logger.debug("post-repopulateMap questionIndex: " + questionIndex);
        	
	 		mapQuestionContent.remove(new Long(longQuestionIndex).toString());	
	 		logger.debug("removed the question content with index: " + longQuestionIndex);
	 		request.getSession().setAttribute("mapQuestionContent", mapQuestionContent);
	    	
	    	logger.debug("post-delete count " + mapQuestionContent.size());
	    	logger.debug("post-delete map content:  " + mapQuestionContent);
    }

    
    /**
     * reconstructQuestionContentMapForSubmit(TreeMap mapQuestionContent, HttpServletRequest request)
     * return void
     * 
    */
    protected  void reconstructQuestionContentMapForSubmit(Map mapQuestionContent, HttpServletRequest request)
    {
    	logger.debug("pre-submit Map:" + mapQuestionContent);
    	logger.debug("pre-submit Map size :" + mapQuestionContent.size());
    	
    	repopulateMap(mapQuestionContent, request);
    	Map mapFinalQuestionContent = new TreeMap(new QaComparator());
    	
    	Iterator itMap = mapQuestionContent.entrySet().iterator();
	    while (itMap.hasNext()) {
	        Map.Entry pairs = (Map.Entry)itMap.next();
	        if ((pairs.getValue() != null) && (!pairs.getValue().equals("")))
    		{
	        	mapFinalQuestionContent.put(pairs.getKey(), pairs.getValue());
	        	logger.debug("adding the  pair: " +  pairs.getKey() + " = " + pairs.getValue());
    		}
	    }
	    
	    mapQuestionContent=(TreeMap)mapFinalQuestionContent;
	    request.getSession().setAttribute("mapQuestionContent", mapQuestionContent);
	    logger.debug("final mapQuestionContent:" + mapQuestionContent);
    }
    
    
    /**
     * repopulateMap(TreeMap mapQuestionContent, HttpServletRequest request)
     * return void 
     * repopulates the user entries into the Map
     */
    protected void repopulateMap(Map mapQuestionContent, HttpServletRequest request)
    {
    	logger.debug("queIndex: " + request.getSession().getAttribute("queIndex"));
    	long queIndex= new Long(request.getSession().getAttribute("queIndex").toString()).longValue();
    	logger.debug("queIndex: " + queIndex);

    	/** if there is data in the Map remaining from previous session remove those */
		mapQuestionContent.clear();
		logger.debug("Map got initialized: " + mapQuestionContent);
		
		for (long i=0; i < queIndex ; i++)
		{
			String candidateQuestionEntry =request.getParameter("questionContent" + i);
			if (i==0)
    		{
    			request.getSession().setAttribute("defaultQuestionContent", candidateQuestionEntry);
    			logger.debug("defaultQuestionContent set to: " + candidateQuestionEntry);
    		}
			if ((candidateQuestionEntry != null) && (candidateQuestionEntry.length() > 0))
			{
				logger.debug("using key: " + i);
				mapQuestionContent.put(new Long(i+1).toString(), candidateQuestionEntry);
				logger.debug("added new entry.");	
			}
		}
    }


    public QaContent saveOrUpdateQaContent(Map mapQuestionContent, IQaService qaService, QaAuthoringForm qaAuthoringForm)
    {
        UserDTO toolUser = (UserDTO) SessionManager.getSession().getAttribute(AttributeNames.USER);
        
        boolean isQuestionsSequenced=false;
        boolean isSynchInMonitor=false;
        boolean isUsernameVisible=false;
        String reportTitle = qaAuthoringForm.getReportTitle();
        String richTextTitle = qaAuthoringForm.getTitle(); 
        String monitoringReportTitle = qaAuthoringForm.getMonitoringReportTitle();
        String richTextOfflineInstructions = qaAuthoringForm.getOnlineInstructions(); 
        String richTextInstructions = qaAuthoringForm.getInstructions(); 
        String richTextOnlineInstructions = qaAuthoringForm.getOfflineInstructions(); 
        String endLearningMessage = qaAuthoringForm.getEndLearningMessage();
        
        String questionsSequenced = qaAuthoringForm.getQuestionsSequenced();
        logger.debug("questionsSequenced: " + questionsSequenced);
        
        String synchInMonitor = qaAuthoringForm.getSynchInMonitor(); 
        logger.debug("synchInMonitor: " + synchInMonitor);
        
        String usernameVisible = qaAuthoringForm.getUsernameVisible();
        logger.debug("usernameVisible: " + usernameVisible);
        
        boolean questionsSequencedBoolean=false;
        if (questionsSequenced.equalsIgnoreCase(ON))
        	questionsSequencedBoolean=true;
        
        boolean synchInMonitorBoolean=false;
        if (synchInMonitor.equalsIgnoreCase(ON))
        	synchInMonitorBoolean=true;
        
        boolean usernameVisibleBoolean=false;
        if (usernameVisible.equalsIgnoreCase(ON))
        	usernameVisibleBoolean=true;
        
        long userId = toolUser.getUserID().longValue();
        
        
        QaContent qa = qaService.loadQa(Long.parseLong(qaAuthoringForm.getToolContentId()));
        if(qa==null)
            qa = new QaContent();
            
        qa.setTitle(richTextTitle);
        qa.setInstructions(richTextInstructions);
        qa.setUpdateDate(new Date(System.currentTimeMillis())); /**keep updating this one*/
        qa.setCreatedBy(userId); /**make sure we are setting the userId from the User object above*/
        qa.setUsernameVisible(isUsernameVisible);
        qa.setQuestionsSequenced(isQuestionsSequenced); /**the default question listing in learner mode will be all in the same page*/
        qa.setSynchInMonitor(isSynchInMonitor);
        qa.setOnlineInstructions(richTextOnlineInstructions);
        qa.setOfflineInstructions(richTextOfflineInstructions);
        qa.setEndLearningMessage(endLearningMessage);
        qa.setReportTitle(reportTitle);
        qa.setMonitoringReportTitle(monitoringReportTitle);
        qa.setUsernameVisible(usernameVisibleBoolean);
        qa.setQuestionsSequenced(questionsSequencedBoolean);
        qa.setSynchInMonitor(synchInMonitorBoolean);
    
        /**
         * TODO: right now the code simply remove all the questions and recreate them.
         *       Ideally, when existing questions changed it should be updated accordingly
         *       and when new questions is added it should be created in the in the database.  
         */
            
        if(qa.getQaContentId() == null){
            qa.setQaContentId(new Long(qaAuthoringForm.getToolContentId()));
            logger.debug("will create: " + qa);
            qaService.createQa(qa);
        }
        else
        {
        	logger.debug("will update: " + qa);
            qaService.updateQa(qa);
        }
        
        QaContent qaContent=qaService.loadQa(new Long(qaAuthoringForm.getToolContentId()).longValue());
        qaContent=createQuestionContent(mapQuestionContent, qaService, qaContent);
        
        return qaContent;
    }
    
    /**
     * createQuestionContent(TreeMap mapQuestionContent, HttpServletRequest request)
     * return void
     * 
     * persist the questions in the Map the user has submitted 
     */
    protected QaContent createQuestionContent(Map mapQuestionContent, IQaService qaService, QaContent qaContent)
    {    
        logger.debug("content uid is: " + qaContent.getUid());
        List questions=qaService.retrieveQaQueContentsByToolContentId(qaContent.getUid().longValue());
        logger.debug("questions: " + questions);
        
		Iterator listIterator=questions.iterator();
		Long mapIndex=new Long(1);
		
		while (listIterator.hasNext())
		{
			QaQueContent queContent=(QaQueContent)listIterator.next();
			logger.debug("queContent data: " + queContent);
			logger.debug("queContent: " + queContent.getQuestion() + "" + queContent.getDisplayOrder());
		}
        
        Iterator itMap = mapQuestionContent.entrySet().iterator();
        int diplayOrder=0;
        while (itMap.hasNext()) 
	    {
	        Map.Entry pairs = (Map.Entry)itMap.next();
	        logger.debug("using the pair: " +  pairs.getKey() + " = " + pairs.getValue());
	        
	        if (pairs.getValue().toString().length() != 0)
	        {
	        	logger.debug("starting createQuestionContent: pairs.getValue().toString():" + pairs.getValue().toString());
	        	logger.debug("starting createQuestionContent: qaContent: " + qaContent);
	        	logger.debug("starting createQuestionContent: diplayOrder: " + diplayOrder);
	        	
	        	
	        	QaQueContent queContent=  new QaQueContent(pairs.getValue().toString(), 
		        											++diplayOrder, 
															qaContent,
															null,
															null);
		        
		        
			       /* is this question already recorded?*/
			       logger.debug("question text is: " + pairs.getValue().toString());
			       logger.debug("content uid is: " + qaContent.getUid());
			       logger.debug("question display order is: " + diplayOrder);
			       QaQueContent existingQaQueContent=qaService.getQuestionContentByDisplayOrder(new Long(diplayOrder), qaContent.getUid());
			       logger.debug("existingQaQueContent: " + existingQaQueContent);
			       if (existingQaQueContent == null)
			       {
			       		logger.debug("creating a new question content: " + queContent);
			       		logger.debug("adding a new question to content: " + queContent);
			       		qaContent.getQaQueContents().add(queContent);
			       		queContent.setQaContent(qaContent);
	
			       		qaService.createQaQue(queContent);
			       }
			       else
			       {
			       		existingQaQueContent.setQuestion(pairs.getValue().toString());
			       		logger.debug("updating the existing question content: " + existingQaQueContent);
			       		qaService.updateQaQueContent(existingQaQueContent);
			       }
	        }
	        logger.debug("returning qaContent: " + qaContent);
	    }
        return qaContent;
    }
}