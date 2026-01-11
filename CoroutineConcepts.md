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

```bash theme
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

### CoroutineScope in Kotlin Playground
In this codelab, you used `runBlocking()` which provides a `CoroutineScope` for your program. You also learned how
to use `coroutineScope { }` to create a new scope within the `getWeatherReport()` function.

### CoroutineScope in Android apps
Android provides coroutine scope support in entities that have a well-defined lifecycle, such as `Activity` (`lifecycleScope`) and `ViewModel` (`viewModelScope`). Coroutines that are started within these scopes will adhere to the lifecycle of the corresponding entity, such as `Activity` or `ViewModel`.

For example, say you start a coroutine in an Activity with the provided coroutine scope called `lifecycleScope`. If the `activity` gets destroyed, then the `lifecycleScope` will get canceled and all its child coroutines will automatically get canceled too. You just need to decide if the coroutine following the `lifecycle` of the `Activity` is the behavior you want.

In the `Race Tracker Android app` you will be working on, you'll learn a way to scope your coroutines to the lifecycle of a composable.

## 3. CoroutineContext