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


package org.lamsfoundation.lams.tool.kaltura.dao;

import org.lamsfoundation.lams.dao.IBaseDAO;
import org.lamsfoundation.lams.tool.kaltura.model.KalturaUser;

/**
 * DAO for accessing the KalturaUser objects - interface defining methods to be implemented by the Hibernate or other
 * implementation.
 */
public interface IKalturaUserDAO extends IBaseDAO {
    /**
     *
     * @param userId
     * @param toolSessionId
     * @return
     */
    KalturaUser getByUserIdAndSessionId(Long userId, Long toolSessionId);

    void saveOrUpdate(KalturaUser kalturaUser);

    /**
     *
     * @param loginName
     * @param sessionID
     * @return
     */
    KalturaUser getByLoginNameAndSessionId(String loginName, Long toolSessionId);

    /**
     *
     * @param uid
     * @return
     */
    KalturaUser getByUid(Long uid);

    KalturaUser getByUserIdAndContentId(Long userId, Long contentId);
}
