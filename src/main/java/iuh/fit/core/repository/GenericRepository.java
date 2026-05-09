package iuh.fit.core.repository;


import iuh.fit.core.entity.BaseEntity;
import iuh.fit.core.db.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class   GenericRepository<T, ID> {

    private final Class<T> entityClass;

    protected GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected <R> R executeTransaction(Function<EntityManager, R> function) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.getEntityManager();
            tx = em.getTransaction();
            tx.begin();
            R result = function.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            safeRollback(tx, e);
            throw new RuntimeException(e);
        } finally {
            safeClose(em);
        }
    }

    protected void doInTransaction(Consumer<EntityManager> consumer) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.getEntityManager();
            tx = em.getTransaction();
            tx.begin();
            consumer.accept(em);
            tx.commit();
        } catch (Exception e) {
            safeRollback(tx, e);
            throw new RuntimeException(e);
        } finally {
            safeClose(em);
        }
    }

    protected <R> R doInSession(Function<EntityManager, R> function) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return function.apply(em);
        }
    }

    public void save(T entity) {
        doInTransaction(em -> em.persist(entity));
    }

    public T findById(ID id) {
        return doInSession(em -> em.find(entityClass, id));
    }

    public List<T> findAll() {
        boolean isSoftDeletable = BaseEntity.class.isAssignableFrom(entityClass);
        String jpql = isSoftDeletable
                ? "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.deletedAt IS NULL"
                : "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return doInSession(em ->
                em.createQuery(jpql, entityClass)
                        .getResultList());
    }

    public void update(T entity) {
        executeTransaction(em -> em.merge(entity));
    }

    public void delete(ID id) {
        doInTransaction(em -> {
            T entity = em.find(entityClass, id);
            if (entity instanceof iuh.fit.core.entity.BaseEntity base) {
                base.softDelete();
                em.merge(entity);
            } else if (entity != null) {
                em.remove(entity);
            }
        });
    }

    public long count() {
        boolean isSoftDeletable = BaseEntity.class.isAssignableFrom(entityClass);
        String jpql = isSoftDeletable
                ? "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e.deletedAt IS NULL"
                : "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
        return doInSession(em ->
                em.createQuery(jpql, Long.class)
                        .getSingleResult());
    }

    private void safeRollback(EntityTransaction tx, Exception originalException) {
        if (tx == null) {
            return;
        }

        try {
            if (tx.isActive()) {
                tx.rollback();
            }
        } catch (Exception rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }

    private void safeClose(EntityManager em) {
        if (em == null) {
            return;
        }

        try {
            if (em.isOpen()) {
                em.close();
            }
        } catch (Exception ignored) {
        }
    }
}
