# akka-parent-npe
MWE for a NullPointerException bug in Akka

## High-level description
When a supervising actor dies, `actorRef`s pointing to that actor's children will not be able to deliver messages,
but should at the least route such messages to the dead-letter mailbox. Instead, some such `actorRef`s are nulled,
leading to `NullPointerException`s.

## Running
To compile and run against Scala 2.12,
```shell
sbt run
```

To compile and run against Scala 2.13,

```shell
sbt "++ 2.13.4 run"
```