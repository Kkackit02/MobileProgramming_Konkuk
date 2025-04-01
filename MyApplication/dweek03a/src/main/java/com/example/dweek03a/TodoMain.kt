package com.example.dweek03a

import com.example.dweek03a.model.TodoListFactory


// ALT Enter로 자동 임포트 가능
fun main() {
    /*
    val item1 = Item(content="모프 공부하기1" , time = "03-19 11:20") //status값을 주지 않아서 pending으로 초기화
    val item2 = Item(content="모프 공부하기2" , time = "03-19 12:20" , TodoStatus.COMPLETED)


    println(item1)
    println(item2)
    */
    println("202112346 정근녕")
    val todoService = TodoService(TodoListFactory.makeTodoList())



    while (true) {
        println("===== TodoList 메뉴 =====")
        println("1. 메모 등록")
        println("2. 메모 완료 체크")
        println("3. 메모 검색")
        println("4. 메모 전체 리스트 보기")
        println("5. 종료")
        print("메뉴 번호: ")

        val input = readLine()?.toIntOrNull()

        when (input) {
            1 -> {
                print("메모 내용을 입력하세요: ")
                val content = readLine() ?: ""
                todoService.addContent(content)
            }
            2 -> {
                todoService.listTodo()
                print("완료 처리할 메모 번호를 입력하세요: ")
                val index = readLine()?.toIntOrNull()
                if (index != null) {
                    todoService.completeItem(index)
                } else {
                    println("유효한 번호를 입력하세요.")
                }
            }
            3 -> {
                print("검색할 키워드를 입력하세요: ")
                val keyword = readLine() ?: ""
                todoService.search(keyword)
            }
            4 -> {
                todoService.listTodo()
            }
            5 -> {
                println("프로그램을 종료합니다.")
                break
            }
            else -> {
                println("잘못된 입력입니다. 1~5 사이 숫자를 입력하세요.")
            }
        }
        println();
    }

}