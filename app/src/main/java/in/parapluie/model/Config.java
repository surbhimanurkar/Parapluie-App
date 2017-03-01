package in.parapluie.model;

import java.util.HashMap;

/**
 * Created by surbhimanurkar on 28-09-2016.
 */
public class Config {
    private boolean active;
    private String inactiveMessage;
    private HashMap<String,String> stylistTokens;
    private Long trialSubscriptionPeriod;
    private String stylistName;

    public String getStylistName() {
        return stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }


    public Long getTrialSubscriptionPeriod() {
        return trialSubscriptionPeriod;
    }

    public void setTrialSubscriptionPeriod(Long trialSubscriptionPeriod) {
        this.trialSubscriptionPeriod = trialSubscriptionPeriod;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getInactiveMessage() {
        return inactiveMessage;
    }

    public void setInactiveMessage(String inactiveMessage) {
        this.inactiveMessage = inactiveMessage;
    }

    public HashMap<String, String> getStylistTokens() {
        return stylistTokens;
    }

    public void setStylistTokens(HashMap<String, String> stylistTokens) {
        this.stylistTokens = stylistTokens;
    }

}
