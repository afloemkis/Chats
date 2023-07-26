data class Message(val id: Int, var text: String, val senderId: Int, val chatId: Int, var unread: Boolean = true)

data class Chat(val id: Int, var userIds: List<Int>, val messages: MutableList<Message> = mutableListOf())

class ChatNotFoundException(message: String) : Exception(message)
class MessageNotFoundException(message: String) : Exception(message)

class ChatService {
    private val chats = mutableListOf<Chat>()

    // Создание нового чата при отправке первого сообщения
   fun createChat(senderId: Int, receiverId: Int, text: String): Chat {
        val newChatId = getNextId()
        val newChat = Chat(newChatId, listOf(senderId, receiverId))
        chats.add(newChat)
        newChat.messages.add(Message(getNextMid(newChatId), text, senderId, newChatId))
        return newChat
    }

    // Получение списка чатов для указанного пользователя
    fun getChats(userId: Int): List<Chat> {
        return chats.filter { it.userIds.contains(userId) }
    }

    //Получение списка непрочитанных чатов
    fun getUnreadChats(userId: Int): List<Chat> {
        return getChats(userId).filter { chat -> chat.messages.any { it.unread } }
    }

    // Получение списка последних сообщений из чатов (можно в виде списка строк)
    fun getLastMessages(userId: Int): List<String> {
        return chats.map { it.messages.lastOrNull()?.text ?: "нет сообщений" }
    }

    // Получение списка сообщений из чата, начиная с заданного messageId
    fun getMessagesFromChat(userId: Int, chatId: Int, messageId: Int, count: Int): List<Message> {
        val chat = getChatById(chatId)
        return chat.messages.filter { it.id >= messageId }.take(count).onEach  { it.unread = false } // Отмечаем сообщения как прочитанные
    }

    // Отправка сообщения
    fun sendMessage(senderId: Int, receiverId: Int, chatId: Int? = null, text: String) {
        if (chatId != null && chats.any { it.id == chatId }) {
            val chat = getChatById(chatId)
            val messageId = getNextMid(chatId)
            val newMessage = Message(messageId, text, senderId, chatId)
            chat.messages.add(newMessage)
        } else {
            createChat(senderId, receiverId, text)
        }
    }

    // Удаление сообщения
    fun deleteMessage(userId: Int, messageId: Int) {
        val message = getMessageById(messageId)
        if (message.senderId != userId) {
            throw MessageNotFoundException("User $userId can only delete their own messages")
        }
        val chat = getChatById(message.chatId)
        chat.messages.remove(message)
    }

    // Удаление чата
    fun deleteChat(userId: Int, chatId: Int) {
        val chat = getChatById(chatId)
        if (!chat.userIds.contains(userId)) {
            throw ChatNotFoundException("User $userId cannot delete this chat")
        }
        chats.remove(chat)
    }

    // Вспомогательные функции для получения чата и сообщения по их ID
    private fun getChatById(chatId: Int): Chat {
        return chats.find { it.id == chatId } ?: throw ChatNotFoundException("Chat with ID $chatId not found")
    }

    private fun getMessageById(messageId: Int): Message {
        return chats.flatMap { it.messages }.find { it.id == messageId }
            ?: throw MessageNotFoundException("Message with ID $messageId not found")
    }

    //Вспомогательная функция для генерации нового id чата
    private fun getNextId(): Int {
        return chats.maxByOrNull { it.id }?.id?.plus(1) ?: 1
    }

    //Вспомогательная функция для генерации нового id сообщения
    private fun getNextMid(chatId: Int): Int {
        return chats.find {it.id == chatId}?.messages?.maxByOrNull { it.id }?.id?.plus(1) ?: 1
    }




}