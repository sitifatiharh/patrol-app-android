package com.rahma.findingpatrol.util

/**
 * Checks if the user role is 'PATROL'.
 * This is case-insensitive and trims whitespace.
 * Returns false if role is null.
 */
fun isPatrolRole(role: String?): Boolean {
    if (role == null) return false
    return role.trim().equals("PATROL", ignoreCase = true)
}

/**
 * Checks if the user role is 'PIC'.
 * This is case-insensitive and trims whitespace.
 * Returns false if role is null.
 */
fun isPicRole(role: String?): Boolean {
    if (role == null) return false
    return role.trim().equals("PIC", ignoreCase = true)
}

/**
 * Checks if the user role is 'IMPROVEMENT'.
 * It also accepts "imp" as an alias.
 * This is case-insensitive and trims whitespace.
 * Returns false if role is null.
 */
fun isImprovementRole(role: String?): Boolean {
    if (role == null) return false
    val trimmedRole = role.trim()
    return trimmedRole.equals("IMPROVEMENT", ignoreCase = true) || trimmedRole.equals("imp", ignoreCase = true)
}
