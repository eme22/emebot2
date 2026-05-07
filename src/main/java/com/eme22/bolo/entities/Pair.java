package com.eme22.bolo.entities;

public class Pair<K, V> {
   private final K key;
   private final V value;

   public Pair(K key, V value) {
      this.key = key;
      this.value = value;
   }

   public K getKey() {
      return this.key;
   }

   public V getValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Pair user = (Pair)o;
         return this.key.equals(user.getKey()) && this.value.equals(user.getValue());
      } else {
         return false;
      }
   }
}
