package SWRE.ruleChaining;

import SWRE.Ontology2SDB2MySQL.OWLUtilities;
import SWRE.Ontology2SDB2MySQL.SDBUtilities;
import org.apache.jena.base.Sys;

import java.util.ArrayList;

public class ForwardChaining {

    /*
     * Converts the required triples into a SPARQL query. This function is only used for Forward Chaining.
     */

    public static String createQuery(ArrayList<String> Rule, OWLUtilities owlUtilities, String prefix) throws Exception {

	    int index,loop=0;
    	    String left="";
    	    String right="";
            String query = "";
            String ontologyPrefix = prefix;
	    String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	    String owl = "http://www.w3.org/2002/07/owl#";
	    int len = Rule.size();

	    /*
	     * Calls overloaded insertTriples method from OWLUtilities, this method is to insert into the database that
	     * the predicate in the Consequent part of a new rule is also a object property.
	     * This is done to add a new "PREDICATE" in the fact table. Later, with that fact table, we can add triples accordingly
	     */

       owlUtilities.insertTriples(rdf, owl, Rule.get(len-2),"type", "ObjectProperty");

    	    query=query + "SELECT " + Rule.get(len-3) + " " + Rule.get(len-1) + " { ";
    	    if(Rule.get(0).charAt(0)=='?')
    	    left=Rule.get(0);
    	    else
    	    left = " <" + prefix + Rule.get(0) +"> ";
            //if-else conditions for checking what prefix to apply to predicate
            if (Rule.get(1).equalsIgnoreCase("subClassOf") || Rule.get(1).equalsIgnoreCase("subPropertyOf") || Rule.get(1).equalsIgnoreCase("domain") || Rule.get(1).equalsIgnoreCase("range")) {
                prefix = "http://www.w3.org/2000/01/rdf-schema#";
                left = left + " <" + prefix + Rule.get(1) + "> ";
            } else if (Rule.get(1).equalsIgnoreCase("type")) {
                prefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
                left = left + " <" + prefix + Rule.get(1) + "> ";
            } else if (Rule.get(1).equalsIgnoreCase("inverseOf")) {
                prefix = "http://www.w3.org/2002/07/owl#";
                left = left + " <" + prefix + Rule.get(1) + "> ";
            } else       //Here we can have one more else if condition for owl prefix
                left = left + " <" + prefix + Rule.get(1) + "> ";
            prefix = ontologyPrefix;

            //if charAt(0)='  it means we hava data of data type property so no need to apply prefix
            if (Rule.get(2).charAt(0) == '?' || Rule.get(2).charAt(0) == '\'')
                left = left + Rule.get(2) + " ";
            else if (Rule.get(2).equalsIgnoreCase("Ontology") || Rule.get(2).equalsIgnoreCase("Thing") || Rule.get(2).equalsIgnoreCase("Class") || Rule.get(2).equalsIgnoreCase("ObjectProperty") || Rule.get(2).equalsIgnoreCase("NamedIndividual") || Rule.get(2).equalsIgnoreCase("DatatypeProperty") || Rule.get(2).equalsIgnoreCase("SymmetricProperty") || Rule.get(2).equalsIgnoreCase("TransitiveProperty") || Rule.get(2).equalsIgnoreCase("ReflexiveProperty") || Rule.get(2).equalsIgnoreCase("IrreflexiveProperty") || Rule.get(2).equalsIgnoreCase("AsymmetricProperty") || Rule.get(2).equalsIgnoreCase("FunctionalProperty") || Rule.get(2).equalsIgnoreCase("InverseFunctionalProperty")) {
                prefix = "http://www.w3.org/2002/07/owl#";
                left = left + "<" + prefix + Rule.get(2) + "> ";
            } else
                left = left + "<" + prefix + Rule.get(2) + "> ";
            prefix = ontologyPrefix;
    	    //index refer to the index of connector being considered currently
    	    index=(3*(loop+1))+loop;
    	    while(index<len-3)
    	    {
    	    	loop++;
    	    	//right holds the subject,predicate,object present immediately after the connector
    	    	if(Rule.get(index+1).charAt(0)=='?')
    	    	right = Rule.get(index+1) ;
    	    	else
    	    	right = " <" + prefix + Rule.get(index+1) + "> " ;
                if (Rule.get(index + 2).equalsIgnoreCase("subClassOf") || Rule.get(index + 2).equalsIgnoreCase("subPropertyOf") || Rule.get(index + 2).equalsIgnoreCase("range") || Rule.get(index + 2).equalsIgnoreCase("domain")) {
                    prefix = "http://www.w3.org/2000/01/rdf-schema#";
                    right = right + " <" + prefix + Rule.get(index + 2) + "> ";
                } else if (Rule.get(index + 2).equalsIgnoreCase("type")) {
                    prefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
                    right = right + " <" + prefix + Rule.get(index + 2) + "> ";
                } else if (Rule.get(index + 2).equalsIgnoreCase("inverseOf")) {
                    prefix = "http://www.w3.org/2002/07/owl#";
                    right = right + " <" + prefix + Rule.get(index + 2) + "> ";
                } else
                    right = right + " <" + prefix + Rule.get(index + 2) + "> ";
                prefix = ontologyPrefix;
                //right = right + " <" + prefix + queryPart.get(index+2) + "> " ;
                if (Rule.get(index + 3).charAt(0) == '?' || Rule.get(index + 3).charAt(0) == '\'')
                    right = right + Rule.get(index + 3) + " ";
                else if (Rule.get(index + 3).equalsIgnoreCase("Ontology") || Rule.get(index + 3).equalsIgnoreCase("Thing") || Rule.get(index + 3).equalsIgnoreCase("Class") || Rule.get(index + 3).equalsIgnoreCase("ObjectProperty") || Rule.get(index + 3).equalsIgnoreCase("NamedIndividual") || Rule.get(index + 3).equalsIgnoreCase("DatatypeProperty") || Rule.get(index + 3).equalsIgnoreCase("SymmetricProperty") || Rule.get(index + 3).equalsIgnoreCase("TransitiveProperty") || Rule.get(index + 3).equalsIgnoreCase("ReflexiveProperty") || Rule.get(index + 3).equalsIgnoreCase("IrreflexiveProperty") || Rule.get(index + 3).equalsIgnoreCase("AsymmetricProperty") || Rule.get(index + 3).equalsIgnoreCase("FunctionalProperty") || Rule.get(index + 3).equalsIgnoreCase("InverseFunctionalProperty")) {
                    prefix = "http://www.w3.org/2002/07/owl#";
                    right = right + "<" + prefix + Rule.get(index + 3) + "> ";
                } else
                    right = right + "<" + prefix + Rule.get(index + 3) + "> ";
                prefix = ontologyPrefix;
                //left holds entire query before the present connector


    	    	if((Rule.get(index)).equals("OR"))
    	    	{
    	    		left = "{ " + left + " }" + " UNION " + "{ " + right + " }";
    	    	}
    	    	else if((Rule.get(index)).equals("AND"))
    	    	{
    	    		left = left + " . " + right;
    	    	}
    	    	//updating index to get to next connector
    	    	index=(3*(loop+1))+loop;
    	    }
    	    query = query + left + " }";

            return query;
    }

    /*
     * Method to implement Forward Chaining
     * Algorithm ->
     *          1. while a new triple is not being generated by any rule, do the steps
     *              i.   Process a rule i.e. create a SPARQL query.
     *              ii.  Run SPARQL query on the existing fact table
     *              iii. Check for new facts
     *                      a. If no facts are added, increment the RULE COMPLETE counter by 1
     *                      b. If facts are added, update the fact table
     */
    public static void ForwardChaining(ArrayList<ArrayList<String>> ruleList) throws Exception {

        /*
         * Flag sets when no new triple is added after each forward pass iteration
         */
        boolean newTriple = false;

        int ruleListLength = ruleList.size();
        SDBUtilities sdbUtilities = new SDBUtilities();
        sdbUtilities.DBinit();
        OWLUtilities owlUtilities = new OWLUtilities(sdbUtilities);

        /*
         * SELECT (COUNT(*) as ?Triples) WHERE { ?s ?p ?o}
         * SPARQL query to count the number of triples
         */

        int[] previousNumberOfTriples = new int[ruleListLength];

        for(int i=0;i<ruleListLength;i++)   previousNumberOfTriples[i] = -1;

        for(int pass = 0; ; pass++) {

            // Counts number of rules for which no new triple is generated
            int noNewTripleForRuleCount = 0;

            for (int loop = 0; loop < ruleListLength; loop++) {

                // Extract each rule from the rule
                ArrayList<String> Rule = ruleList.get(loop);
                // Create query for the current rule
                String query = createQuery(Rule, owlUtilities,sdbUtilities.getOntologyPrefix());
                System.out.println("Query generated by rule " + loop + " is " + query);
                // Obtaing triples due to the above query
                int ruleLength = Rule.size();
                String subject = Rule.get(ruleLength - 3);
                String predicate = Rule.get(ruleLength - 2);
                String object = Rule.get(ruleLength - 1);
                ArrayList<ArrayList<String>> triples = owlUtilities.SDBQuery(query, subject, object);

                int numberOfTriplesGenerated = triples.size();

                if(previousNumberOfTriples[loop] == numberOfTriplesGenerated) {
                    noNewTripleForRuleCount++;
                }
                else{
                    previousNumberOfTriples[loop] = numberOfTriplesGenerated;
                    System.out.println("Iteration " + pass + " Rule " + loop + " yeilded " + numberOfTriplesGenerated + " new triples");
                    // Update the fact table for triples obtained from the above query
                    int tripleLength = triples.size();
                    for (int loop1 = 0; loop1 < tripleLength; loop1++) {
                        subject = triples.get(loop1).get(0);
                        object = triples.get(loop1).get(1);
                        owlUtilities.insertTriples(subject, predicate, object);
                    }
                }
            }
            // No new triple generated for the iteration
            if(noNewTripleForRuleCount == ruleListLength)   break;
        }
    }
}
