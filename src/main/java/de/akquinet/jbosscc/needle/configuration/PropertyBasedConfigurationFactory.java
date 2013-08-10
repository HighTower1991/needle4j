package de.akquinet.jbosscc.needle.configuration;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.akquinet.jbosscc.needle.db.operation.AbstractDBOperation;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import de.akquinet.jbosscc.needle.injection.InjectionProviderInstancesSupplier;
import de.akquinet.jbosscc.needle.mock.MockProvider;

public class PropertyBasedConfigurationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(NeedleConfiguration.class);

    private static NeedleConfiguration CONFIGURATION = null;

    public static NeedleConfiguration get() {
        if (CONFIGURATION == null) {

            CONFIGURATION = new PropertyBasedConfigurationFactory().init();
        }
        return CONFIGURATION;
    }

    public static NeedleConfiguration get(final String resourceName) {
        return new PropertyBasedConfigurationFactory(resourceName).init();
    }

    private final Map<String, String> configurationProperties;

    private final LookupCustomClasses lookupCustomClasses;

    private PropertyBasedConfigurationFactory() {
        this(new ConfigurationLoader());
    }

    private PropertyBasedConfigurationFactory(final String resourceName) {
        this(new ConfigurationLoader(resourceName));
    }

    private PropertyBasedConfigurationFactory(final ConfigurationLoader configurationLoader) {
        configurationProperties = configurationLoader.getConfigProperties();
        lookupCustomClasses = new LookupCustomClasses(configurationProperties);
    }

    private NeedleConfiguration init() {

        NeedleConfiguration configuration = new NeedleConfiguration();

        final Set<Class<Annotation>> customInjectionAnnotations = lookupCustomClasses
                .lookup(ConfigurationProperties.CUSTOM_INJECTION_ANNOTATIONS_KEY);
        configuration.setCustomInjectionAnnotations(customInjectionAnnotations);

        final Set<Class<InjectionProvider<?>>> customInjectionProviderClasses = lookupCustomClasses
                .lookup(ConfigurationProperties.CUSTOM_INJECTION_PROVIDER_CLASSES_KEY);
        configuration.setCustomInjectionProviderClasses(customInjectionProviderClasses);

        final Set<Class<InjectionProviderInstancesSupplier>> supplier = lookupCustomClasses
                .lookup(ConfigurationProperties.CUSTOM_INSTANCES_SUPPLIER_CLASSES_KEY);
        configuration.setCustomInjectionProviderInstancesSupplierClasses(supplier);

        configuration.setHibernateCfgFilename(configurationProperties
                .get(ConfigurationProperties.HIBERNATE_CFG_FILENAME_KEY));
        configuration.setPersistenceunitName(configurationProperties
                .get(ConfigurationProperties.PERSISTENCEUNIT_NAME_KEY));

        Class<? extends MockProvider> mockProviderClass = lookupMockProviderClass(configurationProperties
                .get(ConfigurationProperties.MOCK_PROVIDER_KEY));
        configuration.setMockProviderClass(mockProviderClass);

        configuration.setDBOperationClass(lookupDBOperationClass(configurationProperties
                .get(ConfigurationProperties.DB_OPERATION_KEY)));
        configuration.setJdbcUrl(configurationProperties.get(ConfigurationProperties.JDBC_URL_KEY));
        configuration.setJdbcDriver(configurationProperties.get(ConfigurationProperties.JDBC_DRIVER_KEY));
        configuration.setJdbcUser(configurationProperties.get(ConfigurationProperties.JDBC_USER_KEY));
        configuration.setJdbcPassword(configurationProperties.get(ConfigurationProperties.JDBC_PASSWORD_KEY));

        LOG.info("Needle Configuration: {}", configuration);

        return configuration;
    }

    static Class<? extends MockProvider> lookupMockProviderClass(final String mockProviderClassName) {

        try {
            if (mockProviderClassName != null) {
                return lookupClass(MockProvider.class, mockProviderClassName);
            }
        } catch (final Exception e) {
            throw new RuntimeException("could not load mock provider class: '" + mockProviderClassName + "'", e);
        }

        throw new RuntimeException("no mock provider configured");
    }

    static Class<? extends AbstractDBOperation> lookupDBOperationClass(final String dbOperation) {
        try {

            return lookupClass(AbstractDBOperation.class, dbOperation);

        } catch (final Exception e) {
            LOG.warn("error while loading db operation class {}, {}", dbOperation, e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> lookupClass(Class<T> type, final String className) throws ClassNotFoundException {
        return (Class<T>) Class.forName(className);

    }

}
