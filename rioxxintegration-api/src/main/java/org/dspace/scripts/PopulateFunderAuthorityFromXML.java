package org.dspace.scripts;

import java.io.File;

import org.dspace.authority.AuthorityValue;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.authority.FunderXmlFileParser;
import org.dspace.authority.service.AuthorityValueService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 24 Apr 2015
 */
public class PopulateFunderAuthorityFromXML extends PopulateAuthorityFromXML<FunderAuthorityValue> {

    @Autowired(required = true)
    protected AuthorityValueService authorityValueService;
    
    public static void main(String[] args) {
        PopulateFunderAuthorityFromXML script = new PopulateFunderAuthorityFromXML();
        script.mainImpl(args);
    }

    @Override
    protected AuthorityValue findCachedRecord(FunderAuthorityValue value)
    {
        AuthorityValue cachedRecord = authorityValueService.findByFunderID(null, value.getFunderID());
        return cachedRecord;
    }

    @Override
    protected FunderAuthorityValue updateValues(AuthorityValue cachedRecord, FunderAuthorityValue value)
    {
                FunderAuthorityValue record = (FunderAuthorityValue) cachedRecord;
                record.setValues(value);
        return record;
    }

    @Override
    public void parseXML(File file) {
        FunderXmlFileParser funderXmlFileParser = new FunderXmlFileParser(this);
        funderXmlFileParser.setProgressWriter(print);
        funderXmlFileParser.getFunderAuthorities(file);
        }

    @Override
    protected boolean isValid(FunderAuthorityValue value)
    {
        return value != null && value.isValid();
        }
    }