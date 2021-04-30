package SWRE.Ontology2SDB2MySQL;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.sql.JDBC;
import org.apache.jena.sdb.sql.SDBConnection;
import org.apache.jena.sdb.store.DatabaseType;
import org.apache.jena.sdb.store.LayoutType;

/*
 * Description:	Enables various utilities required to import an RDF/XML ontology to a relational database. This can also be used as a relational
 * 				triple store.
 * 				Semantic Web ontology is stored in MySQL using SDB Jena which is abstracted over JDBC. Various model implementation e.g. GraphDB
 * 				can be used to represent the ontology. 
 * 				SPARQL can be used to query the data.
 * 				All the code snippets are extracted from https://jena.apache.org/documentation/sdb/
 * 				Further examples taken into consideration are https://github.com/apache/jena/tree/master/jena-sdb/src-examples/sdb/examples
 */
public class SDBUtilities {
	
	// JDBC Configuration Variables
	private static String jdbcURL;
	private static String dbusername;
	private static String dbpassword;
	private static String jdbcDriver;
	private static String ontology;
	private static String ontologyPrefix;
	private static String ontologyNamespace;
	private static StoreDesc storeDesc;
	private static SDBConnection sdbconnection;
	private static Store store;
	
	public SDBUtilities() {
		// TODO Auto-generated constructor stub
		jdbcURL = null;
		dbusername = null;
		dbpassword = null;
		jdbcDriver = null;
		ontology = null;
		ontologyPrefix = null;
		ontologyNamespace = null;
		storeDesc = null;
		sdbconnection = null;
		store = null;
	}

	public static String getJdbcDriver() {
		return jdbcDriver;
	}
	public static void setJdbcDriver(String jdbcDriver) {
		SDBUtilities.jdbcDriver = jdbcDriver;
	}
	public static String getOntologyPrefix(){ return ontologyPrefix; }
	public static String getOntologyNamespace () { return ontologyNamespace; }
	public static String getOntology(){ return ontology; }
	public static StoreDesc getStoreDesc(){ return storeDesc; }
	public static SDBConnection getSdbconnection() { return sdbconnection; }
	public static Store getStore() { return store; }

	// Reads the configuration file and updates JDBC variables 
	public static void DBinit() throws Exception{

		/*
		 * The dbconfig.properties file is read twice as it dymically changes the filepath as per the newly inserted
		 * ontology. Initially the ontology file is uploaded with temporary values and later updated with required values.
		 */

		InputStream inputStream = SDBUtilities.class.getClassLoader().getResourceAsStream("dbconfig.properties");
		Properties property = new Properties();
		property.load(inputStream);
		String targetPath = property.getProperty("TARGET_PATH");
		inputStream.close();

		String newConfigPath = targetPath + "dbconfig.properties";
		PropertiesConfiguration updatedProperties = new PropertiesConfiguration(newConfigPath);

		jdbcURL = updatedProperties.getString("SDB_URL");
		dbusername = updatedProperties.getString("SDB_USERNAME");
		dbpassword = updatedProperties.getString("SDB_PASSWORD");
		setJdbcDriver(updatedProperties.getString("JDBC_DRIVER"));
		ontology = updatedProperties.getString("ONTOLOGY");
		ontologyPrefix = updatedProperties.getString("ONTOLOGY_PREFIX");
		ontologyNamespace = updatedProperties.getString("ONTOLOGY_NAMESPACE");
		System.out.println(ontology+" "+ontologyPrefix+" "+ontologyNamespace);

		/* Creates a storage description for the MySQL Database, here TripleNodeHash means that each node in the RDF/XML file
		 * will be provided a hash and the triples table will consists all the hashes.
		 * The specified database is MySQL
		 */

		storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL);
		JDBC.loadDriverMySQL();
		// Connecting SDB to JDBC
		sdbconnection = new SDBConnection(jdbcURL, dbusername, dbpassword);
		// Each table/database in SDB is considered as a store
		store = SDBFactory.connectStore(sdbconnection, storeDesc);
	}

	/*
	 * For better handling of database, minimise loose ends
	 */

	public static void DBclose(){
		store.close();
		sdbconnection.close();
	}

	/*
	 * Function takes in an OWL RDF/XML file, a namespace and the main prefix
	 * If connects the database for the first time, creates necessary changes in the database i.e. tables
	 * Creates a SDB Store and parses the OWL into relational model 
	 */
	public static String ont2SDB2SQL() {

		System.out.println(ontology+" "+ontologyPrefix+" "+ontologyNamespace);
		// One time operation. Formats the tables in database namely, Nodes, Triples, Prefix and Quads
		store.getTableFormatter().create();
		// These models are provided by Jena. Use GraphDB if you want to store graphical data in relational format
		Model model = SDBFactory.connectDefaultModel(store);
		model.setNsPrefix(ontologyNamespace,ontologyPrefix);
		model.read(ontology);
		model.commit();
		return "success";
	}
	
	/*
	 * Kills the database in MySQL and re-arranges the database
	 */
	public static void dbstoreKill() {

		store.getTableFormatter().truncate();
	}
}
