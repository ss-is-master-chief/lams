package org.lamsfoundation.lams.tool.forum.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * User: conradb
 * Date: 7/06/2005
 * Time: 12:42:05
 */
public class MessageTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCreateAndDeleteMessage()  {
        //Populate a Forum entity for test purposes
        Forum forum = new Forum();
        ForumUser createUser = new ForumUser();
        createUser.setFirstName("creator");
        ForumUser modifyUser = new ForumUser();
        modifyUser.setFirstName("updator");

        //save
        ForumDao dao = new ForumDao();
        dao.saveOrUpdate(forum);

        Message message = new Message();
        message.setBody("Test Message");
        message.setSubject("Test Message");
        //TODO:need fix
//        message.setToolSession(forum);
        message.setIsAnonymous(false);
        message.setIsAuthored(true);
        message.setCreatedBy(createUser);
        
        message.setModifiedBy(modifyUser);

        MessageDao messageDao = new MessageDao();
        messageDao.saveOrUpdate(message);

        assertNotNull(message.getUid());
        assertNotNull("date created is null", message.getCreated());
        assertNotNull("date updated is null", message.getUpdated());
        assertEquals("date created and updated are different for first save", message.getCreated(), message.getUpdated());

        //load
        Message reloaded = (Message) messageDao.getById(message.getUid());
        //just because MySQL will wrap millisecond to zero. it is nonsesnce to compare data at this care.
        message.setCreated(reloaded.getCreated());
        message.setUpdated(reloaded.getUpdated());
        assertEquals("reloaded message not equal", message, reloaded);
        assertEquals("reloaded message body should be: Test Message", "Test Message", reloaded.getBody());
        assertEquals("reloaded message Subject should be: Test Message", "Test Message", reloaded.getSubject());
        assertEquals("reloaded message Forum not equal", forum.getUid(), reloaded.getToolSession().getUid());
        assertEquals("reloaded message isAnnonymous not equal", false, reloaded.getIsAnonymous());
        assertEquals("reloaded message isAuthored not equal", true, reloaded.getIsAuthored());
        assertEquals("reloaded message createdBy not equal", new Long(1000), reloaded.getCreatedBy());
        assertEquals("reloaded message modifiedBy not equal", new Long(1002), reloaded.getModifiedBy());

        Message message2 = new Message();
        message2.setBody("Test Message2");
        message2.setSubject("Test Message2");
        //TODO:need fix
//      message.setToolSession(forum);
        message2.setIsAnonymous(true);
        message2.setIsAuthored(true);
        message2.setCreatedBy(createUser);
        message2.setModifiedBy(modifyUser);

        messageDao.saveOrUpdate(message2);
        Message reloaded2 = (Message) messageDao.getById(message2.getUid());
        //just because MySQL will wrap millisecond to zero. it is nonsesnce to compare data at this care.
        message2.setCreated(reloaded2.getCreated());
        message2.setUpdated(reloaded2.getUpdated());

        assertEquals("reloaded message not equal", message2, reloaded2);
        assertEquals("reloaded message body should be: Test Message", "Test Message2", reloaded2.getBody());
        assertEquals("reloaded message Subject should be: Test Message", "Test Message2", reloaded2.getSubject());
        assertEquals("reloaded message Forum not equal", forum.getUid(), reloaded2.getToolSession().getUid());
        assertEquals("reloaded message isAnnonymous not equal", true, reloaded2.getIsAnonymous());
        assertEquals("reloaded message isAuthored not equal", true, reloaded2.getIsAuthored());
        assertEquals("reloaded message createdBy not equal", new Long(1005), reloaded2.getCreatedBy());
        assertEquals("reloaded message modifiedBy not equal", new Long(1006), reloaded2.getModifiedBy());

        //find
        List values = dao.findByNamedQuery("allMessages");
        assertTrue("find all result not containing object", values.contains(message));
        assertTrue("find all result not containing object", values.contains(message2));

        Message message3 = new Message();
        message3.setBody("Test Message2");
        message3.setSubject("Test Message2");
        //TODO:need fix
//      message.setToolSession(forum);
        message3.setIsAnonymous(true);
        message3.setIsAuthored(true);
        message3.setCreatedBy(createUser);
        message3.setModifiedBy(modifyUser);
        Set replies = new HashSet();
        replies.add(message3);
        reloaded.set(replies);

        messageDao.saveOrUpdate(reloaded);

        reloaded = (Message) messageDao.getById(reloaded.getUid());
        Set reloadedReplies = reloaded.getReplies();
        assertTrue("reloaded message does not have a child", reloadedReplies.contains(message3));


        //delete
        
        messageDao.delete(reloaded);
        messageDao.deleteForumMessage(forum.getUid());
        dao.delete(forum);
        assertNull("message object not deleted", messageDao.getById(message.getUid()));
        assertNull("reply message object not deleted", messageDao.getById(message2.getUid()));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
