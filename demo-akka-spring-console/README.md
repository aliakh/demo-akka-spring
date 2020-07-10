# demo-java-akka-spring

Spring Boot with Akka: Part 1

In the previous posts (parts one, two, three, four) was explained some Akka usage patterns in a Scala+Spray web-application. But a lot of regular JVM applications use much simpler tools stack. In this post is explained how to use Akka in a Java+Spring console application. (Notice that Akka has rather different Scala and Java APIs).

Akka ActorSystem can be integrated with Spring ApplicationContext in three steps.

Firstly, the SpringActorProducer is used to create actors by getting them as Spring beans from the ApplicationContext by name (instead of creation actors from their classes by Java reflection).

Secondly, an Akka Extension is used to add additional functionality to the ActorSystem. The SpringExtension uses Akka Props to create actors with the SpringActorProducer.

Thirdly, a Spring @Configuration is used to provide the ActorSystem as a Spring bean. The ApplicaionConfiguration creates the ActorSystem from the Akka configuration overriding file application.conf and registers the SpringExtension in it.

The WorkerActor is a statefull actor that receives and sends messages (they have to be immutable) with other actors inside the onReceive method. Don't forget to use the unhandled method if the received message doesn't match. Notice that actors have to be defined in Spring prototype scope.

The BusinessService is a simple service that is injected in the WorkerActor by Spring. 

The application itself is a console Spring Boot application. A Spring Boot CommandLineRunner is used to get a WorkerActor from the ActorSystem inside the ApplicationContext, to send sequence of requests and receive a response, and finally to terminate the ActorSystem.  Notice that the method Await.result is blocking, so it should be used in very limited cases (in integration the actor-based part with the rest of the application or in unit tests).
 
The application can be found in the GitHub repository.

To be continued.
