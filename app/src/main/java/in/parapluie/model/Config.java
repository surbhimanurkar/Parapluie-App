package in.parapluie.model;

import java.util.HashMap;

/**
 * Created by surbhimanurkar on 28-09-2016.
 */
public class Config {
    private boolean active;
    private String inactiveMessage;
    private HashMap<String,String> stylistTokens;


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
