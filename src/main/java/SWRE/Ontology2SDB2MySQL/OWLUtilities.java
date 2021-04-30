package SWRE.Ontology2SDB2MySQL;

/*
 * Layer of abtraction between the OWL file and the sdb database. This class supports addition of data into the SQL data
 * and query from it i.e. the CRUD operations on the relational triple store
 */

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.store.DatasetStore;

import java.util.ArrayList;
import java.util.Hashtable;

public class OWLUtilities {

    private static SDBUtilities sdbUtilities = null;

    public OWLUtilities() throws Exception {
        sdbUtilities = new SDBUtilities();
        sdbUtilities.DBinit();
    }

    public OWLUtilities(SDBUtilities sdbUtilities) {
        this.sdbUtilities = sdbUtilities;
    }

    /*
     * This method, creates a new resource for the predicate if the predicate is missing and if not, inserts the triple into the database
     * The method also checks for various properties of different predicates and inserts the triple iff the properties are not being violated
     */

    public static void insertTriples(String subject, String predicate, String object) {

        //flag to deicide whether to insert the triples or not
        boolean dontAdd = false;
        String query = "";
        //pre holds the prefix that is to be used with the predicate
        String pre = "";

        //obj_pre holds the prefix that is to be used with the object
        String obj_pre = "";
        ArrayList<String> irreflexive = new ArrayList<String>();
        ArrayList<String> asymmetric = new ArrayList<String>();
        Model model = SDBFactory.connectDefaultModel(sdbUtilities.getStore());
        model.setNsPrefix(sdbUtilities.getOntologyNamespace(), sdbUtilities.getOntologyPrefix());
        model.read(sdbUtilities.getOntology());
        //create new triples
        org.apache.jena.rdf.model.Resource Subject = model.createResource(subject);
        if (predicate.equalsIgnoreCase("subClassOf") || predicate.equalsIgnoreCase("subPropertyOf") || predicate.equalsIgnoreCase("domain") || predicate.equalsIgnoreCase("range")) {
            pre = "http://www.w3.org/2000/01/rdf-schema#";
        } else if (predicate.equalsIgnoreCase("type")) {
            pre = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        } else if (predicate.equalsIgnoreCase("inverseOf")) {
            pre = "http://www.w3.org/2002/07/owl#";
        } else {       //Here we can have one more else if condition for owl prefix
            pre = sdbUtilities.getOntologyPrefix();
        }

        Property Predicate = model.createProperty(pre + predicate);
        org.apache.jena.rdf.model.Resource Object = model.createResource(object);
        predicate = pre + predicate;
        /* Major Doubt:
        Should we store all irreflexive and asymmetric properties in a database
        Moreover for asymmetric properties we can store the result of all triples with that property in database , so that
        we have to only loop to check here and not query at every call of insert triples
        */
        query = "SELECT ?irreflexive {?irreflexive <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#IrreflexiveProperty>}";
        irreflexive = OWLUtilities.SDBQuery(query, "irreflexive");
        query = "SELECT ?asymmetric {?asymmetric <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#AsymmetricProperty>}";
        asymmetric = OWLUtilities.SDBQuery(query, "asymmetric");

        /* This method is to check if predicate is irreflexive and if it is does subject and object of triple to be
         * inserted are same if same don't insert
         */
        for (int i = 0; i < irreflexive.size(); i++) {
            if (irreflexive.get(i).equals(predicate)) {
                if (subject.equals(object)) {
                    dontAdd = true;
                    break;
                }
            }
        }

        // Similar thing as above for asymmetric (logic of asymmetric applies)
        if (!dontAdd) {
            for (int i = 0; i < asymmetric.size(); i++) {
                if (asymmetric.get(i).equals(predicate)) {
                    ArrayList<ArrayList<String>> queryResult = new ArrayList<ArrayList<String>>();
                    query = "SELECT ?object ?subject {?subject <" + predicate + "> ?object}";
                    queryResult = OWLUtilities.SDBQuery(query, "object", "subject");
                    for (int j = 0; j < queryResult.size(); j++) {
                        if (subject.equals(queryResult.get(i).get(0)) && object.equals(queryResult.get(i).get(1))) {
                            dontAdd = true;
                            break;
                        }
                    }
                    if (dontAdd)
                        break;
                }
            }
        }
        if (!dontAdd) {
            //add triples in data base
            model.add(Subject, Predicate, Object);
            model.commit();
        }
    }

    /*
     * For each new predicate(object property), some data-properties are tagged with them. Thus, a triple needs to be
     * inserted for each of the new predicate
     */

    public static void insertTriples(String rdf, String owl, String subject, String predicate, String object) {

        Model model = SDBFactory.connectDefaultModel(sdbUtilities.getStore());
        model.setNsPrefix(sdbUtilities.getOntologyNamespace(), sdbUtilities.getOntologyPrefix());
        model.read(sdbUtilities.getOntology());
        //create new triples
        org.apache.jena.rdf.model.Resource Subject = model.createResource(sdbUtilities.getOntologyPrefix() + subject);
        Property Predicate = model.createProperty(rdf + predicate);
        org.apache.jena.rdf.model.Resource Object = model.createResource(owl + object);
        //add triples in data base
        model.add(Subject, Predicate, Object);
        model.commit();
    }

    /*
     * Below are overloaded methods of SDBQuery, each serving a different purpose
     * Methods take a SPARQL Query in the input and returns the respective output
     */

    public static ResultSet SDBQuery(String queryString) {

        /*
         * This method is for demo i.e. used to query the sdbjena as in SPARQL API
         * The method gives exact resultset as what the SPARQL API will return without any editting
         */

        ResultSet rs = null;
        ArrayList<String> result = new ArrayList<>();
        Dataset dataset = DatasetStore.create(sdbUtilities.getStore());
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            rs = qe.execSelect();
            ResultSetFormatter.out(rs);
        }
        return rs;
    }

    /*
     * This method take a SPARQL string query, a plausible consequent subject and object and returns the triples
     */
    public static ArrayList<ArrayList<String>> SDBQuery(String queryString, String consequentSubject, String consequentObject) {

        ResultSet rs = null;
        Dataset dataset = DatasetStore.create(sdbUtilities.getStore());
        Query query = QueryFactory.create(queryString);
        ArrayList<ArrayList<String>> triple = new ArrayList<ArrayList<String>>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            rs = qe.execSelect();

            while (rs.hasNext()) {
                ArrayList<String> temp = new ArrayList<String>();
                QuerySolution q = rs.next();

                temp.add(q.get(consequentSubject).toString());
                temp.add(q.get(consequentObject).toString());
                triple.add(temp);
            }
        }
        return triple;
    }

    /*
     * This method take a SPARQL string query, a plausible consequent and it is used for literals query.
     */
    public static ArrayList<String> SDBQuery(String queryString, String consequent) {

        ResultSet rs = null;
        Dataset dataset = DatasetStore.create(sdbUtilities.getStore());
        Query query = QueryFactory.create(queryString);
        ArrayList<String> triple = new ArrayList<String>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            rs = qe.execSelect();

            while (rs.hasNext()) {
                QuerySolution q = rs.next();
                triple.add(q.get(consequent).toString());
            }
        }
        return triple;
    }

    /*
     * Generic query paradigm where any SPARQL can be triggered. The select variables of the query should be passed as
     * the argument arraylist.
     */
    public static ArrayList<ArrayList<String>> SDBQuery(String queryString, ArrayList<String> selectPart) {
        ResultSet rs = null;
        Dataset dataset = DatasetStore.create(sdbUtilities.getStore());
        Query query = QueryFactory.create(queryString);
        ArrayList<ArrayList<String>> triple = new ArrayList<ArrayList<String>>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            rs = qe.execSelect();

            while (rs.hasNext()) {
                ArrayList<String> temp = new ArrayList<String>();
                QuerySolution q = rs.next();
                for (int loop = 0; loop < selectPart.size(); loop++) {
                    temp.add(q.get(selectPart.get(loop)).toString());
                }
                triple.add(temp);
            }
        }
        return triple;
    }

    /*
     * This method uses a hard coded SPARQL query and returns all the nodes
     * Each of the nodes, also termed as class in OWL can be queried and retrieved using this method
     * The class name is followed by an URI therefore, the string is trimmed too
     */
    public static ArrayList<String> getNode() {

        ArrayList<String> node = new ArrayList<String>();
        ResultSet rs = null;
        Dataset dataset = DatasetStore.create(sdbUtilities.getStore());
        String queryString = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
                "SELECT ?x  WHERE { ?x rdf:type owl:Class}";
        String outputColumnHeader = "x";
        Query query = QueryFactory.create(queryString);

        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            rs = qe.execSelect();
            while (rs.hasNext()) {

                QuerySolution querySolution = rs.next();
                String tempNode = querySolution.get(outputColumnHeader).toString();
                String[] Node = tempNode.split("#", 0);
                node.add("?" + Node[Node.length - 1]);
            }
        }
        return node;
    }

    /*
     * This method retrieves all the possible predicates present in the ontology
     */
    public static ArrayList<String> getObjectProperties() {

        ArrayList<String> objectProperty = new ArrayList<String>();
        ResultSet rs = null;
        Dataset dataset = DatasetStore.create(sdbUtilities.getStore());
        String queryString = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
                "select distinct ?x {{?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>} UNION {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty>}}";

        String outputColumnHeader = "x";
        Query query = QueryFactory.create(queryString);

        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            rs = qe.execSelect();
            while (rs.hasNext()) {

                QuerySolution querySolution = rs.next();
                String tempObjectProperty = querySolution.get(outputColumnHeader).toString();
                String[] trimmedObjectProperty = tempObjectProperty.split("#", 0);
                objectProperty.add(trimmedObjectProperty[trimmedObjectProperty.length - 1]);
            }
        }
        //Adding additional predicates that can appear in preidcate part but are not ObjectProperties or DatatypeProperties
        objectProperty.add("subClassOf");
        objectProperty.add("subPropertyOf");
        objectProperty.add("type");
        objectProperty.add("inverseOf");
        objectProperty.add("domain");
        objectProperty.add("range");
        return objectProperty;
    }

    /*
     * Create the query as per the query and select mechanism. Executes the query created
     */
    public static ArrayList<ArrayList<String>> executeUserQuery(ArrayList<String> queryPart, ArrayList<String> selectPart) {
        Hashtable<String, Integer> hash_table = new Hashtable<String, Integer>();
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
        int index, loop = 0;
        int len = queryPart.size();
        String query = "";
        String left = "";
        String right = "";
        String prefix = sdbUtilities.getOntologyPrefix();
        System.out.println(prefix);

        /* from the entire query part , put parts with '?' into selectPart ArrayList
         * this will become the variables that we get as output, hash table is used to ensure every variable
         * appears only once in ouput
         */
        for (int loopIn = 0; loopIn < queryPart.size(); loopIn++) {
            if (queryPart.get(loopIn).charAt(0) == '?' && !(hash_table.containsKey(queryPart.get(loopIn)))) {
                selectPart.add(queryPart.get(loopIn));
                hash_table.put(queryPart.get(loopIn), 10);
            }
        }

        /* Start creating the query by putting all the entries in select Part
         *
         */
        query = query + "Select ";
        for (int loopIn = 0; loopIn < selectPart.size(); loopIn++) {
            query = query + selectPart.get(loopIn) + " ";
        }


        query = query + "{ ";

        /*
         * For the subject , predicate and object before the first connector make them as string that would
         * appear in the query and store in "left"
         */

        /*
         *  if ? then it is a variable and no need to apply prefix else apply the relevant prefix
         * since for subject prefix can be only ontology prefix so no for if-else conditions
         * */
        if (queryPart.get(0).charAt(0) == '?')
            left = queryPart.get(0);
        else
            left = " <" + prefix + queryPart.get(0) + "> ";
        //if-else conditions for checking what prefix to apply to predicate
        if (queryPart.get(1).equalsIgnoreCase("subClassOf") || queryPart.get(1).equalsIgnoreCase("subPropertyOf") || queryPart.get(1).equalsIgnoreCase("domain") || queryPart.get(1).equalsIgnoreCase("range")) {
            prefix = "http://www.w3.org/2000/01/rdf-schema#";
            left = left + " <" + prefix + queryPart.get(1) + "> ";
        } else if (queryPart.get(1).equalsIgnoreCase("type")) {
            prefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
            left = left + " <" + prefix + queryPart.get(1) + "> ";
        } else if (queryPart.get(1).equalsIgnoreCase("inverseOf")) {
            prefix = "http://www.w3.org/2002/07/owl#";
            left = left + " <" + prefix + queryPart.get(1) + "> ";
        } else       //Here we can have one more else if condition for owl prefix
            left = left + " <" + prefix + queryPart.get(1) + "> ";
        prefix = sdbUtilities.getOntologyPrefix();

        //if charAt(0)='  it means we hava data of data type property so no need to apply prefix
        if (queryPart.get(2).charAt(0) == '?' || queryPart.get(2).charAt(0) == '\'')
            left = left + queryPart.get(2) + " ";
        else if (queryPart.get(2).equalsIgnoreCase("Ontology") || queryPart.get(2).equalsIgnoreCase("Thing") || queryPart.get(2).equalsIgnoreCase("Class") || queryPart.get(2).equalsIgnoreCase("ObjectProperty") || queryPart.get(2).equalsIgnoreCase("NamedIndividual") || queryPart.get(2).equalsIgnoreCase("DatatypeProperty") || queryPart.get(2).equalsIgnoreCase("SymmetricProperty") || queryPart.get(2).equalsIgnoreCase("TransitiveProperty") || queryPart.get(2).equalsIgnoreCase("ReflexiveProperty") || queryPart.get(2).equalsIgnoreCase("IrreflexiveProperty") || queryPart.get(2).equalsIgnoreCase("AsymmetricProperty") || queryPart.get(2).equalsIgnoreCase("FunctionalProperty") || queryPart.get(2).equalsIgnoreCase("InverseFunctionalProperty")) {
            prefix = "http://www.w3.org/2002/07/owl#";
            left = left + "<" + prefix + queryPart.get(2) + "> ";
        } else
            left = left + "<" + prefix + queryPart.get(2) + "> ";
        prefix = sdbUtilities.getOntologyPrefix();


        //index refer to the index of connector being considered currently
        index = (3 * (loop + 1)) + loop;
        while (index < len) {
            loop++;
            //right holds the subject,predicate,object present immediately after the connector
            if (queryPart.get(index + 1).charAt(0) == '?')
                right = queryPart.get(index + 1);
            else
                right = " <" + prefix + queryPart.get(index + 1) + "> ";
            if (queryPart.get(index + 2).equalsIgnoreCase("subClassOf") || queryPart.get(index + 2).equalsIgnoreCase("subPropertyOf") || queryPart.get(index + 2).equalsIgnoreCase("range") || queryPart.get(index + 2).equalsIgnoreCase("domain")) {
                prefix = "http://www.w3.org/2000/01/rdf-schema#";
                right = right + " <" + prefix + queryPart.get(index + 2) + "> ";
            } else if (queryPart.get(index + 2).equalsIgnoreCase("type")) {
                prefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
                right = right + " <" + prefix + queryPart.get(index + 2) + "> ";
            } else if (queryPart.get(index + 2).equalsIgnoreCase("inverseOf")) {
                prefix = "http://www.w3.org/2002/07/owl#";
                right = right + " <" + prefix + queryPart.get(index + 2) + "> ";
            } else
                right = right + " <" + prefix + queryPart.get(index + 2) + "> ";
            prefix = sdbUtilities.getOntologyPrefix();
            //right = right + " <" + prefix + queryPart.get(index+2) + "> " ;
            if (queryPart.get(index + 3).charAt(0) == '?' || queryPart.get(index + 3).charAt(0) == '\'')
                right = right + queryPart.get(index + 3) + " ";
            else if (queryPart.get(index + 3).equalsIgnoreCase("Ontology") || queryPart.get(index + 3).equalsIgnoreCase("Thing") || queryPart.get(index + 3).equalsIgnoreCase("Class") || queryPart.get(index + 3).equalsIgnoreCase("ObjectProperty") || queryPart.get(index + 3).equalsIgnoreCase("NamedIndividual") || queryPart.get(index + 3).equalsIgnoreCase("DatatypeProperty") || queryPart.get(index + 3).equalsIgnoreCase("SymmetricProperty") || queryPart.get(index + 3).equalsIgnoreCase("TransitiveProperty") || queryPart.get(index + 3).equalsIgnoreCase("ReflexiveProperty") || queryPart.get(index + 3).equalsIgnoreCase("IrreflexiveProperty") || queryPart.get(index + 3).equalsIgnoreCase("AsymmetricProperty") || queryPart.get(index + 3).equalsIgnoreCase("FunctionalProperty") || queryPart.get(index + 3).equalsIgnoreCase("InverseFunctionalProperty")) {
                prefix = "http://www.w3.org/2002/07/owl#";
                right = right + "<" + prefix + queryPart.get(index + 3) + "> ";
            } else
                right = right + "<" + prefix + queryPart.get(index + 3) + "> ";
            prefix = sdbUtilities.getOntologyPrefix();
            //left holds entire query before the present connector


            if ((queryPart.get(index)).equals("OR")) {
                //For Union both LHS and RHS must be in { }
                left = "{ " + left + " }" + " UNION " + "{ " + right + " }";
            } else if ((queryPart.get(index)).equals("AND")) {
                left = left + " . " + right;
            }
            //updating index to get to next connector
            index = (3 * (loop + 1)) + loop;
        }
        query = query + left + " }";
        System.out.println(query);
        result = OWLUtilities.SDBQuery(query, selectPart);
        return result;
    }
}
