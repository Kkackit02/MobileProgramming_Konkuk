package com.example.dweek03a.model

//Ctrl + Alt + L -> 자동 코드 정리


data class Item(
    val content: String,
    val time: String,
    var status: TodoStatus = TodoStatus.PENDING
) {
    override fun toString(): String {
        return "$content\t$time\t${if (status == TodoStatus.COMPLETED) "완료" else "미완료"}"
    }
}
