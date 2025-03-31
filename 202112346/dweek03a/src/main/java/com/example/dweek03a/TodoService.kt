package com.example.dweek03a

import com.example.dweek03a.model.Item
import com.example.dweek03a.model.TodoStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TodoService (val todoList: MutableList<Item>){

    fun addContent(){
        print("메모를 등록하세요: ")
        val temp = readlnOrNull() ?: "NULL"
        val currentTime = LocalDateTime
            .now()
            .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
        todoList.add(Item(temp, currentTime))
        println("메모 등록됨: ${todoList.last()}")
    }

    fun completeContent(){
        print("완료 처리할 메모의 인덱스를 입력하세요: ")
        val temp = readlnOrNull()?.toIntOrNull() ?: (todoList.size)
        if(temp in todoList.indices){
            todoList[temp].status = TodoStatus.COMPLETED
            println("메모 완료 처리됨: ${todoList[temp]}")
        } else {
            println("잘못된 인덱스 입력됨")
        }
        println("202112354 한승현")
    }

    fun searchContent(){
        print("검색할 키워드를 입력하세요: ")
        val temp = readlnOrNull() ?: ""
        println("검색 결과: ")
        for(i in todoList.indices){
            if(temp in todoList[i].content){
                println("$i: ${todoList[i]}")
            }
        }
        println("202112354 한승현")
    }

    fun listTodos() {
        if (todoList.isEmpty()) {
            println("등록된 일정이 없습니다.")
        } else {
            todoList.forEachIndexed { index, item ->
                println("$index : $item")
            }
        }
    }
}