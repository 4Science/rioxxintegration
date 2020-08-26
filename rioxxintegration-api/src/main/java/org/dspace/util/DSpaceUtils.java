/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.util;

import org.dspace.utils.DSpace;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

public class DSpaceUtils {
	
	private DSpace dspace = new DSpace();
	
    public SessionFactory getSessionFactory()
    {
        return (SessionFactory) dspace.getServiceManager().getServiceByName("&sessionFactory", LocalSessionFactoryBean.class).getObject();
    }
}
