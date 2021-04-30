package SWRE.WebApplication;

import SWRE.Ontology2SDB2MySQL.SDBUtilities;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.apache.jena.base.Sys;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.nio.file.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * This class loads the ontology i.e. the default University Ontology or the new ontology to be uploaded by the user
 */

@Path("/DataLoader")
public class OntologyController {

    private void setDatabase() throws Exception {

        SDBUtilities sdbUtilities = new SDBUtilities();
        sdbUtilities.DBinit();
        System.out.println("Created new database");
        sdbUtilities.ont2SDB2SQL();
        sdbUtilities.DBclose();
    }

    @POST
    @Path("/NewOntology")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadNewOntology(
            @FormDataParam("dbname") String database,
            @FormDataParam("prefixname") String prefix,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception {

        System.out.println("Loading New Ontology!");
        InputStream inputStream = SDBUtilities.class.getClassLoader().getResourceAsStream("dbconfig.properties");
        Properties property = new Properties();
        property.load(inputStream);

        String targetPath = property.getProperty("TARGET_PATH");
        String JDBCDriver = property.getProperty("MYSQL")+property.getProperty("PORT")+"/";

        System.out.println(targetPath);

        inputStream.close();

        System.out.println(database);
        System.out.println(prefix);

        // creating path
        String explicitRule = targetPath + "explicitRules.xml";
        String implicitRule = targetPath + "implicitRules.xml";
        String ontology = targetPath + fileDetail.getFileName();

        // Setting up database if it does not exist
        JDBCDriver = JDBCDriver + database + "?createDatabaseIfNotExist=true";

        String fileLocation = targetPath + fileDetail.getFileName();

        try {
            FileOutputStream out ;
            int read = 0;
            byte[] bytes = new byte[1024];
            out = new FileOutputStream(new File(fileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Updating the configuration file

        try {
            PropertiesConfiguration updatedProperties = new PropertiesConfiguration(targetPath+"dbconfig.properties");
            updatedProperties.setProperty("SDB_URL", JDBCDriver);
            updatedProperties.setProperty("ONTOLOGY", ontology);
            updatedProperties.setProperty("ONTOLOGY_NAMESPACE", database);
            updatedProperties.setProperty("ONTOLOGY_PREFIX", prefix);
            updatedProperties.setProperty("EXPLICIT_RULE_STORE",explicitRule);
            updatedProperties.setProperty("IMPLICIT_RULE_STORE",implicitRule);
            updatedProperties.save();
        }
        catch (ConfigurationException e) {
            e.printStackTrace();
        }

        String output = "System Message: File successfully uploaded to : " + fileLocation;
        System.out.println(output);

        // Setting the database
        setDatabase();

        return Response.ok().build();
    }

    /*
     * Sets the path variables and other required variable as per the default University ontology
     * (COMMENT THE FUNCTION FOR NORMAL USE ELSE RULES AND DATABASE WILL BE CREATED, SLOWING THE SYSTEM)
     */
    @GET
    @Path("/University")
    public Response loadUniversityOntology() throws Exception {

        System.out.println("I am here, check for updates!");
        InputStream inputStream = SDBUtilities.class.getClassLoader().getResourceAsStream("dbconfig.properties");
        Properties property = new Properties();
        property.load(inputStream);

        // Loading pre-existing variables to update the configuration file
        String targetPath = property.getProperty("TARGET_PATH");
        String explicitRule = property.getProperty("EXPLICIT_RULE_STORE");
        String implicitRule = property.getProperty("IMPLICIT_RULE_STORE");
        String JDBCDriver = property.getProperty("MYSQL")+property.getProperty("PORT")+"/";
        String defaultOntology = property.getProperty("DEFAULT_ONTOLOGY");
        String defaultOntologyNamespace = property.getProperty("DEFAULT_ONTOLOGY_NAMESPACE");
        String defaultOntologyPrefix = property.getProperty("DEFAULT_ONTOLOGY_PREFIX");
        String defaultExplicitRule = property.getProperty("DEFAULT_EXPLICIT_RULE_STORE");
        String defaultImplicitRule = property.getProperty("DEFAULT_IMPLICIT_RULE_STORE");

        System.out.println(targetPath);

        // Creating paths
        explicitRule = targetPath + explicitRule;
        implicitRule = targetPath + implicitRule;
        defaultOntology = targetPath + defaultOntology;

        // Deleting the old implicit and explicit rules and creating new rules
        Files.deleteIfExists(Paths.get(explicitRule));
        Files.deleteIfExists(Paths.get(implicitRule));

        explicitRule = targetPath + defaultExplicitRule;
        implicitRule = targetPath + defaultImplicitRule;

        // Setting up database if it does not exist
        JDBCDriver = JDBCDriver + "SWRE?createDatabaseIfNotExist=true";

        inputStream.close();

        // Updating configuration file
        try {
            PropertiesConfiguration updatedProperties = new PropertiesConfiguration(targetPath+"dbconfig.properties");
            updatedProperties.setProperty("SDB_URL", JDBCDriver);
            updatedProperties.setProperty("ONTOLOGY", defaultOntology);
            updatedProperties.setProperty("ONTOLOGY_NAMESPACE", defaultOntologyNamespace);
            updatedProperties.setProperty("ONTOLOGY_PREFIX", defaultOntologyPrefix);
            updatedProperties.setProperty("EXPLICIT_RULE_STORE",explicitRule);
            updatedProperties.setProperty("IMPLICIT_RULE_STORE",implicitRule);
            updatedProperties.save();
            System.out.println("I am here, check for updates");
        }
        catch (ConfigurationException e) {
            e.printStackTrace();
        }
        /*
         * Uncomment the below for regularily refreshing with new database
         */
        //setDatabase();
        return Response.ok().build();
    }
}
