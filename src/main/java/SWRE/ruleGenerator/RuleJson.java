package SWRE.ruleGenerator;

import java.util.List;

/*
 * This is a wrapper class to convert the JSON rules obtained from the front-end
 * into an object format to sync it with the program readable format
 */

public class RuleJson {

    private List<String> rules ;

    public RuleJson() {
    }

    public RuleJson(List<String>rules){
        this.rules=rules;
    }
    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }
}
