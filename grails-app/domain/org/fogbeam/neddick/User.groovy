package org.fogbeam.neddick

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
class User implements Serializable, UserDetails
{
	public User()
	{
		this.uuid = java.util.UUID.randomUUID().toString();
	}
	
    static constraints = {
        userProfile(nullable:true)
	    userId( size:3..20, unique:true )
        homepage( url:true, nullable:true )
        validator: { passwd, user -> 
            return passwd != user.userId 
        }
		email(nullable:true)
		displayName( nullable:true)
		bio( nullable:true)
		dateCreated()
    }

	@XmlElement
    String uuid;
	
	@XmlElement
	String userId;
	
	/* stuff objects of this class "carry around" but aren't persisted as part of the object. 
	 * This stuff is pulled in from an external source, like, say, LDAP. */
	String password = "notused"; // normal users login using CAS and don't have a password stored locally

	@XmlElement
	String homepage;
	
	@XmlElement
	String firstName;
	
	@XmlElement
	String lastName;
	
	@XmlElement
	String displayName;
	
	// String fullName;  // see below
	String bio;
	
	@XmlElement
	String email;
	
	@XmlElement
	boolean disabled;
	
	@XmlElement
    boolean accountExpired;
    
	@XmlElement
    boolean accountLocked;
    
	@XmlElement
    boolean passwordExpired;	
	
	@XmlElement
    Date dateCreated;
	
    UserProfile userProfile;
    
	static transients = [ "templateName", "fullName" ]
	static fetchMode = [roles: 'eager', permissions:'eager'];
	
	static mapping = {
		table 'uzer'
		currentStatus lazy:false, cascade:'delete'; // eagerly fetch the currentStatus
		roles lazy: false;		  // eagerly fetch roles
		permissions lazy:false, cascade:'delete';   // eagerly fetch permissions
		streams cascade: 'delete';
		userFavoriteChannels lazy: false;
	}
	
   
    static hasMany = [	savedEntries : Entry, 
						hiddenEntries: Entry, 
						childUserLinks:UserToUserLink, 
						parentUserLinks: UserToUserLink, 
						userEntryScoreLinks:UserEntryScoreLink,
						userFavoriteChannels:UserFavoriteChannelLink,
						roles: AccountRole ];
					
    static mappedBy = [ userFavoriteChannels: 'user', savedEntries : "savers", hiddenEntries:"hiders", userProfile:"owner", childUserLinks:"owner", parentUserLinks:"target"  ];
	
	
    Set<GrantedAuthority> getAuthorities()
    {
        Set<GrantedAuthority> authorities = null;
        UserAccountRoleMapping.withNewSession
        {
            authorities = (UserAccountRoleMapping.findAllByUser(this) as List<UserAccountRoleMapping>)*.role as Set<GrantedAuthority>
        }
        
        return authorities;
    }
	
	@Override
	public String getUsername()
	{
		return userId;
	}
	
    @Override
    public boolean isAccountNonExpired()
    {
        return !accountExpired;
    }


    @Override
    public boolean isAccountNonLocked()
    {
        return !accountLocked;
    }


    @Override
    public boolean isCredentialsNonExpired()
    {
        return !passwordExpired;
    }


    @Override
    public boolean isEnabled()
    {
        return !disabled;
    }

    public void setUuid( String uuid )
	{
    	
    	// never overwrite existing uuid value with NULL
    	if( uuid != null )
    	{
    		this.uuid = uuid;
    	}
    }

	public String getFullName()
	{
		return firstName + " " + lastName;	
	}	

	public void setFullName( String fullName )
	{}
	
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
		UserToUserLink.link(this, user );
		return children;
	}

	List removeFromChildUserLinks( User user ) 
	{ 
		UserToUserLink.unlink(this, user );
		return children
	}
}
