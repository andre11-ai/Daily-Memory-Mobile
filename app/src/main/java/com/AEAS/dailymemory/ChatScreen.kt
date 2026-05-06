package com.AEAS.dailymemory

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

val MintPrimary = Color(0xFF00C8A3)
val AccentBlue = Color(0xFF45B6FE)
val ChatBgLight = Color(0xFFF8F9FA)

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Mundial") }
    var showHelpModal by remember { mutableStateOf(true) }

    val blurEffect by animateDpAsState(targetValue = if (showHelpModal) 16.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground(modifier = Modifier.blur(blurEffect)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp)
                        .background(Color(0xFFF8F9FA).copy(alpha = 0.95f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily Memory", color = MintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Menú", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val isTablet = maxWidth > 700.dp
                    if (isTablet) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            ChatTabsColumn(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                modifier = Modifier.width(160.dp).padding(top = 8.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            ChatArea(selectedTab, chatViewModel, modifier = Modifier.weight(1f).fillMaxHeight())
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ChatTabsRow(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )
                            ChatArea(selectedTab, chatViewModel, modifier = Modifier.weight(1f).fillMaxWidth())
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showHelpModal = true },
            containerColor = AccentBlue,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp, end = 24.dp),
            shape = CircleShape
        ) {
            Icon(Icons.Default.QuestionMark, contentDescription = "Ayuda")
        }

        if (showHelpModal) {
            ChatHelpModal(onClose = { showHelpModal = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatArea(tabName: String, chatViewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val currentUid = FirebaseProviders.auth.currentUser?.uid ?: ""
    var inputText by remember { mutableStateOf("") }
    var showEmojis by remember { mutableStateOf(false) }

    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showManageGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    val myGroups by chatViewModel.myGroups.collectAsState()
    var selectedGroup by remember { mutableStateOf<GroupModel?>(null) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    var searchUserQuery by remember { mutableStateOf("") }
    val searchResults by chatViewModel.searchResults.collectAsState()
    val recentChats by chatViewModel.recentChats.collectAsState()
    var selectedUser by remember { mutableStateOf<UserSearch?>(null) }
    var isSearchFocused by remember { mutableStateOf(false) }

    LaunchedEffect(tabName, selectedGroup, selectedUser) {
        when (tabName) {
            "Grupos" -> chatViewModel.listenToGroupChat(selectedGroup?.id ?: "")
            "Personal" -> chatViewModel.listenToPersonalChat(selectedUser?.uid ?: "")
        }
    }

    val messages by when (tabName) {
        "Grupos" -> chatViewModel.groupMessages.collectAsState()
        "Personal" -> chatViewModel.personalMessages.collectAsState()
        else -> chatViewModel.globalMessages.collectAsState()
    }

    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (tabName == "Mundial") Icons.Default.Public else if (tabName == "Grupos") Icons.Default.Groups else Icons.Default.Person,
                    contentDescription = null, tint = MintPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat $tabName", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MintPrimary)
            }

            if (tabName == "Grupos") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ExposedDropdownMenuBox(
                        expanded = groupDropdownExpanded,
                        onExpandedChange = { groupDropdownExpanded = !groupDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedGroup?.name ?: "Selecciona un grupo",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                            modifier = Modifier.menuAnchor().weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = groupDropdownExpanded,
                            onDismissRequest = { groupDropdownExpanded = false }
                        ) {
                            if (myGroups.isEmpty()) {
                                DropdownMenuItem(text = { Text("No tienes grupos") }, onClick = { })
                            } else {
                                myGroups.forEach { group ->
                                    DropdownMenuItem(
                                        text = { Text(group.name) },
                                        onClick = {
                                            selectedGroup = group
                                            groupDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showCreateGroupDialog = true },
                        colors = ButtonDefaults.buttonColors(AccentBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Crear")
                    }

                    if (selectedGroup != null) {
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { showManageGroupDialog = true },
                            colors = ButtonDefaults.buttonColors(MintPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            } else if (tabName == "Personal") {
                if (recentChats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentChats) { chat ->
                            val isChatSelected = selectedUser?.uid == chat.uid
                            Row(
                                modifier = Modifier
                                    .background(if (isChatSelected) MintPrimary else ChatBgLight, CircleShape)
                                    .border(1.dp, AccentBlue, CircleShape)
                                    .clickable {
                                        selectedUser = UserSearch(chat.uid, chat.username)
                                        searchUserQuery = chat.username
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isChatSelected) Color.White else MintPrimary)
                                Spacer(Modifier.width(4.dp))
                                Text(chat.username, color = if (isChatSelected) Color.White else MintPrimary, fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    modifier = Modifier.size(16.dp).clickable {
                                        chatViewModel.deletePersonalChat(chat.uid)
                                        if (isChatSelected) {
                                            selectedUser = null
                                            searchUserQuery = ""
                                        }
                                    },
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchUserQuery,
                        onValueChange = {
                            searchUserQuery = it
                            chatViewModel.searchUsers(it)
                            isSearchFocused = true
                            if(selectedUser != null && it != selectedUser?.username) selectedUser = null
                        },
                        placeholder = { Text("Buscar usuario por nombre...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentBlue) }
                    )

                    if (isSearchFocused && searchResults.isNotEmpty() && selectedUser == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 60.dp).shadow(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column {
                                searchResults.forEach { user ->
                                    Text(
                                        text = user.username,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedUser = user
                                                searchUserQuery = user.username
                                                isSearchFocused = false
                                            }
                                            .padding(16.dp),
                                        color = MintPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(ChatBgLight, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                if (tabName == "Grupos" && selectedGroup == null) {
                    Text("Selecciona o crea un grupo para comenzar.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                } else if (tabName == "Personal" && selectedUser == null) {
                    Text("Busca o selecciona un usuario para conversar.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(messages) { msg -> MessageBubble(msg) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isInputEnabled = tabName == "Mundial" || (tabName == "Grupos" && selectedGroup != null) || (tabName == "Personal" && selectedUser != null)

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(onClick = { showEmojis = !showEmojis }, enabled = isInputEnabled) {
                        Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji", tint = if (isInputEnabled) AccentBlue else Color.LightGray)
                    }
                    DropdownMenu(expanded = showEmojis, onDismissRequest = { showEmojis = false }) {
                        val emojiList = listOf("😀", "😂", "🥰", "😎", "🤔", "😭", "👍", "❤️")
                        Row(modifier = Modifier.padding(8.dp)) {
                            emojiList.forEach { emoji ->
                                Text(
                                    text = emoji, fontSize = 24.sp,
                                    modifier = Modifier.clickable { inputText += emoji; showEmojis = false }.padding(4.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Escribe tu mensaje...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = isInputEnabled,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, unfocusedBorderColor = AccentBlue)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val targetId = when (tabName) {
                                "Grupos" -> selectedGroup?.id ?: return@Button
                                "Personal" -> selectedUser?.uid ?: return@Button
                                else -> "global"
                            }
                            val targetName = if (tabName == "Personal") selectedUser?.username ?: "" else ""

                            chatViewModel.sendMessage(inputText, tabName, targetId, targetName)
                            inputText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = isInputEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                }
            }
        }
    }

    if (showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("Crear nuevo grupo", color = MintPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    placeholder = { Text("Nombre del grupo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            chatViewModel.createGroup(newGroupName)
                            newGroupName = ""
                            showCreateGroupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(AccentBlue)
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) { Text("Cancelar", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }

    if (showManageGroupDialog && selectedGroup != null) {
        val group = selectedGroup!!
        val isAdmin = group.adminId == currentUid
        var inviteQuery by remember { mutableStateOf("") }
        var isInviteSearchFocused by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showManageGroupDialog = false },
            title = { Text("Gestionar: ${group.name}", color = MintPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (isAdmin) {
                        Text("Añadir miembros:", fontWeight = FontWeight.Bold, color = BlueDark, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = inviteQuery,
                                onValueChange = {
                                    inviteQuery = it
                                    chatViewModel.searchUsers(it)
                                    isInviteSearchFocused = true
                                },
                                placeholder = { Text("Buscar usuario...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentBlue) }
                            )

                            if (isInviteSearchFocused && searchResults.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp).shadow(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column {
                                        searchResults.forEach { user ->
                                            Text(
                                                text = user.username,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        chatViewModel.addMemberToGroup(group.id, user)
                                                        inviteQuery = ""
                                                        isInviteSearchFocused = false
                                                    }
                                                    .padding(16.dp),
                                                color = MintPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Divider()
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    Text("Miembros del grupo:", fontWeight = FontWeight.Bold, color = BlueDark, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp).background(ChatBgLight, RoundedCornerShape(8.dp)).padding(8.dp)) {
                        items(group.memberNames.entries.toList()) { member ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(member.value, color = BlueDark, fontSize = 14.sp)
                                if (member.key == group.adminId) {
                                    Text(" (Admin)", color = MintPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (isAdmin) {
                    Button(
                        onClick = {
                            chatViewModel.deleteGroup(group.id)
                            selectedGroup = null
                            showManageGroupDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(Color.Red)
                    ) { Text("Eliminar Grupo") }
                } else {
                    Button(
                        onClick = {
                            chatViewModel.leaveGroup(group.id)
                            selectedGroup = null
                            showManageGroupDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(Color.Red)
                    ) { Text("Salir del Grupo") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showManageGroupDialog = false }) { Text("Cerrar", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    val isMine = msg.isMine
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            AvatarIcon(msg.senderName, MintPrimary)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .background(
                    if (isMine) Brush.horizontalGradient(listOf(MintPrimary, AccentBlue))
                    else Brush.horizontalGradient(listOf(AccentBlue, AccentBlue)),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(msg.senderName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(msg.time, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(msg.text, color = Color.White, fontSize = 15.sp)
        }

        if (isMine) {
            Spacer(modifier = Modifier.width(8.dp))
            AvatarIcon(msg.senderName, AccentBlue)
        }
    }
}

@Composable
fun AvatarIcon(name: String, bgColor: Color) {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun ChatTabsColumn(selectedTab: String, onTabSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TabButton("Mundial", Icons.Default.Public, selectedTab == "Mundial", Modifier.fillMaxWidth()) { onTabSelected("Mundial") }
        TabButton("Grupos", Icons.Default.Groups, selectedTab == "Grupos", Modifier.fillMaxWidth()) { onTabSelected("Grupos") }
        TabButton("Personal", Icons.Default.Person, selectedTab == "Personal", Modifier.fillMaxWidth()) { onTabSelected("Personal") }
    }
}

@Composable
fun ChatTabsRow(selectedTab: String, onTabSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        TabButton("Mundial", Icons.Default.Public, selectedTab == "Mundial", Modifier.weight(1f)) { onTabSelected("Mundial") }
        Spacer(modifier = Modifier.width(8.dp))
        TabButton("Grupos", Icons.Default.Groups, selectedTab == "Grupos", Modifier.weight(1f)) { onTabSelected("Grupos") }
        Spacer(modifier = Modifier.width(8.dp))
        TabButton("Personal", Icons.Default.Person, selectedTab == "Personal", Modifier.weight(1f)) { onTabSelected("Personal") }
    }
}

@Composable
fun TabButton(title: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(if (isSelected) MintPrimary else Color.White, RoundedCornerShape(24.dp))
            .border(1.5.dp, if (isSelected) MintPrimary else AccentBlue, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else BlueDark, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = if (isSelected) Color.White else BlueDark, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun ChatHelpModal(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001E28).copy(alpha = 0.5f))
            .padding(24.dp)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FloatingMascot(size = 180.dp)
            Canvas(modifier = Modifier.size(width = 16.dp, height = 24.dp)) {
                val path = Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(0f, size.height / 2f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(path, MintPrimary)

                val innerPath = Path().apply {
                    moveTo(size.width, 2f)
                    lineTo(3f, size.height / 2f)
                    lineTo(size.width, size.height - 2f)
                    close()
                }
                drawPath(innerPath, Color.White)
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, MintPrimary),
                modifier = Modifier.widthIn(min = 400.dp, max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("CHAT GLOBAL", color = MintPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("¿Cómo funciona?", color = BlueDark, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Comunícate con otros usuarios en tiempo real.", color = Color.Gray, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    BulletText("Mundial:", "Habla con todos los conectados.")
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Grupos:", "Crea salas privadas para amigos.")
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Personal:", "Envía mensajes directos a un usuario.")

                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.horizontalGradient(listOf(MintPrimary, AccentBlue)))
                            .clickable { onClose() }
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                    ) {
                        Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BulletText(boldPart: String, normalPart: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.DarkGray)) {
                append("• $boldPart ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, color = Color.DarkGray)) {
                append(normalPart)
            }
        },
        fontSize = 15.sp,
        lineHeight = 22.sp
    )
}