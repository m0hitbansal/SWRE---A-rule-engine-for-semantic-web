package SWRE.ruleChaining;

import SWRE.Ontology2SDB2MySQL.OWLUtilities;
import SWRE.Ontology2SDB2MySQL.SDBUtilities;
import SWRE.ruleGenerator.RuleBox;

import java.util.ArrayList;
public class BackwardChaining {

    //This function will execute rules one by one and add the newly generated triples into the database
    public static void executeRules(String prefix, ArrayList<ArrayList<String> > rulesToExecute) throws Exception {

        String query = "";
        String subject = "";
        String object = "";
        String predicate = "";
        int tripleLength;
        ArrayList<String> rule =new ArrayList<String>();

        OWLUtilities owlUtilities =new OWLUtilities();
        for(int loop=rulesToExecute.size()-1;loop>=0;loop--) {

            ArrayList<ArrayList<String>> triples = new ArrayList<ArrayList<String>>();
            rule = rulesToExecute.get(loop);
            subject = rule.get(rule.size()-3);
            predicate = rule.get(rule.size()-2);
            object = rule.get(rule.size()-1);
            query = ForwardChaining.createQuery(rule,owlUtilities,prefix);
            System.out.println(query);
            triples = owlUtilities.SDBQuery(query,subject,object);
            tripleLength = triples.size();
            for (int loop1 = 0; loop1 < tripleLength; loop1++) {
                subject = triples.get(loop1).get(0);
                object = triples.get(loop1).get(1);
                owlUtilities.insertTriples(subject, predicate, object);
            }
        }
    }
    /*
     * Method to implement Backward Chaining
     * Algorithm ->
     *          1. while a new triple is not being generated by the selected rule and the query is false, do the steps
     *              i.   Process a rule i.e. create a SPARQL query.
     *              ii.  Run SPARQL query on the existing fact table
     *              iii. Match for the subjects and objects required with the subject and object produced
     *              iv.  If the query solution is found, break else
     *              v.   Search for the predicate in the consequent of the other rules
     *              vi.  Continue the process
     */
    public static boolean backwardChaining(String subject, String predicate, String object) throws Exception {

        ArrayList<ArrayList<String>> possibleTargetValues = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> rulesToExecute = new ArrayList<ArrayList<String>>();
        ArrayList<String> lastIterationPredicates = new ArrayList<String>();
        ArrayList<String> currentIteartionPredicates = new ArrayList<String>();
        lastIterationPredicates.add(predicate);
        String query="";
        String currentPredicate="";
        SDBUtilities sdbUtilities = new SDBUtilities();
        sdbUtilities.DBinit();
        String prefix = sdbUtilities.getOntologyPrefix();
        subject = prefix + subject;

        /*if charAt(0) is ' it means it's a literal , so we do not add prefix to it
         * but since result of a query generating triples won't be enclosed within single quotes
         * so we remove the single quotes
         * */

        if (object.charAt(0) == '\'') {
            object = object.substring(1,object.length()-1);
        }
        //if object is any of the following types then owl prefix has to be added
        else if (object.equalsIgnoreCase("Ontology") || object.equalsIgnoreCase("Thing") || object.equalsIgnoreCase("Class") || object.equalsIgnoreCase("ObjectProperty") || object.equalsIgnoreCase("NamedIndividual") || object.equalsIgnoreCase("DatatypeProperty") || object.equalsIgnoreCase("SymmetricProperty") || object.equalsIgnoreCase("TransitiveProperty") || object.equalsIgnoreCase("ReflexiveProperty") || object.equalsIgnoreCase("IrreflexiveProperty") || object.equalsIgnoreCase("AsymmetricProperty") || object.equalsIgnoreCase("FunctionalProperty") || object.equalsIgnoreCase("InverseFunctionalProperty")) {
            object = "http://www.w3.org/2002/07/owl#"+object;
        } else {
            object = prefix + object;
        }

        //to choose what prefix to be added to predicate , here we only choose the prefix but not add
        if (predicate.equalsIgnoreCase("subClassOf") || predicate.equalsIgnoreCase("subPropertyOf") || predicate.equalsIgnoreCase("domain") || predicate.equalsIgnoreCase("range")) {
            prefix = "http://www.w3.org/2000/01/rdf-schema#";
        } else if (predicate.equalsIgnoreCase("type")) {
            prefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        } else if (predicate.equalsIgnoreCase("inverseOf")) {
            prefix = "http://www.w3.org/2002/07/owl#";
        } else {       //Here we can have one more else if condition for owl prefix
            prefix = sdbUtilities.getOntologyPrefix();
        }
        boolean found = false;
        RuleBox ruleBox = new RuleBox();
        ruleBox.init(true);
        ArrayList<ArrayList<String>> ruleList = ruleBox.getRules();

        //Iterate thorugh this untill the result of the query becomes true or we exhaust all possible matches
        while(!lastIterationPredicates.isEmpty() && !found){

            //to check whether the queried triple present in the database or not
            query = "Select ?x ?y {?x " + "<" + prefix + predicate +"> ?y}";
            possibleTargetValues = OWLUtilities.SDBQuery(query,"x","y");
            int possibleTargetLength = possibleTargetValues.size();
            for(int loop=0;loop<possibleTargetLength;loop++) {
                if(subject.equals(possibleTargetValues.get(loop).get(0)) && object.equals(possibleTargetValues.get(loop).get(1))) {
                    found=true;
                    break;
                }
            }
            if(found == true)
                break;

            /*last iteration predicates contains the predicates of the if part of the rule for which consequent predicate had matched
             * initally it will have the queried predicate
             */

            int lastIterationPredicatesLength = lastIterationPredicates.size();
            for(int outer_loop=0;outer_loop < lastIterationPredicatesLength; outer_loop++) {
                currentPredicate=lastIterationPredicates.get(outer_loop);

                /*for every predicate in the lastIterationPreicate array we'll run through all the rules
                 *and check whether this predicate matches the predicate of consequent part of some rule
                 */
                for (int loop = 0; loop < ruleList.size(); loop++) {
                    if ((ruleList.get(loop).get((ruleList.get(loop).size()) - 2)).equals(currentPredicate)) {
                        //if the predicate matches with consequent predicate then we add this rule to rulesToExecute
                        rulesToExecute.add(ruleList.get(loop));
                        //and for this rule we add all the predicates in the if part to currentIterationPredicates
                        for(int antecedentPredicateIndex=1;antecedentPredicateIndex<((ruleList.get(loop).size()) - 3);antecedentPredicateIndex=antecedentPredicateIndex+4) {
                            currentIteartionPredicates.add(ruleList.get(loop).get(antecedentPredicateIndex));
                        }

                        executeRules(prefix, rulesToExecute);
                    }
                }
            }
            /*now since we iterated through all the lastIterationPredicates ,
             *for the next iteration we need to iterate through the predicates that were added in this iteration
             *so we empty the lastIterationPredicates and move data from currentIterationPredicates to lastIterationPreidcates
             */
            lastIterationPredicates.clear();
            for(int loop=0;loop<currentIteartionPredicates.size();loop++)
                lastIterationPredicates.add(currentIteartionPredicates.get(loop));
            currentIteartionPredicates.clear();
        }
        System.out.println("The result of the query by Backward Chaining is :" + found );
        return found;
    }
}