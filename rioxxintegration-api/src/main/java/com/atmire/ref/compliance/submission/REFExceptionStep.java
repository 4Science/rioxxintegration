/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.ref.compliance.submission;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by jonas - jonas@atmire.com on 08/04/16.
 */
public class REFExceptionStep extends AbstractProcessingStep {



    @Override
    public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo subInfo) throws ServletException, IOException, SQLException, AuthorizeException {
        String buttonPressed = Util.getSubmitButton(request, NEXT_BUTTON);
        Item item = subInfo.getSubmissionItem().getItem();

        if(StringUtils.isNotBlank(buttonPressed)) {
            String selectedOption =  request.getParameter("exception-options");
            if(StringUtils.isNotBlank(selectedOption)){
                String explanation = request.getParameter(selectedOption+"Explanation");
                clearExistingExceptions(item);
                if(StringUtils.isNotBlank(explanation)){
                    item.addMetadata("refterms",selectedOption.equals("exceptionFreeText")?selectedOption:selectedOption+"Explanation",null,null,explanation);
                }
                String selectedDropdown = request.getParameter(selectedOption+"-dropdown");
                if(StringUtils.isNotBlank(selectedDropdown)){
                    item.addMetadata("refterms",selectedOption,null,null,selectedDropdown);
                }
                item.update();
                context.commit();
                }
            }

        return STATUS_COMPLETE;
    }

    private void clearExistingExceptions(Item item) {
        String[] specifiedExceptions =  new DSpace().getServiceManager().getServiceByName("configuredExceptions", String[].class);
        for(int i = 0; i<specifiedExceptions.length; i++){
            item.clearMetadata("refterms",specifiedExceptions[i], Item.ANY,Item.ANY);
            item.clearMetadata("refterms",specifiedExceptions[i]+"Explanation",Item.ANY,Item.ANY);
        }
    }

    @Override
    public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
        return 1;
    }

}
