package jFx2.router

data class RouteMatch(
    val route: Route,
    val fullPath: String,        // z.B. "/login"
    val params: Map<String, String> = emptyMap()
)