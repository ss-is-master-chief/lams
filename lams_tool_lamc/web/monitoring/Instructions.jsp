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

	<h2> <font size=2> <bean:message key="button.instructions"/> </font> </h2>
	<div id="datatablecontainer">
		<table class="forms">
			<tr>
				<td>
					<table align="center">
						<tr> 
							<td NOWRAP valign=top>
				  				<b> <font size=2> <bean:message key="label.offlineInstructions.col" /> </font> </b>
				  			</td>
							<td NOWRAP valign=top>
								  <font size=2> <c:out value="${richTextOfflineInstructions}" escapeXml="false" />	</font>					  
							</td> 
						</tr>
						
						<tr>
							<td colspan=2> &nbsp&nbsp&nbsp </td>
						</tr>
				
						<tr> 
							<td NOWRAP valign=top>
				  				<b> <font size=2> <bean:message key="label.onlineInstructions.col" /> </font></b>
				  			</td>
							<td NOWRAP valign=top>
								  <font size=2> <c:out value="${sessionScope.richTextOnlineInstructions}" escapeXml="false" />	</font>					  
							</td> 
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</div>		

