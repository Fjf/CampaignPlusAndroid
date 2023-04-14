package com.example.campaignplus._data;


import org.json.JSONException;
import org.json.JSONObject;

public class PlayerProficiencyData {
    private int arcana = 0;
    private int athletics = 0;
    private int deception = 0;
    private int history = 0;
    private int insight = 0;
    private int intimidation = 0;
    private int investigation = 0;
    private int medicine = 0;
    private int nature = 0;
    private int perception = 0;
    private int performance = 0;
    private int persuasion = 0;
    private int religion = 0;
    private int sleightOfHand = 0;
    private int stealth = 0;
    private int survival = 0;
    private int acrobatics = 0;
    private int animalHandling = 0;
    private PlayerStatsData playerStatsData;

    public int isArcana() {
        return arcana;
    }

    public int isAthletics() {
        return athletics;
    }

    public int isDeception() {
        return deception;
    }

    public int isHistory() {
        return history;
    }

    public int isInsight() {
        return insight;
    }

    public int isIntimidation() {
        return intimidation;
    }

    public int isInvestigation() {
        return investigation;
    }

    public int isMedicine() {
        return medicine;
    }

    public int isNature() {
        return nature;
    }

    public int isPerception() {
        return perception;
    }

    public int isPerformance() {
        return performance;
    }

    public int isPersuasion() {
        return persuasion;
    }

    public int isReligion() {
        return religion;
    }

    public int isSleightOfHand() {
        return sleightOfHand;
    }

    public int isStealth() {
        return stealth;
    }

    public int isSurvival() {
        return survival;
    }

    public int isAcrobatics() {
        return acrobatics;
    }

    public int isAnimalHandling() {
        return animalHandling;
    }

    public PlayerProficiencyData(JSONObject obj) throws JSONException {
        this.setData(obj);
    }

    public PlayerProficiencyData() { }

    public void setSelectedPlayerData(PlayerData psd) {
        this.playerStatsData = psd.statsData;
    }

    public void setData(JSONObject obj) throws JSONException {
        this.acrobatics = obj.getInt("acrobatics");
        this.animalHandling = obj.getInt("animal_handling");
        this.arcana = obj.getInt("arcana");
        this.athletics = obj.getInt("athletics");
        this.deception = obj.getInt("deception");
        this.history = obj.getInt("history");
        this.insight = obj.getInt("insight");
        this.intimidation = obj.getInt("intimidation");
        this.investigation = obj.getInt("investigation");
        this.medicine = obj.getInt("medicine");
        this.nature = obj.getInt("nature");
        this.perception = obj.getInt("perception");
        this.performance = obj.getInt("performance");
        this.persuasion = obj.getInt("persuasion");
        this.religion = obj.getInt("religion");
        this.sleightOfHand = obj.getInt("sleight_of_hand");
        this.stealth = obj.getInt("stealth");
        this.survival = obj.getInt("survival");
    }

    private String toBonus(int value) {
        if (value < 0)
            return String.valueOf(value);
        else
            return "+" + value;
    }

    public String getAcrobaticBonus() {
        int value = (acrobatics * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getDexterityModifier());
        return toBonus(value);
    }

    public String getAnimalHandlingBonus() {
        int value = (animalHandling * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String getArcanaBonus() {
        int value = (arcana * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getAthleticsBonus() {
        int value = (athletics * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getStrengthModifier());
        return toBonus(value);
    }

    public String getDeceptionBonus() {
        int value = (deception * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getCharismaModifier());
        return toBonus(value);
    }

    public String getHistoryBonus() {
        int value = (history * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getInsightBonus() {
        int value = (insight * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String getIntimidationBonus() {
        int value = (intimidation * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getCharismaModifier());
        return toBonus(value);
    }

    public String getInvestigationBonus() {
        int value = (investigation * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getMedicineBonus() {
        int value = (medicine * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String getNatureBonus() {
        int value = (nature * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getPerceptionBonus() {
        int value = (perception * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public String getPerformanceBonus() {
        int value = (performance * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getCharismaModifier());
        return toBonus(value);
    }

    public String getPersuasionBonus() {
        int value = (persuasion * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getCharismaModifier());
        return toBonus(value);
    }

    public String getReligionBonus() {
        int value = (religion * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getIntelligenceModifier());
        return toBonus(value);
    }

    public String getSleightOfHandBonus() {
        int value = (sleightOfHand * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getDexterityModifier());
        return toBonus(value);
    }

    public String getStealthBonus() {
        int value = (stealth * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getDexterityModifier());
        return toBonus(value);
    }

    public String getSurvivalBonus() {
        int value = (survival * playerStatsData.getProficiencyModifier()) + Integer.valueOf(playerStatsData.getWisdomModifier());
        return toBonus(value);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("athletics", this.athletics);

        obj.put("acrobatics", this.acrobatics);
        obj.put("sleight_of_hand", this.sleightOfHand);
        obj.put("stealth", this.stealth);

        obj.put("arcana", this.arcana);
        obj.put("history", this.history);
        obj.put("investigation", this.investigation);
        obj.put("nature", this.nature);
        obj.put("religion", this.religion);

        obj.put("deception", this.deception);
        obj.put("intimidation", this.intimidation);
        obj.put("performance", this.performance);
        obj.put("persuasion", this.persuasion);

        obj.put("animal_handling", this.animalHandling);
        obj.put("insight", this.insight);
        obj.put("medicine", this.medicine);
        obj.put("perception", this.perception);
        obj.put("survival", this.survival);

        return obj;
    }
}
