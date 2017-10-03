package uk.ac.man.cs.sim;

/**
 * Created by slava on 15/09/17.
 */
public enum ReasonerName {
    // Valid reasoners
    HERMIT("HERMIT"),
    FACT("FACT"),
    PELLET("PELLET"),
    JFACT("JFACT"),
    TROWL("TROWL");

    private final String name;

    ReasonerName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
