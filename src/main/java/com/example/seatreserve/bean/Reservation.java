package com.example.seatreserve.bean;

public class Reservation {
    private int id;
    private String account;
    private int library;
    private int floor;
    private int seatNum;
    private long startTime;
    private long endTime;
    //====新增这一行====
    private long endTimeOriginal;
    private int isSign;
    private int isBreak;
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getLibrary() {
        return library;
    }

    public void setLibrary(int library) {
        this.library = library;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTimeOriginal() {
        return endTimeOriginal;
    }

    public void setEndTimeOriginal(long endTimeOriginal) {
        this.endTimeOriginal = endTimeOriginal;
    }

    public int getIsSign() {
        return isSign;
    }

    public void setIsSign(int isSign) {
        this.isSign = isSign;
    }

    public int getIsBreak() {
        return isBreak;
    }

    public void setIsBreak(int isBreak) {
        this.isBreak = isBreak;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
