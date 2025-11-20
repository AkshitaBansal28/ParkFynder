package com.example.parkfynder

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainScreen(navController: NavController, userId: String) {

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val userEmail = auth.currentUser?.email ?: "Guest"

    var carName by remember { mutableStateOf("") }
    var carColor by remember { mutableStateOf("") }
    var carNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ Fetch car details EVERY TIME screen opens
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    carName = doc.getString("carName") ?: ""
                    carColor = doc.getString("carColor") ?: ""
                    carNumber = doc.getString("carNumber") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load car details", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    // 🌈 Gradient background
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF42A5F5))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                painter = painterResource(id = R.drawable.baseline_directions_car_24),
                contentDescription = "ParkFynder Logo",
                tint = Color(0xFF0D47A1),
                modifier = Modifier.size(70.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome, $userEmail!",
                style = TextStyle(
                    color = Color(0xFF0D47A1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 🔄 Loading spinner while fetching car details
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF0D47A1))
            } else {
                // 🚘 Car Details
                if (carName.isNotEmpty()) {
                    Text(
                        text = "Your Car: $carName ($carColor)",
                        style = TextStyle(color = Color.DarkGray, fontSize = 16.sp)
                    )
                    Text(
                        text = "Car Number: $carNumber",
                        style = TextStyle(color = Color.DarkGray, fontSize = 16.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 🚗 View Parking Slots
            Button(
                onClick = { navController.navigate("parkingSlotsScreen/$userId") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_directions_car_24),
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Parking Slots", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🚪 Logout (same UI kept)
            OutlinedButton(
                onClick = {
                    auth.signOut()
                    navController.navigate("loginScreen") {
                        popUpTo("mainScreen/$userId") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(25.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_directions_car_24),
                    contentDescription = "Logout",
                    tint = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Smart Parking. Made Simple 🚙",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
