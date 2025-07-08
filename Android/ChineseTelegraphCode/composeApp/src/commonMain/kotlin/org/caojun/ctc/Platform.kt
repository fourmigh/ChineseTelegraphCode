package org.caojun.ctc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform