package it.gmarseglia.app.entity;

public class Metrics {

    private long LOC;
    private long NR;
    private long NAuth;
    private long LOCAdded;
    private long maxLOCAdded;
    private long avgLOCAdded;
    private long Churn;
    private long maxChurn;
    private long avgChurn;
    private long ChangeSetSize;
    private long maxChangeSetSize;
    private long avgChangeSetSize;

    public long getLOC() {
        return LOC;
    }

    public void setLOC(long LOC) {
        this.LOC = LOC;
    }

    public long getNR() {
        return NR;
    }

    public void setNR(long NR) {
        this.NR = NR;
    }

    public long getNAuth() {
        return NAuth;
    }

    public void setNAuth(long NAuth) {
        this.NAuth = NAuth;
    }

    public long getLOCAdded() {
        return LOCAdded;
    }

    public void setLOCAdded(long LOCAdded) {
        this.LOCAdded = LOCAdded;
    }

    public long getMaxLOCAdded() {
        return maxLOCAdded;
    }

    public void setMaxLOCAdded(long maxLOCAdded) {
        this.maxLOCAdded = maxLOCAdded;
    }

    public long getAvgLOCAdded() {
        return avgLOCAdded;
    }

    public void setAvgLOCAdded(long avgLOCAdded) {
        this.avgLOCAdded = avgLOCAdded;
    }

    public long getChurn() {
        return Churn;
    }

    public void setChurn(long churn) {
        Churn = churn;
    }

    public long getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(long maxChurn) {
        this.maxChurn = maxChurn;
    }

    public long getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(long avgChurn) {
        this.avgChurn = avgChurn;
    }

    public long getChangeSetSize() {
        return ChangeSetSize;
    }

    public void setChangeSetSize(long changeSetSize) {
        ChangeSetSize = changeSetSize;
    }

    public long getMaxChangeSetSize() {
        return maxChangeSetSize;
    }

    public void setMaxChangeSetSize(long maxChangeSetSize) {
        this.maxChangeSetSize = maxChangeSetSize;
    }

    public long getAvgChangeSetSize() {
        return avgChangeSetSize;
    }

    public void setAvgChangeSetSize(long avgChangeSetSize) {
        this.avgChangeSetSize = avgChangeSetSize;
    }

    @Override
    public String toString() {
        return "Metrics{" +
                "LOC=" + LOC +
                ", NR=" + NR +
                ", NAuth=" + NAuth +
                ", LOCAdded=" + LOCAdded +
                ", maxLOCAdded=" + maxLOCAdded +
                ", avgLOCAdded=" + avgLOCAdded +
                ", Churn=" + Churn +
                ", maxChurn=" + maxChurn +
                ", avgChurn=" + avgChurn +
                ", ChangeSetSize=" + ChangeSetSize +
                ", maxChangeSetSize=" + maxChangeSetSize +
                ", avgChangeSetSize=" + avgChangeSetSize +
                '}';
    }
}
