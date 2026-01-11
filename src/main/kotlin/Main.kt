//Concurrency involves performing multiple tasks in your app at the same time.
// For example, your app can get data from a web server or save user data on the device,
// while responding to user input events and updating the UI accordingly.


//To do work concurrently in your app, you will be using Kotlin coroutines. Coroutines
//allow the execution of a block of code to be suspended and then resumed later, so that
//other work can be done in the meantime. Coroutines make it easier to write asynchronous code,
//which means one task doesn't need to finish completely before starting the next task,
//enabling multiple tasks to run concurrently.

fun main(){
    println("Weather forecast")
    println("Sunny")
}