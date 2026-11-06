package com.example.fianzapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fianzapp.ui.FianzappNavHost
import com.example.fianzapp.ui.theme.FianzappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FianzappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FianzappNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Given by the skeleton, so that the app opens and you can see SignaturePad working. Replace it
 * with your own screens.
 */
@Composable
fun SkeletonScreen(modifier: Modifier = Modifier) {
    // Kept as text, so that the signature survives a rotation.
    var signatureText by rememberSaveable { mutableStateOf("") }
    val signature = decodeSignature(signatureText)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(text = stringResource(R.string.skeleton_placeholder))
        Text(text = stringResource(R.string.sample_signature_hint))
        SignaturePad(
            signature = signature,
            onSignatureChange = { signatureText = encodeSignature(it) }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (signature.isEmpty()) {
                    stringResource(R.string.signature_empty)
                } else {
                    stringResource(R.string.signature_lines, signature.size)
                }
            )
            TextButton(onClick = { signatureText = "" }) {
                Text(text = stringResource(R.string.signature_clear))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SkeletonScreenPreview() {
    FianzappTheme {
        SkeletonScreen()
    }
}
