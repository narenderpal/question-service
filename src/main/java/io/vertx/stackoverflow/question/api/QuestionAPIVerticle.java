package io.vertx.stackoverflow.question.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.stackoverflow.question.BaseVerticle;
import io.vertx.stackoverflow.question.QuestionService;
import org.bson.types.ObjectId;

/**
 * Created by napal on 25/06/17.
 */
public class QuestionAPIVerticle extends BaseVerticle {

  private static final String ADD_QUESTION = "/question";
  private static final String RETRIEVE_QUESTION = "/question/:id";
  private static final String DELETE_QUESTION = "/question/:id";
  private static final String RETRIEVE_ALL_QUESTIONS = "/question";
  private static final String ADD_ANSWER = "/question/:id/answer";
  private static final String UPDATE_ANSWER = "/question/:qid/answer/:aid";
  private static final String VOTE_QUESTION = "/question/:id/vote";
  private static final String VOTE_ANSWER = "/question/:qid/answer/:aid/vote";


  private final QuestionService service;

  public QuestionAPIVerticle(QuestionService questionService) {
    this.service = questionService;
  }

  @Override
  public void start(Future<Void> future) throws Exception {
    super.start();

    final Router router = Router.router(vertx);
    // add body handler
    router.route().handler(BodyHandler.create());

    //add api route handler
    router.post(ADD_QUESTION).handler(this:: addQuestion);
    router.get(RETRIEVE_QUESTION).handler(this:: retrieveQuestion);
    router.delete(DELETE_QUESTION).handler(this:: deleteQuestion);
    router.get(RETRIEVE_ALL_QUESTIONS).handler(this:: retrieveAllQuestions);
    router.post(ADD_ANSWER).handler(this:: addAnswer);
    router.put(UPDATE_ANSWER).handler(this:: updateAnswer);
    router.put(VOTE_QUESTION).handler(this:: voteQuestion);
    router.put(VOTE_ANSWER).handler(this:: voteAnswer);

    // http server host and port
    String host = config().getString("question.service.http.address", "0.0.0.0");
    int port = config().getInteger("question.service.http.port", 8080);

    // create HTTP server and publish REST service
    // TODO : Just create and start http server... for cloud deployment service discovery and publish endpoint is not needed
    createHttpServer(router, host, port)
      //.compose(serverCreated -> publishHttpEndpoint(SERVICE_NAME, host, port))
      .setHandler(future.completer());

    /*
    vertx.createHttpServer()
      .requestHandler(router::accept).listen(port);

    ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
    //A service Record is an object that describes a service published by a service provider.
    // It contains a name, some metadata, a location object (describing where is the service).
    // This record is the only object shared by the provider (having published it) and the consumer (retrieve it when doing a lookup).
    // When you run your service in a container or on the cloud, it may not know its public IP and public port,
    // so the publication must be done by another entity having this info. Generally it’s a bridge.


    discovery.publish(HttpEndpoint.createRecord("library", "localhost", 8080, "/titles"), ar -> {
      if (ar.succeeded()) {
        System.out.println("Server Ready!");

      } else {
        System.out.println("Unable to start " + ar.cause().getMessage());
      }
    });*/
  }

  private void addQuestion(RoutingContext context) {
    System.out.println("body:" + context.getBodyAsJson().toString());
    JsonObject jsonObject = context.getBodyAsJson();
    if (jsonObject != null) {
      JsonObject result = new JsonObject().put("message", "question added successfully");
      service.addQuestion(jsonObject, resultVoidHandler(context, result));
    } else {
      badRequest(context, new IllegalStateException("Question is not valid"));
    }
  }

  private void retrieveQuestion(RoutingContext context) {
    String id = context.request().getParam("id");
    service.retrieveQuestion(id, resultHandlerNonEmpty(context));
  }

  private void deleteQuestion(RoutingContext context) {
    String id = context.request().getParam("id");
    service.deleteQuestion(id, resultHandlerNonEmpty(context));
  }

  private void retrieveAllQuestions(RoutingContext context) {
    System.out.println("received req  retrieveAllQuestions...");
    service.retrieveAllQuestions(resultHandlerNonEmpty(context));
    System.out.println("completed req retrieveAllQuestions...");
  }

  private void addAnswer(RoutingContext context) {
    String id = context.pathParams().get("id");
    JsonObject answerJson = context.getBodyAsJson();
    answerJson.put("_id", new ObjectId().toHexString());

    System.out.println("question id :" + id);
    System.out.println("answer:" + answerJson.toString());

   if (answerJson != null) {
      JsonObject result = new JsonObject().put("message", "Answer for a question added successfully");
      service.addAnswer(id, answerJson, resultVoidHandler(context, result));
    } else {
      badRequest(context, new IllegalStateException("Answer is not valid"));
    }
  }

  private void updateAnswer(RoutingContext context) {
    String qid = context.pathParams().get("qid");
    String aid = context.pathParams().get("aid");

    JsonObject answerJson = context.getBodyAsJson();
    answerJson.put("_id", aid);

    System.out.println("question id :" + qid);
    System.out.println("answer id :" + aid);

    System.out.println("answer:" + answerJson.toString());

    if (answerJson != null) {
      JsonObject result = new JsonObject().put("message", "Answer for a question updated successfully");
      service.updateAnswer(qid, answerJson, resultVoidHandler(context, result));
    } else {
      badRequest(context, new IllegalStateException("Answer is not valid"));
    }
  }

  private void voteAnswer(RoutingContext context) {
    String qid = context.pathParams().get("qid");
    String aid = context.pathParams().get("aid");

    JsonObject jsonObject = context.getBodyAsJson();
    Integer vote = jsonObject.getInteger("count");

    System.out.println("question id :" + qid);
    System.out.println("answer id :" + aid);
    System.out.println("vote :" + vote);

    if (vote != null) {
      JsonObject result = new JsonObject().put("message", "Vote for answer updated successfully");
      service.voteAnswer(qid, aid, vote, resultVoidHandler(context, result));
    } else {
      badRequest(context, new IllegalStateException("Answer is not valid"));
    }
  }

  private void voteQuestion(RoutingContext context) {
    String qid = context.pathParams().get("qid");

    JsonObject jsonObject = context.getBodyAsJson();
    Integer vote = jsonObject.getInteger("count");
    System.out.println("question id :" + qid);
    System.out.println("vote :" + vote);

    if (vote != null) {
      JsonObject result = new JsonObject().put("message", "Vote for question updated successfully");
      service.voteQuestion(qid, vote, resultVoidHandler(context, result));
    } else {
      badRequest(context, new IllegalStateException("Request is not valid"));
    }
  }

  protected Future<Void> createHttpServer(Router router, String host, int port) {
    Future<HttpServer> httpServerFuture = Future.future();
    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(port, host, httpServerFuture.completer());
    return httpServerFuture.map(r -> null);
  }

  protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, JsonObject result) {
    return resultVoidHandler(context, result, 200);
  }

  protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, JsonObject result, int status) {
    return ar -> {
      if (ar.succeeded()) {
        context.response()
          .setStatusCode(status == 0 ? 200 : status)
          .putHeader("content-type", "application/json")
          .end(result.encodePrettily());
      } else {
        internalError(context, ar.cause());
        ar.cause().printStackTrace();
      }
    };
  }

  protected Handler<AsyncResult<Void>> deleteResultHandler(RoutingContext context) {
    return res -> {
      if (res.succeeded()) {
        context.response().setStatusCode(204)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("message", "delete_success").encodePrettily());
      } else {
        internalError(context, res.cause());
        res.cause().printStackTrace();
      }
    };
  }

  protected <T> Handler<AsyncResult<T>> resultHandlerNonEmpty(RoutingContext context) {
    return ar -> {
      if (ar.succeeded()) {
        T res = ar.result();
        if (res == null) {
          notFound(context);
        } else {
          context.response()
            .putHeader("content-type", "application/json")
            .end(res.toString());
        }
      } else {
        internalError(context, ar.cause());
        ar.cause().printStackTrace();
      }
    };
  }

  // error handler api

  protected void internalError(RoutingContext context, Throwable ex) {
    context.response().setStatusCode(500)
      .putHeader("content-type", "application/json")
      .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
  }

  protected void badRequest(RoutingContext context, Throwable ex) {
    context.response().setStatusCode(400)
      .putHeader("content-type", "application/json")
      .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
  }

  protected void notFound(RoutingContext context) {
    context.response().setStatusCode(404)
      .putHeader("content-type", "application/json")
      .end(new JsonObject().put("message", "not_found").encodePrettily());
  }

}
