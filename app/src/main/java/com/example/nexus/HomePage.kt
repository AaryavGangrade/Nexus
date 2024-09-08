package com.example.nexus

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.concurrent.TimeUnit
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.gson.annotations.SerializedName
import java.util.Calendar

@Composable
fun HomePage(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(false) }
    var apiLink by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var facultyList by remember { mutableStateOf(listOf<FacultyMember>()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 1. Dynamic Greeting based on time of day
    val greetingMessage = getGreetingMessage()

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            coroutineScope.launch {
                uploadPdf(context, it, apiLink, onResult = { response ->
                    isLoading = false
                    skills = response.skills ?: ""
                    facultyList = response.faculty ?: listOf()
                })
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4A5CFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = greetingMessage, // Dynamic greeting
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) // Rounded corners
                .shadow(16.dp, RoundedCornerShape(32.dp)), // Shadow effect
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                state = listState, // Support for sticky header
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Shrink upload button after results
                    AnimatedVisibility(
                        visible = skills.isEmpty() && facultyList.isEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Button(
                            onClick = { pdfLauncher.launch("application/pdf") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5CFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Text(
                                "Upload PDF",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoading) {
                        // 9. Loading skeleton placeholder
                        CircularProgressIndicator(color = Color(0xFF4A5CFF))
                    }

                    // Display skills if available
                    if (skills.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Skills:",
                            color = Color(0xFF4A5CFF),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = skills,
                            color = Color.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 10. Sticky header for faculty list
                    if (facultyList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Top Faculty Matches:",
                            color = Color(0xFF4A5CFF),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }

                // 3. Animate faculty cards
                items(facultyList) { faculty ->
                    FacultyCard(faculty)
                }

                if (skills.isEmpty() && facultyList.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = apiLink,
                            onValueChange = { apiLink = it },
                            label = { Text("API Link") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4A5CFF),
                                focusedLabelColor = Color(0xFF4A5CFF)
                            )
                        )
                    }
                }
            }
        }
    }

    // 7. Icons in bottom navigation bar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomNavigationBar(navController)
    }
}

// Utility to get the dynamic greeting message
@Composable
fun getGreetingMessage(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Composable
fun FacultyCard(faculty: FacultyMember) {
    // Animated appearance for faculty cards
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E6E6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = faculty.name,
                    color = Color(0xFF4A5CFF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Employee ID: ${faculty.employeeId}",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Similarity Score: ${faculty.similarityScore}",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

suspend fun uploadPdf(context: Context, uri: Uri, apiLink: String, onResult: (ApiResponse) -> Unit) {
    try {
        withContext(Dispatchers.IO) {
            val file = getFileFromUri(context, uri)
            val requestBody = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(apiLink) // use the apiLink from the TextField
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            val apiResponse = apiService.uploadPdf(multipartBody)

            if (apiResponse.isSuccessful && apiResponse.body() != null) {
                onResult(apiResponse.body()!!)
            } else {
                onResult(ApiResponse("Upload failed: ${apiResponse.errorBody()?.string() ?: "Unknown error"}", listOf()))
            }
        }
    } catch (e: Exception) {
        onResult(ApiResponse("Upload failed: ${e.message}", listOf()))
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf("Home", "Search", "Upload", "Profile")

    Surface(
        modifier = Modifier
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            .clip(RoundedCornerShape(24.dp)) // Apply rounded corners
            .fillMaxWidth(),
        color = Color(0xFF4A5CFF), // Blue background color
        tonalElevation = 8.dp, // Slight elevation for the "floating" effect
    ) {
        NavigationBar(
            containerColor = Color.Transparent, // Transparent as the Surface provides color
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { index, label ->
                NavigationBarItem(
                    icon = { Text(label) }, // Placeholder for icons
                    label = { Text(label) },
                    selected = false, // Handle selected state if needed
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    ),
                    onClick = {
                        navController.navigate("screen_$index") // Define the navigation logic here
                    }
                )
            }
        }
    }
}


private fun getFileFromUri(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val tempFile = File(context.cacheDir, "temp_pdf_file.pdf")

    contentResolver.openInputStream(uri)?.use { inputStream ->
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return tempFile
}

interface ApiService {
    @Multipart
    @POST("upload")
    suspend fun uploadPdf(@Part file: MultipartBody.Part): retrofit2.Response<ApiResponse>
}

data class ApiResponse(
    @SerializedName("Skills") val skills: String?,
    @SerializedName("Top Faculty") val faculty: List<FacultyMember>?
)

data class FacultyMember(
    @SerializedName("Name") val name: String,
    @SerializedName("Employee ID") val employeeId: String,
    @SerializedName("Similarity Score") val similarityScore: String
)


@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    val navController = rememberNavController()
    HomePage(navController = navController)
}