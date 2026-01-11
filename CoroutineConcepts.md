   Coroutine concepts
  ====================
When executing work asynchronously or concurrently, there are questions that you need to answer about how the work
will be executed, how long the coroutine should exist for, what should happen if it gets canceled or fails with an
error, and more. Coroutines follow the principle of **structured concurrency**, which enforces you to answer these 
questions when you use coroutines in your code using a combination of mechanisms.

Job
===
When you launch a coroutine with the **launch()** function, it returns an instance of **Job**. The Job holds a handle,
or reference, to the coroutine, so you can manage its lifecycle.

`val job = launch { ... }`
**Note:** The **Deferred** object that is returned from a coroutine started with the **async()** function is a Job as well, 
and it holds the future result of the coroutine.
