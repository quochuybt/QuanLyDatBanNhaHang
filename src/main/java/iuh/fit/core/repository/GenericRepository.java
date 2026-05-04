package iuh.fit.core.repository;


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
        EntityTransaction tx = null;
        try (EntityManager em = JPAUtil.getEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            R result = function.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    protected void doInTransaction(Consumer<EntityManager> consumer) {
        EntityTransaction tx = null;
        try (EntityManager em = JPAUtil.getEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            consumer.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException(e);
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
        return doInSession(em ->
                em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                        .getResultList());
    }

    public void update(T entity) {
        executeTransaction(em -> em.merge(entity));
    }

    public void delete(ID id) {
        doInTransaction(em -> {
            T entity = em.find(entityClass, id);
            if (entity != null) em.remove(entity);
        });
    }

    public long count() {
        return doInSession(em ->
                em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                        .getSingleResult());
    }
}
