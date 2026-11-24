package com.example.fianzapp.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fianzapp.R

/** The three paths of a runtime permission (S11 Guide, section 4.1). */
private enum class CameraPermission { GRANTED, ASK, DENIED_ONCE, DENIED_FOR_GOOD }

/** A8: asks for the camera when it opens, then shows the preview and takes one photo. */
@Composable
fun CameraScreen(
    onPhotoSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    fun isGranted() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

    var asked by rememberSaveable { mutableStateOf(false) }
    var permission by remember { mutableStateOf(if (isGranted()) CameraPermission.GRANTED else CameraPermission.ASK) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        asked = true
        permission = when {
            granted -> CameraPermission.GRANTED
            // After a "no", Android only offers to explain again if the person did not say "don't ask again".
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true ->
                CameraPermission.DENIED_ONCE
            else -> CameraPermission.DENIED_FOR_GOOD
        }
    }

    // A9: the location is asked for on this screen too, after the camera. The app must work with
    // only the approximate one, and with none: the photo is then saved without a position.
    var hasLocation by remember { mutableStateOf(hasLocationPermission(context)) }
    var locationAsked by rememberSaveable { mutableStateOf(false) }
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationAsked = true
        hasLocation = result.values.any { it }
    }

    // Ask when the screen opens, not when the app starts.
    LaunchedEffect(Unit) {
        if (!isGranted() && !asked) launcher.launch(Manifest.permission.CAMERA)
    }
    LaunchedEffect(permission) {
        if (permission == CameraPermission.GRANTED && !hasLocation && !locationAsked) {
            locationLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }
    // Coming back from Settings with the permission turned on.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (isGranted()) permission = CameraPermission.GRANTED
        hasLocation = hasLocationPermission(context)
    }

    when (permission) {
        CameraPermission.GRANTED -> CameraPreview(viewModel, hasLocation, onPhotoSaved, modifier)
        CameraPermission.ASK -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        CameraPermission.DENIED_ONCE -> PermissionMessage(
            text = stringResource(R.string.camera_denied_once),
            button = stringResource(R.string.camera_ask_again),
            onClick = { launcher.launch(Manifest.permission.CAMERA) },
            modifier = modifier
        )
        CameraPermission.DENIED_FOR_GOOD -> PermissionMessage(
            text = stringResource(R.string.camera_denied_for_good),
            button = stringResource(R.string.camera_open_settings),
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            },
            modifier = modifier
        )
    }
}

@Composable
private fun CameraPreview(
    viewModel: CameraViewModel,
    hasLocation: Boolean,
    onPhotoSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }
    var saving by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    // Binds the back camera to this screen's lifecycle, and lets it go when the screen closes.
    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) {
                failed = true
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            if (providerFuture.isDone) providerFuture.get().unbindAll()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(if (hasLocation) R.string.camera_location_on else R.string.camera_location_off),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            if (failed) {
                Text(
                    stringResource(R.string.camera_failed),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            Button(
                enabled = !saving,
                onClick = {
                    saving = true
                    failed = false
                    val file = viewModel.newPhotoFile()
                    // The photo goes to a file in the internal folder, the only one the FileProvider shares.
                    val options = ImageCapture.OutputFileOptions.Builder(file).build()
                    imageCapture.takePicture(
                        options,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                viewModel.savePhoto(file, onPhotoSaved)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                file.delete()
                                saving = false
                                failed = true
                            }
                        }
                    )
                }
            ) {
                Text(stringResource(if (saving) R.string.camera_saving else R.string.camera_take))
            }
        }
    }
}

@Composable
private fun PermissionMessage(
    text: String,
    button: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Button(onClick = onClick) { Text(button) }
        Text(
            stringResource(R.string.camera_rest_of_app),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
