import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

//..........................Launch() function..........................................//

//We use the launch() function from the coroutines library to launch a new coroutine. To execute
//tasks concurrently, add multiple launch() functions to your code so that multiple
//coroutines can be in progress at the same time.


//Coroutines in Kotlin follow a key concept called structured concurrency, where your code is
//sequential by default and cooperates with an underlying event loop, unless you explicitly ask
//for concurrent execution (e.g. using launch()). The assumption is that if you call a function,
//it should finish its work completely by the time it returns regardless of how many coroutines
//it may have used in its implementation details. Even if it fails with an exception, once the
//exception is thrown, there are no more pending tasks from the function. Hence, all work is
//finished once control flow returns from the function, whether it threw an exception or
//completed its work successfully.


fun main() {
    val time = measureTimeMillis {
        runBlocking {
            println("Weather forecast")
            launch {
                printForecast2()
            }
            launch {
                printTemperature2()
            }
            println("Have a good day")
        }
    }
    println("Time taken: ${time/1000} seconds")
    println("................................................................................................")

    asyncFunction()
}

        suspend fun printForecast2() {
            delay(1000)
            println("Sunny")
        }

        suspend fun printTemperature2() {
            delay(1000)
            println("30\u00b0C")
        }

//From the above code output, you can observe that after the two new coroutines are launched for printForecast2()
//and printTemperature2(), you can proceed with the next instruction which prints Have a good day!. This
//demonstrates the "fire and forget" nature of launch(). You fire off a new coroutine with launch(), and don't
//have to worry about when its work is finished.

//Later the coroutines will complete their work, and print the remaining output statements. Once all the work
//(including all coroutines) in the body of the runBlocking() call have been completed, then runBlocking()
//returns and the program ends.





//..........................................Async() function................................//

//In the real world, you won't know how long the network requests for forecast and temperature will take.
//If you want to display a unified weather report when both tasks are done, then the current approach with
//launch() isn't sufficient. That's where async() comes in.

//We use the async() function from the coroutines library if we care about when the coroutine finishes
// and need a return value from it.

//The async() function returns an object of type Deferred, which is like a promise that the result will be
//in there when it's ready. You can access the result on the Deferred object using await(). The await() function
//ensures that each coroutine completes its work and returns its result, before we return from the function.

fun asyncFunction() {
    runBlocking {

        println("Weather forecast")
        println(getWeatherReport())
        println("Have a good day")

//        val forecast: Deferred<String> = async {
//            getForecast()
//        }
//
//        val temperature: Deferred<String> = async {
//            getTemperature()
//        }
//        println("${forecast.await()} ${temperature.await()}")

    }
}
    suspend fun getForecast(): String {
        delay(1000)
        return "Sunny"
    }

    suspend fun getTemperature(): String {
        delay(1000)
        return "35\u00b0C"
    }

//-----------------------  Parallel Decomposition ------------------------------------------------//
//Parallel decomposition involves taking a problem and breaking it into smaller subtasks that can be solved
//in parallel. When the results of the subtasks are ready, you can combine them into a final result.

//In our code, extract out the logic of the weather report from the body of runBlocking() into a single
//getWeatherReport() function that returns the combined string of Sunny 30°C.

suspend fun getWeatherReport() = coroutineScope {
    val forecast = async { getForecast() }
    val temperature = async { getTemperature() }
    "${forecast.await()} ${temperature.await()}"

}

//The output is the same, but there are some noteworthy takeaways here. As mentioned earlier, coroutineScope()
//will only return once all its work, including any coroutines it launched, have completed. In this case, both
//coroutines getForecast() and getTemperature() need to finish and return their respective results. Then the
//Sunny text and 30°C are combined and returned from the scope. This weather report of Sunny 30°C gets printed
//to the output, and the caller can proceed to the last print statement of Have a good day!.

//With coroutineScope(), even though the function is internally doing work concurrently, it appears to the
//caller as a synchronous operation because coroutineScope won't return until all work is done.

//The key insight here for structured concurrency is that you can take multiple concurrent operations and put
//it into a single synchronous operation, where concurrency is an implementation detail. The only requirement
//on the calling code is to be in a suspend function or coroutine. Other than that, the structure of the
//calling code doesn't need to take into account the concurrency details.