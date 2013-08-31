Neddick
========

Neddick is an Open Source Information Discovery Platform, which shares an approach to information discovery with 
consumer facing websites like [Reddit](http://www.reddit.com), [Digg](http://www.digg.com), and other aggregator 
applications.  Neddick makes up one component of the [Fogcutter Suite](http://code.google.com/p/fogcutter) and is 
based on Groovy & Grails.   

Neddick could be termed "Reddit for the Enterprise" but that would be oversimplifying a bit.  Neddick 
is intended mainly for organizational use and therefore has features that Reddit lacks, but, in turn, 
lacks certain things that Reddit has.  But Reddit was absolutely our inspiration for Neddick and you'll 
notice the commonality almost immediately.


Why "Neddick?"
----------------

We like lighthouses, so Neddick is named after the famous Cape Neddick "Nubble" Lighthouse.


Features
----------

Relative to Reddit, Neddick adds features like:

* Channels can subscribe to RSS feeds - any "channel" in Neddick (roughly equivalent to a Sub-Reddit on Reddit) can link to 0 or more RSS feeds, and the channel will automatically be populated from those feeds on a scheduled basis.
* Tags - Neddick supports the application of arbitrary tags to entries, and provides each user a view of the tags they have used, to enable rapid access to specific content.
* Channel Filters - Filters allow you to filter your view of a channel based on criteria including: body keyword, title keyword, score, and tags.
* More powerful "sharing" capability - Neddick supports sharing content by email, XMPP, and HTTP POST.
* ActivityStrea.ms support - Neddick can HTTP POST entries to remote endpoints in [ActivityStrea.ms format](http://www.activitystrea.ms).  This is how we built our integration with [Quoddy](http://code.google.com/p/quoddy) - the Enterprise Social Network component of the Fogcutter Suite.
* Channel Triggers - Triggers allow a user to specify actions to occur when Entry related events occur which match criteria which may include:  A new Entry is posted which matches a body keyword or title keyword, an Entry is voted up past a specified score threshold, or a specified tag is applied to an Entry.  
* Trigger actions include sharing via email, XMPP and HTTP post, and in future releases will be extend to include sending JMS messages, launching workflows via BPM integration, and running user provided scripts.

To see Neddick in action, visit [the demo site](http://demo.fogbeam.org:8080/neddick/). (login: testuser1/secret)  

Building & Deploying Neddick
----------

Instructions for deploying the latest release can be found at:

https://github.com/fogbeam/Neddick/releases/tag/v0.0.0-tpr4

Once the app is up and running, you can login using testuser1/secret  as the username/password pair.  
  

Roadmap
----------

See the [Roadmap wiki page](http://code.google.com/p/neddick/wiki/Roadmap).

Commercial Support
------------------

Commercial support is available from [Fogbeam Labs](http://www.fogbeam.com).  For more information on
Neddick Enterprise, please visit [http://www.fogbeam.com/neddick_enterprise.html](http://www.fogbeam.com/neddick_enterprise.html).

