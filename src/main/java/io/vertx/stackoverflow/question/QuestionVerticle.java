package io.vertx.stackoverflow.question;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.stackoverflow.question.api.QuestionAPIVerticle;
import io.vertx.stackoverflow.question.impl.QuestionServiceImpl;

/**
 * Created by napal on 25/06/17.
 */
public class QuestionVerticle extends BaseVerticle {

  private QuestionService service;

  @Override
  public void start(Future<Void> future) throws Exception {
    super.start();

    //create the service instance
    service = new QuestionServiceImpl(vertx, config());

    //ProxyHelper.registerService(StoreCRUDService.class, vertx, crudService, SERVICE_ADDRESS);

    // publish service and deploy REST verticle
    // TODO no need to publish event bus service, remove this
    //publishEventBusService("question-service", "service.question", QuestionService.class)
    //  .compose(servicePublished -> deployRestVerticle(service))
    //  .setHandler(future.completer());

    // TODO : just deploy the Rest verticle here , which will initialize service discovery and publish http end point
    deployRestVerticle(service);

  }

  private Future<Void> deployRestVerticle(QuestionService service) {
    Future<String> future = Future.future();
    vertx.deployVerticle(new QuestionAPIVerticle(service),
      new DeploymentOptions().setConfig(config()),
      future.completer());
    return future.map(r -> null);
  }

}
