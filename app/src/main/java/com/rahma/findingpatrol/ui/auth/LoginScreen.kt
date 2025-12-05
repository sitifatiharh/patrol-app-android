package com.rahma.findingpatrol.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.rahma.findingpatrol.viewmodel.auth.LoginViewModel
import com.rahma.findingpatrol.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color(0xFFECEAF0)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_avatar),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("User Login", color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // Username field (rounded)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F4F7),
                unfocusedContainerColor = Color(0xFFF2F4F7),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            placeholder = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "Hide" else "Show",
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .padding(end = 8.dp),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F4F7),
                unfocusedContainerColor = Color(0xFFF2F4F7),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Login button
        Button(
            onClick = {
                focusManager.clearFocus()
                loading = true
                loginMessage = ""
                viewModel.login(username.trim(), password) { success, message ->
                    loading = false
                    loginMessage = message
                    if (success) {
                        onLoginSuccess()
                    }
                }
            },
            modifier = Modifier
                .height(44.dp)
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(22.dp),
            enabled = username.isNotBlank() && password.isNotBlank() && !loading
        )

        {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("Login")
            }
        }

        if (loginMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(loginMessage, color = Color.Red)
        }
    }

}
