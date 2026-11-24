package com.example.fianzapp.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fianzapp.R
import com.example.fianzapp.ui.theme.FianzappTheme

/*
 * Home screen: only its look. The three options and where they go are decided in
 * FianzappNavHost.kt. Colours are local to this screen, so the inner screens keep the app theme.
 */

private val Navy = Color(0xFF12324F)
private val Teal = Color(0xFF16877B)

private data class HomePalette(
    val background: Color,
    val header: Color,
    val onHeader: Color,
    val onHeaderMuted: Color,
    val card: Color,
    val cardBorder: Color,
    val title: Color,
    val body: Color,
    val accent: Color,
    val iconBackground: Color
)

@Composable
private fun homePalette(): HomePalette =
    if (isSystemInDarkTheme()) {
        HomePalette(
            background = Color(0xFF0E1620),
            header = Color(0xFF0A1C2E),
            onHeader = Color.White,
            onHeaderMuted = Color(0xFFB9C7D3),
            card = Color(0xFF16222E),
            cardBorder = Color(0x26FFFFFF),
            title = Color(0xFFE8EEF3),
            body = Color(0xFFA7B4C0),
            accent = Color(0xFF5CC2B5),
            iconBackground = Color(0x1F5CC2B5)
        )
    } else {
        HomePalette(
            background = Color(0xFFF4F6F8),
            header = Navy,
            onHeader = Color.White,
            onHeaderMuted = Color(0xFFC9D6E2),
            card = Color.White,
            cardBorder = Color(0xFFE1E6EB),
            title = Navy,
            body = Color(0xFF5A6876),
            accent = Teal,
            iconBackground = Color(0xFFE6F3F1)
        )
    }

private class HomeOption(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onLease: () -> Unit,
    onRooms: () -> Unit,
    onChecklist: () -> Unit,
    onDeadline: () -> Unit,
    onReport: () -> Unit,
    onLandlord: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = homePalette()
    val options = listOf(
        HomeOption(R.string.home_lease, R.string.home_lease_subtitle, Icons.Filled.Home, onLease),
        HomeOption(R.string.home_rooms, R.string.home_rooms_subtitle, Icons.AutoMirrored.Filled.List, onRooms),
        HomeOption(R.string.home_checklist, R.string.home_checklist_subtitle, Icons.Filled.CheckCircle, onChecklist),
        HomeOption(R.string.home_deadline, R.string.home_deadline_subtitle, Icons.Filled.DateRange, onDeadline),
        HomeOption(R.string.home_report, R.string.home_report_subtitle, Icons.Filled.Edit, onReport),
        HomeOption(R.string.home_landlord, R.string.home_landlord_subtitle, Icons.Filled.Person, onLandlord),
        HomeOption(R.string.home_settings, R.string.home_settings_subtitle, Icons.Filled.Settings, onSettings)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader(palette)

        // Content is centred and capped in width, so it reads well on phones, tablets and landscape.
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.home_section_title).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = palette.body
            )
            Spacer(Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = palette.card),
                border = BorderStroke(1.dp, palette.cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                options.forEachIndexed { index, option ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp),
                            color = palette.cardBorder
                        )
                    }
                    HomeOptionRow(option, palette)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_footer),
                style = MaterialTheme.typography.bodySmall,
                color = palette.body
            )
        }
    }
}

@Composable
private fun HomeHeader(palette: HomePalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.header)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Home, contentDescription = null, tint = palette.onHeader)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = palette.onHeader
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.onHeaderMuted
            )
        }
    }
}

@Composable
private fun HomeOptionRow(option: HomeOption, palette: HomePalette) {
    Card(
        onClick = option.onClick,
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(palette.iconBackground, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(option.icon, contentDescription = null, tint = palette.accent)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(option.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.title
                )
                Text(
                    text = stringResource(option.subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.body
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = palette.body
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun HomeScreenPreview() {
    FianzappTheme {
        HomeScreen(onLease = {}, onRooms = {}, onChecklist = {}, onDeadline = {}, onReport = {}, onLandlord = {}, onSettings = {})
    }
}
