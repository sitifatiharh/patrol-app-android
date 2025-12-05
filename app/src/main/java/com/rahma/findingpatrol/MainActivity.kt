package com.rahma.findingpatrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
//import androidx.compose.material3.Text
//import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rahma.findingpatrol.data.local.DataStoreManager
import com.rahma.findingpatrol.data.remote.api.DashboardApi
import com.rahma.findingpatrol.data.remote.api.FindingApi
import com.rahma.findingpatrol.data.remote.retrofit.RetrofitInstance
import com.rahma.findingpatrol.data.repository.AuthRepository
import com.rahma.findingpatrol.data.repository.DashboardRepository
import com.rahma.findingpatrol.data.repository.FindingRepository
import com.rahma.findingpatrol.ui.auth.LoginScreen
import com.rahma.findingpatrol.ui.dashboard.DashboardScreen
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardViewModel
import com.rahma.findingpatrol.viewmodel.auth.LoginViewModel
import com.rahma.findingpatrol.ui.theme.FindingPatrolTheme
import androidx.compose.material3.Surface


class MainActivity : ComponentActivity() {

    private lateinit var dataStore: DataStoreManager
    private lateinit var authRepository: AuthRepository
    private lateinit var loginViewModel: LoginViewModel

    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var findingRepository: FindingRepository
    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataStore
        dataStore = DataStoreManager(this)

        // Retrofit
        RetrofitInstance.init("http://10.0.2.2:3000/kaizen/", dataStore)

        authRepository = AuthRepository(RetrofitInstance.authApi, dataStore)
        val findingApi = RetrofitInstance.retrofit.create(FindingApi::class.java)
        val dashboardApi = RetrofitInstance.retrofit.create(DashboardApi::class.java)

        loginViewModel = LoginViewModel(authRepository)
        findingRepository = FindingRepository(findingApi)
        dashboardRepository = DashboardRepository(dashboardApi)
        dashboardViewModel = DashboardViewModel(dashboardRepository, findingRepository)

        enableEdgeToEdge()

        setContent {
            FindingPatrolTheme {
                Surface {
                    AppNavigation(
                        loginViewModel = loginViewModel,
                        dashboardViewModel = dashboardViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToProfile = { /* TODO */ },
                onNavigateToFindings = { navController.navigate("history") },
                onNavigateToCreateFinding = { navController.navigate("createFinding") }
            )
        }

        // Tambahkan route lain: history, createFinding, profile
    }
}
