package com.mnsoo.dividends.model.constants;

public enum Month {

    JAN("Jan", 1),
    FEB("Feb", 2),
    MAR("Mar", 3),
    APR("Apr", 4),
    MAY("May", 5),
    JUN("Jun", 6),
    JUL("Jul", 7),
    AUG("Aug", 8),
    SEP("Sep", 9),
    OCT("Oct", 10),
    NOV("Nov", 11),
    DEC("Dec", 12);

    private String s;
    private int number;

    Month(String s, int n){
        this.s = s;
        this.number = n;
    }

    public static int strToNumber(String s){
        for(var m : Month.values()){
            if(m.s.equals(s)){
                return m.number;
            }
        }

        return -1;
    }
}
