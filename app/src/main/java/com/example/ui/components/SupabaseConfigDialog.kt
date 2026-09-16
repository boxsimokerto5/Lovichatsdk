package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.supabase.SupabaseManager
import com.example.ui.theme.LovyPrimary
import com.example.ui.theme.OnlineGreen
import kotlinx.coroutines.launch

@Composable
fun SupabaseConfigDialog(
    supabaseManager: SupabaseManager,
    onDismiss: () -> Unit
) {
    val currentUrl by supabaseManager.supabaseUrl.collectAsState()
    val currentKey by supabaseManager.supabaseAnonKey.collectAsState()
    val isConnected by supabaseManager.isConnected.collectAsState()
    val lastStatus by supabaseManager.lastStatus.collectAsState()

    var inputUrl by remember { mutableStateOf(currentUrl) }
    var inputKey by remember { mutableStateOf(currentKey) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = LovyPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Konfigurasi Supabase",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Lovy Chat terhubung ke cloud database Supabase untuk otentikasi Google, sinkronisasi profil, pesan terenkripsi, dan komunitas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current Status Badge
                Surface(
                    color = if (isConnected) OnlineGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isConnected) OnlineGreen else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lastStatus,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isConnected) OnlineGreen else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Supabase Project URL Input
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://xyz.supabase.co") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, tint = LovyPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_supabase_url")
                )

                // Supabase Anon Key Input
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Supabase Anon Key") },
                    placeholder = { Text("eyJhbGciOi...") },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = LovyPrimary)
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_supabase_anon_key")
                )

                // Test Connection Feedback
                if (testResult != null) {
                    val (success, msg) = testResult!!
                    Text(
                        text = (if (success) "✅ " else "❌ ") + msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (success) OnlineGreen else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Test Button
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isTesting = true
                            testResult = null
                            // Temporarily test with input credentials
                            supabaseManager.updateConfig(inputUrl, inputKey)
                            val res = supabaseManager.testConnection()
                            testResult = res
                            isTesting = false
                        }
                    },
                    enabled = !isTesting && inputUrl.isNotBlank() && inputKey.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_test_supabase_connection")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menguji Koneksi...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uji Koneksi Supabase", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    supabaseManager.updateConfig(inputUrl, inputKey)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LovyPrimary),
                modifier = Modifier.testTag("btn_save_supabase_config")
            ) {
                Text("Simpan Konfigurasi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
