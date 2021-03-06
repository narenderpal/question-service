package io.vertx.stackoverflow.question;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by napal on 25/06/17.
 */
public interface QuestionService {

  void addQuestion(JsonObject question, Handler<AsyncResult<JsonObject>> resultHandler);
  void retrieveQuestion(String id, Handler<AsyncResult<JsonObject>> resultHandler);
  void deleteQuestion(String id, Handler<AsyncResult<JsonObject>> resultHandler);
  void updateQuestion(String questionId,  JsonObject question, Handler<AsyncResult<JsonObject>> resultHandler);
  void retrieveAllQuestions(Handler<AsyncResult<List<JsonObject>>> resultHandler);
  void addAnswer(String questionId, JsonObject answer, Handler<AsyncResult<JsonObject>> resultHandler);
  void updateAnswer(String questionId,  JsonObject answer, Handler<AsyncResult<JsonObject>> resultHandler);
  void voteQuestion(String questionId, Integer vote, Handler<AsyncResult<JsonObject>> resultHandler);
  void voteAnswer(String questionId, String answerId, Integer vote, Handler<AsyncResult<JsonObject>> resultHandler);
}
