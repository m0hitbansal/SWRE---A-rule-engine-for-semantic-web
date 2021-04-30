package SWRE.ruleGenerator;

import SWRE.Ontology2SDB2MySQL.OWLUtilities;

import java.util.ArrayList;

/*
 * This class runs once and creates implicitRules.xml
 * It extracts the rules or properties associated with ObjectProperty specified by OWL ontology
 * For each of the properties, it creates a hard-coded SPARQL query to extract ObjectProperty along with their associated property.
 * InverseOf an ObjectProperty is a sub-property associated with the ObjectProperty, thus the second query just evaluates InverseOf ObjectProperty and creates rules accordingly
 */

public class ImplicitRule {

    public static void createImplicitRule() throws Exception {

        RuleBox ruleBox = new RuleBox();
        ruleBox.init(false);
        OWLUtilities owlUtilities = new OWLUtilities();

        /*
         * SPARQL query for properties associated with ObjectProperty
         */
        String query = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                       "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                       "PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
                       "SELECT distinct ?property1 ?property2 {{?property1 rdf:type owl:TransitiveProperty} UNION " +
                                                             "{?property1 rdf:type owl:SymmetricProperty} UNION " +
                                                             "{?property1 rdf:type owl:FunctionalProperty} UNION "+
                                                             "{?property1 rdf:type owl:IrreflexiveProperty} UNION "+
                                                             "{?property1 rdf:type owl:AsymmetricProperty} UNION "+
                                                             "{?property1 rdf:type owl:InverseFunctionalProperty} UNION "+
                                                             "{?property1 rdf:type owl:ReflexiveProperty} . ?property1 rdf:type ?property2 } " +
                                                             "order by ?property1";

        ArrayList<ArrayList<String>> result = owlUtilities.SDBQuery(query,"property1","property2");

        int len = result.size();
        String[] temp, antecedent, consequent;

        /*
         * Generating rules for each of the ObjectProperty
         */
        for(int i=0;i<len;i++){

            temp = result.get(i).get(0).split("#",0);
            String predicate = temp[temp.length-1];
            temp = result.get(i).get(1).split("#",0);
            String property = temp[temp.length-1];

            if(property.equalsIgnoreCase("SymmetricProperty")){

                antecedent = new String[3]; consequent = new String[3];
                antecedent[0] = "?x"; antecedent[1] = predicate; antecedent[2] = "?y";
                consequent[0] = "?y"; consequent[1] = predicate; consequent[2] = "?x";
                ruleBox.addRule(antecedent, consequent);
            }
            else if(property.equalsIgnoreCase("TransitiveProperty")){

                antecedent = new String[7]; consequent = new String[3];
                antecedent[0] = "?x"; antecedent[1] = predicate; antecedent[2] = "?y";  antecedent[3] = "AND";
                antecedent[4] = "?y"; antecedent[5] = predicate; antecedent[6] = "?z";
                consequent[0] = "?x"; consequent[1] = predicate; consequent[2] = "?z";
                ruleBox.addRule(antecedent, consequent);
            }
            else if(property.equalsIgnoreCase("ReflexiveProperty")){
                antecedent = new String[3]; consequent = new String[3];
                antecedent[0] = "?x"; antecedent[1] = predicate ; antecedent[2] = "?y";
                consequent[0] = "?x"; consequent[1] = predicate ; consequent[2] = "?x";
                ruleBox.addRule(antecedent,consequent);
            }
        }

        /*
         * Query to find the inverse predicate relationships e.g
         * For the ObjectProperty " <owl:ObjectProperty rdf:about="http://www.iiitb.org/university#taught_by">
                                    <owl:inverseOf rdf:resource="http://www.iiitb.org/university#teaches"/>
                                    </owl:ObjectProperty>"
         *
         * This is an inverse of teaches
         */
        query = "PREFIX owl:<http://www.w3.org/2002/07/owl#>"+
                "SELECT ?property1 ?property2 {?property1 owl:inverseOf ?property2}";

        result = owlUtilities.SDBQuery(query,"property1","property2");


        len = result.size();
        antecedent = new String[3];
        consequent = new String[3];

        /*
         * Creating rules to be put in implicitRule XML
         */
        for(int i=0;i<len;i++){

            temp = result.get(i).get(0).split("#",0);
            String inversePredicate = temp[temp.length-1];
            temp = result.get(i).get(1).split("#",0);
            String predicate = temp[temp.length-1];

            antecedent[0] = "?x"; antecedent[1] = predicate; antecedent[2] = "?y";
            consequent[0] = "?y"; consequent[1] = inversePredicate; consequent[2] = "?x";

            ruleBox.addRule(antecedent, consequent);
        }
    }
}
