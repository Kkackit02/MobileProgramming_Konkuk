package com.example.dweek03a.model

data class Item(val content: String,
                val time:String,
                var status:TodoStatus = TodoStatus.PENDING)
{
    override fun toString():String{
        return "$content\t$time\t${if(status == TodoStatus.PENDING) "미완료" else "완료"}"
    }
}