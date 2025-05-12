package com.example.week09.example03.model


sealed class Routes (val route: String, val isRoot : Boolean = true) {
    object Main : Routes("Main")
    object Home : Routes("Home")
    object Contacts : Routes("Contacts")
    object Favorites : Routes("Favorites")
    object User : Routes("User")
    object Login : Routes("Login", false)
    object Register : Routes("Register",false)
    object Welcome : Routes("Welcome", false)

    companion object{
        fun getRoutes(route:String):Routes{
            return when(route){
                Home.route -> Home
                Contacts.route -> Contacts
                Favorites.route -> Favorites
                Login.route -> Login
                Register.route -> Register
                Welcome.route -> Welcome
                else-> Home
            }
        }
    }
}