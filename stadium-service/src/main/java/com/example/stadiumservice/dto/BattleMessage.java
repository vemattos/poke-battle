package com.example.stadiumservice.dto;
public class BattleMessage {

    private MessageType type;
    private String from;
    private String to;
    private UserDTO user;
    private String battleId;
    private String opponentName;
    private Integer target;
    private BattleAction action;
    private Integer damage;
    private String battleLog;

    public BattleMessage() {
    }

    public BattleMessage(MessageType type, String from, String to, UserDTO user,
                         String battleId, String opponentName, Integer target,
                         BattleAction action, Integer damage, String battleLog) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.user = user;
        this.battleId = battleId;
        this.opponentName = opponentName;
        this.target = target;
        this.action = action;
        this.damage = damage;
        this.battleLog = battleLog;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public BattleAction getAction() {
        return action;
    }

    public void setAction(BattleAction action) {
        this.action = action;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public String getBattleLog() {
        return battleLog;
    }

    public void setBattleLog(String battleLog) {
        this.battleLog = battleLog;
    }

    public Integer getTarget(){
        return target;
    }

    public void setTarget(Integer target){
        this.target = target;
    }

    public enum MessageType {
        LOGIN, BATTLE_START, PLAYER_ACTION, TURN_RESULT, BATTLE_END, BATTLE_STATE
    }

    public enum BattleAction {
        ATTACK, SWITCH_POKEMON
    }
}
