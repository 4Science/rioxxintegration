/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.openaccess.service;

import java.sql.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;

/**
 * @author philip at atmire.com
 */
public interface CompliantOpenAccessService {

    public String updateItem(Context context, Item item) throws SQLException, IllegalArgumentException, AuthorizeException;
}
