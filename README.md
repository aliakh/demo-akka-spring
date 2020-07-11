# Using Akka with Spring


## Introduction

In this article is explained how to integrate Akka actors into Spring _console_ and _web_ applications.


## Spring console application with Akka

Akka _ActorSystem_ can be integrated with Spring _ApplicationContext_ in three steps.

Firstly, the _SpringActorProducer_ is used to create actors by getting them as Spring beans from the _ApplicationContext_ by name (instead of creating actors from their classes by Java reflection).


```
public class SpringActorProducer implements IndirectActorProducer {

   private final ApplicationContext applicationContext;
   private final String actorBeanName;

   public SpringActorProducer(ApplicationContext applicationContext, String actorBeanName) {
       this.applicationContext = applicationContext;
       this.actorBeanName = actorBeanName;
   }

   @Override
   public Actor produce() {
       return (Actor) applicationContext.getBean(actorBeanName);
   }

   @Override
   public Class<? extends Actor> actorClass() {
       return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
   }
}
```


Secondly, an Akka _Extension_ is used to add additional functionality to the _ActorSystem_. The _SpringExtension_ uses Akka _Props_ to create actors with the _SpringActorProducer_.


```
@Component
public class SpringExtension implements Extension {

   private ApplicationContext applicationContext;

   public void initialize(ApplicationContext applicationContext) {
       this.applicationContext = applicationContext;
   }

   public Props props(String actorBeanName) {
       return Props.create(SpringActorProducer.class, applicationContext, actorBeanName);
   }
}
```


Thirdly, a Spring _@Configuration_ is used to provide the _ActorSystem_ as a Spring bean. The _ApplicaionConfiguration_ creates the _ActorSystem_ from the Akka configuration, overriding file _application.conf,_ and registers the _SpringExtension_ in it.


```
@Configuration
class ApplicationConfiguration {

   @Autowired
   private ApplicationContext applicationContext;

   @Autowired
   private SpringExtension springExtension;

   @Bean
   public ActorSystem actorSystem() {
       ActorSystem actorSystem = ActorSystem.create("demo-actor-system", akkaConfiguration());
       springExtension.initialize(applicationContext);
       return actorSystem;
   }

   @Bean
   public Config akkaConfiguration() {
       return ConfigFactory.load();
   }
}
```


The _WorkerActor_ is a stateful actor that receives and sends messages (they have to be immutable) with other actors inside the _onReceive_ method. Don't forget to use the _unhandled_ method if the received message doesn't match. Notice that actors have to be defined in the Spring _prototype_ scope.


```
@Component("workerActor")
@Scope("prototype")
public class WorkerActor extends UntypedActor {

   @Autowired
   private BusinessService businessService;

   private final CompletableFuture<Message> completableFuture;

   public WorkerActor(CompletableFuture<Message> completableFuture) {
       this.completableFuture = completableFuture;
   }

   @Override
   public void onReceive(Object message) throws Exception {
       businessService.perform(this + " " + message);

       if (message instanceof Message) {
           completableFuture.complete((Message) message);
       } else {
           unhandled(message);
       }

       getContext().stop(self());
   }
}
```


The _BusinessService_ is a simple service that is injected in the _WorkerActor_ by Spring.


```
@Service
public class BusinessService {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   public void perform(Object o) {
       logger.info("Perform: {}", o);
   }
}
```


The example application is a console Spring Boot application. A Spring Boot _CommandLineRunner_ is used to get a _WorkerActor_ from the _ActorSystem_ inside the _ApplicationContext_, to send a sequence of requests and receive a response, and finally to terminate the _ActorSystem_. Notice that the _Await.result_ method is blocking, so it should be used in very limited cases (e.g. in integration the actor-based part with the rest of the application or in the unit tests).


```
@Component
class Runner implements CommandLineRunner {

   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private ActorSystem actorSystem;

   @Autowired
   private SpringExtension springExtension;

   @Override
   public void run(String[] args) throws Exception {
       try {
           ActorRef workerActor = actorSystem.actorOf(springExtension.props("workerActor"), "worker-actor");

           workerActor.tell(new WorkerActor.Request(), null);
           workerActor.tell(new WorkerActor.Request(), null);
           workerActor.tell(new WorkerActor.Request(), null);

           FiniteDuration duration = FiniteDuration.create(1, TimeUnit.SECONDS);
           Future<Object> awaitable = Patterns.ask(workerActor, new WorkerActor.Response(), Timeout.durationToTimeout(duration));

           logger.info("Response: " + Await.result(awaitable, duration));
       } finally {
           actorSystem.terminate();
           Await.result(actorSystem.whenTerminated(), Duration.Inf());
       }
   }
}
```



## Spring Web application with Akka

In the previous section was explained how to use Akka in a Spring _console _application. The main purpose of that example was to illustrate how to get actors from Spring _ApplicationContext_. But the drawback of this example was a blocking call between the actor-based part and the rest of the application. Such usage can cease all Akka advantages in production applications. So in this section is explained how to use Akka in an asynchronous and non-blocking Spring _web_-application.

For this can be used asynchronous request processing in Spring MVC that is based on Servlet 3.0/3.1 specification. Instead of returning a value, a _@Controller_ method should return a _DeferredResult_ or a _Callable_ of the value. In multi-tier applications, a _@Service_ method should return a _future (_also known as _promise_, _delay_, or _deferred)_ - a proxy to a value that isn’t completed yet. There are some interfaces that have support for _future_ processing in their frameworks:



*   _java.util.concurrent.CompletableFuture_ (Java 8)
*   _rx.Observable_ (RxJava)
*   _org.springframework.util.concurrent.ListenableFuture_ (Spring Core)
*   _com.google.common.util.concurrent.ListenableFuture_ (Google Guava)

The main difference with the previous example application is that the _WorkerActor_ has a non-default constructor. That required refactoring of _SpringActorProducer_ and _SpringExtension_ to have the ability to pass the constructor arguments.


```
public class SpringActorProducer implements IndirectActorProducer {

   private final ApplicationContext applicationContext;
   private final String actorBeanName;
   private final Object[] args;

   public SpringActorProducer(ApplicationContext applicationContext, String actorBeanName, Object... args) {
       this.applicationContext = applicationContext;
       this.actorBeanName = actorBeanName;
       this.args = args;
   }

   @Override
   public Actor produce() {
       if (args == null) {
           return (Actor) applicationContext.getBean(actorBeanName);
       } else {
           return (Actor) applicationContext.getBean(actorBeanName, args);
       }
   }

   @Override
   public Class<? extends Actor> actorClass() {
       return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
   }
}

@Component
public class SpringExtension implements Extension {

   private ApplicationContext applicationContext;

   public void initialize(ApplicationContext applicationContext) {
       this.applicationContext = applicationContext;
   }

   public Props props(String actorBeanName) {
       return Props.create(SpringActorProducer.class, applicationContext, actorBeanName);
   }

   public Props props(String actorBeanName, Object... args) {
       return Props.create(SpringActorProducer.class, applicationContext, actorBeanName, args);
   }
}
```


The example application is a web application that is based on Spring Boot. In the _CompletableFutureService.get_ method, a _WorkerActor_ is created with an incomplete _CompletableFuture_ as a constructor parameter. Notice how the Spring _prototype_-scope actor is injected into the _singleton_-scope _CompletableFutureService_. Then a _Message_ is sent to the _WorkerActor_ with the _tell_ method.


```
@Service
public class CompletableFutureService {

   @Autowired
   private ActorSystem actorSystem;

   @Autowired
   private SpringExtension springExtension;

   public CompletableFuture<Message> get(String payload, Long id) {
       CompletableFuture<Message> completableFuture = new CompletableFuture<>();
       ActorRef workerActor = actorSystem.actorOf(springExtension.props("workerActor", completableFuture), "worker-actor");
       workerActor.tell(new Message(payload, id), null);
       return completableFuture;
   }
}
```


The _WorkerActor_ immediately completes the _CompletableFuture_, but in real applications, there can be more complicated interaction between actors. Notice that at the end of the _onReceive_ method the _WorkerActor_ is destroyed. It’s not an issue because creating and destroying actors is a cheap operation (should the actor be saved or destroyed and recreated again depends on the actors’ supervision strategy in the application).


```
@Component("workerActor")
@Scope("prototype")
public class WorkerActor extends UntypedActor {

   @Autowired
   private BusinessService businessService;

   private final CompletableFuture<Message> completableFuture;

   public WorkerActor(CompletableFuture<Message> completableFuture) {
       this.completableFuture = completableFuture;
   }

   @Override
   public void onReceive(Object message) throws Exception {
       businessService.perform(this + " " + message);

       if (message instanceof Message) {
           completableFuture.complete((Message) message);
       } else {
           unhandled(message);
       }

       getContext().stop(self());
   }
}
```


Finally, in the _DeferredResultController.getAsyncNonBlocking_ method, the _CompletableFuture_ is converted to a _DeferredResult_.


```
@RestController
public class DeferredResultController {

   private static final Long DEFERRED_RESULT_TIMEOUT = 1000L;

   private final AtomicLong id = new AtomicLong(0);

   @Autowired
   private CompletableFutureService completableFutureService;

   @RequestMapping("/async-non-blocking")
   public DeferredResult<Message> getAsyncNonBlocking() {
       DeferredResult<Message> deferredResult = new DeferredResult<>(DEFERRED_RESULT_TIMEOUT);
       CompletableFuture<Message> completableFuture = completableFutureService.get("async-non-blocking", id.getAndIncrement());
       completableFuture.whenComplete((result, error) -> {
           if (error != null) {
               deferredResult.setErrorResult(error);
           } else {
               deferredResult.setResult(result);
           }
       });
       return deferredResult;
   }
}
```



## Conclusion

Code examples are available in the [GitHub repository](https://github.com/aliakh/demo-akka-spring).
