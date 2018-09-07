package org.fogbeam.neddick.security

import org.fogbeam.neddick.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.transaction.annotation.Transactional


@Transactional
class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService
{
    def userService;
    
    @Override
    public UserDetails loadUserByUsername( String username )
            throws UsernameNotFoundException
    {
        println "username: " + username;
        
        
        User ourUser = null;
        User.withNewSession
        {
            ourUser = userService.findUserByUserId( username );
        }
        
        if( ourUser == null )
        {
            ourUser = userService.findUserByUserId( "SYS_anonymous" );
        }
        
        println "returning user: " + ourUser;
        
        return ourUser;
    }
}
