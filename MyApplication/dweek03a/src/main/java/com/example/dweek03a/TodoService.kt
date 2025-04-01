package com.example.dweek03a

import com.example.dweek03a.model.Item
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.dweek03a.model.TodoStatus

class TodoService(val todoList: MutableList<Item>) {

    fun addContent(content: String) {
        val currentTime = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
        //함수 기능이 궁금하면 더블클릭하고 ctrl + B
        todoList.add(Item(content, currentTime)) // status는 자동으로 pending
    }

    fun completeItem(index: Int) {
    //202112346 정근녕
        if (index in todoList.indices) {
            todoList[index].status = TodoStatus.COMPLETED
            print("메모 완료 처리됨: ")
            println(todoList[index])
        } else {
            println("유효하지 않은 인덱스입니다.")
        }
    }

    fun search(keyword: String) {
        val result = todoList.filter {
            it.content.contains(keyword, ignoreCase = true)
        }

        if (result.isEmpty()) {
            println("검색 결과가 없습니다.")
        } else {
            println("검색 결과:")
            result.forEachIndexed { index, item ->
                println("$index : $item")
            }
        }
    }


    fun listTodo() //print all element
    {
        println("전체 메모: ")
        if (todoList.isEmpty()) {
            println("등록된 일정이 없습니다.")
        } else {
            todoList.forEachIndexed { index, item ->
                println("$index : $item")
            }
        }
    }
}