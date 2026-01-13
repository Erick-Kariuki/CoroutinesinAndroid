   Coroutine concepts
  ====================
When executing work asynchronously or concurrently, there are questions that you need to answer about how the work
will be executed, how long the coroutine should exist for, what should happen if it gets canceled or fails with an
error, and more. Coroutines follow the principle of **structured concurrency**, which enforces you to answer these 
questions when you use coroutines in your code using a combination of mechanisms.

## 1. Job
When you launch a coroutine with the **launch()** function, it returns an instance of **Job**. The Job holds a handle,
or reference, to the coroutine, so you can manage its lifecycle.

`val job = launch { ... }`

**Note:** The **Deferred** object that is returned from a coroutine started with the **async()** function is a Job as well, 
and it holds the future result of the coroutine.
The job can be used to control the life cycle, or how long the coroutine lives for, such as cancelling the 
coroutine if you don't need the task anymore.

`job.cancel()`

With a job, you can check if it's active, canceled, or completed. The job is completed if the coroutine and any
coroutines that it launched have completed all of their work. Note that the coroutine could have completed due to
a different reason, such as being cancelled, or failing with an exception, but the job is still considered 
completed at that point.

Jobs also keep track of the parent-child relationship among coroutines.

### Job Hierarchy
When a coroutine launches another coroutine, the job that returns from the new coroutine is called the child of
the original parent job.

```bash
val job = launch {
    ...            

    val childJob = launch { ... }

    ...
}
```
These parent-child relationships form a job hierarchy, where each job can launch jobs, and so on.

This parent-child relationship is important because it will dictate certain behavior for the child and parent, 
and other children belonging to the same parent. You saw this behavior in the earlier examples with the weather 
program.
 - If a parent job gets cancelled, then its child jobs also get cancelled.
 - When a child job is canceled using `job.cancel()`, it terminates, but it does not cancel its parent.
 - If a job fails with an exception, it cancels its parent with that exception. This is known as propagating 
   the error upwards (to the parent, the parent's parent, and so on). .

## 2. Coroutine Scope
Coroutines are typically launched into a **CoroutineScope**. This ensures that we don't have coroutines that are 
unmanaged and get lost, which could waste resources.

`launch()` and `async()` are **extension functions** on `CoroutineScope`. Call `launch()` or `async()` on the scope to create
a new coroutine within that scope.

A `CoroutineScope` is tied to a lifecycle, which sets bounds on how long the coroutines within that scope will live.
If a scope gets cancelled, then its job is cancelled, and the cancellation of that propagates to its child jobs.
If a child job in the scope fails with an exception, then other child jobs get cancelled, the parent job gets
cancelled, and the exception gets re-thrown to the caller.

### CoroutineScope in Kotlin
In this repo, we used `runBlocking()` which provides a `CoroutineScope` for your program. We also learned how
to use `coroutineScope { }` to create a new scope within the `getWeatherReport()` function.

### CoroutineScope in Android apps
Android provides coroutine scope support in entities that have a well-defined lifecycle, such as `Activity` (`lifecycleScope`) and `ViewModel` (`viewModelScope`). Coroutines that are started within these scopes will adhere to the lifecycle of the corresponding entity, such as `Activity` or `ViewModel`.

For example, say you start a coroutine in an Activity with the provided coroutine scope called `lifecycleScope`. If the `activity` gets destroyed, then the `lifecycleScope` will get canceled and all its child coroutines will automatically get canceled too. You just need to decide if the coroutine following the `lifecycle` of the `Activity` is the behavior you want.

In the `Race Tracker Android app` you will be working on, you'll learn a way to scope your coroutines to the lifecycle of a composable.

## 3. CoroutineContext
The CoroutineContext provides information about the context in which the coroutine will be running in. The CoroutineContext is essentially a map that stores elements where each element has a unique key. These are not required fields, but here are some examples of what may be contained in a context:

 - name - name of the coroutine to uniquely identify it
 - job - controls the lifecycle of the coroutine
 - dispatcher - dispatches the work to the appropriate thread
 - exception handler - handles exceptions thrown by the code executed in the coroutine

**Note:** These are default values for the **CoroutineContext**, which will be used if you don't provide values for them:

 - "coroutine" for the coroutine name
 - no parent job
 - Dispatchers.Default for the coroutine dispatcher
 - no exception handler

Each of the elements in a context can be appended together with the `+` operator. For example, one `CoroutineContext` could be defined as follows:

```bash
Job() + Dispatchers.Main + exceptionHandler
```
Because a name is not provided, the default coroutine name is used.

Within a coroutine, if you launch a new coroutine, the child coroutine will inherit the CoroutineContext from the parent coroutine, but replace the job specifically for the coroutine that just got created. You can also override any elements that were inherited from the parent context by passing in arguments to the `launch()` or `async()` functions for the parts of the context that you want to be different.
```bash
scope.launch(Dispatchers.Default) {
    ...
}
```
You've seen the mention of dispatcher several times. Its role is to dispatch or assign the work to a thread. Let's discuss threads and dispatchers in more detail.

## Dispatcher
Coroutines use dispatchers to determine the thread to use for its execution. A **thread** can be started, does some
work (executes some code), and then terminates when there's no more work to be done.

When a user starts your app, the Android system creates a new process and a single thread of execution for your
app, which is known as the **main thread**. The main thread handles many important operations for your app 
including Android system events, drawing the UI on the screen, handling user input events, and more. As a 
result, most of the code you write for your app will likely run on the main thread.

There are two terms to understand when it comes to the threading behavior of your code: **blocking** and **non-blocking**. 
A regular function blocks the calling thread until its work is completed. That means it does not yield the calling 
thread until the work is done, so no other work can be done in the meantime. Conversely, non-blocking code yields the
calling thread until a certain condition is met, so you can do other work in the meantime. You can use an asynchronous
function to perform non-blocking work because it returns before its work is completed.

In the case of Android apps, you should only call blocking code on the main thread if it will execute fairly quickly.
The goal is to keep the main thread unblocked, so that it can execute work immediately if a new event is triggered.
This main thread is the **UI thread** for your activities and is responsible for **UI drawing** and **UI related 
events**. When there's a change on the screen, the UI needs to be redrawn. For something like an animation on the 
screen, the UI needs to be redrawn frequently so that it appears like a smooth transition. If the main thread needs 
to execute a long-running block of work, then the screen won't update as frequently and the user will see an abrupt 
transition (known as "jank") or the app may hang or be slow to respond.

Hence, we need to move any long-running work items off the main thread and handle it in a different thread. Your app
starts off with a single main thread, but you can choose to create multiple threads to perform additional work. 
These additional threads can be referred to as **worker threads**. It's perfectly fine for a long-running task to
block a worker thread for a long time, because in the meantime, the main thread is unblocked and can actively respond
to the user.

There are some built-in dispatchers that Kotlin provides:

 - **Dispatchers.Main:** Use this dispatcher to run a coroutine on the main Android thread. This dispatcher is used primarily for handling UI updates and interactions, and performing quick work.
 - **Dispatchers.IO:** This dispatcher is optimized to perform disk or network I/O outside of the main thread. For example, read from or write to files, and execute any network operations.
 - **Dispatchers.Default:** This is a default dispatcher used when calling `launch()` and `async()`, when no dispatcher is specified in their context. You can use this dispatcher to perform computationally-intensive work outside of the main thread. For example, processing a bitmap image file.

**Note:** There's also **Executor.asCoroutineDispatcher()** and **Handler.asCoroutineDispatcher()** extensions, if you need to make a **CoroutineDispatcher** from a **Handler** or **Executor** that you already have available.

Try the following example in Kotlin Playground to better understand coroutine dispatchers.

 1. Replace any code you have in Kotlin Playground with the following code:

```bash
import kotlinx.coroutines.*

fun main() {
    runBlocking {
        launch {
            delay(1000)
            println("10 results found.")
        }
        println("Loading...")
    }
}
```
 2. Now wrap the contents of the launched coroutine with a call to `withContext()` to change the `CoroutineContext`
that the coroutine is executed within, and specifically override the dispatcher. Switch to using the 
`Dispatchers.Default` (instead of `Dispatchers.Main` which is currently being used for the rest of the coroutine
code in the program).
```bash
 ...

fun main() {
    runBlocking {
        launch {
            withContext(Dispatchers.Default) {
                delay(1000)
                println("10 results found.")
            }
        }
        println("Loading...")
    }
}
 ```
Switching dispatchers is possible because `withContext()` is itself a **suspending function**. It executes the provided
block of code using a new `CoroutineContext`. The new context comes from the context of the parent job (the outer 
`launch()` block), except it overrides the dispatcher used in the parent context with the one specified here: 
`Dispatchers.Default`. This is how we are able to go from executing work with `Dispatchers.Main` to using 
`Dispatchers.Default`.

 3. Run the program. The output should be:
``
    Loading...
    10 results found.
``
 4. Add print statements to see what thread you are on by calling `Thread.currentThread().name`.
```bash
 import kotlinx.coroutines.*

fun main() {
    runBlocking {
        println("${Thread.currentThread().name} - runBlocking function")
                launch {
            println("${Thread.currentThread().name} - launch function")
            withContext(Dispatchers.Default) {
                println("${Thread.currentThread().name} - withContext function")
                delay(1000)
                println("10 results found.")
            }
            println("${Thread.currentThread().name} - end of launch function")
        }
        println("Loading...")
    }
} 
 ```
 5. Run the program. The output should be:

```
    main @coroutine#1 - runBlocking function
    Loading...
    main @coroutine#2 - launch function
    DefaultDispatcher-worker-1 @coroutine#2 - withContext function
    10 results found.
    main @coroutine#2 - end of launch function
```

From this output, you can observe that most of the code is executed in coroutines on the **main thread**. However, for the portion of your code in the `withContext(Dispatchers.Default)` block, that is executed in a coroutine on a Default Dispatcher worker thread (which is not the main thread). Notice that after withContext() returns, the coroutine returns to running on the main thread (as evidenced by output statement: main @coroutine#2 - end of launch function). This example demonstrates that you can switch the dispatcher by modifying the context that is used for the coroutine.

If you have coroutines that were started on the **main thread**, and you want to move certain operations off the main thread, then you can use `withContext` to switch the dispatcher being used for that work. Choose appropriately from the available dispatchers: `Main`, `Default`, and `IO` depending on the type of operation it is. Then that work can be assigned to a thread (or group of threads called a thread pool) designated for that purpose. Coroutines can suspend themselves, and the dispatcher also influences how they resume.

Note that when working with popular libraries like `Room` and `Retrofit`, you may not have to explicitly switch the dispatcher yourself if the library code already handles doing this work using an alternative coroutine dispatcher like `Dispatchers.IO`. In those cases, the suspend functions that those libraries reveal may already be **main-safe** and can be called from a coroutine running on the **main thread**. The library itself will handle switching the dispatcher to one that uses worker threads.

Now we've got a high-level overview of the important parts of coroutines and the role that **`CoroutineScope`**, **`CoroutineContext`**, **`CoroutineDispatcher`**, and Jobs play in shaping the lifecycle and behavior of a coroutine.


