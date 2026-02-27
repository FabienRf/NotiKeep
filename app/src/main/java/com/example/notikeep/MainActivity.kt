package com.example.notikeep

import android.Manifest
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.notikeep.service.NotificationService
import com.example.notikeep.ui.theme.NotiKeepTheme
import kotlinx.coroutines.launch


val Context.dataStore by preferencesDataStore(name = "user_tm") // tm > title, message

object PreferencesKeys {
    val TITLE = stringPreferencesKey("title")
    val MESSAGE = stringPreferencesKey("message")
}


class NotificationData(private val context: Context){
    val titleFlow: Flow<String> = context.dataStore.data
        .map{ prefs -> prefs[PreferencesKeys.TITLE] ?: ""}
    val messageFlow: Flow<String> = context.dataStore.data
        .map{ prefs -> prefs[PreferencesKeys.MESSAGE] ?: ""}

    suspend fun saveTitle(title: String){
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.TITLE] = title }
    }
    suspend fun saveMessage(message: String){
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.MESSAGE] = message }
    }
}


@Composable
fun NotificationScreen(){
    val context = LocalContext.current
    val notificationData = remember { NotificationData(context) }
    val scope = rememberCoroutineScope()

    val titleStored by notificationData.titleFlow.collectAsState(initial = "")
    val messageStored by notificationData.messageFlow.collectAsState(initial = "")

    var title by remember { mutableStateOf(titleStored) }
    var message by remember { mutableStateOf(messageStored) }
    var statusText by remember { mutableStateOf("") }

    LaunchedEffect(titleStored) { title = titleStored }
    LaunchedEffect(messageStored) { message = messageStored }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(context, NotificationService::class.java)
            intent.putExtra("title", title)
            intent.putExtra("message", message)
            context.startForegroundService(intent)
            statusText = ""
        } else {
            statusText = "Notification permission is required on Android 13+."
        }
    }

    fun UpdateNotificationData(title: String, message: String){
        scope.launch { notificationData.saveTitle(title) }
        scope.launch { notificationData.saveMessage(message) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = title,
            onValueChange =  { newTitle -> title = newTitle },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { newMessage -> message = newMessage},
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permission = Manifest.permission.POST_NOTIFICATIONS
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        val intent = Intent(context, NotificationService::class.java)
                        intent.putExtra("title", title)
                        intent.putExtra("message", message)
                        context.startForegroundService(intent)
                        UpdateNotificationData(title, message)
                    } else {
                        permissionLauncher.launch(permission)
                    }
                } else {
                    val intent = Intent(context, NotificationService::class.java)
                    intent.putExtra("title", title)
                    intent.putExtra("message", message)
                    context.startForegroundService(intent)
                    UpdateNotificationData(title, message)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create notification")
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (statusText.isNotEmpty()) {
            Text(
                text = statusText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val intent = Intent(context, NotificationService::class.java)
                context.stopService(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete notification")
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiKeepTheme {
                NotificationScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotiKeepTheme {
        Greeting("Android")
    }
}