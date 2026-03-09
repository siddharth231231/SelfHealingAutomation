package selfhealing.healing;

import java.util.LinkedHashMap;
import java.util.Map;

public class LiveNodeCandidate {

    private String xpath;
    private Map<String, Object> nodePath = new LinkedHashMap<>();

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public Map<String, Object> getNodePath() {
        return nodePath;
    }

    public void setNodePath(Map<String, Object> nodePath) {
        this.nodePath = nodePath;
    }
}
