//An exception is an unexpected event that happens during execution of your code. You should implement
//appropriate ways of handling these exceptions, to prevent your app from crashing and impacting the user
//experience negatively.

//What happens when one of your coroutines fails with an exception? Within one of the suspending functions,
//intentionally throw an exception to see what the effect would be. This simulates that an unexpected error
//happened when fetching data from the server, which is plausible.

import kotlinx.coroutines.*

fun main() {
    runBlocking {
        println("Weather forecast")
        println(getWeatherReport2())
        println("Have a good day!")
    }
}

suspend fun getWeatherReport2() = coroutineScope {
    val forecast = async { getForecast2() }
    val temperature = async {
       //try {
           getTemperature2()
//       }catch (e: AssertionError){
//           println("Caught exception: $e")
//           "(No temperature found)"
//       }
    }

    delay(200)
    temperature.cancel()
    forecast.await() //${temperature.await()}"
}

suspend fun getForecast2(): String {
    delay(1000)
    return "Sunny"
}

suspend fun getTemperature2(): String {
    delay(1000)
    //throw AssertionError("Invalid temperature")
    return "30\u00b0C"
}

//To understand this behavior, you'll need to know that there is a parent-child relationship among coroutines.
//You can launch a coroutine (known as the child) from another coroutine (parent). As you launch more
//coroutines from those coroutines, you can build up a whole hierarchy of coroutines.

//The coroutine executing getTemperature() and the coroutine executing getForecast() are child coroutines of the
//same parent coroutine.The behavior you're seeing with exceptions in coroutines is due to structured concurrency.
//When one of the child coroutines fails with an exception, it gets propagated upwards. The parent coroutine is
//canceled, which in turn cancels any other child coroutines (e.g. the coroutine running getForecast() in this
//case). Lastly, the error gets propagated upwards and the program crashes with the AssertionError.


//----------------------------- Try-catch exceptions ------------------------------------------------------//

//If you know that certain parts of your code can possibly throw an exception, then you can surround that code
//with a try-catch block. You can catch the exception and handle it more gracefully in your app, such as by
//showing the user a helpful error message.
//In our example Within the runBlocking() function, we add a try-catch block around the code that calls
// getWeatherReport().


//------------------------------- Cancellation -----------------------------------------------------//

//A similar topic to exceptions is cancellation of coroutines. This scenario is typically user-driven when an
//event has caused the app to cancel work that it had previously started.
//For example, say that the user has selected a preference in the app that they no longer want to see temperature
//values in the app. They only want to know the weather forecast (e.g. Sunny), but not the exact temperature.
//Hence, cancel the coroutine that is currently getting the temperature data.