<%--
Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

http://www.gnu.org/licenses/gpl.txt
--%>

<%@ taglib uri="tags-bean" prefix="bean"%> 
<%@ taglib uri="tags-html" prefix="html"%>
<%@ taglib uri="tags-logic" prefix="logic" %>
<%@ taglib uri="tags-logic-el" prefix="logic-el" %>
<%@ taglib uri="tags-core" prefix="c"%>
<%@ taglib uri="tags-fmt" prefix="fmt" %>
<%@ taglib uri="fck-editor" prefix="FCK" %>
<%@ taglib uri="tags-lams" prefix="lams" %>

<c:set var="lams"><lams:LAMSURL/></c:set>
<c:set var="tool"><lams:WebAppURL/></c:set>


			<table class="forms">
				<tr>
	      			<td NOWRAP class="formlabel" valign=top>
		      			<font size=2> 
			      			<b> <bean:message key="radiobox.sln"/>: </b>
		      			</font>
					</td>
					<td NOWRAP valign=top>
						<font size=2>
							<html:radio property="sln"  value="ON">
							 <bean:message key="option.on"/>
							</html:radio>
	
							<html:radio property="sln"  value="OFF">
							 <bean:message key="option.off"/>
							</html:radio>
						</font>
	      			</td>
		         </tr>
		         
   				<tr>
					<td NOWRAP class="formlabel" valign=top>
						<font size=2>
			      			<b> <bean:message key="radiobox.onepq"/>: </b>
		      			</font>
					</td>
					<td NOWRAP valign=top>
						<font size=2>
							<html:radio property="questionsSequenced" value="ON">
							 <bean:message key="option.on"/>
							</html:radio>
	
							<html:radio property="questionsSequenced" value="OFF">
							 <bean:message key="option.off"/>
							</html:radio>
						</font>
	      			</td>
		        </tr>
		         
		         <tr>
					<td NOWRAP class="formlabel" valign=top>
						<font size=2>
			      			<b> <bean:message key="radiobox.retries"/>: </b>
		      			</font>
					</td>
					<td NOWRAP valign=top>
						<font size=2>
							<html:radio property="retries" value="ON">
							 <bean:message key="option.on"/>
							</html:radio>
	
							<html:radio property="retries" value="OFF">
							 <bean:message key="option.off"/>
							</html:radio>
						</font>
	      			</td>

				</tr>	      			
		        
		         <tr> 
					<td NOWRAP class="formlabel" valign=top>
						<font size=2>
					 		<b>	<bean:message key="label.report.title"/>: </b>
				 		</font>
				 	</td>
	  				<td NOWRAP class="formcontrol" valign=top>
		  				<font size=2>
							<FCK:editor id="richTextReportTitle" basePath="/lams/fckeditor/">
								  <c:out value="${sessionScope.richTextReportTitle}" escapeXml="false" />						  
							</FCK:editor>
						</font>
					</td> 
			  	</tr>
			  	
			  	<tr> 
					<td NOWRAP class="formlabel" valign=top>
						<font size=2>
					 		<b>	<bean:message key="label.report.endLearningMessage"/>: </b>
			 			</font>
			 		</td>
	  				<td NOWRAP class="formcontrol">
		  				<font size=2>
							<FCK:editor id="richTextEndLearningMsg" basePath="/lams/fckeditor/">
								  <c:out value="${sessionScope.richTextEndLearningMsg}" escapeXml="false" />						  
							</FCK:editor>
						</font>
					</td> 
			  	</tr>
			  	
		  		<tr>
 				 	<td NOWRAP colspan=2 align=center valign=top>								
						&nbsp&nbsp
				  	</td>
				</tr>

				<tr>
				 	<td NOWRAP valign=top>								
						&nbsp&nbsp
				  	</td>
					 <td NOWRAP valign=top> 
		 			 <font size=2>
						 <html:submit onclick="javascript:submitMethod('doneAdvancedTab');" styleClass="button">
								<bean:message key="button.done"/>
						</html:submit>
					 </font>
					</td> 
 				 </tr>
			</table>	  	
		
			