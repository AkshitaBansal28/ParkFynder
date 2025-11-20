package com.example.parkfynder

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // --- User input states ---
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- Car details ---
    var carName by remember { mutableStateOf("") }
    var carColor by remember { mutableStateOf("") }
    var carNumber by remember { mutableStateOf("") }

    // --- Dropdown controls ---
    var showCarDropdown by remember { mutableStateOf(false) }
    var showColorDropdown by remember { mutableStateOf(false) }

    // --- Focus requesters for dropdowns ---
    val carFocusRequester = remember { FocusRequester() }
    val colorFocusRequester = remember { FocusRequester() }

    // --- Car options ---
    val carModels = listOf(
        "Swift", "Brezza", "Baleno", "Creta", "Nexon",
        "Innova", "Fortuner", "Thar", "XUV700"
    )
    val carColors = listOf("White", "Black", "Red", "Blue", "Grey", "Silver", "Brown", "Yellow")

    // --- OTP verification ---
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }

    // --- Background gradient ---
    val gradient = Brush.verticalGradient(colors = listOf(Color(0xFF0D47A1), Color(0xFF42A5F5)))

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
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Create Your ParkFynder Account",
                style = TextStyle(
                    color = Color(0xFF0D47A1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Name ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- Email ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- Phone + OTP ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Button(onClick = {
                    if (phone.length != 10) {
                        Toast.makeText(context, "Enter valid phone number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+91$phone")
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(context as Activity)
                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                Toast.makeText(context, "Phone Verified!", Toast.LENGTH_SHORT).show()
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                Toast.makeText(context, "OTP Failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }

                            override fun onCodeSent(
                                id: String,
                                token: PhoneAuthProvider.ForceResendingToken
                            ) {
                                verificationId = id
                                otpSent = true
                                Toast.makeText(context, "OTP Sent!", Toast.LENGTH_SHORT).show()
                            }
                        }).build()

                    PhoneAuthProvider.verifyPhoneNumber(options)
                }) {
                    Text(if (otpSent) "Resend" else "Send OTP")
                }
            }

            if (otpSent) {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it },
                    label = { Text("Enter OTP") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                        auth.signInWithCredential(credential).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Verify OTP") }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Password ---
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Car Details", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // --- Car Name Dropdown ---
            ExposedDropdownMenuBox(
                expanded = showCarDropdown,
                onExpandedChange = { showCarDropdown = !showCarDropdown }
            ) {
                OutlinedTextField(
                    value = carName,
                    onValueChange = {
                        carName = it
                        showCarDropdown = true
                    },
                    label = { Text("Car Name") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCarDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .focusRequester(carFocusRequester)
                )

                DropdownMenu(
                    expanded = showCarDropdown && carName.isNotBlank(),
                    onDismissRequest = { showCarDropdown = false }
                ) {
                    carModels.filter { it.contains(carName, ignoreCase = true) }
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    carName = item
                                    showCarDropdown = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Car Color Dropdown ---
            ExposedDropdownMenuBox(
                expanded = showColorDropdown,
                onExpandedChange = { showColorDropdown = !showColorDropdown }
            ) {
                OutlinedTextField(
                    value = carColor,
                    onValueChange = {
                        carColor = it
                        showColorDropdown = true
                    },
                    label = { Text("Car Color") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showColorDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .focusRequester(colorFocusRequester)
                )

                DropdownMenu(
                    expanded = showColorDropdown && carColor.isNotBlank(),
                    onDismissRequest = { showColorDropdown = false }
                ) {
                    carColors.filter { it.contains(carColor, ignoreCase = true) }
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    carColor = item
                                    showColorDropdown = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Car Number ---
            OutlinedTextField(
                value = carNumber,
                onValueChange = { carNumber = it },
                label = { Text("Car Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Sign Up Button ---
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val userId = auth.currentUser!!.uid
                            val data = hashMapOf(
                                "uid" to userId,
                                "name" to name,
                                "email" to email,
                                "phone" to phone,
                                "carName" to carName,
                                "carColor" to carColor,
                                "carNumber" to carNumber
                            )

                            firestore.collection("users")
                                .document(userId)
                                .set(data)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("parkingSlotsScreen/$userId") {
                                        popUpTo("signupScreen") { inclusive = true }
                                    }
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Signup failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Sign Up", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}
