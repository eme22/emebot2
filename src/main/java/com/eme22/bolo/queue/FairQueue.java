package com.eme22.bolo.queue;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Generated;

@Slf4j
public class FairQueue<T extends Queueable> {
   @Generated
   
   private final List<T> list = new ArrayList<>();
   private final Set<String> set = new HashSet<>();

   public synchronized int add(T item) {
      int lastIndex = this.list.size() - 1;

      while (lastIndex > -1 && !Objects.equals(this.list.get(lastIndex).getIdentifier(), item.getIdentifier())) {
         lastIndex--;
      }

      lastIndex++;
      this.set.clear();

      while (lastIndex < this.list.size() && !this.set.contains(this.list.get(lastIndex).getIdentifier())) {
         this.set.add(this.list.get(lastIndex).getIdentifier());
         lastIndex++;
      }

      this.list.add(lastIndex, item);
      return lastIndex;
   }

   public synchronized void addAt(int index, T item) {
      if (index >= this.list.size()) {
         this.list.add(item);
      } else {
         this.list.add(index, item);
      }
   }

   public synchronized int size() {
      return this.list.size();
   }

   public synchronized T pull() {
      return this.list.remove(0);
   }

   public synchronized boolean isEmpty() {
      return this.list.isEmpty();
   }

   public synchronized List<T> getList() {
      return new ArrayList<>(this.list);
   }

   public synchronized T get(int index) {
      return this.list.get(index);
   }

   public synchronized T remove(int index) {
      return this.list.remove(index);
   }

   public synchronized int removeAll(String identifier) {
      int count = 0;

      for (int i = this.list.size() - 1; i >= 0; i--) {
         if (Objects.equals(this.list.get(i).getIdentifier(), identifier)) {
            this.list.remove(i);
            count++;
         }
      }

      return count;
   }

   public synchronized int removeAllFrom(String userId) {
      return removeAll(userId);
   }

   public synchronized void clear() {
      this.list.clear();
   }

   public synchronized int shuffle(String identifier) {
      List<Integer> iset = new ArrayList<>();

      for (int i = 0; i < this.list.size(); i++) {
         if (Objects.equals(this.list.get(i).getIdentifier(), identifier)) {
            iset.add(i);
         }
      }

      for (int j = 0; j < iset.size(); j++) {
         int first = iset.get(j);
         int second = iset.get((int)(Math.random() * iset.size()));
         T temp = this.list.get(first);
         this.list.set(first, this.list.get(second));
         this.list.set(second, temp);
      }

      return iset.size();
   }

   public synchronized void shuffle() {
      Collections.shuffle(this.list);
   }

   public synchronized void skip(int number) {
      for (int i = 0; i < number; i++) {
         this.list.remove(0);
      }
   }

   public synchronized T moveItem(int from, int to) {
      T item = this.list.remove(from);
      this.list.add(to, item);
      return item;
   }

   public synchronized int shuffleMy(long identifier) {
      return shuffle(String.valueOf(identifier));
   }
}

