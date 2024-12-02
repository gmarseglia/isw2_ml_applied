package it.gmarseglia.app.entity;

public class Metrics {

    private long loc;
    private long age;
    private long stepAge;
    private long nr;
    private long nAuth;
    private long locAdded;
    private long maxLOCAdded;
    private long avgLOCAdded;
    private long churn;
    private long maxChurn;
    private long avgChurn;
    private long changeSetSize;
    private long maxChangeSetSize;
    private long avgChangeSetSize;

    public long getLoc() {
        return loc;
    }

    public void setLoc(long loc) {
        this.loc = loc;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public long getStepAge() {
        return stepAge;
    }

    public void setStepAge(long stepAge) {
        this.stepAge = stepAge;
    }

    public long getNr() {
        return nr;
    }

    public void setNr(long nr) {
        this.nr = nr;
    }

    public long getnAuth() {
        return nAuth;
    }

    public void setnAuth(long nAuth) {
        this.nAuth = nAuth;
    }

    public long getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(long locAdded) {
        this.locAdded = locAdded;
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
        return churn;
    }

    public void setChurn(long churn) {
        this.churn = churn;
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
        return changeSetSize;
    }

    public void setChangeSetSize(long changeSetSize) {
        this.changeSetSize = changeSetSize;
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
                "LOC=" + loc +
                ", age=" + age +
                ", stepAge=" + stepAge +
                ", NR=" + nr +
                ", NAuth=" + nAuth +
                ", LOCAdded=" + locAdded +
                ", maxLOCAdded=" + maxLOCAdded +
                ", avgLOCAdded=" + avgLOCAdded +
                ", Churn=" + churn +
                ", maxChurn=" + maxChurn +
                ", avgChurn=" + avgChurn +
                ", ChangeSetSize=" + changeSetSize +
                ", maxChangeSetSize=" + maxChangeSetSize +
                ", avgChangeSetSize=" + avgChangeSetSize +
                '}';
    }
}
