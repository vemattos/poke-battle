package com.example.stadiumservice.dto;

public class BattleMessage {

    private MessageType type;
    private String from;
    private String to;
    private UserDTO user;
    private String battleId;
    private String opponentName;
    private Stadium stadium;
    private Integer target;
    private BattleAction action;
    private Integer damage;
    private String battleLog;
    private String instanceId;

    public BattleMessage() {
    }

    public BattleMessage(MessageType type, String from, String to, UserDTO user,
                         String battleId, String opponentName, Stadium stadium, Integer target,
                         BattleAction action, Integer damage, String battleLog, String instanceId) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.user = user;
        this.battleId = battleId;
        this.opponentName = opponentName;
        this.stadium = stadium;
        this.target = target;
        this.action = action;
        this.damage = damage;
        this.battleLog = battleLog;
        this.instanceId = instanceId;
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

    public Stadium getStadium() { return stadium; }

    public void setStadium(Stadium stadium) { this.stadium = stadium; }

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

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public enum MessageType {
        LOGIN, BATTLE_START, PLAYER_ACTION, TURN_RESULT, BATTLE_END, BATTLE_STATE
    }

    public enum BattleAction {
        ATTACK, SWITCH_POKEMON, FLEE
    }

    @Override
    public String toString() {
        return "BattleMessage{" +
                "type=" + type +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", user=" + (user != null ? user.getName() : "null") +
                ", battleId='" + battleId + '\'' +
                ", opponentName='" + opponentName + '\'' +
                ", stadium=" + stadium +
                ", target=" + target +
                ", action=" + action +
                ", damage=" + damage +
                ", battleLog='" + battleLog + '\'' +
                ", instanceId='" + instanceId + '\'' +
                '}';
    }
}