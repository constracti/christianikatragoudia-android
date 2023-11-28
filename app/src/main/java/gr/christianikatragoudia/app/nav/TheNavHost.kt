package gr.christianikatragoudia.app.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import gr.christianikatragoudia.app.ui.InformationDestination
import gr.christianikatragoudia.app.ui.LicenseDestination
import gr.christianikatragoudia.app.ui.OptionsDestination
import gr.christianikatragoudia.app.ui.RecentDestination
import gr.christianikatragoudia.app.ui.SearchDestination
import gr.christianikatragoudia.app.ui.SongDestination
import gr.christianikatragoudia.app.ui.StarredDestination
import gr.christianikatragoudia.app.ui.ThemeDestination
import gr.christianikatragoudia.app.ui.TonalitiesDestination
import gr.christianikatragoudia.app.ui.VersionsDestination
import gr.christianikatragoudia.app.ui.WelcomeDestination

@Composable
fun TheNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = WelcomeDestination.route,
    ) {
        composable(route = WelcomeDestination.route) {
            WelcomeDestination.TheScreen(
                navigateToSearch = {
                    navController.navigate(SearchDestination.route) {
                        popUpTo(WelcomeDestination.route) {
                            inclusive = true
                        }
                    }
                },
                navigateToInformation = {
                    navController.navigate(InformationDestination.route)
                },
                navigateToLicense = {
                    navController.navigate(LicenseDestination.route)
                },
            )
        }
        composable(route = SearchDestination.route) {
            SearchDestination.TheScreen(
                navigateToStarred = {
                    navController.navigate(StarredDestination.route)
                },
                navigateToRecent = {
                    navController.navigate(RecentDestination.route)
                },
                navigateToOptions = {
                    navController.navigate(OptionsDestination.route)
                },
                navigateToSong = {
                    navController.navigate("${SongDestination.route}/${it}")
                },
            )
        }
        composable(route = StarredDestination.route) {
            StarredDestination.TheScreen(
                navigateToSearch = {
                    navController.navigateUp()
                },
                navigateToRecent = {
                    navController.navigate(RecentDestination.route) {
                        popUpTo(SearchDestination.route)
                    }
                },
                navigateToOptions = {
                    navController.navigate(OptionsDestination.route) {
                        popUpTo(SearchDestination.route)
                    }
                },
                navigateToSong = {
                    navController.navigate("${SongDestination.route}/${it}")
                },
            )
        }
        composable(route = RecentDestination.route) {
            RecentDestination.TheScreen(
                navigateToSearch = {
                    navController.navigateUp()
                },
                navigateToStarred = {
                    navController.navigate(StarredDestination.route) {
                        popUpTo(SearchDestination.route)
                    }
                },
                navigateToOptions = {
                    navController.navigate(OptionsDestination.route) {
                        popUpTo(SearchDestination.route)
                    }
                },
                navigateToSong = {
                    navController.navigate("${SongDestination.route}/${it}")
                },
            )
        }
        composable(
            route = SongDestination.routeWithArgs,
            arguments = listOf(navArgument(SongDestination.songIdArg) {
                type = NavType.IntType
            })
        ) {
            SongDestination.TheScreen(
                navigateBack = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = OptionsDestination.route) {
            OptionsDestination.TheScreen(
                navigateToSearch = {
                    navController.navigateUp()
                },
                navigateToStarred = {
                    navController.navigate(StarredDestination.route) {
                        popUpTo(SearchDestination.route)
                    }
                },
                navigateToRecent = {
                    navController.navigate(RecentDestination.route) {
                        popUpTo(SearchDestination.route)
                    }
                },
                navigateToTonalities = {
                    navController.navigate(TonalitiesDestination.route)
                },
                navigateToTheme = {
                    navController.navigate(ThemeDestination.route)
                },
                navigateToInformation = {
                    navController.navigate(InformationDestination.route)
                },
                navigateToLicense = {
                    navController.navigate(LicenseDestination.route)
                },
                navigateToVersions = {
                    navController.navigate(VersionsDestination.route)
                },
            )
        }
        composable(route = TonalitiesDestination.route) {
            TonalitiesDestination.DestinationScreen(
                navigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(route = ThemeDestination.route) {
            ThemeDestination.TheScreen(
                    navigateBack = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = InformationDestination.route) {
            InformationDestination.TheScreen(
                navigateBack = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = LicenseDestination.route) {
            LicenseDestination.TheScreen(
                navigateBack = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = VersionsDestination.route) {
            VersionsDestination.TheScreen(
                navigateBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}
