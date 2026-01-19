package com.anjunar.technologyspeaks

fun String.toKebabCase(): String =
    replace(Regex("([a-z0-9])([A-Z])"), "$1-$2")
        .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1-$2")
        .lowercase()