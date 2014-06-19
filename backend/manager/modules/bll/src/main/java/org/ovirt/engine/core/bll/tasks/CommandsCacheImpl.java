package org.ovirt.engine.core.bll.tasks;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class CommandsCacheImpl implements CommandsCache {

    private static final String COMMAND_MAP_NAME = "commandMap";
    CacheWrapper<Guid, CommandEntity> commandMap;
    private volatile boolean cacheInitialized;
    private Object LOCK = new Object();

    public CommandsCacheImpl() {
        commandMap = CacheProviderFactory.<Guid, CommandEntity> getCacheWrapper(COMMAND_MAP_NAME);
    }

    private void initializeCache() {
        if (!cacheInitialized) {
            synchronized(LOCK) {
                if (!cacheInitialized) {
                    List<CommandEntity> cmdEntities = DbFacade.getInstance().getCommandEntityDao().getAll();
                    for (CommandEntity cmdEntity : cmdEntities) {
                        commandMap.put(cmdEntity.getId(), cmdEntity);
                    }
                    cacheInitialized = true;
                }
            }
        }
    }

    public Set<Guid> keySet() {
        initializeCache();
        return commandMap.keySet();
    }

    @Override
    public CommandEntity get(Guid commandId) {
        initializeCache();
        return commandMap.get(commandId);
    }

    @Override
    public void remove(final Guid commandId) {
        commandMap.remove(commandId);
        DbFacade.getInstance().getCommandEntityDao().remove(commandId);
    }

    @Override
    public void put(final CommandEntity cmdEntity) {
        commandMap.put(cmdEntity.getId(), cmdEntity);
        DbFacade.getInstance().getCommandEntityDao().saveOrUpdate(cmdEntity);
    }

    public void removeAllCommandsBeforeDate(DateTime cutoff) {
        DbFacade.getInstance().getCommandEntityDao().removeAllBeforeDate(cutoff);
        cacheInitialized = false;
        initializeCache();
    }

    public void updateCommandStatus(Guid commandId, AsyncTaskType taskType, CommandStatus status) {
        final CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setCommandStatus(status);
            if (taskType.equals(AsyncTaskType.notSupported) || cmdEntity.isCallBackEnabled()) {
                DbFacade.getInstance().getCommandEntityDao().saveOrUpdate(cmdEntity);
            }
        }
    }


    public void updateCallBackNotified(final Guid commandId) {
        CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setCallBackNotified(true);
            DbFacade.getInstance().getCommandEntityDao().updateNotified(commandId);
        }
    }
}
