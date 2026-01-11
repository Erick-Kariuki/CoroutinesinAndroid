import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

//In synchronous code, only one conceptual task is in progress at a time.
//You can think of it as a sequential linear path. One task must finish completely
//before the next one is started.


fun main(args: Array<String>) {

    val time = measureTimeMillis {
        runBlocking {
            println("Weather forecast")
            printForecast()
            printTemperature()
        }
        println("Have a good day")
    }
    println("Execution time: ${time/1000} seconds")
}

suspend fun printForecast(){
    delay(1000)
    println("Sunny")
}

suspend fun printTemperature(){
    delay(1000)
    println("30\u00b0C")
}

//The code in a coroutine is invoked sequentially by default. You have to be explicit if
//you want things to run concurrently.

// Note that delay() is actually a special suspending function provided by the Kotlin coroutines library.
//Execution of the main() function will suspend (or pause) at this point, and then resume once the
//specified duration of the delay is over (one second in this case).