package com.adwitiya.cs7cs3.towerpower.Helpers;

import java.util.Date;

/**
 * Created by Stefano on 23/03/2018.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stefano on 23/03/2018.
 */

public class GameInfo {
    private Map<String, Object> initialPosition;
    private Map<String, Object>  hints;
    private Map<String, Object>  materials;
    private Map<String, Object> bases;
    private Map<String, Object>  towers;
    private long materialsInventory;
    private long hintsInventory;
    private long timeBonus;
    private boolean won;
    private PositionHelper startLocation;
    private Date startTime;

    public GameInfo() {
        this.initialPosition = new HashMap<String, Object>();
        this.hints = new HashMap<String, Object>();
        this.materials = new HashMap<String, Object>();
        this.bases = new HashMap<String, Object>();
        this.towers = new HashMap<String, Object>();
        this.materialsInventory = 0;
        this.hintsInventory = 0;
        this.startTime = new Date();
        this.won = false;
    }

    public Map<String, Object> getInitialPosition() {
        return initialPosition;
    }

    public void setInitialPosition(Map<String, Object> initialPosition) {
        this.initialPosition = initialPosition;
    }

    public Map<String, Object> getHints() {
        return hints;
    }

    public void addHint(String key, double lat, double lon) {
        this.hints.put( key, new PositionHelper(lat,lon) );
    }

    public Map<String, Object> getMaterials() {
        return materials;
    }

    public void addMaterial(String key, double lat, double lon) {
        this.materials.put( key, new PositionHelper(lat,lon) );
    }

    public String collect(double lat, double lon){
        int i=0;
        String finalKey=null;
        PositionHelper pos;
        for ( String key : this.getHints().keySet() ){
            pos = (PositionHelper) this.getHints().get(key);
            if (pos.getLatitude() == lat && pos.getLongitude() == lon) {
                finalKey = key;
                this.getHints().remove(finalKey);
                addHintToInvetory();
                return finalKey;
            }
        }
        for ( String key : this.getMaterials().keySet() ){
            pos = (PositionHelper) this.getMaterials().get(key);
            if (pos.getLatitude() == lat && pos.getLongitude() == lon) {
                finalKey = key;
                this.getMaterials().remove(finalKey);
                addMaterialToInvetory();
                return finalKey;
            }
        }
        for ( String key : this.getBases().keySet() ){
            pos = (PositionHelper) this.getBases().get(key);
            if (pos.getLatitude() == lat && pos.getLongitude() == lon) {
                finalKey = key;
                if ( this.getMaterialsInventory() >= 5){
                    this.useMaterials();
                    //this.getBases().remove(finalKey);
                }
                else {
                    finalKey = null;
                }
                return finalKey;
            }
        }
        for ( String key : this.getTowers().keySet() ){
            pos = (PositionHelper) this.getTowers().get(key);
            if (pos.getLatitude() == lat && pos.getLongitude() == lon) {
                finalKey = key;
                if ( this.getHintsInventory() >= 4){
                    this.useHints();
                    this.getTowers().remove(finalKey);
                }
                else {
                    finalKey = null;
                }
                return finalKey;
            }
        }

        return finalKey;
    }

    public Map<String, Object> getBases() {
        return bases;
    }

    public void addBase(String key, double lat, double lon) {
        this.bases.put( key, new PositionHelper(lat,lon) );
    }

    public Map<String, Object> getTowers() {
        return towers;
    }

    public void addTower(String key, double lat, double lon) {
        this.towers.put( key, new PositionHelper(lat,lon) );
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public PositionHelper getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(PositionHelper startLocation) {
        this.startLocation = startLocation;
    }

    public void setHintsInventory(long hintsInventory) {
        this.hintsInventory = hintsInventory;
    }

    public void addHintToInvetory(){
        hintsInventory = hintsInventory+1;
    }

    public void useHints (){
        hintsInventory = hintsInventory - 4;
    }

    public long getHintsInventory() {
        return hintsInventory;
    }

    public void addMaterialToInvetory(){
        materialsInventory = materialsInventory+1 ;
    }

    public long getMaterialsInventory() {
        return materialsInventory;
    }

    public void setMaterialsInventory(long materialsInventory) {
        this.materialsInventory = materialsInventory;
    }

    public void useMaterials(){
        materialsInventory = materialsInventory - 5;
    }

    public long getTimeBonus() {
        return timeBonus;
    }

    public void setTimeBonus(long timeBonus) {
        this.timeBonus = timeBonus;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }
}