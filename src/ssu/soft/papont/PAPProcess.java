package ssu.soft.papont;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

import java.util.List;

/**
 * Created by hanter on 15. 8. 23..
 */
public interface PAPProcess {
    public RDFNode onProcess(RDFNode subject, Model model);
    public List<Statement> onPostProcess(RDFNode subject, Model model);
}
