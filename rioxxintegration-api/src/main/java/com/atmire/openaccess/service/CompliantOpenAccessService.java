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
