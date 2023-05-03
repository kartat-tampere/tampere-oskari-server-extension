package flyway.tre3d;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.ViewTypes;

public class V1_0_1__replace_hierarchical_layerlist_with_layerlist extends BaseJavaMigration {

    private static final String GEOPORTAL_3D_APPLICATION = "geoportal-3d";
    private static final String HIERARCHICAL_BUNDLE_NAME = "hierarchical-layerlist";
    private static final String LAYER_LIST_BUNDLE_NAME = "layerlist";

    public void migrate(Context context) throws Exception {
        Connection c = context.getConnection();

        List<Long> default3dAppIds = AppSetupHelper.getSetupsForApplicationByType(c, GEOPORTAL_3D_APPLICATION, ViewTypes.DEFAULT);
        // There should only be one, but let's just migrate "all of them"
        for (long id : default3dAppIds) {
            replaceBundleInPlace(c, id, HIERARCHICAL_BUNDLE_NAME, LAYER_LIST_BUNDLE_NAME);
        }
    }

    private void replaceBundleInPlace(Connection c, long applicationId, String bundleToRemove, String bundleToAdd) throws SQLException {
        if (AppSetupHelper.appContainsBundle(c, applicationId, bundleToAdd)) {
            return;
        }

        AppSetupHelper.addBundleToApp(c, applicationId, bundleToAdd);
        Bundle toAdd = AppSetupHelper.getAppBundle(c, applicationId, bundleToAdd);

        Bundle toRemove = AppSetupHelper.getAppBundle(c, applicationId, bundleToRemove);
        if (toRemove == null) {
            return;
        }

        AppSetupHelper.removeBundleFromApp(c, applicationId, bundleToRemove);

        toAdd.setSeqNo(toRemove.getSeqNo());
        AppSetupHelper.updateAppBundle(c, applicationId, toAdd);
    }

}
