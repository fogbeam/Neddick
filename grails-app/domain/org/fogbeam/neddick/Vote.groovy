package org.fogbeam.neddick

class Vote {

    static constraints = {
    }

    // weight
	int weight;
    User submitter;
    boolean enabled;
    
    static belongsTo = [ entry: Entry ];
}
