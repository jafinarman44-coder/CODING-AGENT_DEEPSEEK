// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL
// Unauthorized use, reproduction, or distribution is prohibited.

pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "LIFEEASY"
include(
    ":app", ":core:designsystem", ":core:domain", ":core:data", ":core:security",
    ":feature:agent", ":feature:memory", ":feature:voice", ":feature:docs",
    ":feature:monitor", ":feature:chat", ":feature:settings"
)
