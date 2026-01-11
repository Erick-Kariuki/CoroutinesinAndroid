 **Concurrency and Coroutines in Kotlin**
 This repo contains different projects that served as a great aid in understanding of concurrency in kotlin language.
 Understanding concurrency is a critical skill for android development, as it enables development of applications 
 with greater user experience.
 
*Concurrency* involves performing multiple tasks in your app at the same time. For example, your app can get data
from a web server or save user data on the device, while responding to user input events and updating 
the UI accordingly.
To do work concurrently in an app, we use Kotlin *coroutines*. Coroutines allow the execution of a block of code to
be suspended and then resumed later, so that other work can be done in the meantime. Coroutines make it easier to
write *asynchronous* code, which means one task doesn't need to finish completely before starting the next task, 
enabling multiple tasks to run concurrently.

Through this repo, I was able to learn:
 - How Kotlin coroutines can simplify asynchronous programming
 - The purpose of structured concurrency and why it matters