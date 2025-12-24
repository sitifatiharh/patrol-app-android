package com.rahma.findingpatrol

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rahma.findingpatrol.data.local.DataStoreManager
import com.rahma.findingpatrol.data.remote.ApiConfig
import com.rahma.findingpatrol.data.remote.retrofit.RetrofitInstance
import com.rahma.findingpatrol.data.repository.AuthRepository
import com.rahma.findingpatrol.data.repository.DashboardRepository
import com.rahma.findingpatrol.data.repository.FindingRepository
import com.rahma.findingpatrol.data.repository.MetadataRepository
import com.rahma.findingpatrol.ui.auth.LoginScreen
import com.rahma.findingpatrol.ui.dashboard.DashboardScreen
import com.rahma.findingpatrol.ui.debug.ImageDebugScreen
import com.rahma.findingpatrol.ui.finding.*
import com.rahma.findingpatrol.ui.theme.FindingPatrolTheme
import com.rahma.findingpatrol.viewmodel.auth.LoginViewModel
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardViewModel
import com.rahma.findingpatrol.viewmodel.finding.CreateFindingViewModel
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailViewModel
import com.rahma.findingpatrol.viewmodel.finding.FindingHistoryViewModel
import com.rahma.findingpatrol.viewmodel.metadata.MetadataViewModel

class MainActivity : ComponentActivity() {

    private lateinit var dataStore: DataStoreManager
    private lateinit var authRepository: AuthRepository
    private lateinit var loginViewModel: LoginViewModel

    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var findingRepository: FindingRepository
    private lateinit var metadataRepository: MetadataRepository
    
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var createFindingViewModel: CreateFindingViewModel
    private lateinit var findingDetailViewModel: FindingDetailViewModel
    private lateinit var metadataViewModel: MetadataViewModel
    private lateinit var findingHistoryViewModel: FindingHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataStore
        dataStore = DataStoreManager(this)

        // Init Retrofit with centralized URL
        RetrofitInstance.init(ApiConfig.BASE_URL + "/kaizen/", dataStore)

        // Repositories
        authRepository = AuthRepository(RetrofitInstance.authApi, dataStore)
        findingRepository = FindingRepository(RetrofitInstance.findingApi)
        dashboardRepository = DashboardRepository(RetrofitInstance.dashboardApi)
        metadataRepository = MetadataRepository(RetrofitInstance.metadataApi)
        
        // ViewModels
        loginViewModel = LoginViewModel(authRepository)
        dashboardViewModel = DashboardViewModel(dashboardRepository, findingRepository, dataStore)
        createFindingViewModel = CreateFindingViewModel(findingRepository)
        findingDetailViewModel = FindingDetailViewModel(findingRepository)
        metadataViewModel = MetadataViewModel(metadataRepository)
        findingHistoryViewModel = FindingHistoryViewModel(findingRepository, metadataRepository)

        enableEdgeToEdge()

        setContent {
            FindingPatrolTheme {
                Surface {
                    val userRole by dataStore.getUserRoleFlow().collectAsState(initial = "")
                    val userId by dataStore.getUserIdFlow().collectAsState(initial = -1)
                    val userMode by dataStore.getUserModeFlow().collectAsState(initial = "patrol")
                    val sectionId by dataStore.getUserSectionIdFlow().collectAsState(initial = -1)
                    val sectionName by dataStore.getUserSectionNameFlow().collectAsState(initial = "")

                    AppNavigation(
                        loginViewModel = loginViewModel,
                        dashboardViewModel = dashboardViewModel,
                        createFindingViewModel = createFindingViewModel,
                        findingDetailViewModel = findingDetailViewModel,
                        metadataViewModel = metadataViewModel,
                        findingHistoryViewModel = findingHistoryViewModel,
                        userRole = userRole,
                        userId = userId,
                        userMode = userMode,
                        sectionId = sectionId,
                        sectionName = sectionName
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    dashboardViewModel: DashboardViewModel,
    createFindingViewModel: CreateFindingViewModel,
    findingDetailViewModel: FindingDetailViewModel,
    metadataViewModel: MetadataViewModel,
    findingHistoryViewModel: FindingHistoryViewModel,
    userRole: String,
    userId: Int,
    userMode: String,
    sectionId: Int,
    sectionName: String
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
                userId = userId,
                userRole = userRole,
                userMode = userMode,
                sectionId = sectionId,
                sectionName = sectionName,
                onNavigateToProfile = { /* TODO */ },
                onNavigateToFindings = { navController.navigate("findingHistory") },
                onNavigateToCreateFinding = { navController.navigate("createFinding") },
                onNavigateToDetail = { findingId ->
                    navController.navigate("findingDetail/$findingId")
                },
                onNavigateToDebug = { navController.navigate("debug_image") },
                onNavigateToFindingStats = { statusId, statusName, mode ->
                    navController.navigate("findingStats/$statusId/$statusName/$mode")
                }
            )
        }
        
        composable("findingHistory") {
            FindingHistoryScreen(
                viewModel = findingHistoryViewModel,
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { findingId ->
                    navController.navigate("findingDetail/$findingId")
                }
            )
        }

        composable(
            route = "findingStats/{statusId}/{statusName}/{userMode}",
            arguments = listOf(
                navArgument("statusId") { type = NavType.IntType },
                navArgument("statusName") { type = NavType.StringType },
                navArgument("userMode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val statusId = backStackEntry.arguments?.getInt("statusId") ?: 0
            val statusName = backStackEntry.arguments?.getString("statusName") ?: ""
            val mode = backStackEntry.arguments?.getString("userMode") ?: "patrol"
            FindingStatsScreen(
                viewModel = findingHistoryViewModel,
                initialStatusId = statusId,
                initialStatusName = statusName,
                userId = userId,
                userRole = userRole,
                sectionId = sectionId,
                userMode = mode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { findingId ->
                    navController.navigate("findingDetail/$findingId")
                }
            )
        }
        
        composable("debug_image") { 
            ImageDebugScreen { navController.popBackStack() }
        }

        composable(
            route = "createFinding?findingId={findingId}",
            arguments = listOf(
                navArgument("findingId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val findingId = backStackEntry.arguments?.getInt("findingId") ?: 0
            NewEditFindingScreen(
                viewModel = createFindingViewModel,
                metadataViewModel = metadataViewModel,
                findingDetailViewModel = findingDetailViewModel,
                findingId = findingId,
                onNavigateBack = { navController.popBackStack() },
                navController = navController,
                onNavigateToDetail = { id ->
                    navController.navigate("findingDetail/$id") {
                        popUpTo("dashboard")
                    }
                }
            )
        }

        composable("camera") {
            CameraScreen(
                onImageCaptured = { uri: Uri ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("new_image", uri)
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = "improvementAction/{findingId}",
            arguments = listOf(navArgument("findingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val findingId = backStackEntry.arguments?.getInt("findingId") ?: 0
            
            ImprovementActionScreen(
                navController = navController, // Pass NavController here
                viewModel = createFindingViewModel,
                metadataViewModel = metadataViewModel,
                findingDetailViewModel = findingDetailViewModel,
                onNavigateBack = { navController.popBackStack() },
                findingId = findingId
            )
        }
        
        composable(
            route = "resolveFinding/{findingId}",
            arguments = listOf(navArgument("findingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val findingId = backStackEntry.arguments?.getInt("findingId") ?: 0
            PicScreen(
                navController = navController, // Pass NavController here
                viewModel = createFindingViewModel,
                findingDetailViewModel = findingDetailViewModel,
                onNavigateBack = { navController.popBackStack() },
                findingId = findingId,
                isOverdue = false
            )
        }

        composable(
            route = "findingDetail/{findingId}",
            arguments = listOf(navArgument("findingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val findingId = backStackEntry.arguments?.getInt("findingId") ?: 0
            FindingDetailScreen(
                viewModel = findingDetailViewModel,
                findingId = findingId,
                userRole = userRole,
                userMode = userMode,
                currentUserId = userId,
                sectionId = sectionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImprovementAction = { id ->
                    navController.navigate("improvementAction/$id")
                },
                onNavigateToResolve = { id ->
                    navController.navigate("resolveFinding/$id")
                },
                onNavigateToPatrolEdit = { id ->
                     navController.navigate("createFinding?findingId=$id")
                }
            )
        }
    }
}
