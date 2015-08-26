package ssu.soft.papont;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
 * Created by hanter on 15. 8. 23..
 */
public class PAPExecutor {
    private boolean D = true;

    private Map<String, PAPProcess> papProcessMap;

    public PAPExecutor() {
        papProcessMap = new HashMap<>();
    }

    public void bindProcess(PAPProcess papProcess) {
        PAPUri uriAnno = papProcess.getClass().getDeclaredAnnotation(PAPUri.class);
        String predicateURI = uriAnno.value();

        papProcessMap.put(predicateURI, papProcess);
        //System.out.println("Add New PAP URI = " + predicateURI);      //debug log
    }

    public void executeProcess(Model model) {
        Set<String> processURIKeySet = papProcessMap.keySet();
        for (String processURI : processURIKeySet) {
            if(D) findDomain(processURI, model);
            List<RDFNode> subjectList = findSubject(processURI, model);

            for(RDFNode subject : subjectList) {
                //process predicate
                final PAPProcess process = papProcessMap.get(processURI);
                RDFNode resultObject = process.onProcess(subject, model);

                //add result object into model

                //add addtional triples
                List<Statement> additionalTriples = process.onPostProcess(subject, model);
                if(additionalTriples != null) model.add(additionalTriples);
            }
        }
    }


    /**
     * @deprecated
     * This is UNUSED function. Only use for debugging.
     * For find subject of domain, please use <code>{@link #findSubject(String, Model)}</code>
     */
    @Deprecated
    private List<RDFNode> findDomain(String predicateURI, Model model) {
        List<RDFNode> nodeList = new ArrayList<>();

        predicateURI = '<' + predicateURI + '>';
        QueryExecution qexec = makeQueryExec("SELECT ?domain WHERE { " +
                        predicateURI + " rdfs:subPropertyOf pap:process . " +
                        predicateURI + " rdfs:domain ?superDomain . " +
                        "?domain rdfs:subClassOf ?superDomain }", model);
        try {
            ResultSet rs = qexec.execSelect();
            while(rs.hasNext()) {
                QuerySolution soln = rs.nextSolution();
                RDFNode domainResource = soln.get("?domain");
                if(domainResource != null) {
                    nodeList.add(domainResource);
                }
            }
        } finally {
            qexec.close();
        }

        if(D) {
            System.out.println("<PAP_DEBUG_MSG> Find domainList for Predicate: " + predicateURI);
            for (RDFNode domain : nodeList) {
                System.out.println("\t - " + domain.toString());
            }
            System.out.println();
        }

        return nodeList;
    }

    private List<RDFNode> findSubject(String predicateURI, Model model) {
        if(D) System.out.println("<PAP_DEBUG_MSG> Find subjectList for Predicate: " + predicateURI);

        List<RDFNode> nodeList = new ArrayList<>();

        predicateURI = '<' + predicateURI + '>';
        QueryExecution qexec = makeQueryExec("SELECT DISTINCT ?subject " + //"?domain " +
                " WHERE { " +
                predicateURI + " rdfs:subPropertyOf pap:process . " +
                predicateURI + " rdfs:domain ?superDomain . " +
                "?domain rdfs:subClassOf ?superDomain . " +
                "?subject rdf:type ?domain }", model);
        try {
            ResultSet rs = qexec.execSelect();
            while(rs.hasNext()) {
                QuerySolution soln = rs.nextSolution();
                RDFNode subjectResource = soln.get("?subject");
                //RDFNode domainResource = soln.get("?domain");
                if(subjectResource != null) {
                    nodeList.add(subjectResource);

                    if(D) {
                        System.out.println("\t - " + subjectResource.toString());/* + " typeOf "
                                + domainResource.toString());*/
                    }
                }
            }
        } finally {
            qexec.close();
        }

        if(D) System.out.println();

        return nodeList;
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

        //Now add query
        queryStr.append(queryRequest);
        Query query = QueryFactory.create(queryStr.toString());
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        return qexec;
    }
}

