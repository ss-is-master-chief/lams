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

package org.lamsfoundation.lams.web.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.util.Configuration;
import org.lamsfoundation.lams.util.ConfigurationKeys;

/**
 * Output the server url from the value stored in the shared session. This
 * will be the same value as the server url in the lams.xml configuration file.
 * 
 * @jsp.tag name="LAMSURL"
 * 			bodycontent="empty"
 * 			display-name="LAMS URL"
 * 			description="Output the Server URL as defined in the lams.xml configuration file."
 * 	
 * @author Fiona Malikoff
 */
public class LAMSURLTag extends TagSupport {

	private static final long serialVersionUID = -3773379475085729642L;

	private static final Logger log = Logger.getLogger(LAMSURLTag.class);

	/**
	 * 
	 */
	public LAMSURLTag() {
		super();
	}
	
	public int doStartTag() throws JspException {
		String serverURL = Configuration.get(ConfigurationKeys.SERVER_URL);
		serverURL = ( serverURL != null ? serverURL.trim() : null);
   		if ( serverURL != null || serverURL.length()>0 ) {
   			JspWriter writer = pageContext.getOut();
   			try {
				writer.print(serverURL);
   			} catch ( IOException e ) {
				log.error("ServerURLTag unable to write out server URL due to IOException. ", e);
				throw new JspException(e);
   			}
   		} else {
	   		log.warn("ServerURLTag unable to write out server URL as it is missing from the configuration file.");
   		}
	   	
    	return SKIP_BODY;
	}

	public int doEndTag() {
		return EVAL_PAGE;
	}
	
}
