package com.example.parkfynder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BookingSummaryScreen(
    navController: NavController,
    userId: String,
    day: String,
    area: String,
    duration: String,
    cost: Int
) {
    val firestore = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("") }
    var carName by remember { mutableStateOf("") }
    var carNumber by remember { mutableStateOf("") }

    // Fetch user details
    LaunchedEffect(Unit) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener {
                name = it.getString("name") ?: ""
                carName = it.getString("carName") ?: ""
                carNumber = it.getString("carNumber") ?: ""
            }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF42A5F5))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.95f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Booking Summary",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )

                Spacer(modifier = Modifier.height(20.dp))

                SummaryRow("Name", name)
                SummaryRow("Car Name", carName)
                SummaryRow("Car Number", carNumber)
                SummaryRow("Day", day)
                SummaryRow("Area", area)
                SummaryRow("Duration", duration)
                SummaryRow("Estimated Cost", "₹$cost")

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        navController.navigate("paymentScreen/$userId/$duration/$cost")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Proceed to Payment 💳", fontSize = 18.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Go Back & Edit", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color(0xFF0D47A1))
        Text(text = value, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}
