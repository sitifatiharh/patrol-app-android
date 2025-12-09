package com.rahma.findingpatrol

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rahma.findingpatrol.data.local.DataStoreManager
import com.rahma.findingpatrol.data.remote.retrofit.RetrofitInstance
import com.rahma.findingpatrol.data.repository.AuthRepository
import com.rahma.findingpatrol.data.repository.DashboardRepository
import com.rahma.findingpatrol.data.repository.FindingRepository
import com.rahma.findingpatrol.data.repository.MetadataRepository
import com.rahma.findingpatrol.ui.auth.LoginScreen
import com.rahma.findingpatrol.ui.dashboard.DashboardScreen
import com.rahma.findingpatrol.ui.finding.FindingDetailScreen
import com.rahma.findingpatrol.ui.finding.ImprovementActionScreen
import com.rahma.findingpatrol.ui.finding.NewEditFindingScreen
import com.rahma.findingpatrol.ui.finding.PicScreen
import com.rahma.findingpatrol.viewmodel.dashboard.DashboardViewModel
import com.rahma.findingpatrol.viewmodel.auth.LoginViewModel
import com.rahma.findingpatrol.viewmodel.finding.CreateFindingViewModel
import com.rahma.findingpatrol.viewmodel.finding.FindingDetailViewModel
import com.rahma.findingpatrol.viewmodel.metadata.MetadataViewModel
import com.rahma.findingpatrol.ui.theme.FindingPatrolTheme


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataStore
        dataStore = DataStoreManager(this)

        RetrofitInstance.init("http://192.17.102.221:3000/kaizen/", dataStore)

        // Gunakan instance API yang sudah disediakan oleh RetrofitInstance (Konsisten & Menghilangkan Warning)
        authRepository = AuthRepository(RetrofitInstance.authApi, dataStore)
        findingRepository = FindingRepository(RetrofitInstance.findingApi)
        dashboardRepository = DashboardRepository(RetrofitInstance.dashboardApi)
        metadataRepository = MetadataRepository(RetrofitInstance.metadataApi)
        
        loginViewModel = LoginViewModel(authRepository)
        dashboardViewModel = DashboardViewModel(dashboardRepository, findingRepository)
        createFindingViewModel = CreateFindingViewModel(findingRepository)
        findingDetailViewModel = FindingDetailViewModel(findingRepository)
        metadataViewModel = MetadataViewModel(metadataRepository)

        enableEdgeToEdge()

        setContent {
            FindingPatrolTheme {
                Surface {
                    // Read Role and User ID from DataStore
                    val userRole by dataStore.getUserRoleFlow().collectAsState(initial = "")
                    val userId by dataStore.getUserIdFlow().collectAsState(initial = -1)

                    AppNavigation(
                        loginViewModel = loginViewModel,
                        dashboardViewModel = dashboardViewModel,
                        createFindingViewModel = createFindingViewModel,
                        findingDetailViewModel = findingDetailViewModel,
                        metadataViewModel = metadataViewModel,
                        userRole = userRole,
                        userId = userId
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
    userRole: String,
    userId: Int
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // User Role is now passed dynamically from MainActivity

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
                onNavigateToCreateFinding = { navController.navigate("createFinding") },
                onNavigateToDetail = { findingId ->
                    navController.navigate("findingDetail/$findingId")
                }
            )
        }
        
        // Form 1 & 2 (Create & Patrol/PIC Edit Open)
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
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Form 3, 5, 6 (Improvement Action - All Statuses)
        composable(
            route = "improvementAction/{findingId}",
            arguments = listOf(navArgument("findingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val findingId = backStackEntry.arguments?.getInt("findingId") ?: 0
            
            ImprovementActionScreen(
                viewModel = createFindingViewModel,
                metadataViewModel = metadataViewModel,
                findingDetailViewModel = findingDetailViewModel,
                onNavigateBack = { navController.popBackStack() },
                findingId = findingId,
                currentUserId = userId // Dynamic
            )
        }
        
        // Form 4 (PIC Resolve)
        composable(
            route = "resolveFinding/{findingId}",
            arguments = listOf(navArgument("findingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val findingId = backStackEntry.arguments?.getInt("findingId") ?: 0
            PicScreen(
                viewModel = createFindingViewModel,
                onNavigateBack = { navController.popBackStack() },
                findingId = findingId,
                isOverdue = false // Bisa fetch detail kalau perlu logic overdue
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
                userRole = userRole, // Dynamic
                currentUserId = userId, // Dynamic
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
