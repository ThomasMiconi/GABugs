# Evolving reinforcement learning in a foraging agent


## Task description

An agent has access to two kinds of "preys". One kind of prey is food, while the
other kind is poisonous and must be avoided. However, the agent does not know
which is which. Furthermore, the prey types switch during each episode (food
becomes poison and vice versa).

![Bugs UI](https://github.com/ThomasMiconi/Bugs/blob/master/World.gif)

The agent is controlled by a simple recurrent neural network, with two
actuators (speed and heading) and six sensors (one for each type of prey on the
left and right, one for 'pain' and one for 'yum'). It can detect each prey type
on either side of its body (with intensity inversely proportional to distance),
and can  also detect whether whatever it just ate is food ('yum') or poison
('pain'). From this, it must learn the relevant associations, chase the "good"
type of prey and void the "wrong" type, and adapt when they switch.

## Evolution

The evolutionary algorithm is a simple real-valued genetic algorithm (inspired
by Karl Sims): Evaluate each agent in turn by letting them interact with the
environment for 10,000 timesteps and calculate their total score (simply the
total number of food minus poison eaten). When the entire population has been
evaluated, select the 20% best, and replenish the rest of the population with
mutated copies of these 20%. Mutation occurs by adding a Cauchy-distributed
number to 5% of the network's weights. There is no crossover and parents for
each new individual are chosen randomly from the 20%, inorder to maximize
exploration. Another important aspect is that we disallow direct connections
from input sensors to actuators. This forces the sensors to take input from
other neurons, which seems to facilitate the discovery of the relevant
computations.

## Results

It turns out that there are two main strategies to solve this problem: a workable, but suboptimal strategy, and an optimal strategy. 

The suboptimal
strategy is to follow one type of prey, and if too much pain is detected,
abandon any chasing and simply spin around; while spinning (since you will randomly catch some preys),if a lot of 'yum' is detected,
resume the chasing of the preferred prey type.

This strategy works because while spinning, you get random (and thus roughly
equal) amounts of poison and food, while when chasing you will get a lot more
preys of your preferred type. The pain/'yum' detectors can tell you whether it
is currently better to chase or spin, Importantly, you don't need to switch
your preferred prey - just whether you chase it or not. As a result, this
strategy is simple, and evolves readily in every run.

The optimal strategy is simply to switch the type of prey you are chasing
depending on pain/'yum' inputs. This strategy yields much higher scores, but is
more complex to implement (it requires a true 'cognitive switch' rather than
simply a brake on motor outputs) and thus harder to evolve.

With default parameters, the program evolves the optimal strategy within 1000
generations.

## How to run

You need a Java interpreter.  
Simply run  `java World VISUAL 0`.  This will start the genetic algorithm but will not produce any graphical output. After each generation, the current best
agent will be stored in a file called 'bestpopXXX.txt' (wher 'XXX' is a bunch of
parameter values). To visualize
this agent, run `java World FILENAME bestpopXXX.txt`. This will start the
graphical interface and allow you to observe the agent's behavior.
Be sure to press the + and - buttons to increase
or decrease the refreshing time (reduce refreshing time to 0 for maximum
speed).

The code is written in simple Java to encourage tinkering, so fire up your
editor and code up something cool!

To recompile the Java bytecode, simply run `javac World.java`. If
you modify any other Java source file than World.java, you need to recompile
them explicitly too (or alternatively, just run `rm *.class; javac World.java`
to force a full rebuild). 


## Related work

It is well-known that suitably trained recurrent networks can perform
reinforcement learning - that is, learning from sparse, delayed rewards (for
recent work on this, see [Wang et al.  2017](https://arxiv.org/abs/1611.05763),
[Duan et al.  2016](https://arxiv.org/abs/1611.02779)). Evolving recurrent
networks to do this has a long history (see [Blynel and Floreano
2003](https://link.springer.com/chapter/10.1007/3-540-36605-9_54); [Yamauchi and
Beer 1994](http://dl.acm.org/citation.cfm?id=189951)). I haven't seen anything
about using this method in a foraging task, but I'd be surprised if it hasn't
been done. 

