package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ContactListScreen()
        }
    }

    @Composable
    fun ContactListScreen() {
        var contacts by remember { mutableStateOf<List<String>>(emptyList()) }
        var hasPermission by remember { mutableStateOf(false) }
        val requestPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                hasPermission = isGranted
                if (isGranted) {
                    contacts = loadContacts()
                }
            }

        LaunchedEffect(Unit) {
            when {
                ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    hasPermission = true
                    contacts = loadContacts()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }
        }

        if (hasPermission) {
            if (contacts.isNotEmpty()) {
                ContactList(contacts)
            } else {
                Text("Нет контактов")
            }
        } else {
            Text("Доступа нет")
        }
    }

    private fun loadContacts(): List<String> {
        val contactList = mutableListOf<String>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                contactList.add("$name: $number")
            }
        } ?: run {
            // Обработка случая, когда курсор равен null
            // Вы можете добавить логирование или сообщение об ошибке здесь
        }

        return contactList
    }

    @Composable
    fun ContactList(contacts: List<String>) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            contacts.forEach { contact ->
                item {
                    Text(
                        text = contact,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).height(48.dp)
                    )
                }
            }
        }
    }
}