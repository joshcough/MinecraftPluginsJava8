package com.joshcough.minecraft.fp;

public class None<T> extends Option<T> {
  public boolean isDefined() { return false; }
  public T get(){ throw new RuntimeException("can't get from a None"); }
}
