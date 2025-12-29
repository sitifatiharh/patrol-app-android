package com.rahma.findingpatrol.util

import com.rahma.findingpatrol.data.remote.ApiConfig

fun fixImageUrl(localPath: String): String {
    if (localPath.startsWith("http")) return localPath

    val normalized = localPath.replace("\\", "/")
    val filename = normalized.substringAfterLast("/")

    val folder = when {
        normalized.contains("/improved/", ignoreCase = true) -> "improved"
        else -> "findings"
    }

    return "${ApiConfig.BASE_URL}/kaizenUploads/$folder/$filename"
}
