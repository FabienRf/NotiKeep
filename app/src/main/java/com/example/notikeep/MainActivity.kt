package com.example.notikeep

import android.Manifest
import android.content.Intent
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
import com.example.notikeep.service.NotificationService
import com.example.notikeep.ui.theme.NotiKeepTheme




@Composable
fun NotificationScreen(){
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = title,
            onValueChange =  { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
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
                        statusText = ""
                    } else {
                        permissionLauncher.launch(permission)
                    }
                } else {
                    val intent = Intent(context, NotificationService::class.java)
                    intent.putExtra("title", title)
                    intent.putExtra("message", message)
                    context.startForegroundService(intent)
                    statusText = ""
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