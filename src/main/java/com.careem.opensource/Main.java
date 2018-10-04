package com.careem.opensource;



public class Main {

  public static void main(String[] args) {
    new Reporter("/tmp","gc.log", new Parser()).start();
  }
}
