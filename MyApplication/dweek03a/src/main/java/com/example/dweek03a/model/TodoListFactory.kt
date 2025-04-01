package com.example.dweek03a.model


//data를 제공하는 클래스는 보통 factory가 붙음
object TodoListFactory {
    fun makeTodoList() = mutableListOf(
        Item("아침 명상하기", "03-01 05:30", TodoStatus.COMPLETED),
        // = 생략하고 바로 인자만 넣어도 됨

        Item("오전 운동", "03-01 06:30", TodoStatus.PENDING),
        Item("책 읽기", "03-01 08:30", TodoStatus.PENDING),
        Item("점심 먹기", "03-01 12:30", TodoStatus.COMPLETED),
        Item("모프 공부하기", "03-01 17:30", TodoStatus.PENDING)
    )

}