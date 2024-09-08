package com.example.nexus

import com.google.accompanist.flowlayout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    var apiLink by remember { mutableStateOf("https://detailed-mv-creates-returning.trycloudflare.com") }
    var skills by remember { mutableStateOf("") }
    var facultyList by remember { mutableStateOf(listOf<FacultyMember>()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
            .background(Color(0xFF262626)) // Dark background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = getGreetingMessage(),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LazyColumn(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (skills.isEmpty() && facultyList.isEmpty()) 120.dp else 60.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF69B4), Color(0xFF8A2BE2)) // Pink to Purple gradient
                                )
                            )
                    ) {
                        Button(
                            onClick = { pdfLauncher.launch("application/pdf") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)), // Dark background to match the theme
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(1.dp) // This creates a thin border effect
                        ) {
                            Text(
                                "Upload PDF",
                                color = Color.White,
                                fontSize = if (skills.isEmpty() && facultyList.isEmpty()) 32.sp else 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF673AB7)) // Use purple accent color
                    }
                }

                if (skills.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Skills:",
                            color = Color(0xFFBB86FC), // Light purple for labels
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        FlowRow(
                            mainAxisSpacing = 8.dp,
                            crossAxisSpacing = 8.dp,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            skills.split(",").forEach { skill ->
                                SkillTag(skill.trim())
                            }
                        }
                    }
                }

                if (facultyList.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Top Faculty Matches:",
                            color = Color(0xFFBB86FC),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    items(facultyList) { faculty ->
                        FacultyCard(faculty)
                    }
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
                                focusedBorderColor = Color(0xFFBB86FC),
                                focusedLabelColor = Color(0xFFBB86FC)
                            )
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNavigationBar(navController)
        }
    }
}

@Composable
fun SkillTag(skill: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF6200EE), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = skill, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun FacultyCard(faculty: FacultyMember) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF333333)), // Dark gray background
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFFF69B4), Color(0xFF8A2BE2)) // Pink to Purple gradient
            )
        ),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = faculty.name,
                color = Color(0xFFBB86FC), // Light purple
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
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun UploadButton(onClick: () -> Unit, isDataLoaded: Boolean) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF222222) // Dark background to match the theme
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isDataLoaded) 60.dp else 120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF69B4), Color(0xFF8A2BE2)) // Pink to Purple gradient
                )
            )
    ) {
        Text(
            "Upload PDF",
            color = Color.White,
            fontSize = if (isDataLoaded) 18.sp else 32.sp,
            fontWeight = FontWeight.Bold
        )
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
    val items = listOf(
        Triple("Home", Icons.Default.Home, "screen_0"),
        Triple("Search", Icons.Default.Search, "screen_1"),
        Triple("Upload", Icons.Default.Add, "screen_2"),
        Triple("Profile", Icons.Default.Person, "screen_3")
    )

    Box(
        modifier = Modifier
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF69B4), Color(0xFF8A2BE2))
                )
            )
    ) {
        Surface(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth(),
            color = Color(0xFF222222),
            shape = RoundedCornerShape(22.dp),
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach { (label, icon, route) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = false,
                        onClick = { navController.navigate(route) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFBB86FC),
                            selectedTextColor = Color(0xFFBB86FC),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF333333)
                        )
                    )
                }
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
