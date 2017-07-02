package org.ovirt.engine.ui.frontend.gwtservices;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;
import com.google.gwt.user.server.rpc.NoXsrfProtect;

@RemoteServiceRelativePath("GenericApiGWTService")
public interface GenericApiGWTService extends XsrfProtectedService {

    VdcQueryReturnValue runQuery(QueryType search,
            VdcQueryParametersBase searchParameters);

    VdcReturnValueBase runAction(ActionType actionType,
            ActionParametersBase params);

    @NoXsrfProtect
    VdcQueryReturnValue runPublicQuery(QueryType queryType,
            VdcQueryParametersBase params);

    ArrayList<VdcQueryReturnValue> runMultipleQueries(
            ArrayList<QueryType> queryTypeList,
            ArrayList<VdcQueryParametersBase> paramsList);

    List<VdcReturnValueBase> runMultipleActions(
            ActionType actionType,
            ArrayList<ActionParametersBase> multipleParams,
            boolean isRunOnlyIfAllValidationPass);

    List<VdcReturnValueBase> runMultipleActions(
            ActionType actionType,
            ArrayList<ActionParametersBase> multipleParams,
            boolean isRunOnlyIfAllValidationPass, boolean isWaitForResult);

    void storeInHttpSession(String key, String value);

    String retrieveFromHttpSession(String key);

}
