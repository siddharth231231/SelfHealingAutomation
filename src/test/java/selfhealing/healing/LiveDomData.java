package selfhealing.healing;

import java.util.ArrayList;
import java.util.List;

public class LiveDomData {

    private String usedAnchor;
    private String rawDom;
    private String cleanedDom;
    private String structuralFingerprintJson;
    private String liveNeighborhoodHash;
    private List<LiveNodeCandidate> candidates = new ArrayList<>();

    public String getUsedAnchor() {
        return usedAnchor;
    }

    public void setUsedAnchor(String usedAnchor) {
        this.usedAnchor = usedAnchor;
    }

    public String getRawDom() {
        return rawDom;
    }

    public void setRawDom(String rawDom) {
        this.rawDom = rawDom;
    }

    public String getCleanedDom() {
        return cleanedDom;
    }

    public void setCleanedDom(String cleanedDom) {
        this.cleanedDom = cleanedDom;
    }

    public String getStructuralFingerprintJson() {
        return structuralFingerprintJson;
    }

    public void setStructuralFingerprintJson(String structuralFingerprintJson) {
        this.structuralFingerprintJson = structuralFingerprintJson;
    }

    public String getLiveNeighborhoodHash() {
        return liveNeighborhoodHash;
    }

    public void setLiveNeighborhoodHash(String liveNeighborhoodHash) {
        this.liveNeighborhoodHash = liveNeighborhoodHash;
    }

    public List<LiveNodeCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<LiveNodeCandidate> candidates) {
        this.candidates = candidates;
    }
}
