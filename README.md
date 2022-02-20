## Running

Use `sbt run` to run the application, `sbt test` to run tests.

## Configuration

You can adjust date filter by modifying `DirectFollowerExtraction.scala:22-23`. Change `DirectFollowerExtraction.scala:36-37` to use an alternative algorithm(details below).

## Assumptions

I found that the implementation and therefore the results depend on the definition of "direct follower events". The task description is not entirely clear about it. I ended up implementing 2 algorithms using slightly different assumptions:

1. The naive approach is implemented by `Trace.directFollowersSimple`, which assumes that 2 events are direct followers, if they are consecutive after sorting by start date.

2. Slightly more sophisticated approach is implemented by `Trace.directFollowers`, which assumes that 2 events are direct followers, if one of them starts before the other one. It might be that 2 events start at the same time and have the same preceeding event. In that case events form a DAG and the algorithm correctly handles this case.

Both algorithms give pretty close results.

It would probably make sense to take into account the event completion time, but I had to stop somewhere!

## Implementation notes

I have used a library to parse the CSV file, since CSV parsing is out of the scope of this task. The only other dependency is Scalatest.

The solution loads the test data to memory and is not suitable for files of much bigger size. For that, I would go with a streaming solution of some kind, that could process files of any size with a constant memory consumption. Still, I have tried to optimize things by at least using `Vector`s over `List`s to store events and traces.

`Event` contains CSV parsing logic. `Trace` has methods to aggregate events into traces and calculate direct followers for a trace. `DirectFollowerExtraction` builds a DFM given a sequence of direct followers and renders the result. `models/package.scala` contains some type aliases to ease readability.