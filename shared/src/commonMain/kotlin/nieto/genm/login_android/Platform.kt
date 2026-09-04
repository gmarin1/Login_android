package nieto.genm.login_android

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform