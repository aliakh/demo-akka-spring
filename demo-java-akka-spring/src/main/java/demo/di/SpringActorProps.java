package demo.di;

import akka.actor.Actor;
import akka.actor.Props;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringActorProps {

	@Autowired
	private ApplicationContext applicationContext;

	public Props props(String actorBeanName, Object... args) {
		return Props.create(SpringActorNameProducer.class, applicationContext, actorBeanName, args);
	}

	public Props props(Class<? extends Actor> actorClass, Object... args) {
		return Props.create(SpringActorClassProducer.class, applicationContext, actorClass, args);
	}
}