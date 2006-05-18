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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $$Id$$ */	
package org.lamsfoundation.lams.tool.forum.persistence;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.lamsfoundation.lams.contentrepository.ItemNotFoundException;
import org.lamsfoundation.lams.contentrepository.NodeKey;
import org.lamsfoundation.lams.contentrepository.RepositoryCheckedException;
import org.lamsfoundation.lams.tool.forum.util.ForumToolContentHandler;

/**
 * @author conradb
 *
 * @hibernate.class table="tl_lafrum11_message"
 *
 */
public class Message implements Cloneable{
	
	private static Logger log = Logger.getLogger(Message.class);
	
	private Long uid;
	private String subject;
	private String body;
	private boolean isAuthored;
	private boolean isAnonymous;

	
	private Date created;
	private Date updated;
	private Date lastReplyDate;
	private int replyNumber;
	private boolean hideFlag;

	private Message parent;
	private ForumToolSession toolSession;
	private Forum forum;
	private ForumUser createdBy;
	private ForumUser modifiedBy;
	private Set attachments;
	private ForumReport report;

	private ForumToolContentHandler toolContentHandler;
	
	public Message(){
		attachments = new TreeSet();
	}
//  **********************************************************
  	//		Function method for Message
//  **********************************************************
	public static Message newInstance(Message fromMsg,ForumToolContentHandler toolContentHandler){
		Message toMsg = new Message();
		fromMsg.toolContentHandler = toolContentHandler;
		toMsg = (Message) fromMsg.clone();
		return toMsg;
	}
	/**
	 * <em>This method DOES NOT deep clone <code>Forum</code> to avoid dead loop in clone.</em>
	 */
  	public Object clone(){
  		
  		Message msg = null;
  		try{
  			msg = (Message) super.clone();
  			msg.setUid(null);
  			if(parent != null){
  				msg.parent = (Message) parent.clone();
  				//try to keep parent uid, so avoid persistant a new instance in database for parent message 
  				msg.parent.uid = parent.uid;
  			}
  			if(toolSession != null){
  				msg.toolSession = (ForumToolSession) toolSession.clone();
  			}
  			//don't  deep clone forum to avoid dead loop in clone
  			if(createdBy != null){
  				msg.createdBy = (ForumUser) createdBy.clone();
  			}
  			if(modifiedBy != null)
  				msg.modifiedBy = (ForumUser) modifiedBy.clone();
  			//clone attachment
  			if(attachments != null){
  				Iterator iter = attachments.iterator();
  				Set set = new TreeSet();
  				while(iter.hasNext()){
  					Attachment file = (Attachment)iter.next();
  					Attachment newFile = (Attachment) file.clone();
  					if(toolContentHandler != null){
						//duplicate file node in repository
						NodeKey keys = toolContentHandler.copyFile(file.getFileUuid());
						newFile.setFileUuid(keys.getUuid());
						newFile.setFileVersionId(keys.getVersion());
  					}
  					set.add(newFile);
  				}
  				msg.attachments = set;
  			}
		} catch (CloneNotSupportedException e) {
			log.error("When clone " + Forum.class + " failed");
		} catch (ItemNotFoundException e) {
			log.error("When clone " + Forum.class + " failed");
		} catch (RepositoryCheckedException e) {
			log.error("When clone " + Forum.class + " failed");
		}
  		
  		return msg;
  	}
	/**
	 * Updates the modification data for this entity.
	 */
	public void updateModificationData() {
		long now = System.currentTimeMillis();
		if (created == null) {
			this.setCreated(new Date(now));
		}
		this.setUpdated(new Date(now));
	}
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Message))
			return false;
		
		final Message genericEntity = (Message) o;

      	return new EqualsBuilder()
      	.append(this.uid,genericEntity.uid)
      	.append(this.subject,genericEntity.subject)
      	.append(this.body,genericEntity.body)
      	.append(this.replyNumber,genericEntity.replyNumber)
      	.append(this.lastReplyDate,genericEntity.lastReplyDate)
      	.append(this.created,genericEntity.created)
      	.append(this.updated,genericEntity.updated)
      	.append(this.createdBy,genericEntity.createdBy)
      	.append(this.modifiedBy,genericEntity.modifiedBy)
      	.isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder().append(uid)
		.append(subject).append(body)
		.append(created)
		.append(updated).append(createdBy)
		.append(modifiedBy)
		.toHashCode();
	}
	
//  **********************************************************
  	//		get/set methods
//  **********************************************************
	/**
	 * Returns the object's creation date
	 *
	 * @return date
	 * @hibernate.property column="create_date"
	 */
	public Date getCreated() {
      return created;
	}

	/**
	 * Sets the object's creation date
	 *
	 * @param created
	 */
	public void setCreated(Date created) {
	    this.created = created;
	}
	/**
	 * Returns this topic last reply date
	 *
	 * @return date
	 * @hibernate.property column="last_reply_date"
	 */
	public Date getLastReplyDate() {
		return lastReplyDate;
	}

	public void setLastReplyDate(Date lastPostDate) {
		this.lastReplyDate = lastPostDate;
	}
	/**
	 * Returns the object's date of last update
	 *
	 * @return date updated
	 * @hibernate.property column="update_date"
	 */
	public Date getUpdated() {
        return updated;
	}

	/**
	 * Sets the object's date of last update
	 *
	 * @param updated
	 */
	public void setUpdated(Date updated) {
        this.updated = updated;
	}

    /**
     * @return Returns the userid of the user who created the Forum.
     *
     * @hibernate.many-to-one
     * 		column="create_by"
     *  	cascade="none"
     *
     */
    public ForumUser getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy The userid of the user who created this Forum.
     */
    public void setCreatedBy(ForumUser createdBy) {
        this.createdBy = createdBy;
    }


    /**
     * @hibernate.many-to-one
     * 		column="modified_by"
     * 		cascade="none"
     * 
     * @return Returns the userid of the user who modified the posting.
     */
    public ForumUser getModifiedBy() {
        return modifiedBy;
    }

    /**
     * @param modifiedBy The userid of the user who modified the posting.
     */
    public void setModifiedBy(ForumUser modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    /**
     * @hibernate.id column="uid" generator-class="native"
     */
	public Long getUid() {
		return uid;
	}

	public void setUid(Long uuid) {
		this.uid = uuid;
	}

    /**
     * @return Returns the subject of the Message.
     *
     * @hibernate.property
     * 		column="subject"
     *
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject The subject of the Message to be set.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

	/**
	 * @return Returns the body of the Message.
	 *
	 * @hibernate.property 
	 * 		column="body"
     *      type="text"
	 *  	 
	 */
	public String getBody() {
		return body;
	}
	
	/**
	 * @param body The body of the Message to set.
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return Returns true if the Message was an Authored Message.
	 *
	 * @hibernate.property
	 * 		column="is_authored"
	 *
	 */
	public boolean getIsAuthored() {
		return isAuthored;
	}

	/**
	 * @param isAuthored Set isAuthored to true if Message was authored
	 * 		otherwise set to false.
	 */
	public void setIsAuthored(boolean isAuthored) {
		this.isAuthored = isAuthored;
	}

	/**
	 * @return Returns whether the Message should be shown as an
	 * 		Annonymous message.
	 *
	 * @hibernate.property 
	 * 		column="is_anonymous"
	 *  	 
	 */
	public boolean getIsAnonymous() {
		return isAnonymous;
	}
	
	/**
	 * @param isAnonymous Indicates that the Message is to be shown
	 * 			as an Annonymous message when set to true.
	 */
	public void setIsAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}

    /**
      * Gets the toolSession
      *
      * @hibernate.many-to-one
      *	class="org.lamsfoundation.lams.tool.forum.persistence.ForumToolSession"
      *	              column="forum_session_uid"
      *
      */
     public ForumToolSession getToolSession() {
         return toolSession;
     }


	/**
	 * @param toolSession The toolSession that this Message belongs to
     */
    public void setToolSession(ForumToolSession session) {
         this.toolSession = session;
     }

	/**
	 * @param parent The parent of this Message
     */
    public void setParent(Message parent) {
        this.parent = parent;
    }
    /**
     * @hibernate.many-to-one column="parent_uid"
     * @return
     */
    public Message getParent() {
        return parent;
    }
	/**
     * @return a set of Attachments to this Message.
     *
     * @hibernate.set table="ATTACHMENT"
     * inverse="false"
     * lazy="false"
     * cascade="all"
     * @hibernate.collection-key column="message_uid"
     * @hibernate.collection-one-to-many
     * 			class="org.lamsfoundation.lams.tool.forum.persistence.Attachment"
     *
     */
	public Set getAttachments() {
		return attachments;
	}

    /*
	 * @param attachments The attachments to set.
     */
    public void setAttachments(Set attachments) {
		this.attachments = attachments;
	}
    
	/**
	 * @hibernate.many-to-one column="forum_uid"
	 * 			  cascade="none"
	 * @return
	 */
	public Forum getForum() {
		return forum;
	}

	public void setForum(Forum forum) {
		this.forum = forum;
	}
	
	/**
	 * @hibernate.property column="reply_number"
	 * @return
	 */
	public int getReplyNumber() {
		return replyNumber;
	}

	public void setReplyNumber(int replyNumber) {
		this.replyNumber = replyNumber;
	}
	/**
	 * @hibernate.property column="hide_flag"
	 * @return
	 */
	public boolean isHideFlag() {
		return hideFlag;
	}

	public void setHideFlag(boolean hideFlag) {
		this.hideFlag = hideFlag;
	}
	/**
	 * @hibernate.many-to-one column="report_id"
	 * 			cascade="all"
	 * @return
	 */
	public ForumReport getReport() {
		return report;
	}
	public void setReport(ForumReport report) {
		this.report = report;
	}
	
	public String toString() {
		return new ToStringBuilder(this).
			append("uid",uid).
			append("subject",subject).
			append("body",body).toString();
	}
}
