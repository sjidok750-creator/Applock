package com.example.applock.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.applock.data.AppRepository
import com.example.applock.data.InstalledApp
import com.example.applock.data.LockedAppsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    needsPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppRepository(context) }
    val lockedStore = remember { LockedAppsStore(context) }

    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var lockedSet by remember { mutableStateOf(lockedStore.lockedPackages()) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { repo.loadLaunchableApps() }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("AppLock") }) }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            if (needsPermission) {
                PermissionBanner(onRequestPermission)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("앱 검색") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            val filtered = remember(apps, query) {
                if (query.isBlank()) apps
                else apps.filter { it.label.contains(query, ignoreCase = true) }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        locked = app.packageName in lockedSet,
                        onToggle = { checked ->
                            lockedStore.setLocked(app.packageName, checked)
                            lockedSet = lockedStore.lockedPackages()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: InstalledApp, locked: Boolean, onToggle: (Boolean) -> Unit) {
    val bitmap = remember(app.packageName) { app.icon.toImageBitmap() }
    ListItem(
        leadingContent = {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        },
        headlineContent = { Text(app.label) },
        supportingContent = { Text(app.packageName, style = MaterialTheme.typography.bodySmall) },
        trailingContent = { Switch(checked = locked, onCheckedChange = onToggle) }
    )
}

@Composable
private fun PermissionBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("권한이 필요해요", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "‘앱 사용 정보 접근’과 ‘다른 앱 위에 표시’ 권한을 허용해야 잠금이 작동해요.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onClick) { Text("권한 설정 열기") }
        }
    }
}

private fun Drawable.toImageBitmap(size: Int = 96): ImageBitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}
