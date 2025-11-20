package com.example.parkfynder

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.Color   // IMPORTANT

@Composable
fun PaymentScreen(
    navController: NavController,
    userId: String,
    hours: String,
    cost: String
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // Payment input states
    var selectedOption by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    // user details
    var name by remember { mutableStateOf("") }
    var carName by remember { mutableStateOf("") }
    var carColor by remember { mutableStateOf("") }
    var carNumber by remember { mutableStateOf("") }

    // fetch user + car details
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: ""
                    carName = doc.getString("carName") ?: ""
                    carColor = doc.getString("carColor") ?: ""
                    carNumber = doc.getString("carNumber") ?: ""
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header
        Text(
            text = "Payment Summary",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Show user & car details
        Text("Name: $name", fontSize = 18.sp)
        Text("Car: $carName ($carColor)", fontSize = 18.sp)
        Text("Car Number: $carNumber", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Duration and cost
        Text("Parking Duration: $hours hour(s)", fontSize = 18.sp)
        Text(
            "Amount to Pay: ₹$cost",
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(25.dp))

        // Payment method selection
        Text(
            "Select Payment Method",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(15.dp))

        // UPI option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == "UPI",
                onClick = { selectedOption = "UPI" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.baseline_qr_code_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("UPI Payment", fontSize = 18.sp)
        }

        // Card option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == "Card",
                onClick = { selectedOption = "Card" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.baseline_credit_card_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Card Payment", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic fields
        if (selectedOption == "UPI") {
            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("Enter your UPI ID") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (selectedOption == "Card") {
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Expiry Date (MM/YY)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cvv,
                onValueChange = { cvv = it },
                label = { Text("CVV") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Pay Button
        Button(
            onClick = {
                val valid = when (selectedOption) {
                    "UPI" -> upiId.isNotEmpty()
                    "Card" -> cardNumber.isNotEmpty() && expiryDate.isNotEmpty() && cvv.isNotEmpty()
                    else -> false
                }

                if (!valid) {
                    Toast.makeText(context, "Complete payment details!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val bookingInfo = hashMapOf(
                    "userId" to userId,
                    "carNumber" to carNumber,
                    "duration" to hours,
                    "amount" to cost,
                    "method" to selectedOption,
                    "timestamp" to System.currentTimeMillis()
                )

                // save to firestore
                firestore.collection("payments").add(bookingInfo)
                Toast.makeText(context, "Payment Successful!", Toast.LENGTH_LONG).show()

                navController.navigate("mainScreen/$userId") {
                    popUpTo("paymentScreen/$userId/$hours/$cost") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Pay ₹$cost Now", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigate("mainScreen/$userId") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}
