package uk.ac.man.cs.sim;

public enum SimilarityType {
	MODULE("MODULE");
	   
	private final String name;

	SimilarityType(String name) {
		this.name = name;
	}

	public String toString() {
	    return name;
	}
}
