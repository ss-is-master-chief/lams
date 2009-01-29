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

package org.lamsfoundation.lams.tool.chat.web.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.lamsfoundation.lams.tool.chat.model.Chat;
import org.lamsfoundation.lams.tool.chat.service.ChatServiceProxy;
import org.lamsfoundation.lams.tool.chat.service.IChatService;
import org.lamsfoundation.lams.tool.chat.util.ChatConstants;
import org.lamsfoundation.lams.tool.chat.web.forms.ChatPedagogicalPlannerForm;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.action.LamsDispatchAction;
import org.lamsfoundation.lams.web.util.AttributeNames;

/**
 * @author
 * @version
 * 
 * @struts.action path="/pedagogicalPlanner" name="pedagogicalPlannerForm" parameter="dispatch" scope="request"
 *                validate="false"
 * 
 * @struts.action-forward name="success" path="/pages/authoring/pedagogicalPlannerForm.jsp"
 */
public class PedagogicalPlannerAction extends LamsDispatchAction {

    private static Logger logger = Logger.getLogger(PedagogicalPlannerAction.class);

    public IChatService chatService;

    @Override
    protected ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	if (chatService == null) {
	    chatService = ChatServiceProxy.getChatService(this.getServlet().getServletContext());
	}
	return initPedagogicalPlannerForm(mapping, form, request, response);
    }

    public ActionForward initPedagogicalPlannerForm(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	ChatPedagogicalPlannerForm plannerForm = (ChatPedagogicalPlannerForm) form;
	Long toolContentID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID);
	Chat chat = getChatService().getChatByContentId(toolContentID);
	String command = WebUtil.readStrParam(request, AttributeNames.PARAM_COMMAND, true);
	if (command == null) {
	    plannerForm.fillForm(chat);
	    String contentFolderId = WebUtil.readStrParam(request, AttributeNames.PARAM_CONTENT_FOLDER_ID);
	    plannerForm.setContentFolderID(contentFolderId);
	    return mapping.findForward(ChatConstants.SUCCESS);
	} else {
	    try {
		String onlineInstructions = chat.getOnlineInstructions();
		response.setContentType("text/html;charset=utf-8");
		PrintWriter writer = response.getWriter();

		if (AttributeNames.COMMAND_CHECK_EDITING_ADVICE.equals(command)) {
		    Integer activityIndex = WebUtil.readIntParam(request, AttributeNames.PARAM_ACTIVITY_INDEX);
		    String responseText = (StringUtils.isEmpty(chat.getOnlineInstructions()) ? "NO" : "OK") + '&'
			    + activityIndex;
		    writer.print(responseText);

		} else if (AttributeNames.COMMAND_GET_EDITING_ADVICE.equals(command)) {
		    writer.print(onlineInstructions);
		}
	    } catch (IOException e) {
		PedagogicalPlannerAction.logger.error(e);
	    }
	    return null;
	}

    }

    public ActionForward saveOrUpdatePedagogicalPlannerForm(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response) throws IOException {
	ChatPedagogicalPlannerForm plannerForm = (ChatPedagogicalPlannerForm) form;
	ActionMessages errors = plannerForm.validate();
	if (errors.isEmpty()) {
	    String instructions = plannerForm.getInstructions();
	    Long toolContentID = plannerForm.getToolContentID();
	    Chat chat = getChatService().getChatByContentId(toolContentID);
	    chat.setInstructions(instructions);
	    getChatService().saveOrUpdateChat(chat);
	} else {
	    saveErrors(request, errors);
	}
	return mapping.findForward(ChatConstants.SUCCESS);
    }

    private IChatService getChatService() {
	if (chatService == null) {
	    chatService = ChatServiceProxy.getChatService(this.getServlet().getServletContext());
	}
	return chatService;
    }

}