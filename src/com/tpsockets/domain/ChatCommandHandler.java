package com.tpsockets.domain;

@FunctionalInterface
public interface ChatCommandHandler {
  String handle(String arguments);
}
