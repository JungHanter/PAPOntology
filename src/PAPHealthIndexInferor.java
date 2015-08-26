import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import ssu.soft.papont.PAPProcess;
import ssu.soft.papont.PAPUri;

import java.util.List;

/**
 * Created by hanter on 15. 8. 25..
 */
@PAPUri("http://soft.ssu.ac.kr/ontology/paptest#hasHealthIndex")
public class PAPHealthIndexInferor implements PAPProcess {

    @Override
    public RDFNode onProcess(RDFNode subject, Model model) {
        System.out.println("hi");

        return null;
    }

    @Override
    public List<Statement> onPostProcess(RDFNode subject, Model model) {

        return null;
    }

    private void inferHealthIndex() {

    }

}
