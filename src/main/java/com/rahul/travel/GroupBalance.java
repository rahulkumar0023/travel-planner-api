package com.rahul.travel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class GroupBalance {
    private String user;
    private Map<String, Double> owes = new HashMap<>();
    // getters/setters
}