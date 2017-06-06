# Bugs

A simple evolutionary simulation in which a population of agents with simple
sensors and small recurrent neural networks (20 neurons) learn to chase bugs.

For now, the population shares a common network (clonal). The evolutionary
algorithm is a simple evolutionary strategy (technically a 4+16 ES).

You need a java compiler and interpreter.  Compile with `javac World.java`,
then run with `java World`.  Be sure to press the + and - buttons to increase
or decrease the refreshing time (reduce refreshing time to 0 for maximum
speed). Each generation consists of 20 evaluations. Performance starts to
increase almost immediately and keeps increasing over at least the first 150
generations.


