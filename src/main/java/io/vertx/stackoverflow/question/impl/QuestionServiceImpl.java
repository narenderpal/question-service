package io.vertx.stackoverflow.question.impl;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.stackoverflow.question.QuestionService;

/**
 * Created by napal on 25/06/17.
 */
public class QuestionServiceImpl implements QuestionService {

  private static final String COLLECTION = "questions";
  private final MongoClient mongoClient;

  public QuestionServiceImpl(Vertx vertx, JsonObject config) {
    this.mongoClient = MongoClient.createShared(vertx, getMongoDbConfig(config));
  }

  private JsonObject getMongoDbConfig(JsonObject config) {
    String uri = config.getString("mongo_uri");
    if (uri == null) {
      // running locally using local mongo db
      //uri = "mongodb://localhost:27017";

      // using mongo db docker container
      //uri = "mongodb://mongo:27017";

      // Use mongo db as a cloud service
      uri = "mongodb://10.128.0.6:27017";
    }
    String dbName = config.getString("mongo_db");
    if (dbName == null) {
      dbName = "cmad";
    }

    JsonObject mongoConfig = new JsonObject()
      .put("connection_string", uri)
      .put("db_name", dbName);

    return mongoConfig;
  }

  @Override
  public void addQuestion(JsonObject jsonObject, Handler<AsyncResult<Void>> resultHandler) {
    mongoClient.save(COLLECTION, jsonObject,
      asyncResult -> {
        if (asyncResult.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(asyncResult.cause()));
        }
      }
    );
  }

  @Override
  public void retrieveQuestion(String id, Handler<AsyncResult<JsonObject>> resultHandler) {

    JsonObject query = new JsonObject().put("_id", id);
    mongoClient.findOne(COLLECTION, query, null,
      asyncResult -> {
        if (asyncResult.succeeded()) {
          if (asyncResult.result() != null) {
            JsonObject question = asyncResult.result();
            System.out.println("retrieveQuestion :" + question.toString());

            resultHandler.handle(Future.succeededFuture());
          } else {
            System.out.println("retrieveQuestion result is empty:");
          }
        } else {
          resultHandler.handle(Future.failedFuture(asyncResult.cause()));
        }
      });
  }

  @Override
  public void deleteQuestion(String id, Handler<AsyncResult<JsonObject>> resultHandler) {
    System.out.println("Entered deleteUser " + id);
    // TODO : check why api returns 404 "message": "not_found" even after deleting question
    JsonObject query = new JsonObject().put("_id", id);
    mongoClient.removeDocument(COLLECTION, query,
      asyncResult -> {
        if (asyncResult.succeeded()) {
            System.out.println("Question deleted successfully :" + id);
            resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(asyncResult.cause()));
        }
      });
  }

  @Override
  public void retrieveAllQuestions(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
    System.out.println("start retrieveAllQuestions...");
    mongoClient.find(COLLECTION, new JsonObject(),
      asyncResult -> {
        if (asyncResult.succeeded()) {
          if (asyncResult.result() != null) {
            List<JsonObject> questions = asyncResult.result();
            System.out.println("retrieveAllQuestions :" + questions.toArray().toString());
            resultHandler.handle(Future.succeededFuture(asyncResult.result()));
          } else {
            System.out.println("retrieveAllQuestions result is empty:");
          }

        } else {
          System.out.println("retrieveAllQuestions failed to get result");
          resultHandler.handle(Future.failedFuture(asyncResult.cause()));
        }
      });
  }

  @Override
  public void addAnswer(String questionId, JsonObject answerJson, Handler<AsyncResult<Void>> resultHandler) {

    JsonObject query = new JsonObject().put("_id", questionId);
    JsonObject update = new JsonObject().put("$push", new JsonObject()
      .put("answers", answerJson));

    mongoClient.findOneAndUpdate(COLLECTION, query, update,
      asyncResult -> {
        if (asyncResult.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(asyncResult.cause()));
        }
      }
    );
  }

  @Override
  public void updateAnswer(String questionId, JsonObject answerJson, Handler<AsyncResult<Void>> resultHandler) {

    JsonObject query = new JsonObject().put("_id", questionId);
    JsonObject update = new JsonObject().put("$push", new JsonObject()
      .put("answers", answerJson));

    mongoClient.findOneAndUpdate(COLLECTION, query, update,
      asyncResult -> {
        if (asyncResult.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(asyncResult.cause()));
        }
      }
    );
  }

  @Override
  public void voteQuestion(String questionId, Integer vote, Handler<AsyncResult<Void>> resultHandler) {
    JsonObject query = new JsonObject().put("_id", questionId);
    mongoClient.findOne(COLLECTION, query, null,
      asyncResult -> {
        if (asyncResult.succeeded()) {
          if (asyncResult.result() == null) {
            JsonObject question = asyncResult.result();
            if (vote > 0) {
              question.put("upVote", question.getInteger("upVote") + vote);
            } else {
              question.put("downVote", question.getInteger("downVote") + vote);
            }

            mongoClient.findOneAndUpdate(COLLECTION, query, question,
              asyncResult1 -> {
                if (asyncResult1.succeeded()) {
                  resultHandler.handle(Future.succeededFuture());
                } else {
                  resultHandler.handle(Future.failedFuture(asyncResult1.cause()));
                }
              });
          }
        } else {
          resultHandler.handle(Future.failedFuture(asyncResult.cause()));
        }
      });

  }

  @Override
  public void voteAnswer(String questionId, String answerId, Integer vote, Handler<AsyncResult<Void>> resultHandler) {
    JsonObject query = new JsonObject().put("_id", questionId);
    mongoClient.findOne(COLLECTION, query, null,
      asyncResult -> {
        if (asyncResult.succeeded()) {
          if (asyncResult.result() == null) {
            JsonObject question = asyncResult.result();
            JsonArray answers = question.getJsonArray("answers");
            JsonObject answer = null;
            for (Object obj : answers) {
              answer = new JsonObject(obj.toString());
              if (answer != null && answer.getString("_id").equals(answerId)) {
                if (vote > 0) {
                  answer.put("upVote", answer.getInteger("upVote") + vote);
                } else {
                  answer.put("downVote", answer.getInteger("downVote") + vote);
                }
                updateAnswer(questionId, answer, resultHandler);
                break;
              }
            }

            if (answer == null) {
              resultHandler.handle(Future.failedFuture(asyncResult.cause()));
            }
          }
        } else {
          resultHandler.handle(Future.failedFuture(asyncResult.cause()));
        }
      });
  }
}
