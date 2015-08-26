import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import ssu.soft.papont.PAPExecutor;
import ssu.soft.papont.PAPProcess;

import javax.xml.transform.Result;
import java.io.InputStream;

/**
 * Created by hanter on 15. 8. 23..
 */
public class PAPOntTest {
    static final String ontologyDir = "../ontologies/";
    static final String defaultNameSpace = "http://soft.ssu.ac.kr/ontology/paptest#";

    Model papSchemaModel = null;
    Model patientSchemaModel = null;
    InfModel infPatientModel = null;

    PAPExecutor papExecutor = null;
    PAPProcess inferor = null;

    public static void main (String args[]) {
        PAPOntTest testApp = new PAPOntTest();

        testApp.loadPapModel();
        testApp.loadPatientModel();

        testApp.alignmentPapPatientModel();
        testApp.checkSubPropertyOfPAPProcess();

        testApp.addPatientData();
        testApp.checkPatientData();

        testApp.inferHealthIndex();

    }

    private void loadPapModel() {
        papSchemaModel = ModelFactory.createOntologyModel();
        InputStream papOWL = FileManager.get().open(ontologyDir + "pap.n3");
        papSchemaModel.read(papOWL, defaultNameSpace, "TTL");
        try {
            papOWL.close();
        } catch (Exception e) {}
    }

    private void loadPatientModel() {
        patientSchemaModel = ModelFactory.createOntologyModel();
        InputStream papTestOWL = FileManager.get().open(ontologyDir + "paptest.n3");
        patientSchemaModel.read(papTestOWL, defaultNameSpace, "TTL");
        try {
            papTestOWL.close();
        } catch (Exception e) {}
    }

    private void alignmentPapPatientModel() {
        patientSchemaModel.add(papSchemaModel);
    }

    private void addPatientData() {
        //User 1
        Resource resource = patientSchemaModel.createResource(defaultNameSpace + "testuser");
        Property prop = patientSchemaModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Resource obj = patientSchemaModel.createResource(defaultNameSpace + "patient");
        patientSchemaModel.add(resource, prop, obj);

        resource = patientSchemaModel.createResource(defaultNameSpace + "data-pulse-0");
        prop = patientSchemaModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        obj = patientSchemaModel.createResource(defaultNameSpace + "BloodPulse");
        patientSchemaModel.add(resource, prop, obj);

        prop = patientSchemaModel.createProperty(defaultNameSpace + "dataValue");
        Literal lit = patientSchemaModel.createTypedLiteral(76);
        patientSchemaModel.add(resource, prop, lit);

        resource = patientSchemaModel.createResource(defaultNameSpace + "data-bmi-0");
        prop = patientSchemaModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        obj = patientSchemaModel.createResource(defaultNameSpace + "BMI");
        patientSchemaModel.add(resource, prop, obj);

        prop = patientSchemaModel.createProperty(defaultNameSpace + "dataValue");
        lit = patientSchemaModel.createTypedLiteral(32);
        patientSchemaModel.add(resource, prop, lit);

        resource = patientSchemaModel.createResource(defaultNameSpace + "testuser");
        prop = patientSchemaModel.createProperty(defaultNameSpace + "hasMedicalData");
        obj = patientSchemaModel.createResource(defaultNameSpace + "data-pulse-0");
        patientSchemaModel.add(resource, prop, obj);

        resource = patientSchemaModel.createResource(defaultNameSpace + "testuser");
        prop = patientSchemaModel.createProperty(defaultNameSpace + "hasMedicalData");
        obj = patientSchemaModel.createResource(defaultNameSpace + "data-bmi-0");
        patientSchemaModel.add(resource, prop, obj);

        //User 2
        resource = patientSchemaModel.createResource(defaultNameSpace + "testuser2");
        prop = patientSchemaModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        obj = patientSchemaModel.createResource(defaultNameSpace + "emergencyPatient");
        patientSchemaModel.add(resource, prop, obj);

        resource = patientSchemaModel.createResource(defaultNameSpace + "data-pulse-1");
        prop = patientSchemaModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        obj = patientSchemaModel.createResource(defaultNameSpace + "BloodPulse");
        patientSchemaModel.add(resource, prop, obj);

        prop = patientSchemaModel.createProperty(defaultNameSpace + "dataValue");
        lit = patientSchemaModel.createTypedLiteral(76);
        patientSchemaModel.add(resource, prop, lit);

        resource = patientSchemaModel.createResource(defaultNameSpace + "data-bmi-1");
        prop = patientSchemaModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        obj = patientSchemaModel.createResource(defaultNameSpace + "BMI");
        patientSchemaModel.add(resource, prop, obj);

        prop = patientSchemaModel.createProperty(defaultNameSpace + "dataValue");
        lit = patientSchemaModel.createTypedLiteral(32);
        patientSchemaModel.add(resource, prop, lit);

        resource = patientSchemaModel.createResource(defaultNameSpace + "testuser2");
        prop = patientSchemaModel.createProperty(defaultNameSpace + "hasMedicalData");
        obj = patientSchemaModel.createResource(defaultNameSpace + "data-pulse-1");
        patientSchemaModel.add(resource, prop, obj);

        resource = patientSchemaModel.createResource(defaultNameSpace + "testuser2");
        prop = patientSchemaModel.createProperty(defaultNameSpace + "hasMedicalData");
        obj = patientSchemaModel.createResource(defaultNameSpace + "data-bmi-1");
        patientSchemaModel.add(resource, prop, obj);
    }

    private void inferHealthIndex() {
        inferor = new PAPHealthIndexInferor();

        papExecutor = new PAPExecutor();
        papExecutor.bindProcess(inferor);
        papExecutor.executeProcess(patientSchemaModel);
    }

    private void checkSubPropertyOfPAPProcess() {
        System.out.println("=====   SubPropertyOf PAPProcess   =====");

        QueryExecution qexec = makeQueryExec("SELECT ?sub WHERE { ?sub rdfs:subPropertyOf pap:process }", patientSchemaModel);
        try {
            ResultSet rs = qexec.execSelect();
            //ResultSet rs = testApp.runQuery("SELECT ?prd WHERE { ?prd a rdf:Property }", testApp.patientSchemaModel);
            if(!rs.hasNext()) System.out.println("No found!");
            else while( rs.hasNext()){
                QuerySolution soln = rs.nextSolution();
                RDFNode sub = soln.get("?sub");
                if( sub != null ){
                    System.out.println( sub.toString() );
                }
                else
                    System.out.println("Not");
            }
        } finally {
            qexec.close();
        }

        System.out.println();
    }

    private void checkPatientData() {
        System.out.println("=====   PatientData   =====");

        QueryExecution qexec = makeQueryExec("SELECT ?medicalData ?value" +
                " WHERE { paptest:testuser paptest:hasMedicalData ?medicalData . " +
                " ?medicalData paptest:dataValue ?value }", patientSchemaModel);
        try {
            ResultSet rs = qexec.execSelect();
            //ResultSet rs = testApp.runQuery("SELECT ?prd WHERE { ?prd a rdf:Property }", testApp.patientSchemaModel);
            if(!rs.hasNext()) System.out.println("No found!");
            else while( rs.hasNext()){
                QuerySolution soln = rs.nextSolution();
                RDFNode medicalData = soln.get("?medicalData");
                RDFNode value = soln.get("?value");
                if( medicalData != null ){
                    System.out.println( medicalData.toString() + ' ' + value.toString() );
                }
                else
                    System.out.println("Not");
            }
        } finally {
            qexec.close();
        }

        System.out.println();
    }

    private QueryExecution makeQueryExec(String queryRequest, Model model){

        StringBuffer queryStr = new StringBuffer();
        // Establish Prefixes
        //Set default Name space first
        queryStr.append("PREFIX rdfs" + ": <" + "http://www.w3.org/2000/01/rdf-schema#" + "> ");
        queryStr.append("PREFIX rdf" + ": <" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
        queryStr.append("PREFIX xsd" + ": <" + "http://www.w3.org/2001/XMLSchema#" + "> ");
        queryStr.append("PREFIX owl" + ": <" + "http://www.w3.org/2002/07/owl#" + "> ");
        queryStr.append("PREFIX pap" + ": <" + "http://soft.ssu.ac.kr/ontology/pap#" + "> ");
        queryStr.append("PREFIX paptest" + ": <" + defaultNameSpace + "> ");

        //Now add query
        queryStr.append(queryRequest);
        Query query = QueryFactory.create(queryStr.toString());
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        return qexec;
    }

    private void runQuerySearchAll(Model model){

        StringBuffer queryStr = new StringBuffer();
        // Establish Prefixes
        //Set default Name space first
        queryStr.append("PREFIX paptest" + ": <" + defaultNameSpace + "> ");
        queryStr.append("PREFIX rdfs" + ": <" + "http://www.w3.org/2000/01/rdf-schema#" + "> ");
        queryStr.append("PREFIX rdf" + ": <" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
        queryStr.append("PREFIX xsd" + ": <" + "http://www.w3.org/2001/XMLSchema#" + "> ");
        queryStr.append("PREFIX owl" + ": <" + "http://www.w3.org/2002/07/owl#" + "> ");
        queryStr.append("PREFIX pap" + ": <" + "http://soft.ssu.ac.kr/ontology/pap#" + "> ");

        //Now add query
        queryStr.append("SELECT ?sub ?prd ?obj WHERE { ?sub ?prd ?obj }");
        Query query = QueryFactory.create(queryStr.toString());
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet response = qexec.execSelect();
            //qexec.close();

            while( response.hasNext()){
                QuerySolution soln = response.nextSolution();
                RDFNode sub = soln.get("?sub");
                RDFNode prd = soln.get("?prd");
                RDFNode obj = soln.get("?obj");
                if( sub != null ){
                    System.out.println( sub.toString() + ' ' + prd.toString() + ' ' + obj.toString() );
                }
                else
                    System.out.println("No found!");
            }
        } finally { qexec.close();}
    }
}
