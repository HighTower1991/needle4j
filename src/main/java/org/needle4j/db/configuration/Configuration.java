package org.needle4j.db.configuration;

import org.needle4j.configuration.PropertyBasedConfigurationFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Allows a fine configuration of an {@link EntityManagerFactory}.
 */
class Configuration implements PersistenceConfiguration {

    private final EntityManagerFactory factory;
    private final EntityManager entityManager;

    /**
     * Creates an {@link EntityManagerFactory} and {@link EntityManager} for the
     * given entity classes by using the configured hibernate specific
     * configuration file (*cfg.xml).
     *
     * @param entityClasses the entity classes
     */
    public Configuration( final Class<?>[] entityClasses ) {
        factory = createEntityManagerFactory( entityClasses );
        entityManager = EntityManagerProxyFactory.createProxy( factory.createEntityManager() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return factory;
    }

    private static EntityManagerFactory createEntityManagerFactory( final Class<?>[] entityClasses ) {
        final org.hibernate.cfg.Configuration cfg = new org.hibernate.cfg.Configuration();

        // add a regular hibernate.cfg.xml
        cfg.configure( PropertyBasedConfigurationFactory.get().getHibernateCfgFilename() );

        for ( final Class<?> clazz : entityClasses ) {
            cfg.addAnnotatedClass( clazz );
        }

        return javax.persistence.Persistence.createEntityManagerFactory( PropertyBasedConfigurationFactory.get().getPersistenceunitName() );
    }
}
