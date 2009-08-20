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
package org.lamsfoundation.lams.usermanagement.service;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lamsfoundation.lams.dao.IBaseDAO;
import org.lamsfoundation.lams.learningdesign.dao.IGroupDAO;
import org.lamsfoundation.lams.themes.Theme;
import org.lamsfoundation.lams.usermanagement.ForgotPasswordRequest;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.OrganisationType;
import org.lamsfoundation.lams.usermanagement.Role;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.UserOrganisation;
import org.lamsfoundation.lams.usermanagement.UserOrganisationRole;
import org.lamsfoundation.lams.usermanagement.Workspace;
import org.lamsfoundation.lams.usermanagement.WorkspaceFolder;
import org.lamsfoundation.lams.usermanagement.WorkspaceWorkspaceFolder;
import org.lamsfoundation.lams.usermanagement.dao.IOrganisationDAO;
import org.lamsfoundation.lams.usermanagement.dao.IRoleDAO;
import org.lamsfoundation.lams.usermanagement.dao.IUserOrganisationDAO;
import org.lamsfoundation.lams.usermanagement.dto.CollapsedOrgDTO;
import org.lamsfoundation.lams.usermanagement.dto.OrganisationDTO;
import org.lamsfoundation.lams.usermanagement.dto.OrganisationDTOFactory;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.usermanagement.dto.UserFlashDTO;
import org.lamsfoundation.lams.usermanagement.dto.UserManageBean;
import org.lamsfoundation.lams.util.Configuration;
import org.lamsfoundation.lams.util.ConfigurationKeys;
import org.lamsfoundation.lams.util.HashUtil;
import org.lamsfoundation.lams.util.LanguageUtil;
import org.lamsfoundation.lams.util.MessageService;
import org.lamsfoundation.lams.util.audit.IAuditService;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.HttpSessionManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>
 * <a href="UserManagementService.java.html"> <i>View Source </i> </a>
 * </p>
 * 
 * Manually caches the user objects (by user id) in the shared cache. Whenever a user object is modified, the cached
 * version must be removed. TODO complete the caching - need to remove the user from the cache on modification of
 * user/organisation details.
 * 
 * @author Fei Yang, Manpreet Minhas
 */
public class UserManagementService implements IUserManagementService {

    private Logger log = Logger.getLogger(UserManagementService.class);

    private static final String SEQUENCES_FOLDER_NAME_KEY = "runsequences.folder.name";

    private IBaseDAO baseDAO;

    private IGroupDAO groupDAO;

    private IRoleDAO roleDAO;

    private IOrganisationDAO organisationDAO;

    private IUserOrganisationDAO userOrganisationDAO;

    protected MessageService messageService;

    private static IAuditService auditService;

    private IAuditService getAuditService() {
	if (UserManagementService.auditService == null) {
	    WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(HttpSessionManager
		    .getInstance().getServletContext());
	    UserManagementService.auditService = (IAuditService) ctx.getBean("auditService");
	}
	return UserManagementService.auditService;
    }

    /**
     * Set i18n MessageService
     */
    public void setMessageService(MessageService messageService) {
	this.messageService = messageService;
    }

    /**
     * Get i18n MessageService
     */
    public MessageService getMessageService() {
	return messageService;
    }

    public void setBaseDAO(IBaseDAO baseDAO) {
	this.baseDAO = baseDAO;
    }

    public void setGroupDAO(IGroupDAO groupDAO) {
	this.groupDAO = groupDAO;
    }

    public void setRoleDAO(IRoleDAO roleDAO) {
	this.roleDAO = roleDAO;
    }

    public void setOrganisationDAO(IOrganisationDAO organisationDAO) {
	this.organisationDAO = organisationDAO;
    }

    public void setUserOrganisationDAO(IUserOrganisationDAO userOrganisationDAO) {
	this.userOrganisationDAO = userOrganisationDAO;
    }

    public void save(Object object) {
	try {
	    if (object instanceof User) {
		User user = (User) object;
		object = saveUser(user);
	    }
	    baseDAO.insertOrUpdate(object);
	} catch (Exception e) {
	    log.debug(e);
	}
    }

    protected User saveUser(User user) {
	if (user != null) {
	    // LDEV-2196 ensure names saved as UTF-8
	    try {
		user.setFirstName(new String(user.getFirstName().getBytes(), "UTF-8"));
		user.setLastName(new String(user.getLastName().getBytes(), "UTF-8"));
	    } catch (UnsupportedEncodingException e) {
		log.error("Unsupported encoding...", e);
	    }
	    // create user
	    if (user.getUserId() == null) {
		baseDAO.insertOrUpdate(user); // creating a workspace needs a userId
		user = createWorkspaceForUser(user);
	    }
	    // LDEV-2030 update workspace name if name changed
	    Workspace workspace = user.getWorkspace();
	    if (workspace != null && !StringUtils.equals(user.getFullName(), workspace.getName())) {
		workspace.setName(user.getFullName());
		save(workspace);
		WorkspaceFolder folder = workspace.getDefaultFolder();
		if (folder != null) {
		    folder.setName(workspace.getName());
		    save(folder);
		}
	    }
	}
	return user;
    }

    public void saveAll(Collection objects) {
	for (Object o : objects) {
	    if (o instanceof User) {
		baseDAO.insertOrUpdate(o); // creating a workspace needs
		// a userId
		o = createWorkspaceForUser((User) o);
	    }
	}
	baseDAO.insertOrUpdateAll(objects);
    }

    public void delete(Object object) {
	baseDAO.delete(object);
    }

    public void deleteAll(Class clazz) {
	baseDAO.deleteAll(clazz);
    }

    public void deleteAll(Collection objects) {
	baseDAO.deleteAll(objects);
    }

    public void deleteById(Class clazz, Serializable id) {
	baseDAO.deleteById(clazz, id);
    }

    public void deleteByProperty(Class clazz, String name, Object value) {
	baseDAO.deleteByProperty(clazz, name, value);
    }

    public void deleteByProperties(Class clazz, Map<String, Object> properties) {
	baseDAO.deleteByProperties(clazz, properties);
    }

    public void deleteAnythingLike(Object object) {
	baseDAO.deleteAnythingLike(object);
    }

    public Object findById(Class clazz, Serializable id) {
	return baseDAO.find(clazz, id);
    }

    public List findAll(Class clazz) {
	return baseDAO.findAll(clazz);
    }

    public List findByProperty(Class clazz, String name, Object value) {
	return baseDAO.findByProperty(clazz, name, value);
    }

    public List findByProperties(Class clazz, Map<String, Object> properties) {
	return baseDAO.findByProperties(clazz, properties);
    }

    public List findAnythingLike(Object object) {
	return baseDAO.findAnythingLike(object);
    }

    public List searchByStringProperties(Class clazz, Map<String, String> properties) {
	return baseDAO.searchByStringProperties(clazz, properties);
    }

    /**
     * @see org.lamsfoundation.lams.usermanagement.service.IUserManagementService
     *      #getOrganisationRolesForUser(org.lamsfoundation.lams.usermanagement. User, java.util.List<String>)
     */
    public OrganisationDTO getOrganisationsForUserByRole(User user, List<String> restrictToRoleNames) {
	List<OrganisationDTO> list = new ArrayList<OrganisationDTO>();
	Iterator i = user.getUserOrganisations().iterator();

	while (i.hasNext()) {
	    UserOrganisation userOrganisation = (UserOrganisation) i.next();
	    OrganisationDTO dto = userOrganisation.getOrganisation().getOrganisationDTO();
	    boolean aRoleFound = addRolesToDTO(restrictToRoleNames, userOrganisation, dto);
	    if (aRoleFound) {
		list.add(dto);
	    }
	}
	return OrganisationDTOFactory.createTree(list);
    }

    /**
     * @see org.lamsfoundation.lams.usermanagement.service.IUserManagementService
     *      #getOrganisationRolesForUser(org.lamsfoundation.lams.usermanagement. User, java.util.List<String>,
     *      java.util.Integer)
     */
    public OrganisationDTO getOrganisationsForUserByRole(User user, List<String> restrictToRoleNames, Integer courseId,
	    List<Integer> restrictToClassIds) {
	List<OrganisationDTO> dtolist = new ArrayList<OrganisationDTO>();
	Organisation org = (Organisation) baseDAO.find(Organisation.class, courseId);
	dtolist.add(org.getOrganisationDTO());
	getChildOrganisations(user, org, restrictToRoleNames, restrictToClassIds, dtolist);
	OrganisationDTO dtoTree = OrganisationDTOFactory.createTree(dtolist);

	// Want to return the course as the main node, not the dummy root.
	Vector nodes = dtoTree.getNodes();
	return (OrganisationDTO) nodes.get(0);

    }

    @SuppressWarnings("unchecked")
    private void getChildOrganisations(User user, Organisation org, List<String> restrictToRoleNames,
	    List<Integer> restrictToClassIds, List<OrganisationDTO> dtolist) {
	if (org != null) {
	    boolean notCheckClassId = restrictToClassIds == null || restrictToClassIds.size() == 0;
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("user.userId", user.getUserId());
	    map.put("organisation.parentOrganisation.organisationId", org.getOrganisationId());
	    List<UserOrganisation> childOrgs = baseDAO.findByProperties(UserOrganisation.class, map);
	    for (UserOrganisation userOrganisation : childOrgs) {
		OrganisationDTO dto = userOrganisation.getOrganisation().getOrganisationDTO();
		if (notCheckClassId || restrictToClassIds.contains(dto.getOrganisationID())) {
		    boolean aRoleFound = addRolesToDTO(restrictToRoleNames, userOrganisation, dto);
		    if (aRoleFound) {
			dtolist.add(dto);
		    }

		    // now, process any children of this org
		    Organisation childOrganisation = userOrganisation.getOrganisation();
		    if (org.getChildOrganisations().size() > 0) {
			getChildOrganisations(user, childOrganisation, restrictToRoleNames, restrictToClassIds, dtolist);
		    }
		}
	    }
	}
    }

    /**
     * Go through the roles for this user organisation and add the roles to the dto.
     * 
     * @param restrictToRoleNames
     * @param userOrganisation
     * @param dto
     * @return true if a role is found, false otherwise
     */
    private boolean addRolesToDTO(List<String> restrictToRoleNames, UserOrganisation userOrganisation,
	    OrganisationDTO dto) {
	Iterator iter = userOrganisation.getUserOrganisationRoles().iterator();

	boolean roleFound = false;
	while (iter.hasNext()) {

	    UserOrganisationRole userOrganisationRole = (UserOrganisationRole) iter.next();
	    String roleName = userOrganisationRole.getRole().getName();
	    if (restrictToRoleNames == null || restrictToRoleNames.size() == 0
		    || restrictToRoleNames.contains(roleName)) {
		dto.addRoleName(roleName);
		roleFound = true;
	    }
	}
	return roleFound;
    }

    /**
     * Gets an organisation for a user, with the user's roles. Doesn't not return a tree of organisations. Will not
     * return the organisation if there isn't any roles for this user.
     */
    public OrganisationDTO getOrganisationForUserWithRole(User user, Integer organisationId) {
	if (user != null && organisationId != null) {
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("user.userId", user.getUserId());
	    map.put("organisation.organisationId", organisationId);
	    UserOrganisation userOrganisation = (UserOrganisation) baseDAO
		    .findByProperties(UserOrganisation.class, map).get(0);
	    OrganisationDTO dto = userOrganisation.getOrganisation().getOrganisationDTO();
	    addRolesToDTO(null, userOrganisation, dto);
	    return dto;
	}
	return null;
    }

    /**
     * @see org.lamsfoundation.lams.usermanagement.service.IUserManagementService#getRolesForUserByOrganisation(org.lamsfoundation.lams.usermanagement.User,
     *      java.lang.Integer)
     */
    public List<Role> getRolesForUserByOrganisation(User user, Integer orgId) {
	List<Role> list = new ArrayList<Role>();
	Map<String, Object> map = new HashMap<String, Object>();
	map.put("user.userId", user.getUserId());
	map.put("organisation.organisationId", orgId);
	UserOrganisation userOrg = (UserOrganisation) baseDAO.findByProperties(UserOrganisation.class, map).get(0);
	if (userOrg == null) {
	    return null;
	}
	Iterator i = userOrg.getUserOrganisationRoles().iterator();
	while (i.hasNext()) {
	    UserOrganisationRole userOrgRole = (UserOrganisationRole) i.next();
	    list.add(userOrgRole.getRole());
	}
	return list;
    }

    /**
     * @see org.lamsfoundation.lams.usermanagement.service.IUserManagementService#getUsersFromOrganisation(int)
     */
    public List<User> getUsersFromOrganisation(Integer orgId) {
	String query = "select uo.user from UserOrganisation uo" + " where uo.organisation.organisationId=" + orgId
		+ " order by uo.user.login";
	return baseDAO.find(query);
    }

    /**
     * @see org.lamsfoundation.lams.usermanagement.service.IUserManagementService#getUsersFromOrganisationByRole(java.lang.Integer,
     *      java.lang.String)
     */
    public Vector getUsersFromOrganisationByRole(Integer organisationID, String roleName, boolean isFlashCall,
	    boolean getUser) {
	Vector users = null;
	if (isFlashCall) {
	    users = new Vector<UserFlashDTO>();
	} else if (getUser) {
	    users = new Vector<User>();
	} else {
	    users = new Vector<UserDTO>();
	}

	Organisation organisation = (Organisation) baseDAO.find(Organisation.class, organisationID);
	if (organisation != null) {
	    Set uos = organisation.getUserOrganisations();
	    if (uos != null) {
		Iterator iterator = uos.iterator();
		while (iterator.hasNext()) {
		    UserOrganisation userOrganisation = (UserOrganisation) iterator.next();
		    Iterator userOrganisationRoleIterator = userOrganisation.getUserOrganisationRoles().iterator();
		    while (userOrganisationRoleIterator.hasNext()) {
			UserOrganisationRole userOrganisationRole = (UserOrganisationRole) userOrganisationRoleIterator
				.next();
			if (userOrganisationRole.getRole().getName().equals(roleName)) {
			    if (isFlashCall && !getUser) {
				users.add(userOrganisation.getUser().getUserFlashDTO());
			    } else if (getUser) {
				users.add(userOrganisation.getUser());
			    } else {
				users.add(userOrganisation.getUser().getUserDTO());
			    }
			}
		    }
		}
	    }
	}
	return users;
    }

    public Organisation getRootOrganisation() {
	return (Organisation) baseDAO.findByProperty(Organisation.class, "organisationType.organisationTypeId",
		OrganisationType.ROOT_TYPE).get(0);
    }

    public boolean isUserInRole(Integer userId, Integer orgId, String roleName) {
	Map<String, Object> properties = new HashMap<String, Object>();
	properties.put("userOrganisation.user.userId", userId);
	properties.put("userOrganisation.organisation.organisationId", orgId);
	properties.put("role.name", roleName);
	if (baseDAO.findByProperties(UserOrganisationRole.class, properties).size() == 0) {
	    return false;
	}
	return true;
    }

    public List getOrganisationsByTypeAndStatus(Integer typeId, Integer stateId) {
	Map<String, Object> properties = new HashMap<String, Object>();
	properties.put("organisationType.organisationTypeId", typeId);
	properties.put("organisationState.organisationStateId", stateId);
	return baseDAO.findByProperties(Organisation.class, properties);
    }

    public List getUserOrganisationRoles(Integer orgId, String login) {
	Map<String, Object> properties = new HashMap<String, Object>();
	properties.put("userOrganisation.organisation.organisationId", orgId);
	properties.put("userOrganisation.user.login", login);
	return baseDAO.findByProperties(UserOrganisationRole.class, properties);
    }

    public List getUserOrganisationsForUserByTypeAndStatus(String login, Integer typeId, Integer stateId) {
	Map<String, Object> properties = new HashMap<String, Object>();
	properties.put("user.login", login);
	properties.put("organisation.organisationType.organisationTypeId", typeId);
	properties.put("organisation.organisationState.organisationStateId", stateId);
	return baseDAO.findByProperties(UserOrganisation.class, properties);
    }

    public List getUserOrganisationsForUserByTypeAndStatusAndParent(String login, Integer typeId, Integer stateId,
	    Integer parentOrgId) {
	Map<String, Object> properties = new HashMap<String, Object>();
	properties.put("user.login", login);
	properties.put("organisation.organisationType.organisationTypeId", typeId);
	properties.put("organisation.organisationState.organisationStateId", stateId);
	properties.put("organisation.parentOrganisation.organisationId", parentOrgId);
	return baseDAO.findByProperties(UserOrganisation.class, properties);
    }

    public User getUserByLogin(String login) {
	List results = baseDAO.findByProperty(User.class, "login", login);
	return results.isEmpty() ? null : (User) results.get(0);
    }

    public void updatePassword(String login, String password) {
	try {
	    User user = getUserByLogin(login);
	    user.setPassword(HashUtil.sha1(password));
	    baseDAO.update(user);
	} catch (Exception e) {
	    log.debug(e);
	}
    }

    public UserOrganisation getUserOrganisation(Integer userId, Integer orgId) {
	Map<String, Object> properties = new HashMap<String, Object>();
	properties.put("user.userId", userId);
	properties.put("organisation.organisationId", orgId);
	List results = baseDAO.findByProperties(UserOrganisation.class, properties);
	return results.isEmpty() ? null : (UserOrganisation) results.get(0);
    }

    private User createWorkspaceForUser(User user) {
	Workspace workspace = new Workspace(user.getFullName());
	save(workspace);
	WorkspaceFolder folder = new WorkspaceFolder(workspace.getName(), user.getUserId(), new Date(), new Date(),
		WorkspaceFolder.NORMAL);
	save(folder);
	workspace.addFolder(folder);
	workspace.setDefaultFolder(folder);
	user.setWorkspace(workspace);
	return user;
    }

    @SuppressWarnings("unchecked")
    public Workspace createWorkspaceForOrganisation(String workspaceName, Integer userID, Date createDateTime) {

	// this method is public so it can be accessed from the junit test

	WorkspaceFolder workspaceFolder = new WorkspaceFolder(workspaceName, userID, createDateTime, createDateTime,
		WorkspaceFolder.NORMAL);
	save(workspaceFolder);

	String description = getRunSequencesFolderName(workspaceName);
	WorkspaceFolder workspaceFolder2 = new WorkspaceFolder(description, userID, createDateTime, createDateTime,
		WorkspaceFolder.RUN_SEQUENCES);
	workspaceFolder2.setParentWorkspaceFolder(workspaceFolder);
	save(workspaceFolder2);

	workspaceFolder.addChild(workspaceFolder2);
	save(workspaceFolder);

	Workspace workspace = new Workspace(workspaceName);
	workspace.setDefaultFolder(workspaceFolder);
	workspace.setDefaultRunSequencesFolder(workspaceFolder2);
	workspace.addFolder(workspaceFolder);
	workspace.addFolder(workspaceFolder2);
	save(workspace);

	return workspace;
    }

    @SuppressWarnings("unchecked")
    public Organisation saveOrganisation(Organisation organisation, Integer userID) {

	User creator = (User) findById(User.class, userID);

	if (organisation.getOrganisationId() == null) {
	    Date createDateTime = new Date();
	    organisation.setCreateDate(createDateTime);
	    organisation.setCreatedBy(creator);

	    if (organisation.getOrganisationType().getOrganisationTypeId().equals(OrganisationType.COURSE_TYPE)) {
		Workspace workspace = createWorkspaceForOrganisation(organisation.getName(), userID, createDateTime);
		organisation.setWorkspace(workspace);
	    }

	    save(organisation);

	    if (organisation.getOrganisationType().getOrganisationTypeId().equals(OrganisationType.CLASS_TYPE)) {
		Organisation pOrg = organisation.getParentOrganisation();
		// set parent's child orgs
		Set children = pOrg.getChildOrganisations();
		if (children == null) {
		    children = new HashSet();
		}
		children.add(organisation);
		pOrg.setChildOrganisations(children);
		// get course managers and give them staff role in this new
		// class
		Vector<UserDTO> managers = getUsersFromOrganisationByRole(pOrg.getOrganisationId(), Role.GROUP_MANAGER,
			false, false);
		for (UserDTO m : managers) {
		    User user = (User) findById(User.class, m.getUserID());
		    UserOrganisation uo = new UserOrganisation(user, organisation);
		    log.debug("adding course manager: " + user.getUserId() + " as staff");
		    UserOrganisationRole uor = new UserOrganisationRole(uo, (Role) findById(Role.class,
			    Role.ROLE_MONITOR));
		    HashSet uors = new HashSet();
		    uors.add(uor);
		    uo.setUserOrganisationRoles(uors);

		    // attach new UserOrganisation to the Organisation, then
		    // save the UserOrganisation.
		    // this way the Set Organisations.userOrganisations contains
		    // persisted objects,
		    // and we can safely add new UserOrganisations if necessary
		    // (i.e. if there are
		    // several course managers).
		    Set uos = organisation.getUserOrganisations();
		    if (uos == null) {
			uos = new HashSet();
		    }
		    uos.add(uo);
		    organisation.setUserOrganisations(uos);

		    save(uo);
		}
	    }
	} else {
	    // update workspace/folder names
	    Workspace workspace = organisation.getWorkspace();
	    if (workspace != null) {
		workspace.setName(organisation.getName());
		WorkspaceFolder defaultFolder = workspace.getDefaultFolder();
		if (defaultFolder != null) {
		    defaultFolder.setName(organisation.getName());
		}
		WorkspaceFolder runSeqFolder = workspace.getDefaultRunSequencesFolder();
		if (runSeqFolder != null) {
		    runSeqFolder.setName(getRunSequencesFolderName(organisation.getName()));
		}
	    }
	}

	return organisation;
    }

    @SuppressWarnings("unchecked")
    public void updateOrganisationandWorkspaceNames(Organisation organisation) {
	baseDAO.update(organisation);
	if (organisation.getOrganisationId() != null) {
	    Workspace workspace = organisation.getWorkspace();
	    if (workspace != null) {
		workspace.setName(organisation.getName());
		baseDAO.update(workspace);

		WorkspaceFolder defaultFolder = workspace.getDefaultFolder();
		if (defaultFolder != null) {
		    defaultFolder.setName(organisation.getName());
		}
		baseDAO.update(defaultFolder);

		WorkspaceFolder runSeqFolder = workspace.getDefaultRunSequencesFolder();
		if (runSeqFolder != null) {
		    runSeqFolder.setName(getRunSequencesFolderName(organisation.getName()));
		}
		baseDAO.update(runSeqFolder);
	    }
	}
    }

    private String getRunSequencesFolderName(String workspaceName) {
	// get i18n'd message according to server locale
	String[] tokenisedLocale = LanguageUtil.getDefaultLangCountry();
	Locale serverLocale = new Locale(tokenisedLocale[0], tokenisedLocale[1]);
	String runSeqName = messageService.getMessageSource().getMessage(
		UserManagementService.SEQUENCES_FOLDER_NAME_KEY, new Object[] { workspaceName }, serverLocale);

	if (runSeqName != null && runSeqName.startsWith("???")) {
	    log.warn("Problem in the language file - can't find an entry for "
		    + UserManagementService.SEQUENCES_FOLDER_NAME_KEY + ". Creating folder as \"run sequences\" ");
	    runSeqName = "run sequences";
	}
	return runSeqName;
    }

    @SuppressWarnings("unchecked")
    public List<UserManageBean> getUserManageBeans(Integer orgId) {
	String query = "select u.userId,u.login,u.title,u.firstName,u.lastName, r "
		+ "from User u left join u.userOrganisations as uo left join uo.userOrganisationRoles as uor left join uor.role as r where uo.organisation.organisationId=?";
	List list = baseDAO.find(query, orgId);
	Map<Integer, UserManageBean> beansMap = new HashMap<Integer, UserManageBean>();
	for (int i = 0; i < list.size(); i++) {
	    Object[] data = (Object[]) list.get(i);
	    if (beansMap.containsKey(data[0])) {
		beansMap.get(data[0]).getRoles().add((Role) data[5]);
	    } else {
		UserManageBean bean = new UserManageBean();
		bean.setUserId((Integer) data[0]);
		bean.setLogin((String) data[1]);
		bean.setTitle((String) data[2]);
		bean.setFirstName((String) data[3]);
		bean.setLastName((String) data[4]);
		bean.getRoles().add((Role) data[5]);
		beansMap.put((Integer) data[0], bean);
	    }
	}
	List<UserManageBean> userManageBeans = new ArrayList<UserManageBean>();
	userManageBeans.addAll(beansMap.values());
	return userManageBeans;
    }

    /**
     * Remove a user from the system completely. Only able to be done if they don't have any related learning designs,
     * etc.
     * 
     * @param userId
     */
    public void removeUser(Integer userId) throws Exception {

	User user = (User) findById(User.class, userId);
	if (user != null) {

	    if (userHasData(user)) {
		throw new Exception("Cannot remove User ID " + userId + ". User has data.");
	    }

	    // write out an entry in the audit log.

	    Workspace workspace = user.getWorkspace();
	    Set wwfs = workspace != null ? workspace.getWorkspaceWorkspaceFolders() : null;

	    Set<WorkspaceFolder> foldersToDelete = new HashSet<WorkspaceFolder>();
	    if (wwfs != null) {
		Iterator iter = wwfs.iterator();
		while (iter.hasNext()) {
		    WorkspaceWorkspaceFolder wwf = (WorkspaceWorkspaceFolder) iter.next();
		    foldersToDelete.add(wwf.getWorkspaceFolder());

		    log.debug("deleting wkspc_wkspc_folder: " + wwf.getId());
		    delete(wwf);
		}
	    }

	    for (WorkspaceFolder wf : foldersToDelete) {
		log.debug("deleting wkspc_folder: " + wf.getName());
		delete(wf);
	    }

	    if (workspace != null) {
		log.debug("deleting workspace: " + workspace.getName());
		delete(workspace);
	    }

	    log.debug("deleting user " + user.getLogin());
	    delete(user);

	} else {
	    log.error("Requested delete of a user who does not exist. User ID " + userId);
	}
    }

    public Boolean userHasData(User user) {
	if (user.getLearnerProgresses() != null) {
	    if (!user.getLearnerProgresses().isEmpty()) {
		log.debug("user has data, learnerProgresses: " + user.getLearnerProgresses().size());
		return true;
	    }
	}
	if (user.getUserToolSessions() != null) {
	    if (!user.getUserToolSessions().isEmpty()) {
		log.debug("user has data, userToolSessions: " + user.getUserToolSessions().size());
		return true;
	    }
	}
	if (user.getLearningDesigns() != null) {
	    if (!user.getLearningDesigns().isEmpty()) {
		log.debug("user has data, learningDesigns: " + user.getLearningDesigns().size());
		return true;
	    }
	}
	if (user.getLessons() != null) {
	    if (!user.getLessons().isEmpty()) {
		log.debug("user has data, lessons: " + user.getLessons().size());
		return true;
	    }
	}
	int numLessonGroups = groupDAO.getCountGroupsForUser(user.getUserId());
	if (numLessonGroups > 0) {
	    log.debug("user has data, userGroups: " + numLessonGroups);
	    return true;
	}
	return false;
    }

    public void disableUser(Integer userId) {

	User user = (User) findById(User.class, userId);
	user.setDisabledFlag(true);
	Set uos = user.getUserOrganisations();
	Iterator iter = uos.iterator();
	while (iter.hasNext()) {
	    UserOrganisation uo = (UserOrganisation) iter.next();
	    log.debug("removing membership of: " + uo.getOrganisation().getName());
	    delete(uo);
	    iter.remove();
	}
	log.debug("disabling user " + user.getLogin());
	save(user);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.lamsfoundation.lams.usermanagement.service.IUserManagementService#setRolesForUserOrganisation(org.lamsfoundation.lams.usermanagement.User,
     *      java.lang.Integer, java.util.List)
     */
    public void setRolesForUserOrganisation(User user, Integer organisationId, List<String> rolesList) {

	// Don't pass in the org from the web layer. The import for roles
	// doesn't use the HIbernate open session
	// filter, so it may throw a lazy loading exception when it tried to
	// access the org.UserOrganisations set
	// if org has come from the web layer.
	Organisation org = (Organisation) findById(Organisation.class, organisationId);
	setRolesForUserOrganisation(user, org, rolesList);
    }

    private void setRolesForUserOrganisation(User user, Organisation org, List<String> rolesList) {

	// The private version of setRolesForUserOrganisation can pass around
	// the org safely as we are within
	// our transation, so no lazy loading errors. This is more efficient for
	// recursive calls to this method.

	UserOrganisation uo = getUserOrganisation(user.getUserId(), org.getOrganisationId());
	if (uo == null) {
	    uo = new UserOrganisation(user, org);
	    save(uo);
	    log.debug("added " + user.getLogin() + " to " + org.getName());
	    Set uos;
	    if ((uos = org.getUserOrganisations()) == null) {
		uos = new HashSet();
	    }
	    uos.add(uo);
	}

	// if user is to be added to a class, make user a member of parent
	// course also if not already
	if (org.getOrganisationType().getOrganisationTypeId().equals(OrganisationType.CLASS_TYPE)
		&& getUserOrganisation(user.getUserId(), org.getParentOrganisation().getOrganisationId()) == null) {
	    setRolesForUserOrganisation(user, org.getParentOrganisation(), rolesList);
	}

	List<String> rolesCopy = new ArrayList<String>();
	rolesCopy.addAll(rolesList);
	log.debug("rolesList.size: " + rolesList.size());
	Set<UserOrganisationRole> uors = uo.getUserOrganisationRoles();
	Set<UserOrganisationRole> uorsCopy = new HashSet<UserOrganisationRole>();
	if (uors != null) {
	    uorsCopy.addAll(uors);
	    // remove the common part from the rolesList and uors
	    // to get the uors to remove and the roles to add
	    for (String roleId : rolesList) {
		for (UserOrganisationRole uor : uors) {
		    if (uor.getRole().getRoleId().toString().equals(roleId)) {
			// remove from the Copys the ones we are keeping
			rolesCopy.remove(roleId);
			uorsCopy.remove(uor);
		    }
		}
	    }
	    log.debug("removing roles: " + uorsCopy);
	    uors.removeAll(uorsCopy);
	} else {
	    uors = new HashSet<UserOrganisationRole>();
	}
	for (String roleId : rolesCopy) {
	    Role role = (Role) findById(Role.class, Integer.parseInt(roleId));
	    UserOrganisationRole uor = new UserOrganisationRole(uo, role);
	    save(uor);
	    log.debug("setting role: " + role.getName() + " in organisation: " + org.getName());
	    uors.add(uor);
	    // when a user gets these roles, they need a workspace
	    if (role.getName().equals(Role.AUTHOR) || role.getName().equals(Role.AUTHOR_ADMIN)
		    || role.getName().equals(Role.SYSADMIN)) {
		if (user.getWorkspace() == null) {
		    createWorkspaceForUser(user);
		}
	    }
	}
	uo.setUserOrganisationRoles(uors);
	save(user);
	// make sure group managers have monitor and learner in each subgroup
	checkGroupManager(user, org);
    }

    private void checkGroupManager(User user, Organisation org) {
	if (org.getOrganisationType().getOrganisationTypeId().equals(OrganisationType.COURSE_TYPE)) {
	    if (hasRoleInOrganisation(user, Role.ROLE_GROUP_MANAGER, org)) {
		setRolesForGroupManager(user, org.getChildOrganisations());
	    }
	} else if (org.getOrganisationType().getOrganisationTypeId().equals(OrganisationType.CLASS_TYPE)) {
	    if (hasRoleInOrganisation(user, Role.ROLE_GROUP_MANAGER, org.getParentOrganisation())) {
		setRolesForGroupManager(user, org.getParentOrganisation().getChildOrganisations());
	    }
	}
    }

    private void setRolesForGroupManager(User user, Set childOrgs) {
	for (Object o : childOrgs) {
	    Organisation org = (Organisation) o;

	    // add user to user organisation if doesn't exist
	    UserOrganisation uo = getUserOrganisation(user.getUserId(), org.getOrganisationId());
	    if (uo == null) {
		uo = new UserOrganisation(user, org);
		save(uo);
		Set uos = org.getUserOrganisations();
		uos.add(uo);
		log.debug("added " + user.getLogin() + " to " + org.getName());
		uo = setRoleForUserOrganisation(uo, (Role) findById(Role.class, Role.ROLE_MONITOR));
		uo = setRoleForUserOrganisation(uo, (Role) findById(Role.class, Role.ROLE_LEARNER));
		save(uo);
		return;
	    }

	    // iterate through roles and add monitor and learner if don't
	    // already exist
	    Set<UserOrganisationRole> uors = uo.getUserOrganisationRoles();
	    if (uors != null && !uors.isEmpty()) {
		boolean isMonitor = false;
		boolean isLearner = false;
		for (UserOrganisationRole uor : uors) {
		    if (uor.getRole().getName().equals(Role.MONITOR)) {
			isMonitor = true;
		    } else if (uor.getRole().getName().equals(Role.LEARNER)) {
			isLearner = true;
		    }
		    if (isMonitor && isLearner) {
			break;
		    }
		}
		if (!isMonitor) {
		    uo = setRoleForUserOrganisation(uo, (Role) findById(Role.class, Role.ROLE_MONITOR));
		}
		if (!isLearner) {
		    uo = setRoleForUserOrganisation(uo, (Role) findById(Role.class, Role.ROLE_LEARNER));
		}
		save(uo);
	    }
	}
    }

    private UserOrganisation setRoleForUserOrganisation(UserOrganisation uo, Role role) {
	UserOrganisationRole uor = new UserOrganisationRole(uo, role);
	save(uor);
	uo.addUserOrganisationRole(uor);
	log.debug("setting role: " + uor.getRole().getName() + " in organisation: "
		+ uor.getUserOrganisation().getOrganisation().getName());
	return uo;
    }

    public List<Role> filterRoles(List<Role> rolelist, Boolean isSysadmin, OrganisationType orgType) {
	List<Role> allRoles = new ArrayList<Role>();
	allRoles.addAll(rolelist);
	Role role = new Role();
	if (!orgType.getOrganisationTypeId().equals(OrganisationType.ROOT_TYPE) || !isSysadmin) {
	    role.setRoleId(Role.ROLE_SYSADMIN);
	    allRoles.remove(role);
	    role.setRoleId(Role.ROLE_AUTHOR_ADMIN);
	    allRoles.remove(role);
	} else {
	    role.setRoleId(Role.ROLE_AUTHOR);
	    allRoles.remove(role);
	    role.setRoleId(Role.ROLE_LEARNER);
	    allRoles.remove(role);
	    role.setRoleId(Role.ROLE_MONITOR);
	    allRoles.remove(role);
	}
	if (!orgType.getOrganisationTypeId().equals(OrganisationType.COURSE_TYPE)) {
	    role.setRoleId(Role.ROLE_GROUP_MANAGER);
	    allRoles.remove(role);
	    if (!orgType.getOrganisationTypeId().equals(OrganisationType.ROOT_TYPE)) {
		role.setRoleId(Role.ROLE_GROUP_ADMIN);
		allRoles.remove(role);
	    }
	}
	return allRoles;
    }

    public boolean hasRoleInOrganisation(User user, Integer roleId) {
	return hasRoleInOrganisation(user, roleId, getRootOrganisation());
    }

    public boolean hasRoleInOrganisation(User user, Integer roleId, Organisation organisation) {
	if (roleDAO.getUserByOrganisationAndRole(user.getUserId(), roleId, organisation) != null) {
	    return true;
	} else {
	    return false;
	}
    }

    public void deleteChildUserOrganisations(User user, Organisation org) {
	if (!org.getOrganisationType().getOrganisationTypeId().equals(OrganisationType.COURSE_TYPE)) {
	    return;
	}
	Set childOrgs = org.getChildOrganisations();
	Iterator iter = childOrgs.iterator();
	while (iter.hasNext()) {
	    Organisation childOrg = (Organisation) iter.next();
	    Set childOrgUos = childOrg.getUserOrganisations();
	    UserOrganisation uo = getUserOrganisation(user.getUserId(), childOrg.getOrganisationId());
	    if (uo != null) {
		// remove user's membership of this subgroup
		childOrgUos.remove(uo);
		childOrg.setUserOrganisations(childOrgUos);
		save(childOrg);
		// remove User's link to this subgroup
		Set userUos = user.getUserOrganisations();
		userUos.remove(uo);
		user.setUserOrganisations(userUos);
		log.debug("removed userId=" + user.getUserId() + " from orgId=" + childOrg.getOrganisationId());
	    }
	}
    }

    public void deleteUserOrganisation(User user, Organisation org) {
	UserOrganisation uo = getUserOrganisation(user.getUserId(), org.getOrganisationId());
	if (uo != null) {
	    org.getUserOrganisations().remove(uo);
	    save(org);
	    user.getUserOrganisations().remove(uo);
	    log.debug("Removed user " + user.getUserId() + " from organisation " + org.getOrganisationId());
	    if (org.getOrganisationType().equals(OrganisationType.COURSE_TYPE)) {
		deleteChildUserOrganisations(user, org);
	    }
	}
    }

    private Integer getRequestorId() {
	UserDTO userDTO = (UserDTO) SessionManager.getSession().getAttribute(AttributeNames.USER);
	return userDTO != null ? userDTO.getUserID() : null;
    }

    public boolean isUserGlobalGroupAdmin() {
	Integer rootOrgId = getRootOrganisation().getOrganisationId();
	Integer requestorId = getRequestorId();
	return requestorId != null ? isUserInRole(requestorId, rootOrgId, Role.GROUP_ADMIN) : false;
    }

    public boolean isUserSysAdmin() {
	Integer rootOrgId = getRootOrganisation().getOrganisationId();
	Integer requestorId = getRequestorId();
	return requestorId != null ? isUserInRole(requestorId, rootOrgId, Role.SYSADMIN) : false;
    }

    public Integer getCountRoleForSystem(Integer roleId) {
	Integer count = roleDAO.getCountRoleForSystem(roleId);
	if (count != null) {
	    return count;
	} else {
	    return new Integer(0);
	}
    }

    public Integer getCountRoleForOrg(Integer orgId, Integer roleId) {
	Integer count = roleDAO.getCountRoleForOrg(roleId, orgId);
	if (count != null) {
	    return count;
	} else {
	    return new Integer(0);
	}
    }

    public Theme getDefaultFlashTheme() {
	String flashName = Configuration.get(ConfigurationKeys.DEFAULT_FLASH_THEME);
	List list = findByProperty(Theme.class, "name", flashName);
	return list != null ? (Theme) list.get(0) : null;
    }

    public Theme getDefaultHtmlTheme() {
	String htmlName = Configuration.get(ConfigurationKeys.DEFAULT_HTML_THEME);
	List list = findByProperty(Theme.class, "name", htmlName);
	return list != null ? (Theme) list.get(0) : null;
    }

    public void auditPasswordChanged(User user, String moduleName) {
	String[] args = new String[1];
	args[0] = user.getLogin() + "(" + user.getUserId() + ")";
	String message = messageService.getMessage("audit.user.password.change", args);
	getAuditService().log(moduleName, message);
    }

    public void auditUserCreated(User user, String moduleName) {
	String[] args = new String[2];
	args[0] = user.getLogin() + "(" + user.getUserId() + ")";
	args[1] = user.getFullName();
	String message = messageService.getMessage("audit.user.create", args);
	getAuditService().log(moduleName, message);
    }

    public Integer getCountUsers() {
	String query = "select count(u) from User u";
	return getFindIntegerResult(query);
    }

    public Integer getCountUsers(Integer authenticationMethodId) {
	String query = "select count(u) from User u " + "where u.authenticationMethod.authenticationMethodId="
		+ authenticationMethodId;
	return getFindIntegerResult(query);
    }

    private Integer getFindIntegerResult(String query) {
	List list = baseDAO.find(query);
	if (list != null && list.size() > 0) {
	    return ((Number) list.get(0)).intValue();
	}
	return null;
    }

    public List getActiveCourseIdsByUser(Integer userId, boolean isSysadmin) {
	List list = organisationDAO.getActiveCourseIdsByUser(userId, isSysadmin);
	return populateCollapsedOrgDTOs(list, isSysadmin);
    }

    public List getArchivedCourseIdsByUser(Integer userId, boolean isSysadmin) {
	List list = organisationDAO.getArchivedCourseIdsByUser(userId, isSysadmin);
	return populateCollapsedOrgDTOs(list, isSysadmin);
    }

    private List populateCollapsedOrgDTOs(List list, boolean isSysadmin) {
	ArrayList<CollapsedOrgDTO> dtoList = new ArrayList<CollapsedOrgDTO>();
	for (Object obj : list) {
	    // sysadmins get all orgs collapsed; saves storing boolean for every
	    // org,
	    // and saves loading time for the sysadmin
	    if (isSysadmin) {
		dtoList.add(new CollapsedOrgDTO((Integer) obj, Boolean.TRUE));
	    } else {
		Object[] array = (Object[]) obj;
		if (array.length > 1) {
		    if (array[1] != null) {
			dtoList.add(new CollapsedOrgDTO((Integer) array[0], (Boolean) array[1]));
		    } else {
			dtoList.add(new CollapsedOrgDTO((Integer) array[0], Boolean.FALSE));
		    }
		}
	    }
	}
	return dtoList;
    }

    public List searchUserSingleTerm(String term) {
	term = StringEscapeUtils.escapeSql(term);
	String query = "select u from User u where (u.login like '%" + term + "%' or u.firstName like '%" + term
		+ "%' or u.lastName like '%" + term + "%' or u.email like '%" + term + "%')"
		+ " and u.disabledFlag=0 order by u.login";
	List list = baseDAO.find(query);
	return list;
    }

    public List searchUserSingleTerm(String term, Integer filteredOrgId) {
	term = StringEscapeUtils.escapeSql(term);
	String query = "select u from User u where (u.login like '%" + term + "%' or u.firstName like '%" + term
		+ "%' or u.lastName like '%" + term + "%' or u.email like '%" + term + "%')"
		+ " and u.disabledFlag=0 and u.userId not in (select uo.user.userId from UserOrganisation uo"
		+ " where uo.organisation.organisationId=" + filteredOrgId + ") order by u.login";
	List list = baseDAO.find(query);
	return list;
    }

    public List searchUserSingleTerm(String term, Integer orgId, Integer filteredOrgId) {
	term = StringEscapeUtils.escapeSql(term);
	String query = "select uo.user from UserOrganisation uo where (uo.user.login like '%" + term + "%'"
		+ " or uo.user.firstName like '%" + term + "%' or uo.user.lastName like '%" + term + "%'"
		+ " or uo.user.email like '%" + term + "%') and uo.user.disabledFlag=0"
		+ " and uo.organisation.organisationId=" + orgId + " and uo.user.userId not in"
		+ " (select uo.user.userId from UserOrganisation uo where uo.organisation.organisationId="
		+ filteredOrgId + ") order by uo.user.login";
	List list = baseDAO.find(query);
	return list;
    }

    public List searchUserSingleTerm(String term, Integer orgId, boolean includeChildOrgs) {
	term = StringEscapeUtils.escapeSql(term);
	String whereClause = "";
	if (includeChildOrgs) {
	    whereClause = " or uo.organisation.parentOrganisation.organisationId=" + orgId;
	}

	String query = "select u from User u where (u.login like '%" + term + "%' or u.firstName like '%" + term
		+ "%' or u.lastName like '%" + term + "%' or u.email like '%" + term + "%')"
		+ " and u.disabledFlag=0 and u.userId in (select uo.user.userId from UserOrganisation uo"
		+ " where uo.organisation.organisationId=" + orgId + whereClause + ") order by u.login";
	List list = baseDAO.find(query);
	return list;
    }

    public List getAllUsers() {
	String query = "from User u where u.disabledFlag=0 order by u.login";
	return baseDAO.find(query);
    }

    public List getAllUsers(Integer filteredOrgId) {
	String query = "from User u where u.disabledFlag=0 and u.userId not in"
		+ " (select uo.user.userId from UserOrganisation uo where uo.organisation.organisationId="
		+ filteredOrgId + ")" + " order by u.login";
	return baseDAO.find(query);
    }

    public List getAllUsersWithEmail(String email) {
	String query = "from User u where u.email=\'" + email + "\' order by u.login";
	return baseDAO.find(query);
    }

    public List getUsersFromOrganisation(Integer orgId, Integer filteredOrgId) {
	String query = "select uo.user from UserOrganisation uo where uo.organisation.organisationId=" + orgId
		+ " and uo.user.userId not in (select uo.user.userId from UserOrganisation uo"
		+ " where uo.organisation.organisationId=" + filteredOrgId + ") order by uo.user.login";
	return baseDAO.find(query);
    }

    public boolean canEditGroup(Integer userId, Integer orgId) {
	if (isUserSysAdmin() || isUserGlobalGroupAdmin()) {
	    return true;
	}
	Organisation org = (Organisation) findById(Organisation.class, orgId);
	if (org != null) {
	    Integer groupId = orgId;
	    if (org.getOrganisationType().getOrganisationTypeId().equals(OrganisationType.CLASS_TYPE)) {
		groupId = org.getParentOrganisation().getOrganisationId();
	    }
	    return isUserInRole(userId, groupId, Role.GROUP_ADMIN) || isUserInRole(userId, groupId, Role.GROUP_MANAGER);
	}
	return false;
    }

    public ForgotPasswordRequest getForgotPasswordRequest(String key) {
	List results = baseDAO.findByProperty(ForgotPasswordRequest.class, "requestKey", key);
	return results.isEmpty() ? null : (ForgotPasswordRequest) results.get(0);
    }

    public int removeUserFromOtherGroups(Integer userId, Integer orgId) {
	List uos = userOrganisationDAO.userOrganisationsNotById(userId, orgId);
	deleteAll(uos);
	return uos.size();
    }
}