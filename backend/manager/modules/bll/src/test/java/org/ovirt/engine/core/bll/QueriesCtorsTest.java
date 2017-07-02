package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class QueriesCtorsTest extends CtorsTestBase {
    private static Collection<Class<?>> queryClasses;

    @BeforeClass
    public static void initCommandsCollection() {
        queryClasses = commandsFromEnum(QueryType.class, CommandsFactory::getQueryClass);
    }

    @Override
    protected Collection<Class<?>> getClassesToTest() {
        return queryClasses;
    }

    @Override
    protected Stream<MandatoryCtorPredicate> getMandatoryCtorPredicates() {
        return Stream.of(new MandatoryCtorPredicate(VdcQueryParametersBase.class, EngineContext.class));
    }
}
