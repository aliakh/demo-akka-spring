package demo.di;

import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringExtension implements Extension {

	@Autowired
	private ApplicationContext applicationContext;

    public Props props(String actorBeanName, Object... args) {
        return Props.create(SpringActorProducer.class, applicationContext, actorBeanName, args);
	}
}