package org.infinite.solution.generalhealthtools

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform