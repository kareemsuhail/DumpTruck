package com.careem.opensource;

import com.google.common.collect.EvictingQueue;

public class Cache {

  private EvictingQueue data;

  public Cache(int size) {
    this.data = EvictingQueue.create(size);
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
