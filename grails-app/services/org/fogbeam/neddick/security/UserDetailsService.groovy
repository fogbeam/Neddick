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
        log.debug "username: " + username;
        
        
        User ourUser = null;
        User.withNewSession
        {
            ourUser = userService.findUserByUserId( username );
        }
        
        if( ourUser == null )
        {
            ourUser = userService.findUserByUserId( "SYS_anonymous" );
        }
        
        log.debug "returning user: " + ourUser;
        
        return ourUser;
    }
}
