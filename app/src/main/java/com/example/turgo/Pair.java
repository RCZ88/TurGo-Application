package com.example.turgo;

import androidx.annotation.NonNull;

public class Pair <O1, O2>{
    public final O1 one;
    public final O2 two;
    public Pair(O1 first, O2 second){
        one = first;
        two = second;
    }
    public static <A, B> Pair<A,B> newInstance(A first, B second){
        return new Pair<>(first, second);
    }

    @NonNull
    @Override
    public String toString() {
        return "<" + one + ", " + two + ">";
    }
}
