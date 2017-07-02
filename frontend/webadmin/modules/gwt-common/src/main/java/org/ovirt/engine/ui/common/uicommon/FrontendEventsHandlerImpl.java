package org.ovirt.engine.ui.common.uicommon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.frontend.IFrontendEventsHandler;
import org.ovirt.engine.ui.frontend.Message;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.inject.Inject;

public class FrontendEventsHandlerImpl implements IFrontendEventsHandler {

    private final ErrorPopupManager errorPopupManager;
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public FrontendEventsHandlerImpl(ErrorPopupManager errorPopupManager) {
        this.errorPopupManager = errorPopupManager;
    }

    @Override
    public Boolean isRaiseErrorModalPanel(ActionType actionType, EngineFault fault) {
        return !(actionType == ActionType.VmLogon && fault.getError() == EngineError.nonresp);
    }

    @Override
    public Boolean isRaiseErrorModalPanel(QueryType queryType) {
        return false;
    }

    @Override
    public void runActionExecutionFailed(ActionType action, EngineFault fault) {
        if (isRaiseErrorModalPanel(action, fault)) {
            errorPopupManager.show(messages.uiCommonRunActionExecutionFailed(
                    EnumTranslator.getInstance().translate(action), fault.getMessage()));
        }
    }

    @Override
    public void runMultipleActionFailed(ActionType action, List<VdcReturnValueBase> returnValues) {
        List<ActionType> actions = new ArrayList<>();
        for (int i = 0; i < returnValues.size(); i++) {
            actions.add(action);
        }

        runMultipleActionsFailed(actions, returnValues);
    }

    @Override
    public void runMultipleActionsFailed(Map<ActionType, List<VdcReturnValueBase>> failedActionsMap,
            MessageFormatter messageFormatter) {
        List<ActionType> actions = new ArrayList<>();
        List<VdcReturnValueBase> returnValues = new ArrayList<>();

        for (Entry<ActionType, List<VdcReturnValueBase>> entry : failedActionsMap.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); ++i) {
                actions.add(entry.getKey());
            }
            returnValues.addAll(entry.getValue());
        }

        runMultipleActionsFailed(actions, returnValues, messageFormatter);
    }

    @Override
    public void runMultipleActionsFailed(List<ActionType> actions, List<VdcReturnValueBase> returnValues) {
        runMultipleActionsFailed(actions, returnValues, innerMessage -> messages.uiCommonRunActionFailed(innerMessage));
    }

    public void runMultipleActionsFailed(List<ActionType> actions,
            List<VdcReturnValueBase> returnValues,
            MessageFormatter messageFormatter) {

        List<Message> errors = new ArrayList<>();

        int actionNum = 0;
        for (VdcReturnValueBase v : returnValues) {
            if (isRaiseErrorModalPanel(actions.get(actionNum++), v.getFault())) {
                String description =
                        (v.getDescription() != null && !"".equals(v.getDescription().trim())) || returnValues.size() == 1 ? v.getDescription() : ConstantsManager.getInstance().getConstants().action() + " " + actionNum; //$NON-NLS-1$ //$NON-NLS-2$
                if (!v.isValid()) {
                    for (String validateMessage : v.getValidationMessages()) {
                        errors.add(new Message(description, validateMessage));
                    }
                } else {
                    errors.add(new Message(description, v.getFault().getMessage()));
                }
            }
        }

        errorPopupManager.show(messageFormatter.format(ErrorMessageFormatter.formatMessages(errors)));
    }

    @Override
    public void runQueryFailed(List<VdcQueryReturnValue> returnValue) {
        errorPopupManager.show(
                messages.uiCommonRunQueryFailed(
                        returnValue != null ? ErrorMessageFormatter.formatQueryReturnValues(returnValue) : "null")); //$NON-NLS-1$
    }

    @Override
    public void publicConnectionClosed(Exception ex) {
        errorPopupManager.show(
                messages.uiCommonPublicConnectionClosed(ex.getLocalizedMessage()));
    }
}
