package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeName
import com.example.ui.viewmodel.NoorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val activeTheme by viewModel.activeTheme.collectAsState()

    val themesList = listOf(
        ThemeItem(
            name = AppThemeName.FROSTED_GLASS,
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFFFFF1F2),
            backgroundColor = Color(0xFFFDF2F8),
            emoji = "❄️"
        ),
        ThemeItem(
            name = AppThemeName.PINK_PASTEL,
            primaryColor = Color(0xFFFBC4DF),
            secondaryColor = Color(0xFFFFD1DC),
            backgroundColor = Color(0xFFFFF0F5),
            emoji = "🌸"
        ),
        ThemeItem(
            name = AppThemeName.GREEN_NATURAL,
            primaryColor = Color(0xFFADC9A5),
            secondaryColor = Color(0xFFC7E2C4),
            backgroundColor = Color(0xFFF4F9F4),
            emoji = "🌿"
        ),
        ThemeItem(
            name = AppThemeName.BLUE_SKY,
            primaryColor = Color(0xFFAEC6CF),
            secondaryColor = Color(0xFFC6DBE1),
            backgroundColor = Color(0xFFF2F7FA),
            emoji = "🌊"
        ),
        ThemeItem(
            name = AppThemeName.DARK_MINIMAL,
            primaryColor = Color(0xFF90CAF9),
            secondaryColor = Color(0xFF1E293B),
            backgroundColor = Color(0xFF0F172A),
            emoji = "🌙"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Theme Vibe", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "CHOOSE YOUR COHESIVE VIBE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Select a curated color palette that completely transforms the alarm, checklist, weather, and prayer scheduling layouts with soft pastel colors.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(themesList) { item ->
                    val isSelected = item.name == activeTheme
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(item.backgroundColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) item.primaryColor else item.secondaryColor.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.updateTheme(item.name) }
                            .padding(16.dp)
                            .testTag("theme_card_${item.name.name}")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = item.emoji, fontSize = 20.sp)
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected Theme",
                                        tint = item.primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = item.name.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.name == AppThemeName.DARK_MINIMAL) Color.White else Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Palette preview circle nodes
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(item.primaryColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(item.secondaryColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(item.backgroundColor)
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom decorative block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "🌸 Noor's design guidelines specify high visual accessibility so text headings remain thick and easily readable.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class ThemeItem(
    val name: AppThemeName,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val emoji: String
)
