package org.fogbeam.neddick

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
class User {

	public User()
	{
		this.uuid = java.util.UUID.randomUUID().toString();
	}
	
    static constraints = {
        userId( size:3..20, unique:true )
        password( size:6..8 )
        homepage( url:true, nullable:true )
        validator: {passwd, user -> 
                return passwd != user.userId 
            }
        userProfile(nullable:true)
        dateCreated()
    }

	@XmlElement
    String uuid;
	
	@XmlElement
	String userId;
	
    String password;
	
	@XmlElement
    String homepage;
	
	@XmlElement
    String fullName;
	
	@XmlElement
    String bio;
	
	@XmlElement
    String email;
	
	@XmlElement
    Date dateCreated;
	
    UserProfile userProfile;
    
    static mapping = {
    	table 'uzer'
		userFavoriteChannels lazy: false;
    }

    static hasMany = [	savedEntries : Entry, 
						hiddenEntries: Entry, 
						childUserLinks:UserToUserLink, 
						parentUserLinks: UserToUserLink, 
						userEntryScoreLinks:UserEntryScoreLink,
						userFavoriteChannels:UserFavoriteChannelLink,
						roles: AccountRole, 
						permissions: String ];
					
    static mappedBy = [ userFavoriteChannels: 'user', savedEntries : "savers", hiddenEntries:"hiders", userProfile:"owner", childUserLinks:"owner", parentUserLinks:"target"  ];
	// ,
	
	
    public void setUuid( String uuid ){
    	
    	// never overwrite existing uuid value with NULL
    	if( uuid != null )
    	{
    		this.uuid = uuid;
    	}
    }

    public List getParents() 	
	{
    	return parentUserLinks.collect{it.owner}
	}  
    
    public List getChildren()
    {
    	return childUserLinks.collect{it.target}
    }
    
    // add to User Links
	List addToParentUserLinks(User user) 
	{ 
		UserToUserLink.link(this, user );
		return parents;
	}

	List removeFromParentUserLinks( User user ) 
	{ 
		UserToUserLink.unlink(this, user );
		return parents;
	}     

	List addToChildUserLinks(User user) 
	{ 
		
		// NOTE: was this previously a c&p error?!?? Shouldn't this be UserToUserLink, not TagEntryLink, right?
		UserToUserLink.link(this, user );
		return children;
	}

	List removeFromChildUserLinks( User user ) 
	{ 
		// NOTE: was this previously a c&p error?!?? Shouldn't this be UserToUserLink, not TagEntryLink, right?
		UserToUserLink.unlink(this, user );
		return children
	}
	
}
