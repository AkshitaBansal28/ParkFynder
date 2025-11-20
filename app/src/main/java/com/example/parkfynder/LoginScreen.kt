package com.example.parkfynder

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var carName by remember { mutableStateOf("") }
    var carColor by remember { mutableStateOf("") }
    var carNumber by remember { mutableStateOf("") }

    var showCarDropdown by remember { mutableStateOf(false) }
    var showColorDropdown by remember { mutableStateOf(false) }

    val carModels = listOf(
        "Swift",
        "Brezza",
        "Baleno",
        "Creta",
        "Nexon",
        "Innova",
        "Fortuner",
        "Thar",
        "XUV700"
    )
    val carColors = listOf("White", "Black", "Red", "Blue", "Grey", "Silver", "Brown", "Yellow")

    var isLoading by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF004E92), Color(0xFF4286F4))
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
                .padding(28.dp)
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(18.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ICON ROW
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painterResource(id = R.drawable.baseline_directions_car_24),
                    null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(36.dp)
                )
                Icon(
                    painterResource(id = R.drawable.baseline_two_wheeler_24),
                    null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(36.dp)
                )
                Icon(
                    painterResource(id = R.drawable.baseline_local_shipping_24),
                    null,
                    tint = Color(0xFFF57C00),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "Welcome to ParkFynder",
                style = TextStyle(
                    color = Color(0xFF0D47A1),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CAR NAME FIELD
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = carName,
                    onValueChange = {
                        carName = it
                        showCarDropdown = true
                    },
                    label = { Text("Car Name (type to search)") },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = showCarDropdown && carName.isNotBlank(),
                    onDismissRequest = { showCarDropdown = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    carModels.filter { it.contains(carName, true) }.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                carName = model
                                showCarDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CAR COLOR FIELD
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = carColor,
                    onValueChange = {
                        carColor = it
                        showColorDropdown = true
                    },
                    label = { Text("Car Color (type to search)") },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = showColorDropdown && carColor.isNotBlank(),
                    onDismissRequest = { showColorDropdown = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    carColors.filter { it.contains(carColor, true) }.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = {
                                carColor = c
                                showColorDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CAR NUMBER
            OutlinedTextField(
                value = carNumber,
                onValueChange = { carNumber = it },
                label = { Text("Car Number (e.g. CH01AB1234)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // LOGIN BUTTON
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Enter email & password!", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    isLoading = true

                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            isLoading = false

                            if (task.isSuccessful) {

                                val userId = auth.currentUser?.uid ?: ""

                                // SAVE CAR DETAILS
                                val firestore =
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                val map = hashMapOf<String, Any>()
                                if (carName.isNotBlank()) map["carName"] = carName
                                if (carColor.isNotBlank()) map["carColor"] = carColor
                                if (carNumber.isNotBlank()) map["carNumber"] = carNumber

                                if (map.isNotEmpty()) {
                                    firestore.collection("users")
                                        .document(userId)
                                        .set(map, com.google.firebase.firestore.SetOptions.merge())
                                }

                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT)
                                    .show()
                                navController.navigate("mainScreen/$userId") {
                                    popUpTo("loginScreen") { inclusive = true }
                                }

                            } else {
                                Toast.makeText(
                                    context,
                                    "Login failed: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (isLoading)
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                else
                    Text("Login", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ", color = Color.Gray)
                Text(
                    "Sign Up",
                    color = Color(0xFF0D47A1),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("signupScreen") }
                )
            }
        }
    }
}
