package SWRE.WebApplication;

import SWRE.ruleChaining.BackwardChaining;
import SWRE.ruleChaining.ForwardChaining;
import SWRE.ruleGenerator.RuleBox;
import SWRE.ruleGenerator.RuleJson;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/*
 * API to trigger chaining.
 *  /chaining/forwardChaining to trigger forward chaining
 *  /chaining/backwardChaining to trigger backward chaining
 */

@Path("chaining")
public class ChainingController {

    /*
     * For each run, certain rules get stored in the cache i.e. rules selected by the user for one run
     */
    static private ArrayList<ArrayList<String>> ruleCache = null;

    /*
     * Method fetches the rules from front-end and dumps them into an xml file
     * Re-fetches them and feed to forward chaining paradigm
     */

    @POST
    @Path("/forwardChaining")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response updateDetails(RuleJson ruleIndex) throws Exception {

        int len = ruleIndex.getRules().size();
        ruleCache = new ArrayList<>();

        RuleBox ruleBox = new RuleBox();
        ruleBox.init(true);
        ArrayList<ArrayList<String>> rules = ruleBox.getRules();

        // Adding rules to the XML file

        for(int i=0;i<len;i++){
            int idx = Integer.parseInt(ruleIndex.getRules().get(i));
            ruleCache.add(rules.get(idx));
        }

        // Forward Chaining
        System.out.println("\t\t\t*****************FORWARD CHAINING*****************");
        ForwardChaining.ForwardChaining(ruleCache);
        System.out.println("\t\t\t######Selected Explicit Rules Parsed######");
        ruleBox.init(false);
        ruleCache = ruleBox.getRules();
        ForwardChaining.ForwardChaining(ruleCache);
        System.out.println("\t\t\t######Implicit Rules Parsed######");
        return Response.ok().build();
    }

    /*
     * Backward chaining works on a singlet triple
     * Fetches the query and runs backward chaining
     */

    @POST
    @Path("/backwardChaining")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.TEXT_PLAIN})
    public String backQuery( RuleJson re) throws Exception {


        String subject = re.getRules().get(0);
        String predicate = re.getRules().get(1);
        String object = re.getRules().get(2);

        System.out.println(predicate);

        boolean result = BackwardChaining.backwardChaining(subject, predicate, object);
        if(result == true)
            return "true";
        else
            return "false";
    }
}
