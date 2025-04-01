package com.example.myapplication

/**
 * 함수 만들기
 */
//fun add(a: Int, b: Int): Int {
//    return a + b
//}

//fun add(a:Int, b:Int) = a + b
//
//fun main() {
//    val result = add(10, 20)
//    println("10 + 20 = $result")
//}

/**
 * 람다함수 만들기
 */
//fun main() {
//    val multiply: (Int, Int) -> Int = { a, b -> a * b }
//    println("5 * 4 = ${multiply(5, 4)}")
//
//    val greet: (String) -> String = { name -> "Hello, $name!" }
//    println(greet("Kotlin"))
//}

/**
 * 람다함수 - 후행식
 */

//fun performOperation(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
//    return operation(a, b)
//}
//
//fun performOperation(a: Int, b: Int, operation: (Int, Int) -> Int) = operation(a, b)
//
//fun main() {
//    val result = performOperation(8, 2) { x, y -> x - y }
//    println("8 - 2 = $result")
//}

/**
 * 함수타입 변수와 일반함수
 */
//fun subtract(a: Int, b: Int) = a - b
//fun main() {
//    val operation: (Int, Int) -> Int = ::subtract
//    println("15 - 5 = ${operation(15, 5)}")
//}

/**
 * 고차함수
 */
//fun highOrderFunction1(func:()->Unit) = func()
//fun highOrderFunction2():()->Unit = { println("greenjoa") }
//fun highOrderFunction3(func:()->Unit):()->Unit = func
//
//fun main() {
//    highOrderFunction1 { println("hello") }
//    highOrderFunction2()()
//    highOrderFunction3 { println("world") }()
//}

//fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int) = operation(a, b)
//
//fun main() {
//    val sum = calculate(10, 20) { x, y -> x + y }
//    val product = calculate(10, 20) { x, y -> x * y }
//
//    println("10 + 20 = $sum")
//    println("10 * 20 = $product")
//}

/**
 * 확장함수
 */
//fun String.lastChar(): Char {
//    return this[this.length - 1]
//}
//fun main() {
//    println("Kotlin".lastChar())
//}

//fun String.removeFirstLastChar():String = this.substring(1, this.length-1)
//fun main() {
//    println("HelloWorld".removeFirstLastChar())
//}

/**
 * 클래스 만들기
 */
//class Person {
//    var name: String = "Unknown"
//    var age: Int = 0
//
//    fun introduce() {
//        println("안녕하세요, 제 이름은 ${name}이고, 나이는 ${age}살입니다.")
//    }
//}
//
//fun main() {
//    val person = Person()
//    person.name = "Alice"
//    person.age = 25
//    person.introduce()
//}

/**
 * 클래스 주 생성자
 */
//class Person(val name: String, var age: Int) {
//    fun introduce() {
//        println("안녕하세요, 제 이름은 ${name}이고, 나이는 ${age}살입니다.")
//    }
//}
//
//fun main() {
//    val person = Person("Bob", 30)
//    person.introduce()
//}

/**
 * 클래스 보조 생성자 1
 */
//class Person {
//    var name: String
//    var age: Int
//
//    constructor(name: String, age: Int) {
//        this.name = name
//        this.age = age
//    }
//}
//
//fun main() {
//    val person = Person("Charlie", 35)
//    println("${person.name}, ${person.age}살")
//}

/**
 * 클래스 보조 생성자 2
 */
//class Person(val name:String){
//    var addr:String?= null
//    var tel:String?=null
//    constructor(name:String, addr:String):this(name){
//        this.addr = addr
//    }
//    constructor(name:String, addr:String, tel:String):this(name,addr){
//        this.tel = tel
//    }
//    fun printName() = print(name)
//    fun printAddr() = print(addr)
//    fun printTel() = print(tel)
//}

/**
 * 클래스의 커스텀 접근자 getter/setter
 */
//class BankAccount(val accountNumber:Int, var accountBalance:Double){
//    val fees:Double = 25.0
//    var balanceLessFees:Double
//        get(){
//            return accountBalance-fees
//        }
//        set(value){
//            accountBalance += (value-fees)
//        }
//}
//fun main() {
//    val account = BankAccount(10,1000.0)
//    account.balanceLessFees = 1000.0
//    print(account.balanceLessFees)
//}

/**
 * 클래스의 상속
 */
//open class Parent {
//    fun greet() = println("Hello from Parent")
//
//    open fun sayHello() = println("Hello")
//}
//
//class Child : Parent() {
//    override fun sayHello() = println("Hello from Child")
//}
//
//fun main() {
//    val child = Child()
//    child.greet()
//    child.sayHello()
//}

/**
 * 클래스의 생성자와 상속
 */
//open class Parent(val name: String) {
//    init {
//        println("Parent initialized with name: $name")
//    }
//}
//
//class Child(name: String, val age: Int) : Parent(name) {
//    init {
//        println("Child initialized with age: $age")
//    }
//}
//
//fun main() {
//    val child = Child("Alice", 10)
//}

/**
 * 추상 클래스
 */
//abstract class Animal {
//    abstract fun makeSound()
//
//    fun sleep() = println("Sleeping...")
//}
//
//class Dog : Animal() {
//    override fun makeSound() = println("Woof!")
//}
//
//fun main() {
//    val dog = Dog()
//    dog.makeSound()
//    dog.sleep()
//}

/**
 * 인터페이스와 클래스 상속
 */
//abstract class Vehicle {
//    abstract fun drive()
//}

//interface Flyable {
//    fun fly()
//}
//
//class Car : Vehicle() {
//    override fun drive() = println("Driving a car")
//}
//
//class Airplane : Vehicle(), Flyable {
//    override fun drive() = println("Driving an airplane on the runway")
//    override fun fly() = println("Flying an airplane")
//}
//
//fun main() {
//    val car = Car()
//    car.drive()
//
//    val airplane = Airplane()
//    airplane.drive()
//    airplane.fly()
//}

/**
 * object 클래스
 */
//object Counter {
//    var count: Int = 0
//
//    fun increment() {
//        count++
//    }
//}
//
//fun main() {
//    Counter.increment()
//    Counter.increment()
//    println(Counter.count)
//}

//open class Person(name:String, age:Int){
//    init{
//        println("name: $name, age:$age")
//    }
//    fun eat()=println("Eating food.")
//    fun talk()=println("Talking with people")
//    open fun pray() = println("Praying god.")
//}
//
//val atheist = object:Person("greenjoa", 23){
//    override fun pray() {
//        println("I don't pray. I am an atheist.")
//    }
//}
//
//fun main() {
//    atheist.eat()
//    atheist.talk()
//    atheist.pray()
//}

/**
 * Data 클래스
 */
//data class User(val name: String, val age: Int)
//
//fun main() {
//    val user1 = User("Alice", 25)
//    val user2 = User("Alice", 25)
//
//    println(user1)
//    println(user1 == user2) // true
//}

//data class User(val name: String, val age: Int){
//    override fun equals(other: Any?): Boolean {
//        if(other is User)
//            return other.name==name
//        else return false
//    }
//}
//
//fun main() {
//    val user1 = User("Alice", 25)
//    val user2 = User("Alice", 30)
//
//    println(user1)
//    println(user2)
//    println(user1 == user2) // true
//}

/**
 * Sealed Class
 */
//sealed class Result {
//    data class Success(val data: String) : Result()
//    data class Failure(val error: String) : Result()
//    object Loading : Result()
//}
//
//fun handleResult(result: Result) {
//    when (result) {
//        is Result.Success -> println("Data: ${result.data}")
//        is Result.Failure -> println("Error: ${result.error}")
//        Result.Loading -> println("Loading...")
//    }
//}
//
//fun main() {
//    val result = Result.Success("Operation Successful")
//    handleResult(result)
//}

/**
 * enum Class
 */
//enum class Direction {
//    NORTH, SOUTH, EAST, WEST
//}
//
//fun main() {
//    val direction = Direction.NORTH
//
//    when (direction) {
//        Direction.NORTH -> println("북쪽")
//        Direction.SOUTH -> println("남쪽")
//        Direction.EAST -> println("동쪽")
//        Direction.WEST -> println("서쪽")
//    }
//}

//enum class Status(val code: Int, val description: String) {
//    SUCCESS(200, "성공"),
//    ERROR(500, "오류 발생"),
//    NOT_FOUND(404, "찾을 수 없음");
//
//    fun getFullDescription(): String {
//        return "$code: $description"
//    }
//}
//
//fun main() {
//    val status = Status.SUCCESS
//    println(status.getFullDescription())
//}


/**
 * 컬렉션의 유용한 함수
 */
//fun main() {
//    val numbers = listOf(5, 2, 8, 1, 3)
//    val sortedNumbers = numbers.sorted()
//    println(sortedNumbers)
//
//    val descendingNumbers = numbers.sortedDescending()
//    println(descendingNumbers)
//}

//data class User(val name: String, val age: Int) : Comparable<User> {
//    override fun compareTo(other: User): Int {
//        return name.compareTo(other.name)
//    }
//}
//
//fun main() {
//    val users = listOf(
//        User("홍길동", 20),
//        User("이길동", 28),
//        User("고길동", 20)
//    )
//    val sortedUsers = users.sorted()
//    val descendingUsers = users.sortedDescending()
//    val sortedUsers2 = users.sortedBy { it.age }
//    val descendingUsers2 = users.sortedByDescending { it.age }
//    val sortedUsers3 = users.sortedWith(
//        compareBy<User> { it.age }
//            .thenBy { it.name }
//    )
//    val descendingUsers3 = users.sortedWith(
//        compareByDescending<User> { it.age }
//            .thenBy { it.name }
//    )
//
//    println(sortedUsers)
//    println(descendingUsers)
//    println(sortedUsers2)
//    println(descendingUsers2)
//    println(sortedUsers3)
//    println(descendingUsers3)
//}

//fun main() {
//    val words = listOf("apple", "banana", "cherry", "avocado")
//    val groupedByFirstLetter = words.groupBy { it.first() }
//    println(groupedByFirstLetter) // {a=[apple, avocado], b=[banana], c=[cherry]}
//    println(groupedByFirstLetter['a'])
//}

//fun main() {
//    val numbers = listOf(1, 2, 3, 4, 5)
//    val evenNumbers = numbers.filter { it % 2 == 0 }
//    println(evenNumbers) // [2, 4]
//}

//data class User(val name: String, val age: Int)
//
//fun main() {
//    val users = listOf(
//        User("홍길동", 20),
//        User("이길동", 28),
//        User("고길동", 20)
//    )
//    val user = users.filter { it.name=="이길동" }
//    println(user)
//}

//fun main() {
//    val fruits = listOf("Apple", "Banana", "Cherry")
//    println(fruits.contains("Banana")) // true
//    println("Cherry" in fruits) // true
//}

//data class User(val name: String, val age: Int)
//
//fun main() {
//    val users = listOf(
//        User("홍길동", 20),
//        User("이길동", 28),
//        User("고길동", 20)
//    )
//    println(users.contains(User("이길동", 28 )))
//}

//fun main() {
//    val fruits = listOf("Apple", "Banana", "Cherry")
//    for((index, fruit) in fruits.withIndex()){
//        println("Index $index: $fruit")
//    }
//}

//fun main() {
//    val fruits = listOf("Apple", "Banana", "Cherry")
//    val indexedFruits = fruits.mapIndexed {index, fruit ->
//        "Item $index: $fruit"
//    }
//    println(indexedFruits)
//}

//fun main() {
//    val fruits = listOf("Apple", "Banana", "Cherry")
//    fruits.forEachIndexed { index, fruit ->
//        println("Item $index: $fruit")
//    }
//}


/**
 * Scope 함수
 */
class Person {
    var name: String = "Unknown"
    var age: Int = 0
    override fun toString() = "Person(name=$name, age=$age)"
}

//fun main() {
//    val person = Person().apply {
//        name = "Alice"
//        age = 25
//    }
//    println(person)
//}

//fun main() {
//    val name: String? = "Alice"
//    name?.let {
//        println("이름은 $it 입니다.")
//    }
//}

//fun main() {
//    val person = Person().run {
//        this.name = "Bob"
//        this.age = 30
//        "이름은 $name, 나이는 $age 입니다."
//    }
//    println(person)
//}

//fun main() {
//    val numbers = mutableListOf(1, 2, 3)
//    numbers.also {
//        println("변경 전: $it")
//    }.add(4)
//    println("변경 후: $numbers")
//}

//fun main() {
//    val person = Person()
//    val result = with(person) {
//        this.name = "Charlie"
//        this.age = 35
//        println("$name, $age")
//        this.age + 10
//    }
//    println(person)
//    println(result)
//}
