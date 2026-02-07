package com.rejown.howblurworks.presentation.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.res.painterResource
import com.rejown.howblurworks.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var showLicensesSheet by remember { mutableStateOf(false) }
    var showCreatorSheet by remember { mutableStateOf(false) }
    var showAppLicenseSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Appearance Section
            SectionHeader(
                icon = Icons.Default.DarkMode,
                title = "Appearance"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = settings.themeMode == 0,
                            onClick = { viewModel.updateThemeMode(0) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text("Auto")
                        }
                        SegmentedButton(
                            selected = settings.themeMode == 1,
                            onClick = { viewModel.updateThemeMode(1) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text("Light")
                        }
                        SegmentedButton(
                            selected = settings.themeMode == 2,
                            onClick = { viewModel.updateThemeMode(2) },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Text("Dark")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Visualization Section
            SectionHeader(
                icon = Icons.Default.Speed,
                title = "Visualization"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Default Duration",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Target time for visualization (used in Auto speed mode)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = settings.defaultDuration == 0,
                            onClick = { viewModel.updateDefaultDuration(0) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                        ) {
                            Text("5s")
                        }
                        SegmentedButton(
                            selected = settings.defaultDuration == 1,
                            onClick = { viewModel.updateDefaultDuration(1) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                        ) {
                            Text("15s")
                        }
                        SegmentedButton(
                            selected = settings.defaultDuration == 2,
                            onClick = { viewModel.updateDefaultDuration(2) },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                        ) {
                            Text("30s")
                        }
                        SegmentedButton(
                            selected = settings.defaultDuration == 3,
                            onClick = { viewModel.updateDefaultDuration(3) },
                            shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
                        ) {
                            Text("45s")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            SectionHeader(
                icon = Icons.Default.Info,
                title = "About"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // App Title and Version Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(80.dp)
                    )

                    Text(
                        text = "How Blur Works",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Text(
                        text = "An educational app that visualizes how image blur algorithms work in real-time. See the pixel-by-pixel convolution process with kernel overlays and animated processing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Built with Jetpack Compose",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // About Links Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column {
                    ClickableItem(
                        title = "Open Source Licenses",
                        description = "View third-party libraries",
                        onClick = { showLicensesSheet = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ClickableItem(
                        title = "Creator",
                        description = "About the developer",
                        onClick = { showCreatorSheet = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ClickableItem(
                        title = "App License",
                        description = "GNU General Public License v3.0",
                        onClick = { showAppLicenseSheet = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ClickableItem(
                        title = "Contact",
                        description = "Get in touch with the developer",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:kmrejowan@gmail.com".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "How Blur Works Feedback")
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ClickableItem(
                        title = "Source Code",
                        description = "View project on GitHub",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/ahmmedrejowan/HowBlurWorks".toUri()
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Made with love footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Made with ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "❤️",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = " by ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "K M Rejowan Ahmmed",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://rejowan.com".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Open Source Licenses Sheet
    if (showLicensesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLicensesSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            LicensesContent()
        }
    }

    // Creator Info Sheet
    if (showCreatorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreatorSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            CreatorContent()
        }
    }

    // App License Sheet
    if (showAppLicenseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppLicenseSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            AppLicenseContent()
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ClickableItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicensesContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Open Source Licenses",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LicenseItem(
            name = "Jetpack Compose",
            description = "Modern UI toolkit for Android",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose"
        )

        LicenseItem(
            name = "Material Design 3",
            description = "Material Design components for Compose",
            license = "Apache License 2.0",
            url = "https://m3.material.io/"
        )

        LicenseItem(
            name = "Navigation Compose",
            description = "Navigation component for Jetpack Compose",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose/navigation"
        )

        LicenseItem(
            name = "DataStore Preferences",
            description = "Data storage solution for key-value pairs",
            license = "Apache License 2.0",
            url = "https://developer.android.com/topic/libraries/architecture/datastore"
        )

        LicenseItem(
            name = "Kotlinx Coroutines",
            description = "Library support for Kotlin coroutines",
            license = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines"
        )

        LicenseItem(
            name = "CameraX",
            description = "Jetpack camera library for Android",
            license = "Apache License 2.0",
            url = "https://developer.android.com/training/camerax"
        )
    }
}

@Composable
private fun LicenseItem(
    name: String,
    description: String,
    license: String,
    url: String
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    url.toUri()
                )
                context.startActivity(intent)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = license,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CreatorContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About the Creator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "K M Rejowan Ahmmed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Senior Android Developer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CreatorLinkItem(
                    icon = Icons.Default.Language,
                    label = "Website",
                    value = "rejowan.com",
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://rejowan.com".toUri()
                        )
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                CreatorLinkItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = "kmrejowan@gmail.com",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:kmrejowan@gmail.com".toUri()
                        }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                CreatorLinkItem(
                    icon = Icons.Default.Code,
                    label = "GitHub",
                    value = "github.com/ahmmedrejowan",
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/ahmmedrejowan".toUri()
                        )
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                CreatorLinkItem(
                    icon = Icons.Default.Link,
                    label = "LinkedIn",
                    value = "linkedin.com/in/ahmmedrejowan",
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://linkedin.com/in/ahmmedrejowan".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun CreatorLinkItem(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 0.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AppLicenseContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "GNU General Public License v3.0",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = """
How Blur Works - Educational Blur Algorithm Visualizer
Copyright (C) 2025 K M Rejowan Ahmmed

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see the link below.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Key Terms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LicenseTermItem("✓ Freedom to use the software for any purpose")
                LicenseTermItem("✓ Freedom to study and modify the source code")
                LicenseTermItem("✓ Freedom to distribute copies")
                LicenseTermItem("✓ Freedom to distribute modified versions")
                LicenseTermItem("✓ Derivative works must be open source under GPL v3.0")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "For the complete license terms, please visit:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        TextButton(
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://www.gnu.org/licenses/gpl-3.0.en.html".toUri()
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "View Full GPL v3.0 License",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LicenseTermItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
