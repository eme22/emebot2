package com.eme22.bolo.backup;

import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import lombok.Getter;
@Getter
public class Tuple<T1, T2> implements Iterable<Object>, Serializable {
   private static final long serialVersionUID = -3518082018884860684L;
   @NotNull
   final T1 t1;
   @NotNull
   final T2 t2;

   Tuple(T1 t1, T2 t2) {
      this.t1 = Objects.requireNonNull(t1, "t1");
      this.t2 = Objects.requireNonNull(t2, "t2");
   }

    public <R> Tuple<R, T2> mapT1(Function<T1, R> mapper) {
      return new Tuple<>(mapper.apply(this.t1), this.t2);
   }

   public <R> Tuple<T1, R> mapT2(Function<T2, R> mapper) {
      return new Tuple<>(this.t1, mapper.apply(this.t2));
   }

   @Nullable
   public Object get(int index) {
      switch (index) {
         case 0:
            return this.t1;
         case 1:
            return this.t2;
         default:
            return null;
      }
   }

   public List<Object> toList() {
      return Arrays.asList(this.toArray());
   }

   public Object[] toArray() {
      return new Object[]{this.t1, this.t2};
   }

   @Override
   public Iterator<Object> iterator() {
      return Collections.unmodifiableList(this.toList()).iterator();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Tuple<?, ?> tuple2 = (Tuple<?, ?>)o;
         return this.t1.equals(tuple2.t1) && this.t2.equals(tuple2.t2);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.size();
      result = 31 * result + this.t1.hashCode();
      return 31 * result + this.t2.hashCode();
   }

   public int size() {
      return 2;
   }

   @Override
   public final String toString() {
      return tupleStringRepresentation(this.toArray()).insert(0, '[').append(']').toString();
   }

   private static StringBuilder tupleStringRepresentation(Object... values) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < values.length; i++) {
         Object t = values[i];
         if (i != 0) {
            sb.append(',');
         }

         if (t != null) {
            sb.append(t);
         }
      }

      return sb;
   }
}
