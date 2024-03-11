package com.mnsoo.dividends.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ScrapedResult {
    private Company company;
    private List<Dividend> dividends;
    public ScrapedResult(){
        this.dividends = new ArrayList<>();
    }
}
