package com.example.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.model.Tractor
import com.example.data.model.UiState
import com.example.data.repository.TractorRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.components.OrderFormBottomSheet
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.CatalogSearchScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MyOrdersScreen
import com.example.ui.screens.TractorDetailScreen
import com.example.ui.viewmodel.MarketplaceViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_CATALOG = "catalog"
const val ROUTE_ORDERS = "orders"
const val ROUTE_ADMIN = "admin"
const val ROUTE_TRACTOR_DETAIL = "tractor_detail/{tractorId}"

private data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val signedInUser = currentUser
    if (signedInUser == null) {
        AuthScreen(onAuthSuccess = { currentUser = auth.currentUser })
    } else {
        key(signedInUser.uid) {
            AuthenticatedMarketplaceShell(currentUser = signedInUser)
        }
    }
}

@Composable
private fun AuthenticatedMarketplaceShell(currentUser: FirebaseUser) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: MarketplaceViewModel = viewModel(
        key = currentUser.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MarketplaceViewModel(
                    repository = TractorRepository(db),
                    currentUserId = currentUser.uid,
                    currentUser = currentUser
                )
            }
        }
    )

    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val availableTractorsState by viewModel.availableTractorsState.collectAsStateWithLifecycle()
    val filteredTractorsState by viewModel.filteredTractorsState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val reviewsState by viewModel.reviewsState.collectAsStateWithLifecycle()
    val ratingSummaries by viewModel.ratingSummaries.collectAsStateWithLifecycle()
    val myOrdersState by viewModel.myOrdersState.collectAsStateWithLifecycle()
    val adminTractorsState by viewModel.adminTractorsState.collectAsStateWithLifecycle()
    val adminOrdersState by viewModel.adminOrdersState.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    var tractorToOrder by remember { mutableStateOf<Tractor?>(null) }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val tabs = remember(isAdmin) {
        buildList {
            add(
                NavTabItem(
                    route = ROUTE_DASHBOARD,
                    label = "ড্যাশবোর্ড",
                    selectedIcon = Icons.Filled.Dashboard,
                    unselectedIcon = Icons.Outlined.Dashboard,
                    testTag = "nav_tab_dashboard"
                )
            )
            add(
                NavTabItem(
                    route = ROUTE_CATALOG,
                    label = "সার্চ ও ফিল্টার",
                    selectedIcon = Icons.Filled.Search,
                    unselectedIcon = Icons.Outlined.Search,
                    testTag = "nav_tab_catalog"
                )
            )
            add(
                NavTabItem(
                    route = ROUTE_ORDERS,
                    label = "আমার অর্ডার",
                    selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                    unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    testTag = "nav_tab_orders"
                )
            )
            if (isAdmin) {
                add(
                    NavTabItem(
                        route = ROUTE_ADMIN,
                        label = "এডমিন",
                        selectedIcon = Icons.Filled.AdminPanelSettings,
                        unselectedIcon = Icons.Outlined.AdminPanelSettings,
                        testTag = "nav_tab_admin"
                    )
                )
            }
        }
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val isDetailRoute = currentRoute?.startsWith("tractor_detail") == true

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail && !isDetailRoute) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail && !isDetailRoute) {
                    NavigationRail {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DASHBOARD,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_DASHBOARD) {
                        DashboardScreen(
                            userName = currentUser.displayName?.takeIf { it.isNotBlank() } ?: "ক্রেতা",
                            userPhotoUrl = currentUser.photoUrl?.toString(),
                            isAdmin = isAdmin,
                            tractorsState = availableTractorsState,
                            ordersState = myOrdersState,
                            ratingSummaries = ratingSummaries,
                            onSelectBrandShortcut = { brand ->
                                viewModel.selectBrand(brand)
                                navigateToTab(ROUTE_CATALOG)
                            },
                            onNavigateToCatalog = { navigateToTab(ROUTE_CATALOG) },
                            onNavigateToOrders = { navigateToTab(ROUTE_ORDERS) },
                            onNavigateToAdmin = { navigateToTab(ROUTE_ADMIN) },
                            onTractorClick = { tractor ->
                                navController.navigate("tractor_detail/${tractor.id}")
                            },
                            onOrderClick = { tractor -> tractorToOrder = tractor },
                            onSeedDemoTractors = { viewModel.seedDemoTractors() },
                            onSignOutClick = {
                                signOutUser(credentialManager = credentialManager, scope = scope)
                            }
                        )
                    }

                    composable(ROUTE_CATALOG) {
                        CatalogSearchScreen(
                            filteredTractorsState = filteredTractorsState,
                            filterState = filterState,
                            ratingSummaries = ratingSummaries,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSelectBrand = viewModel::selectBrand,
                            onModelQueryChange = viewModel::updateModelQuery,
                            onPriceRangeChange = viewModel::updatePriceRange,
                            onMaxHoursChange = viewModel::updateMaxUsedHours,
                            onToggleCondition = viewModel::toggleConditionFilter,
                            onSelectSort = viewModel::updateSortOption,
                            onResetFilters = viewModel::resetFilters,
                            onTractorClick = { tractor ->
                                navController.navigate("tractor_detail/${tractor.id}")
                            },
                            onOrderClick = { tractor -> tractorToOrder = tractor }
                        )
                    }

                    composable(ROUTE_ORDERS) {
                        MyOrdersScreen(
                            ordersState = myOrdersState,
                            isSubmitting = isSubmitting,
                            onCancelOrder = viewModel::cancelOrder,
                            onExploreCatalog = { navigateToTab(ROUTE_CATALOG) }
                        )
                    }

                    composable(ROUTE_ADMIN) {
                        AdminPanelScreen(
                            ordersState = adminOrdersState,
                            tractorsState = adminTractorsState,
                            isSubmitting = isSubmitting,
                            onUpdateOrderStatus = viewModel::updateOrderStatusAdmin,
                            onSaveTractor = viewModel::saveTractorAdmin,
                            onDeleteTractor = viewModel::deleteTractorAdmin,
                            onSeedDemoTractors = viewModel::seedDemoTractors
                        )
                    }

                    composable(
                        route = ROUTE_TRACTOR_DETAIL,
                        arguments = listOf(navArgument("tractorId") { type = NavType.StringType })
                    ) { entry ->
                        val tractorId = entry.arguments?.getString("tractorId") ?: ""
                        val tractorList = (availableTractorsState as? UiState.Success)?.data
                            ?: (adminTractorsState as? UiState.Success)?.data
                            ?: emptyList()
                        val selectedTractor = tractorList.firstOrNull { it.id == tractorId }

                        if (selectedTractor != null) {
                            val allReviews = (reviewsState as? UiState.Success)?.data.orEmpty()
                            val tractorReviews = allReviews.filter { it.tractorId == selectedTractor.id }
                            TractorDetailScreen(
                                tractor = selectedTractor,
                                currentUserId = currentUser.uid,
                                isAdmin = isAdmin,
                                reviews = tractorReviews,
                                ratingSummary = ratingSummaries[selectedTractor.id],
                                isSubmitting = isSubmitting,
                                onBack = { navController.popBackStack() },
                                onOrderClick = { tractorToOrder = selectedTractor },
                                onSubmitReview = { existingId, rating, comment, onDone ->
                                    viewModel.submitReview(
                                        existingReviewId = existingId,
                                        tractorId = selectedTractor.id,
                                        rating = rating,
                                        comment = comment,
                                        onSuccess = onDone
                                    )
                                },
                                onDeleteReview = viewModel::deleteReview
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    val activeOrderTractor = tractorToOrder
    if (activeOrderTractor != null) {
        OrderFormBottomSheet(
            tractor = activeOrderTractor,
            defaultCustomerName = currentUser.displayName ?: "",
            isSubmitting = isSubmitting,
            onDismiss = { tractorToOrder = null },
            onSubmit = { name, address, phone, note ->
                viewModel.submitOrder(
                    tractor = activeOrderTractor,
                    customerName = name,
                    address = address,
                    phone = phone,
                    note = note,
                    onSuccess = {
                        tractorToOrder = null
                    }
                )
            }
        )
    }
}
