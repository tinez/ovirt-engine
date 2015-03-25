package org.ovirt.engine.core.bll.utils;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

/**
 * This class is made to keep all producers of bll dependencies, generally singletons from other jars in one place for the
 * dependency scanner to identify. except for producers like for Log (see produceLogger),
 * this class will bridge the dependency resolution limitation we have between non-ejb module jars which contains
 * dependencies to the bll ejb-jar. NOTE: future jboss version should solve that so we could have beans dependencies
 * scanned from other non-ejb module jars such as org.ovirt.engine.core:dal
 *
 * Producers could be declared either by a field or by a method, to get more control on the production of an instance
 *
 * There is no need to instantiate this class and there is no visible usage to it but to the dependency scanner
 */
@Singleton
public class BllCDIAdapter {

    @Produces
    private NetworkDao produceNetworkDao(DbFacade dbFacade) {
        return dbFacade.getNetworkDao();
    }

    @Produces
    private NetworkClusterDao produceNetworkClusterDao(DbFacade dbFacade) {
        return dbFacade.getNetworkClusterDao();
    }

    @Produces
    private VdsGroupDAO produceVdsGroupDao(DbFacade dbFacade) {
        return dbFacade.getVdsGroupDao();
    }

    @Produces
    private StoragePoolDAO produceStoragePoolDAO(DbFacade dbFacade) {
        return dbFacade.getStoragePoolDao();
    }

    @Produces
    private VmDynamicDAO produceVmDynamicDAO(DbFacade dbFacade) {
        return dbFacade.getVmDynamicDao();
    }

    @Produces
    private InterfaceDao produceInterfaceDao(DbFacade dbFacade) {
        return dbFacade.getInterfaceDao();
    }

    @Produces
    private HostNetworkQosDao produceHostNetworkQosDao(DbFacade dbFacade) {
        return dbFacade.getHostNetworkQosDao();
    }

    @Produces
    @Singleton
    private PermissionDAO producePermissionDao(DbFacade dbFacade) {
        return dbFacade.getPermissionDao();
    }

    @Produces
    private VmDeviceDAO produceVmDeviceDao(DbFacade dbFacade) {
        return dbFacade.getVmDeviceDao();
    }

    @Produces
    private HostDeviceDao produceHostDeviceDao(DbFacade dbFacade) {
        return dbFacade.getHostDeviceDao();
    }

    @Produces
    private VdsDAO produceVdsDao(DbFacade dbFacade) {
        return dbFacade.getVdsDao();
    }

    @Produces
    private HostNicVfsConfigDao produceHostNicVfsConfigDao(DbFacade dbFacade) {
        return dbFacade.getHostNicVfsConfigDao();
    }

    private BllCDIAdapter() {
        // hide me
    }
}
