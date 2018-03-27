package com.adwitiya.cs7cs3.towerpower;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stefano on 23/03/2018.
 */

public class GameInfo {
    Map<String, Object> initialPositions;
    Map<String, Object> letters;
    Map<String, Object> materials;
    Map<String, Object> passwords;
    Map<String, Object> team;
    Map<String, Object> towers;
    Map<String, Object> commonInventory;
    Map<String, Object> gameArea;
    Date startTime;

    public GameInfo() {
        this.initialPositions = new HashMap<String, Object>();
        this.letters = new HashMap<String, Object>();
        this.materials = new HashMap<String, Object>();
        this.passwords = new HashMap<String, Object>();
        this.team = new HashMap<String, Object>();
        this.towers = new HashMap<String, Object>();
        this.commonInventory = new HashMap<String, Object>();
        this.gameArea = new HashMap<String, Object>();
        this.startTime = new Date();
    }

    public Map<String, Object> getInitialPositions() {
        return initialPositions;
    }

    public void addInitialPosition(String key, double lat, double lon){
        this.initialPositions.put( key, new PositionHelper(lat,lon) );
    }

    public Map<String, Object> getLetters() {
        return letters;
    }

    public void addLetter(String key, String letter){
        this.letters.put( key, letter );
    }

    public Map<String, Object> getMaterials() {
        return materials;
    }

    public void addMaterial(String key, double lat, double lon){
        this.materials.put( key, new PositionHelper(lat,lon) );
    }

    public Map<String, Object> getPasswords() {
        return passwords;
    }

    public void addPassword(String key, String password){
        this.passwords.put( key, password );
    }

    public Map<String, Object> getTeam() {
        return team;
    }

    public void addTeamMember(String key, String userID){
        this.team.put( key, userID );
    }

    public Map<String, Object> getTowers() {
        return towers;
    }

    public void addTower(String key, double lat, double lon){
        this.towers.put( key, new PositionHelper(lat,lon) );
    }

    public Map<String, Object> getCommonInventory() {
        return commonInventory;
    }

    public void addMaterialToCommonInventory() {
        if (this.commonInventory.get("materials") != null ){
            this.commonInventory.put("materials", 0);
        }
        int numOfMaterials = (int) this.commonInventory.get("materials");
        numOfMaterials++;
        this.commonInventory.put("materials", numOfMaterials);
    }

    public void addLetterToCommonInventory(String key, String letter) {
        this.commonInventory.put(key, letter);
    }

    public Map<String, Object> getGameArea() {
        return gameArea;
    }

    public void setGameArea(double lat, double lon, double radius){
        this.initialPositions.put( "center", new PositionHelper(lat,lon) );
        this.initialPositions.put( "radius", radius );
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
