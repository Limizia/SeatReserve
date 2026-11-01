package com.example.seatreserve.bean;

public class User {
    private int id;
    private String account;
    private String password;
    private int breakRule;
    private int hasUsing;

    public User(){}

    public User(int id, String account, String password, int breakRule, int hasUsing) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.breakRule = breakRule;
        this.hasUsing = hasUsing;
    }

    //getter setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getBreakRule() { return breakRule; }
    public void setBreakRule(int breakRule) { this.breakRule = breakRule; }
    public int getHasUsing() { return hasUsing; }
    public void setHasUsing(int hasUsing) { this.hasUsing = hasUsing; }
}
