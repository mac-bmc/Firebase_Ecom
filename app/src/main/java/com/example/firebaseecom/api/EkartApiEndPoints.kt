package com.example.firebaseecom.api

enum class EkartApiEndPoints(val url: String) {
    END_POINT_BASE("https://raw.githubusercontent.com/"),
    END_POINT_PRODUCTS("/mac-bmc/mac-bmc.github.io/main/product-home.json"),
    END_POINT_DETAIL_MULTILINGUAL("/mac-bmc/mac-bmc.github.io/main/product-details-multilanguage.json")
}