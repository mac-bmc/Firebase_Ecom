package com.example.firebaseecom.otp2FA

enum class OtpEndPoints(val url:String) {
    OTP_BASE_URL("https://api.d7networks.com"),
    OTP_AUTH_TOKEN("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJhdXRoLWJhY2tlbmQ6YXBwIiwic3ViIjoiYmM4MDdlNWQtMmRlZS00NmRhLTgwNzYtNWZjMWYxMmViYzNhIn0.cDycc3LoHIgKEfl1LxtQLNqVQJJ22mX4E9oNw85jnQs"),
    OTP_CONTENT_TYPE("application/json")
}

