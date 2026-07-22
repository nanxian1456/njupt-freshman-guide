package cn.xszn.comments.service;

public class InvalidCommentException extends RuntimeException {
  public InvalidCommentException(String message) {
    super(message);
  }
}
