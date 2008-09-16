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
package org.lamsfoundation.lams.tool;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**  
 * Represents the URLs for the internal functionality such as grouping and gates.
 * Eventually these will become "pluggable" tools, so that we can have other
 * types of grouping/gates, without making changes to the core.
 * 
 * @hibernate.class table="lams_system_tool"
 */
public class SystemTool implements Serializable {

	/* System Tool IDs */
	public static final Long GROUPING = new Long(1);
	public static final Long SYNC_GATE = new Long(2);
	public static final Long SCHEDULE_GATE = new Long(3);
	public static final Long PERMISSION_GATE = new Long(4);
	public static final Long SYSTEM_GATE = new Long(5);
	public static final Long TEACHER_CHOSEN_BRANCHING = new Long(6);
	public static final Long GROUP_BASED_BRANCHING = new Long(7);
	public static final Long TOOL_BASED_BRANCHING = new Long(8);
	public static final Long SEQUENCE = new Long(9);
	public static final Long CONDITION_GATE = new Long(9);

	/** identifier field */
	private Long systemToolId;

	/** persistent field - the type of activity */
	private Integer activityTypeId;

	/** persistent field */
	private String toolDisplayName;

	/** nullable persistent field */
	private String description;

	/** persistent field */
	private String learnerUrl;

	/** persistent field */
	private String learnerPreviewUrl;

	/** persistent field */
	private String learnerProgressUrl;

	/** persistent field */
	private String exportPortfolioLearnerUrl;

	/** persistent field */
	private String exportPortfolioClassUrl;

	/** persistent field */
	private String monitorUrl;

	/** persistent field */
	private String contributeUrl;

	/** persistent field */
	private String helpUrl;

	/** persistent field */
	private String adminUrl;

	/** persistent field */
	private Date createDateTime;

	/** 
	 * Entries for an tool in a language property file 
	 */
	public static final String I18N_DISPLAY_NAME = "tool.display.name";
	public static final String I18N_DESCRIPTION = "tool.description";

	/** full constructor */
	public SystemTool(Long systemToolId, Integer activityTypeId, String displayName, String description, String learnerUrl,
			String learnerPreviewUrl, String learnerProgressUrl, String exportPortfolioLearnerUrl,
			String exportPortfolioClassUrl, String monitorUrl, String contributeUrl, String helpUrl, Date createDateTime) {
		this.systemToolId = systemToolId;
		this.activityTypeId = activityTypeId;
		toolDisplayName = displayName;
		this.description = description;

		this.learnerUrl = learnerUrl;
		this.learnerPreviewUrl = learnerPreviewUrl;
		this.learnerProgressUrl = learnerProgressUrl;
		this.exportPortfolioLearnerUrl = exportPortfolioLearnerUrl;
		this.exportPortfolioClassUrl = exportPortfolioClassUrl;
		this.monitorUrl = monitorUrl;
		this.contributeUrl = contributeUrl;
		this.helpUrl = helpUrl;

		this.createDateTime = createDateTime;
	}

	/** default constructor */
	public SystemTool() {
	}

	/** 
	 * @hibernate.id generator-class="native" type="java.lang.Long"
	 *             	 column="system_tool_id"      
	 */
	public Long getSystemToolId() {
		return systemToolId;
	}

	public void setSystemToolId(Long systemToolId) {
		this.systemToolId = systemToolId;
	}

	/** 
	 * @hibernate.property column="learning_activity_type_id" type="java.lang.Integer"
	 *            		   not-null="true"    
	 */
	public Integer getActivityTypeId() {
		return activityTypeId;
	}

	public void setActivityTypeId(Integer activityTypeId) {
		this.activityTypeId = activityTypeId;
	}

	/** 
	 * @hibernate.property column="tool_display_name" length="255"
	 *             		   not-null="true"     
	 */
	public String getToolDisplayName() {
		return toolDisplayName;
	}

	public void setToolDisplayName(String toolDisplayName) {
		this.toolDisplayName = toolDisplayName;
	}

	/** 
	 * @hibernate.property column="description" length="65535"     
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/** 
	 * @hibernate.property column="learner_url" length="65535"
	 *            		   not-null="true"    
	 */
	public String getLearnerUrl() {
		return learnerUrl;
	}

	public void setLearnerUrl(String learnerUrl) {
		this.learnerUrl = learnerUrl;
	}

	/** 
	 * @hibernate.property column="learner_preview_url" length="65535"
	 *            		   not-null="false"    
	 */
	public String getLearnerPreviewUrl() {
		return learnerPreviewUrl;
	}

	public void setLearnerPreviewUrl(String learnerPreviewUrl) {
		this.learnerPreviewUrl = learnerPreviewUrl;
	}

	/** 
	 * @hibernate.property column="learner_progress_url" length="65535"
	 *            		   not-null="false"    
	 */
	public String getLearnerProgressUrl() {
		return learnerProgressUrl;
	}

	public void setLearnerProgressUrl(String learnerProgressUrl) {
		this.learnerProgressUrl = learnerProgressUrl;
	}

	/** 
	 * @hibernate.property column="export_pfolio_learner_url" length="65535"
	 *             		   not-null="false"  
	 */
	public String getExportPortfolioLearnerUrl() {
		return exportPortfolioLearnerUrl;
	}

	public void setExportPortfolioLearnerUrl(String exportPortfolioLearnerUrl) {
		this.exportPortfolioLearnerUrl = exportPortfolioLearnerUrl;
	}

	/** 
	 * @hibernate.property column="export_pfolio_class_url" length="65535"
	 *             		   not-null="false"  
	 */
	public String getExportPortfolioClassUrl() {
		return exportPortfolioClassUrl;
	}

	public void setExportPortfolioClassUrl(String exportPortfolioClassUrl) {
		this.exportPortfolioClassUrl = exportPortfolioClassUrl;
	}

	/**
	 * @hibernate.property column="admin_url" length="65535" not-null="false"  
	 * @return Returns the helpUrl.
	 */
	public String getAdminUrl() {
		return adminUrl;
	}

	/**
	 * @param helpUrl The helpUrl to set.
	 */
	public void setAdminUrl(String adminUrl) {
		this.adminUrl = adminUrl;
	}

	/**
	 * @hibernate.property column="create_date_time"
				           length="19" not-null="true"
	 * @return Returns the createDateTime.
	 */
	public Date getCreateDateTime() {
		return createDateTime;
	}

	/**
	 * @param createDateTime The createDateTime to set.
	 */
	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	/**
	 * Does this tool support contribute? Will be true if the contributeURL is not null/empty string.
	 */
	public boolean getSupportsContribute() {
		String contributeURL = getContributeUrl();
		return contributeURL != null && contributeURL.trim().length() > 0;
	}

	/**
	 * @hibernate.property column="contribute_url" length="65535" not-null="false"  
	 * @return Returns the contributeUrl.
	 */
	public String getContributeUrl() {
		return contributeUrl;
	}

	/**
	 * @param contributeUrl The contributUrl to set.
	 */
	public void setContributeUrl(String contributeUrl) {
		this.contributeUrl = contributeUrl;
	}

	/**
	 * @hibernate.property column="monitor_url" length="65535" not-null="false"  
	 * @return Returns the monitorUrl.
	 */
	public String getMonitorUrl() {
		return monitorUrl;
	}

	/**
	 * @param monitorUrl The monitorUrl to set.
	 */
	public void setMonitorUrl(String monitorUrl) {
		this.monitorUrl = monitorUrl;
	}

	/**
	 * @hibernate.property column="help_url" length="65535" not-null="false"  
	 * @return Returns the helpUrl.
	 */
	public String getHelpUrl() {
		return helpUrl;
	}

	/**
	 * @param helpUrl The helpUrl to set.
	 */
	public void setHelpUrl(String helpUrl) {
		this.helpUrl = helpUrl;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("systemToolId", getSystemToolId()).append("activityTypeId", getActivityTypeId())
				.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SystemTool)) {
			return false;
		}
		SystemTool castOther = (SystemTool) other;
		return new EqualsBuilder().append(this.getSystemToolId(), castOther.getSystemToolId()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getSystemToolId()).toHashCode();
	}

}
