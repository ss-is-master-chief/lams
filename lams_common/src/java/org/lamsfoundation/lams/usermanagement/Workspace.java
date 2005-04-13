package org.lamsfoundation.lams.usermanagement;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.lamsfoundation.lams.usermanagement.dto.WorkspaceDTO;

/** 
 *        @hibernate.class
 *         table="lams_workspace"
 *     
*/
public class Workspace implements Serializable {

    /** identifier field */
    private Integer workspaceId;

    /** persistent field */
    private WorkspaceFolder rootFolder;

    /** persistent field */
    private Set users;

    /** persistent field */
    private Set organisations;
    
    /** nullable persistent field representing the name of the workspace,
     *  defaults to the name of the Organisation 
     * */
    private String name;

	public Workspace(String name) {
		super();
		this.name = name;
	}
    /** full constructor */
    public Workspace(WorkspaceFolder workspaceFolder, Set users, Set organisations) {
        this.rootFolder = workspaceFolder;
        this.users = users;
        this.organisations = organisations;
    }

    /** default constructor */
    public Workspace() {
    }

    /** 
     *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="workspace_id"
     *         
     */
    public Integer getWorkspaceId() {
        return this.workspaceId;
    }

    public void setWorkspaceId(Integer workspaceId) {
        this.workspaceId = workspaceId;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="root_folder_id"         
     *         
     */
    public WorkspaceFolder getRootFolder() {
        return this.rootFolder;
    }

    public void setRootFolder(WorkspaceFolder workspaceFolder) {
        this.rootFolder = workspaceFolder;
    }

    /** 
     *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="workspace_id"
     *            @hibernate.collection-one-to-many
     *             class="org.lamsfoundation.lams.usermanagement.User"
     *         
     */
    public Set getUsers() {
        return this.users;
    }

    public void setUsers(Set users) {
        this.users = users;
    }

    /** 
     *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="workspace_id"
     *            @hibernate.collection-one-to-many
     *             class="org.lamsfoundation.lams.usermanagement.Organisation"
     *         
     */
    public Set getOrganisations() {
        return this.organisations;
    }

    public void setOrganisations(Set organisations) {
        this.organisations = organisations;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("workspaceId", getWorkspaceId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Workspace) ) return false;
        Workspace castOther = (Workspace) other;
        return new EqualsBuilder()
            .append(this.getWorkspaceId(), castOther.getWorkspaceId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getWorkspaceId())
            .toHashCode();
    }

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	public WorkspaceDTO getWorkspaceDTO(){
		return new WorkspaceDTO(workspaceId,
								rootFolder.getWorkspaceFolderId()); 
	}
}
