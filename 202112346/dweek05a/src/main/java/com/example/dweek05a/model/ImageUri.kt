package com.example.dweek05a.model

sealed class ImageUri {
    data class ResImage(val resID:Int) : ImageUri()
    // 모든 ID는 Int
    // drawable의 모든 resource ID는 int로 구성
    data class WebImage(val webUrl:String) : ImageUri()
}