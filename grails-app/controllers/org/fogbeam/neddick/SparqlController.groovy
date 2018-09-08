package org.fogbeam.neddick

import org.fogbeam.neddick.controller.mixins.SidebarPopulatorMixin

import org.apache.jena.query.Dataset
import org.apache.jena.query.Query
import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.query.QuerySolution
import org.apache.jena.query.ReadWrite
import org.apache.jena.query.ResultSet
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.tdb.TDBFactory

import grails.plugin.springsecurity.annotation.Secured

@Mixin(SidebarPopulatorMixin)
class SparqlController
{
	
	def userService;
	def entryService;
	def entryCacheService;
	def channelService;
	def siteConfigService;	
	def filterService;
	def tagService;

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def index()
	{
		User user = userService.getLoggedInUser();
		
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		
		def model = [:];

		model.putAll( sidebarCollections );
		
		
		// render(view:"index", model:model);
		return model;
	}	
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def doSearch()
	{
		
		User user = userService.getLoggedInUser();
		
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		List<Entry> searchResults = new ArrayList<Entry>();
		String baseQueryString = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
								 "PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX dcterm: <http://purl.org/dc/terms/> " +
								 "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
								 "PREFIX dbpo: <http://dbpedia.org/ontology/> PREFIX dbp: <http://dbpedia.org/resource/> PREFIX scorg: <http://schema.org/> ";
		
		String userQueryString = params.sparqlQuery;
		
		String queryString = baseQueryString + userQueryString;
		
		// a sample query
		// final queryString = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> SELECT distinct ?entity ?y WHERE { ?entity <http://purl.org/dc/terms/references> ?y . ?y <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/MusicalArtist> . ?y <http://dbpedia.org/ontology/birthDate> ?birthDate . FILTER ( ?birthDate  < "1950-01-01T00:00:00Z"^^xsd:dateTime) }";

		// Make a TDB-backed dataset
		String neddickHome = System.getProperty( "neddick.home" );
		String directory = "${neddickHome}/jenastore/triples" ;
		log.debug "Opening TDB triplestore at: ${directory}";
		Dataset dataset = TDBFactory.createDataset(directory) ;
		
		dataset.begin(ReadWrite.READ);

		// Get model inside the transaction
		Model tdbModel = dataset.getDefaultModel() ;

		/* Now create and execute the query using a Query object */
		log.debug "Our Query: ${queryString}";
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, tdbModel);
				
		try
		{
			ResultSet results = qexec.execSelect() ;
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				RDFNode x = soln.get("entity" );
				RDFNode y = soln.get( "y" );
				
				System.out.log.debug( x.toString() + " y: " + y.toString() );
				
				// extract our entry UUID from the Subject and locate the matching Entry and
				// add it to searchResults
				String subject = x.toString();
				subject = subject.replace( "neddick:", "" );
				
				Entry theEntry = entryService.findByUuid( subject );
				
				searchResults.add( theEntry );				
			}
		}
		finally
		{
			qexec.close();
		}
		
		dataset.commit();
		dataset.end();

		def model = [searchResults:searchResults];				

		model.putAll( sidebarCollections );
		
		render(view:"displaySearchResults", model:model);
	}
}