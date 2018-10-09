package com.careem.opensource;

import com.google.common.collect.EvictingQueue;

public class Cache {

  private EvictingQueue data;

  public Cache() {
    this.data = EvictingQueue.create(10000);
  }

  public void addItem(GcData gcData) {
    data.add(gcData);
  }

  public EvictingQueue getData() {
    return data;
  }

  public void setData(EvictingQueue data) {
    this.data = data;
  }
}
