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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 * USA 
 * 
 * http://www.gnu.org/licenses/gpl.txt 
 * **************************************************************** 
 */

package org.lamsfoundation.ld.integration.blackboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.lamsfoundation.ld.integration.util.BlackboardUtil;
import org.lamsfoundation.ld.integration.util.LamsSecurityUtil;
import org.lamsfoundation.ld.integration.util.LineitemUtil;

import blackboard.data.ValidationException;
import blackboard.data.content.Content;
import blackboard.data.course.Course;
import blackboard.data.user.User;
import blackboard.persist.PersistenceException;
import blackboard.persist.PkId;
import blackboard.persist.content.ContentDbPersister;
import blackboard.persist.course.CourseDbLoader;
import blackboard.util.StringUtil;

/**
 * Admin on BB side calls this servlet to clone old lesson that were copied to the new course.
 */
public class CloneLessonsServlet extends HttpServlet {

    private static final long serialVersionUID = -3587062723412672084L;
    private static Logger logger = Logger.getLogger(CloneLessonsServlet.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	String courseIdParam = request.getParameter("courseId");
	if (StringUtil.isEmpty(courseIdParam)) {
	    throw new RuntimeException("Required parameters are missing. courseId: " + courseIdParam);
	}

	String newLessonIds = "";
	try {
	    newLessonIds = recreateLessonsAfterCourseCopy(courseIdParam);
	} catch (IllegalStateException e) {
	    throw new ServletException(
		    "LAMS Server timeout, did not get a response from the LAMS server. Please contact your systems administrator",
		    e);
	} catch (Exception e) {
	    throw new ServletException(e);
	}

	//prepare string to write out
	int newLessonsCounts = newLessonIds.length() - newLessonIds.replace(",", "").length();
	String resultStr = "Complete! " + newLessonsCounts + " lessons have been cloned.";
	//add all lessonIds (without the last comma)
	if (newLessonsCounts > 0) {
	    resultStr += " Their updated lessonIds: " + newLessonIds.substring(0, newLessonIds.length() - 2);
	}
	logger.debug(resultStr);

	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	out.write(resultStr);
	out.flush();
	out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	doGet(req, resp);
    }

    /**
     * Recreates lessons after course has been copied. I.e. asks LAMS server to clone old lesson and then updates BB
     * link with the newly created lesson Id.
     * 
     * @param courseIdParam
     *            id of the course that has been copied
     * @return
     * @throws PersistenceException
     * @throws ValidationException
     * @throws IOException
     * @throws ServletException
     */
    public static String recreateLessonsAfterCourseCopy(String courseIdParam)
	    throws PersistenceException, ValidationException, ServletException, IOException {
	String newLessonIds = "";

	ContentDbPersister persister = ContentDbPersister.Default.getInstance();
	CourseDbLoader courseLoader = CourseDbLoader.Default.getInstance();
	Course course = courseLoader.loadByCourseId(courseIdParam);
	PkId courseId = (PkId) course.getId();
	String _course_id = "_" + courseId.getPk1() + "_" + courseId.getPk2();
	logger.debug("Starting clonning course lessons (courseId=" + courseId + ").");

	// find a teacher that will be assigned as lesson's author on LAMS side
	User teacher = BlackboardUtil.getCourseTeacher(courseId);

	//find all lessons that should be updated
	List<Content> lamsContents = BlackboardUtil.getLamsLessonsByCourse(courseId);
	for (Content content : lamsContents) {

	    PkId contentId = (PkId) content.getId();
	    String _content_id = "_" + contentId.getPk1() + "_" + contentId.getPk2();

	    String url = content.getUrl();
	    String urlLessonId = getParameterValue(url, "lsid");
	    String urlCourseId = getParameterValue(url, "course_id");
	    String urlContentId = getParameterValue(url, "content_id");

	    //in case when both courseId and contentId don't coincide with the ones from URL - means lesson needs to be cloned
	    if (!urlCourseId.equals(_course_id) && !urlContentId.equals(_content_id)) {

		final Long newLessonId = LamsSecurityUtil.cloneLesson(teacher, courseIdParam, urlLessonId);

		// update lesson id
		content.setLinkRef(Long.toString(newLessonId));

		// update URL
		url = replaceParameterValue(url, "lsid", Long.toString(newLessonId));
		url = replaceParameterValue(url, "course_id", _course_id);
		url = replaceParameterValue(url, "content_id", _content_id);
		content.setUrl(url);

		// persist updated content
		persister.persist(content);

		//update lineitem details
		LineitemUtil.updateLineitemLessonId(content, _course_id, newLessonId, teacher.getUserName());

		logger.debug("Lesson (lessonId=" + urlLessonId + ") was successfully cloned to the one (lessonId="
			+ newLessonId + ").");

		newLessonIds += newLessonId + ", ";
	    }

	}

	return newLessonIds;
    }

    /**
     * Returns param value, and empty string in case of there is no such param available
     * 
     * @param url
     * 
     * @param paramName
     * 
     * @return
     */
    private static String getParameterValue(String url, String paramName) {
	String paramValue = "";

	int quotationMarkIndex = url.indexOf("?");
	String queryPart = quotationMarkIndex > -1 ? url.substring(quotationMarkIndex + 1) : url;
	String[] paramEntries = queryPart.split("&");
	for (String paramEntry : paramEntries) {
	    String[] paramEntrySplitted = paramEntry.split("=");
	    if ((paramEntrySplitted.length > 1) && paramName.equalsIgnoreCase(paramEntrySplitted[0])) {
		paramValue = paramEntrySplitted[1];
		break;
	    }
	}

	return paramValue;
    }

    private static String replaceParameterValue(String url, String paramName, String newParamValue) {
	String oldParamValue = "";

	int quotationMarkIndex = url.indexOf("?");
	String queryPart = quotationMarkIndex > -1 ? url.substring(quotationMarkIndex + 1) : url;
	String[] paramEntries = queryPart.split("&");
	for (String paramEntry : paramEntries) {
	    String[] paramEntrySplitted = paramEntry.split("=");
	    if ((paramEntrySplitted.length > 1) && paramName.equalsIgnoreCase(paramEntrySplitted[0])) {
		oldParamValue = paramEntrySplitted[1];

		return url.replaceFirst(paramName + "=" + oldParamValue, paramName + "=" + newParamValue);
	    }
	}

	return url;
    }

}