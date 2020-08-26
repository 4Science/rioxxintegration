package com.atmire.openaccess.ctask;

import com.atmire.openaccess.service.*;
import java.io.*;
import org.apache.commons.lang3.*;
import org.apache.log4j.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.curate.*;
import org.dspace.utils.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 15/03/16
 * Time: 16:55
 */
public class DateOfCompliantOpenAccessTask extends AbstractCurationTask {

    private static final Logger log = Logger.getLogger(DateOfCompliantOpenAccessTask.class);

    private CompliantOpenAccessService compliantOpenAccessService = new DSpace().getServiceManager().getServiceByName("CompliantOpenAccessService",CompliantOpenAccessService.class);

    public DateOfCompliantOpenAccessTask() {

    }

    @Override
    public void init(Curator curator, String taskId) throws IOException {
        super.init(curator, taskId);
    }

    @Override
    public int perform(DSpaceObject dso) throws IOException {
        if (dso instanceof Item) {
            try {
                Item item = (Item) dso;
                Context context = Curator.curationContext();

                String result = compliantOpenAccessService.updateItem(context, item);

                if(StringUtils.isNotBlank(result)) {
                    setResult(result);
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Curator.CURATE_ERROR;
            }
        }

        return Curator.CURATE_SUCCESS;
    }



}
