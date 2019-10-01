package com.example.dndapp._data;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.dndapp.Player.PlayerInfoActivity.playerStatsData;

public class PlayerProficiencyData {
    private boolean arcana;
    private boolean athletics;
    private boolean deception;
    private boolean history;
    private boolean insight;
    private boolean intimidation;
    private boolean investigation;
    private boolean medicine;
    private boolean nature;
    private boolean perception;
    private boolean performance;
    private boolean persuasion;
    private boolean religion;
    private boolean sleightOfHand;
    private boolean stealth;
    private boolean survival;
    private boolean acrobatics;
    private boolean animalHandling;

    public boolean isArcana() {
        return arcana;
    }

    public boolean isAthletics() {
        return athletics;
    }

    public boolean isDeception() {
        return deception;
    }

    public boolean isHistory() {
        return history;
    }

    public boolean isInsight() {
        return insight;
    }

    public boolean isIntimidation() {
        return intimidation;
    }

    public boolean isInvestigation() {
        return investigation;
    }

    public boolean isMedicine() {
        return medicine;
    }

    public boolean isNature() {
        return nature;
    }

    public boolean isPerception() {
        return perception;
    }

    public boolean isPerformance() {
        return performance;
    }

    public boolean isPersuasion() {
        return persuasion;
    }

    public boolean isReligion() {
        return religion;
    }

    public boolean isSleightOfHand() {
        return sleightOfHand;
    }

    public boolean isStealth() {
        return stealth;
    }

    public boolean isSurvival() {
        return survival;
    }

    public boolean isAcrobatics() {
        return acrobatics;
    }

    public boolean isAnimalHandling() {
        return animalHandling;
    }

    public PlayerProficiencyData(JSONObject obj) throws JSONException {
        this.setData(obj);
    }

    public void setData(JSONObject obj) throws JSONException {
        this.acrobatics = obj.getBoolean("acrobatics");
        this.animalHandling = obj.getBoolean("animal_handling");
        this.arcana = obj.getBoolean("arcana");
        this.athletics = obj.getBoolean("athletics");
        this.deception = obj.getBoolean("deception");
        this.history = obj.getBoolean("history");
        this.insight = obj.getBoolean("insight");
        this.intimidation = obj.getBoolean("intimidation");
        this.investigation = obj.getBoolean("investigation");
        this.medicine = obj.getBoolean("medicine");
        this.nature = obj.getBoolean("nature");
        this.perception = obj.getBoolean("perception");
        this.performance = obj.getBoolean("performance");
        this.persuasion = obj.getBoolean("persuasion");
        this.religion = obj.getBoolean("religion");
        this.sleightOfHand = obj.getBoolean("sleight_of_hand");
        this.stealth = obj.getBoolean("stealth");
        this.survival = obj.getBoolean("survival");
    }

    private String toBonus(int value) {
        if (value < 0)
            return String.valueOf(value);
        else
            return "+" + value;
    }

    public String getAcrobaticBonus() {
        int value = (acrobatics ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getDexterityModifier());
        return toBonus(value);
    }

    public String getAnimalHandlingBonus() {
        int value = (animalHandling ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String getArcanaBonus() {
        int value = (arcana ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getAthleticsBonus() {
        int value = (athletics ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getStrengthModifier());
        return toBonus(value);
    }

    public String getDeceptionBonus() {
        int value = (deception ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getCharismaModifier());
        return toBonus(value);
    }

    public String getHistoryBonus() {
        int value = (history ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getInsightBonus() {
        int value = (insight ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String getIntimidationBonus() {
        int value = (intimidation ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getCharismaModifier());
        return toBonus(value);
    }

    public String getInvestigationBonus() {
        int value = (investigation ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getMedicineBonus() {
        int value = (medicine ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String getNatureBonus() {
        int value = (nature ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getPerceptionBonus() {
        int value = (perception ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String getPerformanceBonus() {
        int value = (performance ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getCharismaModifier());
        return toBonus(value);
    }

    public String getPersuasionBonus() {
        int value = (persuasion ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getCharismaModifier());
        return toBonus(value);
    }

    public String getReligionBonus() {
        int value = (religion ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getSleightOfHandBonus() {
        int value = (sleightOfHand ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getDexterityModifier());
        return toBonus(value);
    }

    public String getStealthBonus() {
        int value = (stealth ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getDexterityModifier());
        return toBonus(value);
    }

    public String getSurvivalBonus() {
        int value = (survival ? playerStatsData.getProficiencyModifier() : 0) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String toJSON() {
        return "";
    }
}
