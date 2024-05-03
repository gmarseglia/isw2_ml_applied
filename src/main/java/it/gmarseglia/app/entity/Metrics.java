package it.gmarseglia.app.entity;

public class Metrics {

    private long LOC;
    private long NR;

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

    @Override
    public String toString() {
        return "Metrics{" +
                "LOC=" + LOC +
                ", NR=" + NR +
                '}';
    }
}
