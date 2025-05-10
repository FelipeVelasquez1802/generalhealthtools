package org.infinite.solution.generalhealthtools.presentation.common.util

import generalhealthtools.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
internal suspend fun readResource(path: String): String {
    val countriesResource = Res.readBytes(path)
    return countriesResource.decodeToString().trim()
}

@OptIn(ExperimentalResourceApi::class)
internal suspend fun readPemKey(path: String): ByteArray = Res.readBytes(path)