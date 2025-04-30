package org.infinite.solution.generalhealthtools.presentation.dashboard.router

internal sealed class DashboardPath(val path: String) {
    data object Dashboard : DashboardPath(path = "Dashboard")
}