package com.AEAS.dailymemory

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- MODELOS DE DATOS ---
data class ChatMessage(
    val id: String = "",
    val senderName: String = "",
    val text: String = "",
    val time: String = "",
    val isMine: Boolean = false
)

data class MessageModel(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val chatId: String = "global"
)

data class GroupModel(
    val id: String = "",
    val name: String = "",
    val adminId: String = "",
    val memberIds: List<String> = emptyList(),
    val memberNames: Map<String, String> = emptyMap()
)

data class UserSearch(val uid: String = "", val username: String = "")
data class RecentChat(val uid: String = "", val username: String = "")

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _globalMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val globalMessages: StateFlow<List<ChatMessage>> = _globalMessages

    private val _groupMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val groupMessages: StateFlow<List<ChatMessage>> = _groupMessages

    private val _personalMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val personalMessages: StateFlow<List<ChatMessage>> = _personalMessages

    private val _searchResults = MutableStateFlow<List<UserSearch>>(emptyList())
    val searchResults: StateFlow<List<UserSearch>> = _searchResults

    private val _myGroups = MutableStateFlow<List<GroupModel>>(emptyList())
    val myGroups: StateFlow<List<GroupModel>> = _myGroups

    private val _recentChats = MutableStateFlow<List<RecentChat>>(emptyList())
    val recentChats: StateFlow<List<RecentChat>> = _recentChats

    private var globalListener: ListenerRegistration? = null
    private var groupListener: ListenerRegistration? = null
    private var personalListener: ListenerRegistration? = null
    private var recentChatsListener: ListenerRegistration? = null

    init {
        listenToGlobalChat()
        fetchMyGroups()
        listenToRecentChats()
    }

    private fun listenToGlobalChat() {
        globalListener?.remove()
        globalListener = db.collection("global_chat")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _globalMessages.value = snapshot.documents.mapNotNull { doc -> mapToUI(doc) }
            }
    }

    private fun fetchMyGroups() {
        val myUid = auth.currentUser?.uid ?: return
        db.collection("groups")
            .whereArrayContains("memberIds", myUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _myGroups.value = snapshot.documents.mapNotNull { it.toObject(GroupModel::class.java)?.copy(id = it.id) }
            }
    }

    fun createGroup(groupName: String) {
        val currentUser = auth.currentUser ?: return
        val myName = currentUser.displayName ?: "Usuario"
        val newGroup = GroupModel(
            name = groupName,
            adminId = currentUser.uid,
            memberIds = listOf(currentUser.uid),
            memberNames = mapOf(currentUser.uid to myName)
        )
        db.collection("groups").add(newGroup)
    }

    fun listenToGroupChat(groupId: String) {
        groupListener?.remove()
        if (groupId.isEmpty()) {
            _groupMessages.value = emptyList()
            return
        }
        groupListener = db.collection("groups_chat")
            .whereEqualTo("chatId", groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val docs = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(MessageModel::class.java)?.let { it to doc.id }
                }

                _groupMessages.value = docs.sortedBy { it.first.timestamp }.map { (msg, id) ->
                    ChatMessage(
                        id = id,
                        senderName = msg.senderName,
                        text = msg.text,
                        time = formatTime(msg.timestamp),
                        isMine = msg.senderId == auth.currentUser?.uid
                    )
                }
            }
    }

    fun addMemberToGroup(groupId: String, user: UserSearch) {
        val ref = db.collection("groups").document(groupId)
        ref.update(
            "memberIds", FieldValue.arrayUnion(user.uid),
            "memberNames.${user.uid}", user.username
        )
    }

    fun leaveGroup(groupId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val ref = db.collection("groups").document(groupId)
        ref.update(
            "memberIds", FieldValue.arrayRemove(myUid),
            "memberNames.$myUid", FieldValue.delete()
        )
    }

    fun deleteGroup(groupId: String) {
        db.collection("groups").document(groupId).delete()
    }

    fun searchUsers(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        db.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val myUid = auth.currentUser?.uid
                _searchResults.value = snapshot.documents.mapNotNull {
                    val uid = it.getString("uid") ?: it.id
                    val username = it.getString("username") ?: "Usuario"
                    if (uid != myUid) UserSearch(uid, username) else null
                }
            }
    }

    private fun listenToRecentChats() {
        val myUid = auth.currentUser?.uid ?: return
        recentChatsListener?.remove()
        recentChatsListener = db.collection("users").document(myUid).collection("recent_chats")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _recentChats.value = snapshot.documents.mapNotNull { doc ->
                    RecentChat(doc.id, doc.getString("username") ?: "Desconocido")
                }
            }
    }

    fun listenToPersonalChat(targetUserId: String) {
        personalListener?.remove()
        if (targetUserId.isEmpty()) {
            _personalMessages.value = emptyList()
            return
        }
        val myUid = auth.currentUser?.uid ?: return
        val chatId = if (myUid < targetUserId) "${myUid}_$targetUserId" else "${targetUserId}_$myUid"

        personalListener = db.collection("personal_chat")
            .whereEqualTo("chatId", chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val docs = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(MessageModel::class.java)?.let { it to doc.id }
                }

                _personalMessages.value = docs.sortedBy { it.first.timestamp }.map { (msg, id) ->
                    ChatMessage(
                        id = id,
                        senderName = msg.senderName,
                        text = msg.text,
                        time = formatTime(msg.timestamp),
                        isMine = msg.senderId == auth.currentUser?.uid
                    )
                }
            }
    }

    fun deletePersonalChat(targetId: String) {
        val myUid = auth.currentUser?.uid ?: return
        db.collection("users").document(myUid).collection("recent_chats").document(targetId).delete()
        val chatId = if (myUid < targetId) "${myUid}_$targetId" else "${targetId}_$myUid"
        db.collection("personal_chat").whereEqualTo("chatId", chatId).get().addOnSuccessListener { snaps ->
            val batch = db.batch()
            for (doc in snaps) batch.delete(doc.reference)
            batch.commit()
        }
    }

    private fun mapToUI(doc: com.google.firebase.firestore.DocumentSnapshot): ChatMessage? {
        val msg = doc.toObject(MessageModel::class.java) ?: return null
        return ChatMessage(
            id = doc.id,
            senderName = msg.senderName,
            text = msg.text,
            time = formatTime(msg.timestamp),
            isMine = msg.senderId == auth.currentUser?.uid
        )
    }

    fun sendMessage(text: String, tabName: String, targetId: String = "global", targetName: String = "") {
        val currentUser = auth.currentUser ?: return
        val myUid = currentUser.uid
        val myName = currentUser.displayName ?: "Usuario"

        val actualTargetId = if (tabName == "Personal") {
            val chatId = if (myUid < targetId) "${myUid}_$targetId" else "${targetId}_$myUid"
            val myRecent = mapOf("username" to targetName)
            db.collection("users").document(myUid).collection("recent_chats").document(targetId).set(myRecent)
            val theirRecent = mapOf("username" to myName)
            db.collection("users").document(targetId).collection("recent_chats").document(myUid).set(theirRecent)
            chatId
        } else {
            targetId
        }

        val collectionName = when (tabName) {
            "Grupos" -> "groups_chat"
            "Personal" -> "personal_chat"
            else -> "global_chat"
        }

        val newMessage = MessageModel(
            senderId = myUid,
            senderName = myName,
            text = text,
            timestamp = System.currentTimeMillis(),
            chatId = actualTargetId
        )

        db.collection(collectionName).add(newMessage)
    }

    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}