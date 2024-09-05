import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun LoginPage(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4A5CFF)) // Blue background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Hello,",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Sign in to continue",
                color = Color.White,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 32.dp, top = 18.dp).align(Alignment.CenterHorizontally)
            )
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    textStyle = TextStyle(color = Color.Black),
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = password,
                    textStyle = TextStyle(color = Color.Black),
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5CFF))
                ) {
                    Text("Login", color = Color.White, fontSize = 24.sp)
                }

                Button(
                    onClick = { navController.navigate("signup")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5CFF))
                ) {
                    Text("SignUp", color = Color.White, fontSize = 24.sp)
                }
            }
        }
    }
}