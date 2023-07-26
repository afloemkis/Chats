import org.junit.Test

import org.junit.Assert.*


class ChatServiceTest {
    @Test
    fun testCreateChatAndSendMessage() {
        val service = ChatService()
        val chat = service.createChat(senderId = 1, receiverId = 2, text = "Hello!")
        assertEquals(1, chat.messages.size)
        assertEquals("Hello!", chat.messages.first().text)
    }

    @Test
    fun testGetUnreadChats() {
        val service = ChatService()
        val newChat1 = service.createChat(senderId = 1, receiverId = 2, text = "Hello!")
        val newChat = service.createChat(senderId = 1, receiverId = 3, text = "Hi!")
        service.getMessagesFromChat(userId = 1, chatId = newChat1.id, messageId = 1, count = 2)
        service.sendMessage(senderId = 1, receiverId = 3, chatId = newChat.id, text = "HelloHello!")
        val chats = service.getUnreadChats(userId = 1)
        println(chats)
        assertEquals(1, chats.size)
    }

    @Test
    fun testGetLastMessages() {
        val service = ChatService()
        service.createChat(senderId = 1, receiverId = 2, text = "Hello!")
        service.createChat(senderId = 1, receiverId = 3, text = "Hi!")
        val lastMessages = service.getLastMessages(userId = 1)
        assertEquals(2, lastMessages.size)
        assertTrue(lastMessages.contains("Hello!"))
        assertTrue(lastMessages.contains("Hi!"))
    }

    @Test
    fun testGetMessagesFromChat() {
        val service = ChatService()
        val chat = service.createChat(senderId = 1, receiverId = 2, text = "Hello!")
        service.sendMessage(senderId = 2, receiverId = 1, chatId = chat.id, text = "Hi!")
        service.sendMessage(senderId = 1, receiverId = 2, chatId = chat.id, text = "How are you?")
        val messages = service.getMessagesFromChat(userId = 1, chatId = chat.id, messageId = 2, count = 2)
        assertEquals(2, messages.size)
        assertEquals("Hi!", messages[0].text)
        assertEquals("How are you?", messages[1].text)
        assertEquals(false, messages[0].unread)
        assertEquals(false, messages[1].unread)
    }

    @Test
    fun testDeleteMessage() {
        val service = ChatService()
        val chat = service.createChat(senderId = 1, receiverId = 2, text = "Hello!")
        service.sendMessage(senderId = 2, receiverId = 1, chatId = chat.id, text = "Hi!")
        service.deleteMessage(userId = 1, messageId = 1)
        assertEquals(1, chat.messages.size)
    }

    @Test
    fun testDeleteChat() {
        val service = ChatService()
        val chat = service.createChat(senderId = 1, receiverId = 2, text = "Hello!")
        service.deleteChat(userId = 1, chatId = chat.id)
        assertEquals(0, service.getChats(userId = 1).size)
    }

    @Test (expected = ChatNotFoundException::class)
    fun testDeleteChat_Exception() {
        val service = ChatService()
        service.deleteChat(userId = 1, 1)
    }

    @Test (expected = MessageNotFoundException::class)
    fun testDeleteMessage_Exception() {
        val service = ChatService()
        service.deleteMessage(userId = 1, messageId = 1)
    }

}
