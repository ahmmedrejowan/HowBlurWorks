package com.rejown.howblurworks.presentation.home

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rejown.howblurworks.R
import com.rejown.howblurworks.data.ResultHolder
import com.rejown.howblurworks.domain.model.BlurIntensity
import com.rejown.howblurworks.domain.model.BlurTheoryItem
import com.rejown.howblurworks.domain.model.BlurType
import com.rejown.howblurworks.domain.model.KernelPreview
import com.rejown.howblurworks.domain.model.KernelSize
import com.rejown.howblurworks.domain.model.SampleImage
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartVisualization: (imageUri: String, blurType: BlurType, kernelSize: KernelSize, intensity: BlurIntensity) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as? Activity

    var showImageSourceSheet by remember { mutableStateOf(false) }
    var showExitSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val exitSheetState = rememberModalBottomSheetState()
    val theorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle back button press
    BackHandler {
        showExitSheet = true
    }

    // Temp file for camera capture
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Initialize sample images
    LaunchedEffect(Unit) {
        val sampleImages = listOf(
            "Landscape" to R.drawable.sample_landscape,
            "Portrait" to R.drawable.sample_portrait,
            "City" to R.drawable.sample_city,
            "Nature" to R.drawable.sample_nature
        )
        viewModel.initializeSampleImages(context, sampleImages)
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(context, it) }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { viewModel.onImageSelected(context, it) }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            tempImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "How Blur Works",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ═══════════════════════════════════════════════════════════
            // LEARN SECTION
            // ═══════════════════════════════════════════════════════════
            if (uiState.theoryItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LearnTheorySection(
                    items = uiState.theoryItems,
                    onItemClick = { viewModel.onTheoryItemClick(it) }
                )
            }

            // ═══════════════════════════════════════════════════════════
            // VISUALIZATION SECTION
            // ═══════════════════════════════════════════════════════════
            Spacer(modifier = Modifier.height(20.dp))

            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Create Visualization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Start Section
            QuickStartSection(
                bitmap = uiState.selectedBitmap,
                imageWidth = uiState.imageWidth,
                imageHeight = uiState.imageHeight,
                estimatedDuration = uiState.estimatedDuration,
                isLoading = uiState.isLoading,
                onCameraClick = { launchCamera() },
                onGalleryClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearImage = { viewModel.clearImage() }
            )

            // Sample Images Section
            if (uiState.sampleImages.isNotEmpty() && uiState.selectedBitmap == null) {
                Spacer(modifier = Modifier.height(16.dp))
                SampleImagesSection(
                    samples = uiState.sampleImages,
                    onSampleClick = { viewModel.onSampleImageSelected(context, it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Blur Settings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Blur Type
                    Text(
                        text = "Blur Algorithm",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BlurTypeSelector(
                        selectedType = uiState.selectedBlurType,
                        onTypeSelected = { viewModel.onBlurTypeSelected(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Kernel Size Selection
                    Text(
                        text = "Kernel Size",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    KernelSizeSelector(
                        selectedSize = uiState.selectedKernelSize,
                        onSizeSelected = { viewModel.onKernelSizeSelected(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Intensity Selection
                    Text(
                        text = "Intensity",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    IntensitySelector(
                        selectedIntensity = uiState.selectedIntensity,
                        onIntensitySelected = { viewModel.onIntensitySelected(it) }
                    )

                    // Interactive Kernel Preview
                    uiState.kernelPreview?.let { preview ->
                        Spacer(modifier = Modifier.height(16.dp))
                        InteractiveKernelPreview(preview = preview)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start Button
            Button(
                onClick = {
                    uiState.selectedImageUri?.let { uri ->
                        // Store bitmap in holder for sample images (URI parsing won't work for sample://)
                        if (uri.startsWith("sample://")) {
                            ResultHolder.inputBitmap = uiState.selectedBitmap
                        } else {
                            ResultHolder.clearInput()
                        }
                        onStartVisualization(uri, uiState.selectedBlurType, uiState.selectedKernelSize, uiState.selectedIntensity)
                    }
                },
                enabled = uiState.selectedImageUri != null && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Visualization",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Theory Detail Bottom Sheet
        if (uiState.showTheorySheet && uiState.selectedTheoryItem != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissTheorySheet() },
                sheetState = theorySheetState
            ) {
                TheoryDetailSheet(item = uiState.selectedTheoryItem!!)
            }
        }

        // Image Source Bottom Sheet
        if (showImageSourceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImageSourceSheet = false },
                sheetState = sheetState
            ) {
                ImageSourceBottomSheet(
                    onCameraClick = {
                        scope.launch {
                            sheetState.hide()
                            showImageSourceSheet = false
                            launchCamera()
                        }
                    },
                    onGalleryClick = {
                        scope.launch {
                            sheetState.hide()
                            showImageSourceSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
                )
            }
        }

        // Exit Confirmation Bottom Sheet
        if (showExitSheet) {
            ModalBottomSheet(
                onDismissRequest = { showExitSheet = false },
                sheetState = exitSheetState
            ) {
                ExitBottomSheet(
                    onExit = {
                        showExitSheet = false
                        activity?.finish()
                    },
                    onCancel = { showExitSheet = false }
                )
            }
        }
    }
}

@Composable
private fun ExitBottomSheet(
    onExit: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = "Exit App?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Message
        Text(
            text = "Are you sure you want to exit How Blur Works?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onExit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Exit")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LearnTheorySection(
    items: List<BlurTheoryItem>,
    onItemClick: (BlurTheoryItem) -> Unit
) {
    Column {
        Text(
            text = "Learn The Theory",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                TheoryCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun TheoryCard(
    item: BlurTheoryItem,
    onClick: () -> Unit
) {
    val icon = when (item.icon) {
        "blur_on" -> Icons.Default.BlurOn
        "grid_on" -> Icons.Default.GridOn
        "category" -> Icons.Default.Category
        "apps" -> Icons.Default.Apps
        else -> Icons.Default.BlurOn
    }

    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TheoryDetailSheet(item: BlurTheoryItem) {
    val icon = when (item.icon) {
        "blur_on" -> Icons.Default.BlurOn
        "grid_on" -> Icons.Default.GridOn
        "category" -> Icons.Default.Category
        "apps" -> Icons.Default.Apps
        else -> Icons.Default.BlurOn
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = item.content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )

        if (item.bulletPoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    item.bulletPoints.forEach { point ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun QuickStartSection(
    bitmap: android.graphics.Bitmap?,
    imageWidth: Int,
    imageHeight: Int,
    estimatedDuration: Int,
    isLoading: Boolean,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onClearImage: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Select Image")
        Spacer(modifier = Modifier.height(12.dp))

        if (bitmap != null) {
            // Show selected image
            SelectedImageCard(
                bitmap = bitmap,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                estimatedDuration = estimatedDuration,
                onClearImage = onClearImage
            )
        } else {
            // Show image selection options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImageSourceCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    subtitle = "Take a photo",
                    isLoading = isLoading,
                    onClick = onCameraClick
                )
                ImageSourceCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Image,
                    title = "Gallery",
                    subtitle = "Choose photo",
                    isLoading = isLoading,
                    onClick = onGalleryClick
                )
            }
        }
    }
}

@Composable
private fun ImageSourceCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = !isLoading) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedImageCard(
    bitmap: android.graphics.Bitmap,
    imageWidth: Int,
    imageHeight: Int,
    estimatedDuration: Int,
    onClearImage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            // Clear button
            IconButton(
                onClick = onClearImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear image",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Image info
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${imageWidth}×${imageHeight}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "  •  ~${estimatedDuration}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SampleImagesSection(
    samples: List<SampleImage>,
    onSampleClick: (SampleImage) -> Unit
) {
    Column {
        Text(
            text = "Try With Samples",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(samples) { sample ->
                SampleImageCard(sample = sample, onClick = { onSampleClick(sample) })
            }
        }
    }
}

@Composable
private fun SampleImageCard(
    sample: SampleImage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column {
            Image(
                painter = painterResource(id = sample.drawableResId),
                contentDescription = sample.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = sample.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = sample.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InteractiveKernelPreview(preview: KernelPreview) {
    // Fixed dimensions for consistent height
    val matrixContainerHeight = 200.dp
    val gap = 3.dp
    val padding = 12.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        // Divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Kernel matrix display with fixed height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(matrixContainerHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            val matrixSize = preview.matrix.size
            // Calculate cell size to fit within container
            // Available height = containerHeight - 2*padding
            // Total gaps = (matrixSize - 1) * gap
            // Cell size = (availableHeight - totalGaps) / matrixSize
            val availableSpace = matrixContainerHeight - (padding * 2)
            val totalGapSpace = gap * (matrixSize - 1)
            val cellSize = (availableSpace - totalGapSpace) / matrixSize

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                preview.matrix.forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        row.forEachIndexed { colIndex, value ->
                            val isCenter = rowIndex == preview.matrix.size / 2 &&
                                    colIndex == row.size / 2
                            val valueFloat = value.toFloatOrNull() ?: 0f
                            val isActive = valueFloat > 0.001f

                            // Calculate intensity for gradient effect
                            val maxValue = preview.matrix.flatten().mapNotNull { it.toFloatOrNull() }.maxOrNull() ?: 1f
                            val intensity = if (maxValue > 0) (valueFloat / maxValue).coerceIn(0f, 1f) else 0f

                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .background(
                                        when {
                                            isCenter -> MaterialTheme.colorScheme.primary
                                            isActive -> MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.4f + (intensity * 0.6f)
                                            )
                                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                        },
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val displayValue = when {
                                    valueFloat == 0f -> "0"
                                    valueFloat < 0.01f -> ".00"
                                    else -> String.format("%.2f", valueFloat).removePrefix("0")
                                }
                                // Dynamic font size based on cell size
                                val fontSize = when {
                                    matrixSize <= 3 -> 11.sp
                                    matrixSize <= 5 -> 9.sp
                                    else -> 7.sp
                                }
                                Text(
                                    text = displayValue,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize,
                                    fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isCenter -> MaterialTheme.colorScheme.onPrimary
                                        isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Description boxes with fixed height and 2:3 ratio
        val descriptionBoxHeight = 52.dp
        val infoItems = preview.description.split("\n")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left box (size info) - 2/5 width
            if (infoItems.isNotEmpty() && infoItems[0].isNotBlank()) {
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(descriptionBoxHeight)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = infoItems[0],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Right box (type info) - 3/5 width
            if (infoItems.size > 1 && infoItems[1].isNotBlank()) {
                Box(
                    modifier = Modifier
                        .weight(3f)
                        .height(descriptionBoxHeight)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = infoItems[1],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageSourceBottomSheet(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Select Image Source",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ImageSourceOption(
            icon = Icons.Default.CameraAlt,
            title = "Take Photo",
            subtitle = "Use camera to capture",
            onClick = onCameraClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        ImageSourceOption(
            icon = Icons.Default.Image,
            title = "Choose from Gallery",
            subtitle = "Pick existing photo",
            onClick = onGalleryClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ImageSourceOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}

@Composable
private fun BlurTypeSelector(
    selectedType: BlurType,
    onTypeSelected: (BlurType) -> Unit
) {
    val blurTypeInfo = mapOf(
        BlurType.GAUSSIAN to Triple(Icons.Default.BlurOn, "Gaussian", "Smooth, natural"),
        BlurType.BOX to Triple(Icons.Default.GridOn, "Box", "Fast, uniform"),
        BlurType.MOTION_HORIZONTAL to Triple(Icons.AutoMirrored.Filled.TrendingFlat, "Motion H", "Horizontal sweep"),
        BlurType.MOTION_VERTICAL to Triple(Icons.Default.Height, "Motion V", "Vertical sweep")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(BlurType.entries.toList()) { type ->
            val isSelected = type == selectedType
            val (icon, name, desc) = blurTypeInfo[type] ?: Triple(Icons.Default.BlurOn, type.displayName, "")

            Card(
                modifier = Modifier
                    .width(100.dp)
                    .clickable { onTypeSelected(type) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = if (isSelected) BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary
                ) else null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        modifier = Modifier.size(28.dp),
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun KernelSizeSelector(
    selectedSize: KernelSize,
    onSizeSelected: (KernelSize) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KernelSize.entries.forEach { size ->
            val isSelected = size == selectedSize
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSizeSelected(size) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = size.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IntensitySelector(
    selectedIntensity: BlurIntensity,
    onIntensitySelected: (BlurIntensity) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BlurIntensity.entries.forEach { intensity ->
            val isSelected = intensity == selectedIntensity
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onIntensitySelected(intensity) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = intensity.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
