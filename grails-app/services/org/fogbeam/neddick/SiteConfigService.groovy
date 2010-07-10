package org.fogbeam.neddick

import org.fogbeam.neddick.SiteConfigEntry;

class SiteConfigService {

	public String getSiteConfigEntry( final String name )
	{
		SiteConfigEntry entry = SiteConfigEntry.findByName( name );
		return entry.value;
	}
}
