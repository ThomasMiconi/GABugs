# Bugs

A simple evolutionary simulation in which a population of agents with simple
sensors and small neural networks learn to chase food bits.

For now, the population shares a common network (clonal) and evolution is
basically a simple hillclimber (i.e. a 1+1 ES).

You need a java compiler and interpreter.  Compile with `javac World.java`,
then run with `java World`.  Be sure to press the + and - buttons to increase
or decrease the refreshing time (reduce refreshing time to 0 for maximum
speed). 


This version reaches its best performance by ~2K evaluations, which should take
a couple minutes at most (with refreshing delay set to 0). 
