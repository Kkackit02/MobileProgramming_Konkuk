package com.example.dweek03a

import com.example.dweek03a.model.TodoListFactory

fun executeAction(todoService : TodoService) {
    val getInput = GetInput()
    getInput.showMenu()
    var i = getInput.getInput()

    when(i){
        1 -> {
            todoService.addContent()
            executeAction(todoService)
        }
        2 -> {
            todoService.listTodos()
            todoService.completeContent()
            executeAction(todoService)
        }
        3 -> {
            todoService.searchContent()
            executeAction(todoService)
        }
        4->{
            todoService.listTodos()
            executeAction(todoService)
        }
        5->{
            return
        }
        else -> {
            println("INVALID VALUE: 다시 입력하세요")
            executeAction(todoService)
        }
    }
}

fun main() {
    val todoService = TodoService(TodoListFactory.makeTodoList())
    executeAction(todoService)
}