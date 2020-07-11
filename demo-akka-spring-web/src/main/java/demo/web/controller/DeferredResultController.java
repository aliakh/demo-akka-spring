package demo.web.controller;

import demo.web.model.Message;
import demo.web.service.CompletableFutureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

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
