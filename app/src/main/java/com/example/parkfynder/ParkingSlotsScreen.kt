package com.example.parkfynder

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingSlotsScreen(navController: NavController, userId: String) {

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // 🔵 Fetch user car details
    var carName by remember { mutableStateOf("") }
    var carColor by remember { mutableStateOf("") }
    var carNumber by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val doc = firestore.collection("users").document(userId).get().await()
            carName = doc.getString("carName") ?: ""
            carColor = doc.getString("carColor") ?: ""
            carNumber = doc.getString("carNumber") ?: ""
        }
    }

    // Animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.parking_animation))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    // User selections
    var selectedDay by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf("") }

    val parkingAreas = listOf("Sector 17 Plaza", "Elante Mall", "ISBT 43", "Rose Garden", "Sukhna Lake")
    val durations = listOf("1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours")

    // Days
    val daysList = mutableListOf("Today", "Tomorrow")
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    for (i in 2..7) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, i)
        daysList.add("Advance - ${dateFormat.format(cal.time)}")
    }

    // Cost
    val costPerHour = 30
    val totalCost = remember(selectedDuration) {
        val h = selectedDuration.split(" ")[0].toIntOrNull() ?: 0
        h * costPerHour
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFFE3F2FD))
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Background animation
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ⭐ Car Details on Top
            if (carName.isNotEmpty()) {
                Text(
                    "Your Car: $carName ($carColor)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Car Number: $carNumber",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                "Book Your Parking Slot 🅿",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Dropdowns
            DropdownField("Select Day", daysList, selectedDay) { selectedDay = it }
            Spacer(modifier = Modifier.height(15.dp))

            DropdownField("Select Parking Area", parkingAreas, selectedArea) { selectedArea = it }
            Spacer(modifier = Modifier.height(15.dp))

            DropdownField("Select Duration", durations, selectedDuration) { selectedDuration = it }

            Spacer(modifier = Modifier.height(25.dp))

            if (selectedDuration.isNotEmpty()) {
                Text(
                    "Estimated Cost: ₹$totalCost",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            // Proceed Button
            AnimatedVisibility(
                visible = selectedDay.isNotEmpty() && selectedArea.isNotEmpty() && selectedDuration.isNotEmpty(),
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500))
            ) {
                Button(
                    onClick = {
                        val hours = selectedDuration.split(" ")[0]
                        navController.navigate("paymentScreen/$userId/$hours/$totalCost")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.fillMaxWidth(0.8f).height(55.dp)
                ) {
                    Text("Proceed to Payment 💳", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select...") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                ),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        onSelect(it)
                        expanded = false
                    })
                }
            }
        }
    }
}
