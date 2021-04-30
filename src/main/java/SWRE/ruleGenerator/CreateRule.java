package SWRE.ruleGenerator;

import java.util.*;

/*
 * This is a wrapper class to convert the JSON rules obtained from the front-end
 * into an object format to sync it with the program readable format
 */

public class CreateRule {
    private List<String>antecedent;
    private List<String>consequent;
    public CreateRule() {
    }

    public CreateRule(List<String> antecedent,List<String> consequent) {
        this.antecedent = antecedent;
        this. consequent = consequent;
    }

    public List<String> getAntecedent() {
        return antecedent;
    }

    public void setAntecedent(List<String> antecedent) {
        this.antecedent = antecedent;
    }

    public List<String> getConsequent() {
        return consequent;
    }

    public void setConsequent(List<String> consequent) {
        this.consequent = consequent;
    }


}
