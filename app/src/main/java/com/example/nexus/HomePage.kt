package com.example.nexus

import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun HomePage(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF4A5CFF)) // Set background color for HomePage
                .padding(innerPadding), // Ensure content doesn't overlap with the bottom navigation
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome to Home Page",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf("1", "2", "3", "4")

    NavigationBar {
        items.forEachIndexed { index, label ->
            NavigationBarItem(
                icon = { Text(label) }, // Placeholder for icons
                label = { Text(label) },
                selected = false, // Handle selected state if needed
                onClick = {
                    navController.navigate("screen_$index")
                }
            )
        }
    }
}