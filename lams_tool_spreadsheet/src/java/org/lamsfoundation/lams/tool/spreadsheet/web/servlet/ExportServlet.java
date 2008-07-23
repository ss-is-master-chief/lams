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

/* $$Id$$ */

package org.lamsfoundation.lams.tool.spreadsheet.web.servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lamsfoundation.lams.learning.export.web.action.CustomToolImageBundler;
import org.lamsfoundation.lams.notebook.model.NotebookEntry;
import org.lamsfoundation.lams.notebook.service.CoreNotebookConstants;
import org.lamsfoundation.lams.tool.ToolAccessMode;
import org.lamsfoundation.lams.tool.spreadsheet.SpreadsheetConstants;
import org.lamsfoundation.lams.tool.spreadsheet.dto.ReflectDTO;
import org.lamsfoundation.lams.tool.spreadsheet.dto.Summary;
import org.lamsfoundation.lams.tool.spreadsheet.model.Spreadsheet;
import org.lamsfoundation.lams.tool.spreadsheet.model.SpreadsheetSession;
import org.lamsfoundation.lams.tool.spreadsheet.model.SpreadsheetUser;
import org.lamsfoundation.lams.tool.spreadsheet.service.ISpreadsheetService;
import org.lamsfoundation.lams.tool.spreadsheet.service.SpreadsheetApplicationException;
import org.lamsfoundation.lams.tool.spreadsheet.service.SpreadsheetServiceProxy;
import org.lamsfoundation.lams.tool.spreadsheet.util.ReflectDTOComparator;
import org.lamsfoundation.lams.tool.spreadsheet.util.SpreadsheetBundler;
import org.lamsfoundation.lams.tool.spreadsheet.util.SpreadsheetToolContentHandler;
import org.lamsfoundation.lams.util.Configuration;
import org.lamsfoundation.lams.util.ConfigurationKeys;
import org.lamsfoundation.lams.util.FileUtil;
import org.lamsfoundation.lams.web.servlet.AbstractExportPortfolioServlet;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Export portfolio servlet to export all spreadsheets into offline HTML
 * package.
 * 
 * @author Andrey Balan
 */
public class ExportServlet extends AbstractExportPortfolioServlet {
	private static final long serialVersionUID = -4529093489007108143L;

	private static Logger logger = Logger.getLogger(ExportServlet.class);
	
	private final String FILENAME = "spreadsheet_main.html";

	private SpreadsheetToolContentHandler handler;
	
	private ISpreadsheetService service;
	
	@Override
	public void init() throws ServletException {
		service = SpreadsheetServiceProxy.getSpreadsheetService(getServletContext());
		super.init();
	}
	
	public String doExport(HttpServletRequest request, HttpServletResponse response, String directoryName, Cookie[] cookies) {

		//initial sessionMap
		SessionMap sessionMap = new SessionMap();
		request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
		
		try {
			if (StringUtils.equals(mode, ToolAccessMode.LEARNER.toString())) {
				sessionMap.put(AttributeNames.ATTR_MODE, ToolAccessMode.LEARNER);
				learner(request, response, directoryName, cookies, sessionMap);
			} else if (StringUtils.equals(mode, ToolAccessMode.TEACHER.toString())) {
				sessionMap.put(AttributeNames.ATTR_MODE, ToolAccessMode.TEACHER);
				teacher(request, response, directoryName, cookies, sessionMap);
			}
		} catch (SpreadsheetApplicationException e) {
			logger.error("Cannot perform export for spreadsheet tool.");
		}

		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()	+ request.getContextPath();
		
		// Attempting to export required images
		try	{
			SpreadsheetBundler imageBundler = new SpreadsheetBundler();
			imageBundler.bundle(request, cookies, directoryName);
		} catch (Exception e) {
			logger.error("Could not export spreadsheet javascript files, some files may be missing in export portfolio", e);
		}
		
		writeResponseToFile(basePath + "/pages/export/exportportfolio.jsp?sessionMapID=" + sessionMap.getSessionID(), directoryName, FILENAME, cookies);

		return FILENAME;
	}
	
	protected String doOfflineExport(HttpServletRequest request, HttpServletResponse response, String directoryName, Cookie[] cookies) {
        if (toolContentID == null && toolSessionID == null) {
            logger.error("Tool content Id or and session Id are null. Unable to activity title");
        } else {

        	Spreadsheet content = null;
            if ( toolContentID != null ) {
            	content = service.getSpreadsheetByContentId(toolContentID);
            } else {
            	SpreadsheetSession session=service.getSessionBySessionId(toolSessionID);
            	if ( session != null )
            		content = session.getSpreadsheet();
            }
            if ( content != null ) {
            	activityTitle = content.getTitle();
            }
        }
        return super.doOfflineExport(request, response, directoryName, cookies);
	}

	public void learner(HttpServletRequest request, HttpServletResponse response, String directoryName, Cookie[] cookies, HashMap sessionMap) throws SpreadsheetApplicationException {

		if (userID == null || toolSessionID == null) {
			String error = "Tool session Id or user Id is null. Unable to continue";
			logger.error(error);
			throw new SpreadsheetApplicationException(error);
		}

		SpreadsheetUser learner = service.getUserByIDAndSession(userID,toolSessionID);

		if (learner == null) {
			String error = "The user with user id " + userID + " does not exist.";
			logger.error(error);
			throw new SpreadsheetApplicationException(error);
		}

		Spreadsheet content = service.getSpreadsheetBySessionId(toolSessionID);

		if (content == null) {
			String error = "The content for this activity has not been defined yet.";
			logger.error(error);
			throw new SpreadsheetApplicationException(error);
		}
		
		List<Summary> summaryList = service.exportForLearner(toolSessionID, learner);
//		saveFileToLocal(summaryList, directoryName);
		
		// Add flag to indicate whether to render user notebook entries
		sessionMap.put(SpreadsheetConstants.ATTR_REFLECTION_ON, content.isReflectOnActivity());
		
//		// Create reflectList if reflection is enabled.
//		if (content.isReflectOnActivity()) {
//			// Create reflectList, need to follow same structure used in teacher
//			// see service.getReflectList();
//			Map<Long, Set<ReflectDTO>> map = new HashMap<Long, Set<ReflectDTO>>();  
//			Set<ReflectDTO> reflectDTOSet = new TreeSet<ReflectDTO>(new ReflectDTOComparator());
//			reflectDTOSet.add(getReflectionEntry(learner));
//			map.put(toolSessionID, reflectDTOSet);
//			
//			// Add reflectList to sessionMap
//			sessionMap.put(SpreadsheetConstants.ATTR_REFLECT_LIST, map);
//		}
		
		
		sessionMap.put(SpreadsheetConstants.ATTR_RESOURCE, content);
		sessionMap.put(SpreadsheetConstants.ATTR_TITLE, content.getTitle());
		sessionMap.put(SpreadsheetConstants.ATTR_INSTRUCTIONS, content.getInstructions());
		sessionMap.put(SpreadsheetConstants.ATTR_SUMMARY_LIST, summaryList);
	}

	public void teacher(HttpServletRequest request, HttpServletResponse response, String directoryName, Cookie[] cookies, HashMap sessionMap) throws SpreadsheetApplicationException {

		// check if toolContentId exists in db or not
		if (toolContentID == null) {
			String error = "Tool Content Id is missing. Unable to continue";
			logger.error(error);
			throw new SpreadsheetApplicationException(error);
		}

		Spreadsheet content = service.getSpreadsheetByContentId(toolContentID);

		if (content == null) {
			String error = "Data is missing from the database. Unable to Continue";
			logger.error(error);
			throw new SpreadsheetApplicationException(error);
		}
		List<Summary> summaryList = service.exportForTeacher(toolContentID);
//		if(summaryList != null) {
//			for (List<Summary> list : summaryList) {
//				saveFileToLocal(list, directoryName);
//			}
//		}
		
		// Add flag to indicate whether to render user notebook entries
		sessionMap.put(SpreadsheetConstants.ATTR_REFLECTION_ON, content.isReflectOnActivity());
		
//		// Create reflectList if reflection is enabled.
//		if (content.isReflectOnActivity()) {
//			Map<Long, Set<ReflectDTO>> reflectList = service.getReflectList(content.getContentId(), true);
//			// Add reflectList to sessionMap
//			sessionMap.put(SpreadsheetConstants.ATTR_REFLECT_LIST, reflectList);
//		}
		
		// put it into HTTPSession
		sessionMap.put(SpreadsheetConstants.ATTR_RESOURCE, content);
		sessionMap.put(SpreadsheetConstants.ATTR_TITLE, content.getTitle());
		sessionMap.put(SpreadsheetConstants.ATTR_INSTRUCTIONS, content.getInstructions());
		sessionMap.put(SpreadsheetConstants.ATTR_SUMMARY_LIST, summaryList);
	}

    private void saveFileToLocal(List<Summary> list, String directoryName) {
    	handler = getToolContentHandler();
		for (Summary summary : list) {

			try{
				int idx= 1;
				String userName ="";
//				String userName = summary.getUsername();
				String localDir;
				while(true){
					localDir = FileUtil.getFullPath(directoryName,userName + "/" + idx);
					File local = new File(localDir);
					if(!local.exists()){
						local.mkdirs();
						break;
					}
					idx++;
				}
//				summary.setAttachmentLocalUrl(userName + "/" + idx + "/" + summary.getFileUuid() + '.' + FileUtil.getFileExtension(summary.getFileName()));
//				handler.saveFile(summary.getFileUuid(), FileUtil.getFullPath(directoryName, summary.getAttachmentLocalUrl()));
			} catch (Exception e) {
				logger.error("Export forum topic attachment failed: " + e.toString());
			}
		}
		
	}

	private SpreadsheetToolContentHandler getToolContentHandler() {
  	    if ( handler == null ) {
    	      WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
    	      handler = (SpreadsheetToolContentHandler) wac.getBean(SpreadsheetConstants.TOOL_CONTENT_HANDLER_NAME);
    	    }
    	    return handler;
	}
	
	private ReflectDTO getReflectionEntry(SpreadsheetUser spreadsheetUser) {
		ReflectDTO reflectDTO = new ReflectDTO(spreadsheetUser);
		NotebookEntry notebookEntry = service.getEntry(spreadsheetUser.getSession().getSessionId(), CoreNotebookConstants.NOTEBOOK_TOOL, 
				SpreadsheetConstants.TOOL_SIGNATURE, spreadsheetUser.getUserId().intValue());
		
		// check notebookEntry is not null
		if (notebookEntry != null) {
			reflectDTO.setReflect(notebookEntry.getEntry());
			logger.debug("Could not find notebookEntry for SpreadsheetUser: " + spreadsheetUser.getUid());
		}        		
		 return reflectDTO;
	}
	

}
