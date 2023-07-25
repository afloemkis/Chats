data class Message(val id: Int, val text: String, val senderId: Int, val chatId: Int)

data class Chat(val id: Int, val userIds: List<Int>, val messages: MutableList<Message> = mutableListOf())

class ChatNotFoundException(message: String) : Exception(message)
class MessageNotFoundException(message: String) : Exception(message)

class ChatService {
    private val chats = mutableListOf<Chat>()

    // Создание нового чата при отправке первого сообщения
    fun createChat(senderId: Int, receiverId: Int, text: String): Chat {
        val newChat = Chat(chats.size + 1, listOf(senderId, receiverId))
        chats.add(newChat)
        sendMessage(senderId, newChat.id, text)
        return newChat
    }

    // Получение списка чатов для указанного пользователя
    fun getChats(userId: Int): List<Chat> {
        return chats.filter { it.userIds.contains(userId) }
    }

    // Получение списка последних сообщений из чатов (можно в виде списка строк)
    fun getLastMessages(userId: Int): List<String> {
        return chats
            .filter { it.userIds.contains(userId) && it.messages.isNotEmpty() }
            .map { chat ->
                chat.messages.last().text
            }
    }

    // Получение списка сообщений из чата, начиная с заданного messageId
    fun getMessagesFromChat(chatId: Int, messageId: Int, count: Int): List<Message> {
        val chat = getChatById(chatId)
        val startIndex = chat.messages.indexOfFirst { it.id == messageId } + 1
        val endIndex = startIndex + count.coerceAtMost(chat.messages.size - startIndex)
        val messages = chat.messages.subList(startIndex, endIndex)
        messages.forEach { it.senderId } // Отмечаем сообщения как прочитанные
        return messages
    }

    // Отправка сообщения
    fun sendMessage(senderId: Int, chatId: Int, text: String) {
        val chat = getChatById(chatId)
        val messageId = chat.messages.size + 1
        val newMessage = Message(messageId, text, senderId, chatId)
        chat.messages.add(newMessage)
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
}