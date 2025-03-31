package com.example.dweek03a

class GetInput {

    fun showMenu() {
        println("===== TodoList 메뉴 ======")
        println("1. 메모 등록")
        println("2. 메모 완료 체크")
        println("3. 메모 검색")
        println("4. 메모 전체 리스트 보기")
        println("5. 종료")
    }

    fun getInput(): Int {
        print("메뉴 선택: ")
        var inputValue = readlnOrNull()?.toIntOrNull() ?: 0
        return inputValue
    }
}