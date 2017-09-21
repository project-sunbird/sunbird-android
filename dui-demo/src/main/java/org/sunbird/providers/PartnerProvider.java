package org.sunbird.providers;

import org.ekstep.genieproviders.partner.AbstractPartnerProvider;
import org.sunbird.BuildConfig;

/**
 * Created by Vinay on 13/06/17.
 */

public class PartnerProvider extends AbstractPartnerProvider {
    @Override
    public String getPackageName() {
        return BuildConfig.APP_QUALIFIER;
    }
}
