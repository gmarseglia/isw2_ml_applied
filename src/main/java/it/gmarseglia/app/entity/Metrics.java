package it.gmarseglia.app.entity;

public class Metrics {

    private long LOC;
    private long NR;
    private long NAuth;
    private long LOCAdded;

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

    @Override
    public String toString() {
        return "Metrics{" +
                "LOC=" + LOC +
                ", NR=" + NR +
                ", NAuth=" + NAuth +
                ", LOCAdded=" + LOCAdded +
                '}';
    }
}
