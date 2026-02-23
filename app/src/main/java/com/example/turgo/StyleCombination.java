package com.example.turgo;

public class StyleCombination {
    public final boolean bold;
    public final boolean italic;
    public final boolean underline;

    public StyleCombination(boolean bold, boolean italic, boolean underline) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }
    public StyleCombination(){
        this.bold = false;
        this.italic = false;
        this.underline = false;
    }
    public static boolean same(StyleCombination s1, StyleCombination s2){
        return s1.bold == s2.bold && s1.italic == s2.italic && s1.underline == s2.underline;
    }
    public boolean rich(){
        return bold || italic || underline;
    }
}
